package com.sysgears.grain.markup.markdown

import com.github.rjeschke.txtmark.Processor
import groovy.util.logging.Slf4j

/**
 * Markdown processor which uses TxtMark implementation.
 */
@javax.inject.Singleton
@Slf4j
class TxtMarkProcessor implements MarkdownProcessor {

    /**
     * Process source markdown and returns html 
     *
     * @param source markdown
     *
     * @return html
     */
    public String process(String source) {
        Processor.process(source)
    }

    /**
     * @inheritDoc
     */
    @Override
    String getCacheSubdir() {
        'txtmark.0_9'
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
