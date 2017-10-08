/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.exceptions.github;

/**
 * Exception thrown when the data object passed to EvidenceExtractor is of incompatible type.
 * @author Guilherme
 */
public class InvalidProjectData extends Exception{

    public InvalidProjectData(String message) {
        super(message);
    }
    
}
