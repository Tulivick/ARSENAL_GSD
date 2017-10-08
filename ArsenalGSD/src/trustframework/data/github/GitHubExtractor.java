/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.WatcherService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import trustframework.data.DataExtractor;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *Class responsible to retrieve data from github using the GitHub API, generate and update the relations graph.
 * 
 * @author guilherme
 */
public class GitHubExtractor implements DataExtractor {

    private final GitHubClient client;
    private final UserService userService;
    private final PullRequestService pullRequestService;
    private final IssueService issueService;
    private final WatcherService watcherService;

    /**
     * Instantiate a GitHubExtractor for the given GitHub account.
     * 
     * @param username username of a GitHub account
     * @param password password for the GitHub account
     */
    public GitHubExtractor(String username, String password) {
        client = new GitHubClient();
        client.setCredentials(username, password);
        pullRequestService = new PullRequestService(client);
        issueService = new IssueService(client);
        userService = new UserService(client);
        watcherService = new WatcherService(client);
    }

    /**
     * Retrive PullRequests from a given repository that were updated after the provided date.
     * 
     * @param repository GitHub repository.
     * @param begin date that limits the data retrieval.
     * @return list of pull requests
     * @throws IOException 
     */
    private List<PullRequest> retrievePullRequests(RepositoryId repository, Date begin) throws IOException {
        List<PullRequest> prs = pullRequestService.getPullRequests(repository, "all");
        if (begin != null) {
            prs = prs.stream().filter(pr -> pr.getUpdatedAt().after(begin)).collect(Collectors.toList());
        }
        List<PullRequest> prsFull = new ArrayList<>();
        prs.forEach(pr -> {
            try {
                prsFull.add(pullRequestService.getPullRequest(repository, pr.getNumber()));
            } catch (IOException ex) {
                Logger.getLogger(GitHubExtractor.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return prsFull;
    }

    /**
     * Retrive pull request creators from a list of PullRequest.
     * @param pullRequests list of pull request.
     * @return set of creators.
     */
    private Set<UserAdaptor> retrievePullRequestCreators(List<PullRequest> pullRequests) {
        return pullRequests.stream().map(pr -> {
            return new UserAdaptor(pr.getUser());
        }).collect(Collectors.toSet());
    }

    /**
     * Retrieve comments from a give pull request that were updated after the provided date.
     * 
     * @param repository GitHub repository 
     * @param prId pull request id.
     * @param begin date to limit the retrieval.
     * @return list of comments.
     * @throws IOException 
     */
    private List<GitComment> retrieveComments4PR(RepositoryId repository, Integer prId, Date begin) throws IOException {
        List<CommitComment> commitComments = pullRequestService.getComments(repository, prId);
        if(begin!=null){
            commitComments = commitComments.stream().filter(cc -> cc.getUpdatedAt().after(begin)).collect(Collectors.toList());
        }
        List<Comment> issueComments = issueService.getComments(repository, prId);
        if (begin != null) {
            issueComments = issueComments.stream().filter(comment -> comment.getUpdatedAt().after(begin)).collect(Collectors.toList());
        }
        List<GitComment> comments = new ArrayList<>(commitComments.size()+issueComments.size());
        commitComments.forEach(c -> {
            comments.add(new GitComment(c.getBody(), c.getUpdatedAt(), c.getUser(), c.getId()));
        });
        issueComments.forEach(c -> {
            comments.add(new GitComment(c.getBody(), c.getUpdatedAt(), c.getUser(), c.getId()));
        });
        Collections.sort(comments);
        return comments;
    }

    /**
     * Retrive pull request creators from a list of Comment.
     * @param comments list of comments.
     * @return set of creators.
     */
    private Set<UserAdaptor> retrieveCommentsCreators(List<GitComment> comments) {
        return comments.stream().map(c -> {
            return new UserAdaptor(c.getCreator());
        }).collect(Collectors.toSet());
    }

    /**
     * Retrieve repository assign events of a given user.
     * @param repository GitHub repository
     * @param prId pull request id
     * @param userLogin target user
     * @param begin date to limit retrieval
     * @return list of assign events
     */
    private List<IssueEvent> retrieveAssignEvents(RepositoryId repository, Integer prId, String userLogin, Date begin) {
        PageIterator<IssueEvent> pageIssueEvents = issueService.pageIssueEvents(repository.getOwner(), repository.getName(), prId);
        List<IssueEvent> assignEvents = new ArrayList<>();
        while (pageIssueEvents.hasNext()) {
            Collection<IssueEvent> eventsPage = pageIssueEvents.next();
            assignEvents.addAll(eventsPage.stream().filter(ie -> ie.getEvent().equals("assigned")).filter(ie -> ie.getActor().getLogin().equals(userLogin)).collect(Collectors.toList()));
        }
        if (begin != null) {
            assignEvents = assignEvents.stream().filter(ie -> ie.getCreatedAt().after(begin)).collect(Collectors.toList());
        }
        return assignEvents;
    }

    /**
     * Retrieve the assigner from pull request page if there is one. In some cases an user can self assign.
     * @param assignEvents list of assign events.
     * @param pr pull request where the events came from.
     * @return assigner or null if there is no assigner.
     * @throws IOException 
     */
    private Assign getAssigner(List<IssueEvent> assignEvents, PullRequest pr) throws IOException {
        Optional<IssueEvent> lastAssign = assignEvents.stream().sorted(new IssueEventComparator()).findFirst();
        if (lastAssign.isPresent()) {
            Document html = Jsoup.connect(pr.getHtmlUrl()).get();
            Elements assignes = html.select("div.discussion-item-assigned .discussion-item-entity");
            Element assign = assignes.last();
            if (assign == null) {
                return null;
            }
            return new Assign(userService.getUser(assign.text()), lastAssign.get().getCreatedAt());
        }
        return null;
    }

    /**
     * Retrieve the list of users whatched by an user.
     * @param login target user login
     * @return list of watched users
     * @throws IOException 
     */
    private List<String> retrieveWatched(String login) throws IOException {
        return watcherService.getWatched(login).stream().map(repo -> {
            return repo.getOwner().getLogin() + "/" + repo.getName();
        }).collect(Collectors.toList());
    }

    /**
     * Retrieve the list of users followed by an user.
     * @param login target user login
     * @return list of followed users
     * @throws IOException 
     */
    private List<String> retrieveFollowing(String login) throws IOException {
        return userService.getFollowing(login).stream().map(User::getLogin).collect(Collectors.toList());
    }

    /**
     * Retrieve data from a GitHub repository. The retrieved data includes Pull Requests, Comments and Users involved.
     * @param owner repository owner login
     * @param name repository name
     * @param begin date to limit data retrieval
     * @return projec data
     * @throws IOException 
     */
    private ProjectData retrieveData(String owner, String name, Date begin) throws IOException {
        System.out.println("Date:"+begin);
        RepositoryId repository = new RepositoryId(owner, name);
        List<PullRequest> prs = retrievePullRequests(repository, begin);
        System.out.println("Got prs!");
        Set<UserAdaptor> involvedUsers = retrievePullRequestCreators(prs);
        System.out.println("Got pr creators!");
        ProjectData pData = new ProjectData(prs);
        for (PullRequest pr : prs) {
            if(pr.getMergedBy()!=null){
                involvedUsers.add(new UserAdaptor(pr.getMergedBy()));
            }
            List<GitComment> comments = retrieveComments4PR(repository, pr.getNumber(), begin);
            pData.addComments(pr.getNumber(), comments);
            involvedUsers.addAll(retrieveCommentsCreators(comments));
            if (pr.getAssignee() != null) {
                List<IssueEvent> assignEvents = retrieveAssignEvents(repository, pr.getNumber(), pr.getAssignee().getLogin(), begin);
                Assign lastAssign = getAssigner(assignEvents, pr);
                if (lastAssign != null) {
                    pData.addAssign(pr.getNumber(), lastAssign);
                    involvedUsers.add(new UserAdaptor(lastAssign.getUser()));
                }
            }
        }
        pData.setInvolvedUsers(involvedUsers.stream().map(UserAdaptor::getUser).collect(Collectors.toSet()));
        System.out.println("Got comments!");
        for (User u : pData.getInvolvedUsers()) {
            pData.addFollows(u.getLogin(), retrieveFollowing(u.getLogin()));
            pData.addWatched(u.getLogin(), retrieveWatched(u.getLogin()));
        }
        System.out.println("Got follow and watched!");
        return pData;
    }

    @Override
    public Object getProjectData(String owner, String name, Date intervalBegin) {
        try {
            return retrieveData(owner, name, intervalBegin);
        } catch (IOException ex) {
            Logger.getLogger(GitHubExtractor.class.getName()).log(Level.SEVERE, null, ex); //criar minhas exceptions
        }
        return null;
    }

    /**
     * Add nodes to relations graph.
     * @param pData project data.
     * @param graph instance of graph.
     */
    private void addNodes(ProjectData pData, TFGraph graph) {
        pData.getInvolvedUsers().stream().forEach((user) -> {
            graph.addNode(user.getLogin());
        });
//        pData.getAssigns().forEach((prId, assign) -> { axo que nao precisa
//            graph.addNode(assign.getUser().getLogin());
//        });
    }

    /**
     * Return the oldest date between two dates.
     * @param d1 date one
     * @param d2 date two
     * @return the oldest date
     */
    private Date oldest(Date d1, Date d2) {
        int comp = d1.compareTo(d2);
        if (comp < 0) {
            return d1;
        }
        if (comp == 0) {
            return d1;
        }
        return d2;
    }
    
    /**
     * Return the newest date between two dates.
     * @param d1 date one
     * @param d2 date two
     * @return the newest date
     */
    private Date newest(Date d1, Date d2) {
        int comp = d1.compareTo(d2);
        if (comp < 0) {
            return d2;
        }
        if (comp == 0) {
            return d1;
        }
        return d1;
    }

    /**
     * Retrieve participants from a list of comments
     * @param comments list of comments
     * @return a hash with user as key and the date of the last comments he created
     */
    private HashMap<String, Date> getPRParticipantsFromComments(List<GitComment> comments) {
        //Collections.sort(comments, new CommentsComparator()); ja esta ordenado
        HashMap<String, Date> prParticipants = new HashMap<>();
        comments.stream().forEachOrdered((comment) -> {
            prParticipants.put(comment.getCreator().getLogin(), comment.getUpdatedAt());
        });
        return prParticipants;
    }

    /**
     * Add pull request edges between users when they appear in the same pr
     * @param pData project data
     * @param graph an instance of TFGraph
     */
    private void addPREdges(ProjectData pData, TFGraph graph) {
        pData.getAllComments().forEach((prId, comments) -> {
            HashMap<String, Date> prParticipants = getPRParticipantsFromComments(comments);
            PullRequest pr = pData.getPullRequest(prId);
            User prCretor = pr.getUser();
            prParticipants.put(prCretor.getLogin(), pData.getPullRequest(prId).getUpdatedAt());
            if(pr.getMergedBy()!=null){//realizar o merge sem comentar conta como interação
                prParticipants.merge(pr.getMergedBy().getLogin(), pr.getMergedAt(), (d1,d2)->{return newest(d1, d2);});
            }
            if(pData.getAssigns().containsKey(pr.getNumber())){//atribuir sem comentar conta como interacao
                Assign assign = pData.getAssigns().get(pr.getNumber());
                prParticipants.merge(assign.getUser().getLogin(), assign.getDate(), (d1,d2)->{return newest(d1, d2);});
            }
            List<String> prParticipantsList = new ArrayList<>(prParticipants.keySet());
            Collections.sort(prParticipantsList);
            for (int i = 0; i < prParticipants.size(); i++) {
                for (int j = i + 1; j < prParticipants.size(); j++) {
                    Date min = oldest(prParticipants.get(prParticipantsList.get(i)), prParticipants.get(prParticipantsList.get(j)));
                    Set<TFEdge> prEdge = graph.edgesBetween(prParticipantsList.get(i), prParticipantsList.get(j), "PR:" + prId);
                    if(prEdge.isEmpty()){
                        graph.addEdge(prParticipantsList.get(i), prParticipantsList.get(j), "PR:" + prId, 0, min);
                    }else{
                        prEdge.iterator().next().setDate(min);
                    }
                }
            }
        });
    }

    @Override
    public TFGraph generateRelationsGraph(Object project, TFGraph graph) {
        if (project instanceof ProjectData) {
            ProjectData pData = (ProjectData) project;
            addNodes(pData, graph);
            addPREdges(pData, graph);
        }
        return graph;
    }

    /**
     * Remove PR edges older than the limit date
     * @param relations relations graph
     * @param limit limit date
     */
    private void removeOldPREdges(TFGraph relations, Date limit) {
        List<TFEdge> edgesToRemove = relations.allEdges("PR:").stream().filter(e -> e.getDate().before(limit)).collect(Collectors.toList());
        edgesToRemove.forEach(e -> {
            relations.removeEdge(e);
        });
    }

    @Override
    public void updateRelationsGraph(TFGraph relations, Object projectData, Date intervalBegin) {
        if (projectData instanceof ProjectData) {
            removeOldPREdges(relations, intervalBegin);
            ProjectData pData = (ProjectData) projectData;
            addNodes(pData, relations);
            addPREdges(pData, relations);
        }
    }

}
