package com.sysgears.grain.markup.markdown.flexmark

import com.sysgears.grain.markup.markdown.MarkdownProcessor
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.options.DataHolder
import com.vladsch.flexmark.util.options.MutableDataHolder

/**
 * Markdown processor which uses Flexmark (Commonmark 0.27) implementation.
 */
abstract class FlexmarkBaseProcessor implements MarkdownProcessor {

    /**
     * Process source markdown and returns html
     *
     * @param source markdown
     *
     * @return html
     */
    public String process(String source) {

        DataHolder options = getProcessorOptions()

        //Disables fenced code block parsing due to the conflict with highlighting.
        options.set(Parser.FENCED_CODE_BLOCK_PARSER, false)

        Parser parser = Parser.builder(options).build()
        HtmlRenderer renderer = HtmlRenderer.builder().build()
        def result = renderer.render(parser.parse(source))

        return result
    }

    /**
     * This method should be overriden in descendants to return the options to correctly emulate the target mardown
     * flavour.
     */
    abstract MutableDataHolder getProcessorOptions()
}
