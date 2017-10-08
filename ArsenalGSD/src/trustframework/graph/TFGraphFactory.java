/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph;

/**
 * Iterface to provide instances of TFGraph
 * @author guilherme
 */
public interface TFGraphFactory {
    /**
     * Instantiate an implementation of TFGraph
     * @return an instance of TFGraph
     */
    public TFGraph instantiateTFGraph();
}
