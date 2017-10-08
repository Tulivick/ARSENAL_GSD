/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.exceptions;

import java.util.List;

/**
 * This exception is thrown when the framework isn't configured properly.
 * @author Guilherme
 */
public class TFConfigException extends Exception{
    List<String> missingConfigs;

    public TFConfigException(List<String> missing) {
        super("Trust framework isn't configured properly!");
        this.missingConfigs = missing;
    }

    /**
     * Get the list of missing configurations
     * @return missing configurations
     */
    public List<String> getMissingConfigs() {
        return missingConfigs;
    }

    /**
     * Set the missing configurations
     * @param missingConfigs missing configurations
     */
    public void setMissingConfigs(List<String> missingConfigs) {
        this.missingConfigs = missingConfigs;
    }
    
}
