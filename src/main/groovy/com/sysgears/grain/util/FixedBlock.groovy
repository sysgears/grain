package com.sysgears.grain.util

/**
 * Various methods to work with fixed blocks
 */
public class FixedBlock {

    /**
     * Wraps text into fixed block.
     * <p>
     * Note this method escapes source text, so no need escaping beforehand
     * 
     * @param text source text
     * 
     * @return text wrapped into fixed block
     */
    public static String wrapText(final String text) {
        '`!`' + escapeText(text) + '`!`'
    }

    /**
     * Escapes dangerous sequences in the text for inclusion into fixed block. 
     * 
     * @param text source text
     *  
     * @return escaped text
     */
    public static String escapeText(final String text) {
        text.replaceAll(/`!+`/, {
            def cnt = it.toString().count('!')
            '`' + ('!' * (cnt + 1)) + '`'
        })
    }
    
    public static String unescapeText(final String text) {
        text.replaceAll(/`!{2,}`/, {
            def cnt = it.toString().count('!')
            '`' + ('!' * (cnt - 1)) + '`'
        })
    }
}
