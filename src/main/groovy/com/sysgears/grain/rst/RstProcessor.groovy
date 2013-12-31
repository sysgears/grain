package com.sysgears.grain.rst

import com.sysgears.grain.preview.ConfigChangeListener

/**
 * reStructuredText processor
 */
public interface RstProcessor extends ConfigChangeListener {

    /**
     * Process source reStructuredText and returns the result 
     * 
     * @param source reStructuredText
     * 
     * @return output
     */
    public String process(String source)
}