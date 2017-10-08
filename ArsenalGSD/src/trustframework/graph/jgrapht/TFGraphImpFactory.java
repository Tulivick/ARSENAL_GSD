/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph.jgrapht;

import trustframework.graph.TFGraph;
import trustframework.graph.TFGraphFactory;

/**
 *
 * @author guilherme
 */
public class TFGraphImpFactory implements TFGraphFactory{

    @Override
    public TFGraph instantiateTFGraph() {
        return new TFGraphImp();
    }
    
}
