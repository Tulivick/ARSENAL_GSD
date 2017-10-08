/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.User;
import trustframework.data.github.Assign;
import trustframework.data.github.GitComment;
import trustframework.data.github.ProjectData;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.exceptions.github.InvalidProjectData;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *
 * @author guilherme
 */
public class KnowledgeAcceptance implements EvidenceAnalyser {

    private double weigth;
    private final static String EVIDENCE_LABEL = "KnowledgeAcceptance";

    public KnowledgeAcceptance(double weigth) {
        this.weigth = weigth;
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            generatePKnowledgeAcceptanceEdges(pData, relationsGraph);
            generateKnowledgeAcceptanceEdges(pData, relationsGraph);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    @Override
    public double getWeight() {
        return weigth;
    }

    @Override
    public void setWeight(double weight) {
        this.weigth = weight;
    }

    private void generateKnowledgeAcceptanceEdges(ProjectData pData, TFGraph relationsGraph) {
        relationsGraph.allEdges("P" + EVIDENCE_LABEL).stream().collect(Collectors.groupingBy(TFEdge::getSource)).forEach((source, edges) -> {
            Map<String, List<TFEdge>> edgesByTarget = edges.stream().collect(Collectors.groupingBy(TFEdge::getTarget));
            edgesByTarget.forEach((target, edgesT) -> {
                long count1 = relationsGraph.edgesBetween(source, target, "PR:").stream().filter(e -> pData.getPullRequest(Integer.parseInt(e.getLabel().replace("PR:", ""))).getUser().getLogin().equals(target)).count();
                long count2 = relationsGraph.edgesBetween(target, source, "PR:").stream().filter(e -> pData.getPullRequest(Integer.parseInt(e.getLabel().replace("PR:", ""))).getUser().getLogin().equals(target)).count();
                //double totalPRedges = relationsGraph.edgesBetween(source, target, "PR:").size() + relationsGraph.edgesBetween(target, source, "PR:").size();
                double totalPRedges = count1 + count2;
                double value = (double) edgesT.size() / totalPRedges;
                relationsGraph.addEdge(source, target, EVIDENCE_LABEL, value, null);
            });
        });
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) throws InvalidProjectData {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            removeOldEdges(relations, limit);
            generatePKnowledgeAcceptanceEdges(pData, relations, lastAnalysis);
            generateKnowledgeAcceptanceEdges(pData, relations);
        } else {
            throw new InvalidProjectData("projectData is not an instance of " + ProjectData.class.getName());
        }
    }

    private void generatePKnowledgeAcceptanceEdges(ProjectData pData, TFGraph relationsGraph, Date lastAnalysis) {
        pData.getAllComments().forEach((prId, prComments) -> {
            PullRequest pr = pData.getPullRequest(prId); //varer as pull requests
            if (pr.getMergedAt() != null && (lastAnalysis == null || pr.getUpdatedAt().after(lastAnalysis))) { //assim ficou top
                prComments.stream().map(GitComment::getCreator)
                        .map(User::getLogin)
                        .filter(name -> !name.equals(pr.getUser().getLogin()))
                        .distinct()
                        .forEach(name -> {
                            relationsGraph.addEdge(name, pr.getUser().getLogin(), "P" + EVIDENCE_LABEL + ":" + prId, 1.0, pr.getUpdatedAt());
                        });
                relationsGraph.addEdge(pr.getMergedBy().getLogin(), pr.getUser().getLogin(), "P" + EVIDENCE_LABEL + ":" + prId, 1.0, pr.getUpdatedAt());
                if (pData.getAssigns().containsKey(prId)) {
                    Assign assigner = pData.getAssigns().get(prId);
                    relationsGraph.addEdge(assigner.getUser().getLogin(), pr.getUser().getLogin(), "P" + EVIDENCE_LABEL + ":" + prId, 1.0, pr.getUpdatedAt());
                }
            }
        });
    }

    private void generatePKnowledgeAcceptanceEdges(ProjectData pData, TFGraph relationsGraph) {
        generatePKnowledgeAcceptanceEdges(pData, relationsGraph, null);
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
