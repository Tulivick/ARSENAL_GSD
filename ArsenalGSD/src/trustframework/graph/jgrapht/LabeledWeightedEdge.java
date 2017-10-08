/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.graph.jgrapht;

import java.util.Date;
import java.util.Objects;
import org.jgrapht.graph.DefaultEdge;
import trustframework.graph.TFEdge;

/**
 *
 * @author guilherme
 */
public class LabeledWeightedEdge extends DefaultEdge implements TFEdge{
    private final String label;
    private Date date;
    private final String source;
    private final String targed;
    private final double weight;

    public LabeledWeightedEdge(String source, String target, String label, Date date, double weight) {
        this.label = label;
        this.date = date;
        this.source = source;
        this.weight = weight;
        this.targed = target;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        return source+"---"+label+":"+getWeight()+":"+date+"---"+targed;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getTarget() {
        return targed;
    }

    @Override
    public double getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LabeledWeightedEdge other = (LabeledWeightedEdge) obj;
        if (!Objects.equals(this.label, other.label)) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.targed, other.targed)) {
            return false;
        }
        return true;
    }

    @Override
    public void setDate(Date d) {
        this.date = d;
    }

}
