/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence.github;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import trustframework.evidence.EvidenceAnalyser;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *
 * @author guilherme
 */
public class Colaboration implements EvidenceAnalyser {

    private double weigth;
    private static final String EVIDENCE_LABEL = "Colaboration";

    public Colaboration(double weigth) {
        this.weigth = weigth;
    }

    @Override
    public String getEvidenceLabel() {
        return EVIDENCE_LABEL;
    }

    @Override
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) {
        generateColaborationEdges(relationsGraph);
    }

    @Override
    public double getWeight() {
        return weigth;
    }

    @Override
    public void setWeight(double weight) {
        this.weigth = weight;
    }

    private void generateColaborationEdges(TFGraph relationsGraph) {
        relationsGraph.allNodes().forEach(user -> {
            Map<String,Long> frequency = getColaborationFrequencies(relationsGraph, user);
            Long totalColaborations = frequency.remove("*");//frequency.values().stream().reduce(0L, (acc, value) -> acc+value);
            frequency.forEach((target, freq) -> {
                double weight = (double)freq / (double)totalColaborations;
                relationsGraph.addEdge(user, target, EVIDENCE_LABEL, weight, null);
            });
        });
    }

    private Map<String, Long> getColaborationFrequencies(TFGraph relationsGraph, String user) {
        Set<TFEdge> outEdges = relationsGraph.outEdges(user, "PR:");
        Set<TFEdge> inEdges = relationsGraph.inEdges(user, "PR:");
        Set<String> allPrs = outEdges.stream().map(e -> e.getLabel()).collect(Collectors.toSet());
        allPrs.addAll(inEdges.stream().map(e -> e.getLabel()).collect(Collectors.toSet()));
        Map<String, Long> freqOut = outEdges.stream().collect(Collectors.groupingBy(TFEdge::getTarget, Collectors.counting()));
        Map<String, Long> freqIn = inEdges.stream().collect(Collectors.groupingBy(TFEdge::getSource, Collectors.counting()));
        freqIn.forEach((target, freq) -> {
            freqOut.merge(target, freq, (outValue, inValue) -> {
                return outValue + inValue;
            });
        });
        freqOut.put("*", (long)allPrs.size());
        return freqOut;
    }

    @Override
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) {
        relations.allEdges(EVIDENCE_LABEL).stream().forEach(e -> {
            relations.removeEdge(e);
        });    
        generateColaborationEdges(relations);
    }

}
