/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.evidence;

import java.util.Date;
import trustframework.graph.TFGraph;

/**
 * Interface that represents and evidence to the Trust Framework. New evidence can be created implementing this interface.
 * @author guilherme
 */
public interface EvidenceAnalyser {
    
    /**
     * Get the evidence label. This label is used in the relations graph's edges that mantain information about this evidence.0
     * @return the evidence label.
     */
    public String getEvidenceLabel();
    
    /**
     * Analyse project data looking for evidences. This function must add edge(s) to the relations graph representing the evidence value ([0,1])
     * @param projectData project data
     * @param relationsGraph the relations graph
     * @throws java.lang.Exception
     */
    public void analyseEvidence(Object projectData, TFGraph relationsGraph) throws Exception;
    
    /**
     * Get evidence weight used by the Trust Framework to calculate trust value.
     * @return evidence weight
     */
    public double getWeight();
    
    /**
     * Set the evidence weight
     * @param weight weight value
     */
    public void setWeight(double weight);
    
    /**
     * Update evidence values removing old edges, creating new edges for new data and updating the evidence value.
     * @param projectData project data.
     * @param relations relations graph.
     * @param limit limit date to consider data.
     * @param lastAnalysis data of the last analysis where the relation graph were generated.
     */
    public void updateEvidence(Object projectData, TFGraph relations, Date limit, Date lastAnalysis) throws Exception;
}
