package com.sysgears.grain.markdown

import com.sysgears.grain.preview.ConfigChangeListener

/**
 * Markdown processor
 */
public interface MarkdownProcessor extends ConfigChangeListener {

    /**
     * Process source markdown and returns the result 
     * 
     * @param source markdown
     * 
     * @return processed markdown
     */
    public String process(String source)
}