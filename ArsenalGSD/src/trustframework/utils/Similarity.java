/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Guilherme
 */
public class Similarity {
    
    /**
     * Calculates cosine similarity between two frequencies
     * @param <T> Hash key class
     * @param freq1 frequency 1
     * @param freq2 frequency 2
     * @return cosine similarity [0,1]
     */
    public static <T> double cosineSimilarity(Map<T,Integer> freq1, Map<T,Integer> freq2){
        Set<T> terms = new HashSet<>();
        terms.addAll(freq1.keySet());
        terms.addAll(freq2.keySet());
        RealVector v1 = buildRealVector(freq1, terms);
        RealVector v2 = buildRealVector(freq2, terms);
        return (v1.dotProduct(v2)) / (v1.getNorm() * v2.getNorm());
    }
    
    /**
     * Build a real vector from frequencies
     * @param <T> Type of hask keys
     * @param freq hash with frequencies
     * @param keys set of all keys available
     * @return 
     */
    private static <T> RealVector buildRealVector(Map<T,Integer> freq, Set<T> keys){
        RealVector vector = new ArrayRealVector(keys.size());
        int i=0;
        for (T term : keys) {
            int value = freq.containsKey(term) ? freq.get(term) : 0;
            vector.setEntry(i++, value);
        }
        vector = vector.mapDivide(vector.getL1Norm());
        return vector;
    }
    
    /**
     * Calculates jaccard similarity between two collections
     * @param <T> Type of collection elements
     * @param c1 collection 1
     * @param c2 collection 2
     * @return jaccard similarity [0,1]
     */
    public static <T> double jaccardSimilarity(Collection<T> c1, Collection<T> c2){
        Set<T> intersection = new HashSet<>(c1);
        intersection.retainAll(c2);
        Set<T> union = new HashSet<>(c1);
        union.addAll(c2);
        return (double)intersection.size()/(double)union.size();
    }
}
