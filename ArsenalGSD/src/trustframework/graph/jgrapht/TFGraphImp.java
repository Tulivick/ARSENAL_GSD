/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph.jgrapht;

import java.io.PrintStream;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;
import org.jgrapht.graph.DirectedMultigraph;
import trustframework.graph.TFEdge;
import trustframework.graph.TFGraph;

/**
 *
 * @author guilherme
 */
public class TFGraphImp implements TFGraph, Serializable{
    DirectedMultigraph<String, LabeledWeightedEdge> graph;

    public TFGraphImp() {
        graph = new DirectedMultigraph<>(LabeledWeightedEdge.class);
    }

    @Override
    public boolean addNode(String name) {
        return graph.addVertex(name);
    }

    @Override
    public boolean removeNode(String name) {
        return graph.removeVertex(name);
    }

    @Override
    public TFEdge addEdge(String source, String targed, String label, double weight, Date date) {
        LabeledWeightedEdge edge = new LabeledWeightedEdge(source, targed, label, date, weight);
        return graph.addEdge(source, targed, edge)? edge : null;
    }

    @Override
    public boolean removeEdge(TFEdge edge) {
        if(edge instanceof LabeledWeightedEdge){
            return graph.removeEdge((LabeledWeightedEdge)edge);
        }
        return false;
    }

    @Override
    public Set<TFEdge> outEdges(String name, String label) {
        return graph.outgoingEdgesOf(name).stream().filter((edge)-> edge.getLabel().startsWith(label)).collect(Collectors.toSet());
    }

    @Override
    public Set<TFEdge> inEdges(String name, String label) {
        return graph.incomingEdgesOf(name).stream().filter((edge)-> edge.getLabel().startsWith(label)).collect(Collectors.toSet());
    }
    
    /**
     * Print all graph edges on stdout
     */
    public void printGraph(PrintStream out){
        out.println("Arestas:");
        graph.edgeSet().stream().sorted((e1,e2)->{return e1.getSource().compareTo(e2.getSource());}).forEachOrdered((edge)->{
            out.println(edge);
        });
    }

    @Override
    public Set<TFEdge> allEdges(String label) {
        return graph.edgeSet().stream().filter(edge -> edge.getLabel().startsWith(label)).collect(Collectors.toSet());
    }

    @Override
    public Set<String> allNodes() {
        return graph.vertexSet();
    }

    @Override
    public Set<TFEdge> edgesBetween(String source, String target, String label) {
        return outEdges(source, label).stream().filter(edge -> edge.getTarget().equals(target)).collect(Collectors.toSet());
    }

    public DirectedMultigraph<String, LabeledWeightedEdge> getGraph() {
        return graph;
    }
    
}
