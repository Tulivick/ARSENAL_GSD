/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;

/**
 * This class stores data for a GitHub project.
 * @author Guilherme
 */
public class ProjectData {
    private final HashMap<Integer,PullRequest> pullRequests;
    private final HashMap<Integer,List<GitComment>> comments;
    private final HashMap<String,User> involvedUsers;
    private final HashMap<Integer,Assign> assigns;
    private final HashMap<String,List<String>> follow;
    private final HashMap<String,List<String>> watched;
    
    public ProjectData(List<PullRequest> pullRequests) {
        this.pullRequests = new HashMap<>();
        pullRequests.stream().forEach((pr)->{
            this.pullRequests.put(pr.getNumber(), pr);
        });
        this.comments = new HashMap<>();
        this.assigns = new HashMap<>();
        this.involvedUsers = new HashMap<>();
        this.follow = new HashMap<>();
        this.watched = new HashMap<>();
    }
    
    /**
     * Return the list of comments for a pull request
     * @param prId pull request id
     * @return list of comments
     */
    public List<GitComment> getPrComments(Integer prId) {
        return comments.get(prId);
    }

    /**
     * Set the comments for a pull request
     * @param prId pull request id
     * @param prComments list of comments
     */
    public void addComments(Integer prId, List<GitComment> prComments) {
        this.comments.put(prId, prComments);
    }
    
    /**
     * Return a HashMap where the key is the pull request id and the values are lists of comments.
     * @return comments for each pull request
     */
    public HashMap<Integer,List<GitComment>> getAllComments(){
        return comments;
    }

    /**
     * Return a PullRequest for the given Id
     * @param prId pull request id
     * @return pull request 
     */
    public PullRequest getPullRequest(Integer prId) {
        return pullRequests.get(prId);
    }
    
    /**
     * Get the users involved in the project. By users involved we mean users that created a pull request, commented in one or assigned someone to one.
     * @return set of involved users
     */
    public Set<User> getInvolvedUsers() {
        return new HashSet(involvedUsers.values());
    }

    /**
     * Set users involved in the project
     * @param usersInvolved set of involved users
     */
    public void setInvolvedUsers(Set<User> usersInvolved) {
        this.involvedUsers.clear();
        usersInvolved.forEach(user -> {
            this.involvedUsers.put(user.getLogin(), user);
        });
    }

    public HashMap<Integer, Assign> getAssigns() {
        return assigns;
    }
    
    public User getUser(String username){
        return involvedUsers.get(username);
    }

    public void addAssign(Integer pr, Assign assign) {
        this.assigns.put(pr, assign);
    }
    
    public List<String> getWatched(String user){
        return watched.get(user);
    }
    
    public List<String> getFollows(String user){
        return follow.get(user);
    }
    
    public void addWatched(String user, List<String> projects){
        watched.put(user, projects);
    }
    
    public void addFollows(String user, List<String> projects){
        follow.put(user, projects);
    }
}
