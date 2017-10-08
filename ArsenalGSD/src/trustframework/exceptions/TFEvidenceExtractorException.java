/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.exceptions;

/**
 * Exception thrown by Trust Framework when something goes wrong with an EvidenceExtractor
 * @author Guilherme
 */
public class TFEvidenceExtractorException extends Exception{

    public TFEvidenceExtractorException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
