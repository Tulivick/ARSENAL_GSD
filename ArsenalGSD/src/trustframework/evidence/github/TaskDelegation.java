/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import java.util.Date;
import trustframework.data.github.ProjectData;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.exceptions.github.InvalidProjectData;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *
 * @author guilherme
 */
public class TaskDelegation implements EvidenceAnalyser {

    private double weigth;
    private static final String EVIDENCE_LABEL = "Assign";

    public TaskDelegation(double weigth) {
        this.weigth = weigth;
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws InvalidProjectData{
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            generateParcialEdges(pData, relationsGraph);
            generateFinalEdges(pData, relationsGraph);
        }else{
            throw new InvalidProjectData("projectData is not an instance of "+ProjectData.class.getName());
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

    private void generateParcialEdges(ProjectData pData, TFGraph relationsGraph) {
        generateParcialEdges(pData, relationsGraph, null);
//        pData.getAssigns().forEach((prId, assign) -> {
//            relationsGraph.addEdge(assign.getName(), pData.getPullRequest(prId).getAssignee().getLogin(), "P"+EVIDENCE_LABEL+":" + prId, 1, assign.getDate());
//        });
    }

    private void generateParcialEdges(ProjectData pData, TFGraph relationsGraph, Date lastAnalysis) {
        pData.getAssigns().forEach((prId, assign) -> {
            if (lastAnalysis == null || assign.getDate().after(lastAnalysis)) {
                relationsGraph.addEdge(assign.getUser().getLogin(), pData.getPullRequest(prId).getAssignee().getLogin(), "P"+EVIDENCE_LABEL+":" + prId, 1, assign.getDate());
            }
        });
    }

    private void generateFinalEdges(ProjectData pData, TFGraph relationsGraph) {
        pData.getAssigns().values().stream().map(a -> {return a.getUser().getLogin();}).distinct().forEach(source -> {
            relationsGraph.outEdges(source, "P"+EVIDENCE_LABEL+":").stream().map(TFEdge::getTarget).distinct().forEach(target -> {
                relationsGraph.addEdge(source, target, EVIDENCE_LABEL, 1, null);
            });
        });
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) {
        if (projectData instanceof ProjectData) {
            ProjectData pData = (ProjectData) projectData;
            removeOldEdges(relations, limit);
            generateParcialEdges(pData, relations, lastAnalysis);
            generateFinalEdges(pData, relations);
        }
    }

    private void removeOldEdges(TFGraph relations, Date until) {
        relations.allEdges(EVIDENCE_LABEL).stream().forEach(e -> {
            relations.removeEdge(e);
        });
        relations.allEdges("P"+EVIDENCE_LABEL+":").stream().filter(e -> e.getDate().before(until)).forEach(e -> {
            relations.removeEdge(e);
        });
    }
}
