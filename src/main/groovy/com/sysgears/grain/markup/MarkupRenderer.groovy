package com.sysgears.grain.markup

import com.sysgears.grain.markup.asciidoc.AsciiDoctorProcessor
import com.sysgears.grain.markup.markdown.MarkdownProcessor
import com.sysgears.grain.markup.rst.RstProcessor
import com.sysgears.grain.render.FixedBlocksKeeper
import com.sysgears.grain.taglib.GrainUtils
import com.sysgears.grain.taglib.Site

import javax.inject.Inject

/**
 * Processes source markup with honoring fixed blocks and produces output.
 */
@javax.inject.Singleton
class MarkupRenderer {

    /** Markdown processor */
    @Inject private MarkdownProcessor markdownProcessor

    /** Rst processor */
    @Inject private RstProcessor rstProcessor

    /** AsciiDoctor processor */
    @Inject private AsciiDoctorProcessor adocProcessor
    
    /** Site instance */
    @Inject private Site site

    /**
     * Processes markup with honoring fixed blocks.
     * <p>
     * If markup type is unknown - source file contents are returned unchanged 
     * 
     * @param source source file contents
     * @param markup markup type
     * 
     * @return processed markup output
     */
    public String process(String source, String markup) {
        def keeper = new FixedBlocksKeeper(source)
        def input = keeper.strippedSource

        def output = input
        
        def processor = ['md': markdownProcessor, 'rst': rstProcessor, 'adoc': adocProcessor].
                get(markup) as MarkupProcessor

        if (markup in ['rst', 'adoc']) {
            def uniqueName = processor.cacheSubdir

            def cacheDir = new File(site.cache_dir.toString(), "markup/" + uniqueName)
            def cacheFile = new File(cacheDir, GrainUtils.md5((source + '\001' + markup).bytes) + ".html")
            if (uniqueName != null && cacheFile.exists() && cacheFile.length() > 0) {
                output = cacheFile.text
            } else {
                output = processor.process(input)

                if (uniqueName != null) {
                    cacheDir.mkdirs()
                    cacheFile.write(output)
                }
            }
        }

        def result = keeper.reanimateFixedBlocks(output)
        
        // Unwrap highlighted blocks
        result.replaceAll(/(?s)<p>(<figure.+?<\/figure>)<\/p>/, '$1')
    }
}
