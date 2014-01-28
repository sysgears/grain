package com.sysgears.grain.render

import com.sysgears.grain.util.FixedBlock

/**
 * Keeps fixed blocks between markup transitions.
 */
@javax.inject.Singleton
public class FixedBlocksKeeper {

    /**
     * Fixed blocks
     */
    private final List<String> blocks = []

    /**
     * Marker to use in the source in place of fixed blocks.
     */
    private final String MARKER = '```'

    /**
     * Source
     */
    private final String source

    /**
     * Creates an instance of this class
     * 
     * @param source markup source
     */
    public FixedBlocksKeeper(String source) {
        this.source = source
    }

    /**
     * Returns source with fixed blocks replaced by marker 
     * 
     * @return fixed block replaced by marker
     */
    public String getStrippedSource() {
        def result = source.replaceAll(/(?s)`!`(.*?)`!`/, {
            def block = it[1] as String
            blocks.add(FixedBlock.unescapeText(block))
            MARKER
        })
        FixedBlock.unescapeText(result) 
    }

    /**
     * Inserts fixed blocks back into output in place of markers 
     * 
     * @return inserts fixed blocks back into output in place of markers  
     */
    public String reanimateFixedBlocks(String output) {
        def idx = 0
        def result = output.replaceAll(MARKER, { blocks[idx++] })

        result.replaceAll(/(?s)<p>(<figure.+?<\/figure>)<\/p>/, '$1')
        //result.replace('<p><figure', '<figure').replace('</figure></p>', '</figure>')
        //result
    }
}
