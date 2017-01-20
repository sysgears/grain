package com.sysgears.grain.markup

import org.jetbrains.annotations.Nullable


/**
 * Interface for markup processing
 * <p>
 * Classes implementing this interface should be thread-safe.     
 */
public interface MarkupProcessor {

    /**
     * Renders markup content.
     *
     * @param source source
     *
     * @return rendered output 
     */
    public String process(String source)

    /**
     * Returns cache subdirectory of this markup processor.
     *
     * @return cache subdirectory of this markup processor
     */
    @Nullable String getCacheSubdir()
}