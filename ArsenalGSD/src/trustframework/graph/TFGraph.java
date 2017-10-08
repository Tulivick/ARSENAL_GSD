/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph;

import java.util.Date;
import java.util.Set;

/**
 * Inteface that provides the methods for a graph to be used by the Trust Framework
 * @author guilherme
 */
public interface TFGraph {
    /**
     * Create a new node in the graph
     * @param name node name
     * @return true if the node was added
     */
    public boolean addNode(String name);
    
    /**
     * Remove a node from graph
     * @param name name of the node
     * @return true if it was removed
     */
    public boolean removeNode(String name);
    
    /**
     * Add and edge to the graph, from source to target. An edge has a label, weight and date so the trust framework can update data with time.
     * @param source name of the source node
     * @param targed name of the target node
     * @param label label's edge
     * @param weight label's
     * @param date label's
     * @return the edge created or null if it could not be created
     */
    public TFEdge addEdge(String source, String targed, String label, double weight, Date date);
    
    /**
     * Remove an edge from the graph
     * @param edge edge to be removed
     * @return true if the edge was removed
     */
    public boolean removeEdge(TFEdge edge);
    
    /**
     * Return the edges between source and target where the label starts with parameter lable.
     * @param source name of source node
     * @param target name of target node
     * @param label label selector. All the edges starting with this parameter will be returned.
     * @return set of edges that match source, target and label parameters
     */
    public Set<TFEdge> edgesBetween(String source,String target,String label);
    
    /**
     * Return a set of outgoing edges of a node where the label starts with parameter label.
     * @param name node name
     * @param label label selector. All the edges starting with this parameter will be returned.
     * @return set of outgoing edges
     */
    public Set<TFEdge> outEdges(String name,String label);
    
    /**
     * Return a set of ingoing edges of a node where the label starts with parameter label.
     * @param name node name
     * @param label label selector. All the edges starting with this parameter will be returned.
     * @return set of ingoing edges
     */
    public Set<TFEdge> inEdges(String name, String label);
    
    /**
     * Return all edges where the label starts with parameter label.
     * @param label label selector. All the edges starting with this parameter will be returned.
     * @return set of edges
     */
    public Set<TFEdge> allEdges(String label);
    
    /**
     * Return all nodes in the graph
     * @return set o node names
     */
    public Set<String> allNodes();
}
