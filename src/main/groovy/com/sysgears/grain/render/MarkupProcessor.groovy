package com.sysgears.grain.render

import com.sysgears.grain.asciidoc.AsciiDoctorProcessor
import com.sysgears.grain.markdown.MarkdownProcessor
import com.sysgears.grain.rst.RstProcessor

import javax.inject.Inject

/**
 * Processes source markup with honoring fixed blocks and produces output.
 */
@javax.inject.Singleton
public class MarkupProcessor {

    /** Markdown processor */
    @Inject private MarkdownProcessor markdownProcessor

    /** Rst processor */
    @Inject private RstProcessor rstProcessor

    /** AsciiDoctor processor */
    @Inject private AsciiDoctorProcessor adocProcessor

    /**
     * Processes markup with honoring fixed blocks.
     * <p>
     * If source file extension is unknown - source file contents are returned unchanged 
     * 
     * @param source source file contents
     * @param extension source file extension
     * 
     * @return processed markup output
     */
    public String process(String source, String extension) {
        def keeper = new FixedBlocksKeeper(source)
        def input = keeper.strippedSource

        def output

        if (extension in ['markdown', 'md']) {
            output = markdownProcessor.process(input)
        } else if (extension in ['rst']) {
            output = rstProcessor.process(input)
        } else if (extension in ['adoc', 'asciidoctor']) {
            output = adocProcessor.process(input)
        } else {
            output = input
        }
        
        keeper.reanimateFixedBlocks(output)
    }
}
