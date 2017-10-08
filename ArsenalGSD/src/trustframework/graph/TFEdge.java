/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph;

import java.util.Date;

/**
 * Interface that implements an edge of TFGraph
 * @see TFGraph
 * @author guilherme
 */
public interface TFEdge {
    /**
     * Get the source of the edge
     * @return edge source
     */
    public String getSource();
    
    /**
     * Get the source of the edge
     * @return edge target
     */
    public String getTarget();
    
    /**
     * Get the date of the edge
     * @return date
     */
    public Date getDate();
    
    /**
     * Set a date to the edge
     * @param d date
     */
    public void setDate(Date d);
    
    /**
     * Get the label of the edge
     * @return edge label
     */
    public String getLabel();
    
    /**
     * Get the weight of the edge
     * @return edge weight
     */
    public double getWeight();
}
