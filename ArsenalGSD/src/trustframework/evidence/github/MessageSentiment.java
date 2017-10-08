/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import trustframework.data.github.GitComment;
import trustframework.data.github.ProjectData;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.exceptions.github.InvalidProjectData;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;
import uk.ac.wlv.sentistrength.SentiStrength;

/**
 *
 * @author guilherme
 */
public class MessageSentiment implements EvidenceAnalyser {

    private double weigth;
    private static final String EVIDENCE_LABEL = "Sentiment";
    private final SentiStrength sentiStrength;
    private final NaturalLanguageUnderstanding service;
    private final Features features;
    
    public MessageSentiment(double weigth, String sentiDataFolder, String wnluUsername, String wnluPassword) {
        this.weigth = weigth;
        sentiStrength = new SentiStrength();
        String initializationString[] = {"sentidata", sentiDataFolder, "sentenceCombineTot", "paragraphCombineTot", "trinary"};
        sentiStrength.initialise(initializationString);
        service = new NaturalLanguageUnderstanding(NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27, wnluUsername, wnluPassword);
        features = new Features.Builder().entities(new EntitiesOptions.Builder().build()).build();
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            generateParcialEdges(pData, relationsGraph);
            generateFinalEdges(pData, relationsGraph);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    private void generateParcialEdges(ProjectData pData, TFGraph relationsGraph) {
        generateParcialEdges(pData, relationsGraph, null);
    }

    private void generateParcialEdges(ProjectData pData, TFGraph relationsGraph, Date lastAnalysis) {
        List<String> validUsers = pData.getInvolvedUsers().stream().map(User::getLogin).collect(Collectors.toList());
        pData.getAllComments().forEach((prId, prComments) -> {
            PullRequest pr = pData.getPullRequest(prId);
            if (lastAnalysis == null || pr.getUpdatedAt().after(lastAnalysis)) {
                prComments.stream().filter(c -> lastAnalysis == null || c.getUpdatedAt().after(lastAnalysis)).forEach(comment -> {
                    Set<String> targets = getTargets(comment);
                    targets.add(pr.getUser().getLogin());
                    String source = comment.getCreator().getLogin();
                    targets.stream().filter(target -> (!target.equals(source) && validUsers.contains(target))).forEach(target -> {
                        relationsGraph.addEdge(source, target, "P" + EVIDENCE_LABEL + ":" + pr.getNumber() + ":" + comment.getId(), getSentiment(comment), comment.getUpdatedAt());
                    });
                });
            }
        });
    }

    private void generateFinalEdges(ProjectData pData, TFGraph relationsGraph) {
        pData.getInvolvedUsers().forEach(user -> {
            Map<String, List<TFEdge>> collect = relationsGraph.outEdges(user.getLogin(), "P" + EVIDENCE_LABEL).stream().collect(Collectors.groupingBy(TFEdge::getTarget));
            collect.forEach((target, edges) -> {
                double proportion = edges.stream().mapToDouble(TFEdge::getWeight).average().orElse(0);
                relationsGraph.addEdge(user.getLogin(), target, EVIDENCE_LABEL, proportion, null);
            });
        });
    }

    @Override
    public double getWeight() {
        return weigth;
    }

    @Override
    public void setWeight(double weight) {
        this.weigth = weight;
    }

    private Set<String> getTargets(GitComment comment) {
        Set<String> entities = new HashSet<>();
        try {
            AnalyzeOptions parameters = new AnalyzeOptions.Builder()
                                                                .language("en")
                                                                .text(comment.getBody())
                                                                .features(features)
                                                                .build();
            AnalysisResults response = service.analyze(parameters).execute();
            List<String> returnedEntities = response.getEntities().stream().filter((e) -> {return e.getText().equals("Person");}).map((e) -> {return e.getText();}).collect(Collectors.toList());
            entities.addAll(returnedEntities);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        Pattern pattern = Pattern.compile("\\B@\\w+");
        Matcher matcher = pattern.matcher(comment.getBody());
        while(matcher.find()){
            entities.add(matcher.group().substring(1));
        }
        return entities;
    }

    private double getSentiment(GitComment comment) {
        try {
            String sentiment = sentiStrength.computeSentimentScores(comment.getBody()).split(" ")[2];
            return sentiment.equals("1") ? 1.0 : 0.0;
        } catch (NullPointerException ex) {
            return 0.0;
        }
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            removeOldEdges(relations, limit);
            generateParcialEdges(pData, relations, lastAnalysis);
            generateFinalEdges(pData, relations);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    private void removeOldEdges(TFGraph relations, Date until) {
        relations.allEdges(EVIDENCE_LABEL).stream().forEach(e -> {
            relations.removeEdge(e);
        });
        relations.allEdges("P" + EVIDENCE_LABEL + ":").stream().filter(e -> e.getDate().before(until)).forEach(e -> {
            relations.removeEdge(e);
        });
    }

}
