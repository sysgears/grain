package com.sysgears.grain.markup.markdown.flexmark

import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.options.MutableDataHolder
import com.vladsch.flexmark.util.options.MutableDataSet

/**
 * Markdown processor which uses Multimarkdon implementation.
 */
class FlexmarkMultimarkdownProcessor extends FlexmarkBaseProcessor {

    /**
     * Returns the options to emulate the Multimarkdown markdown processor.
     */
    @Override
    MutableDataHolder getProcessorOptions() {
        MutableDataHolder options = new MutableDataSet()
        options.setFrom(ParserEmulationProfile.MULTI_MARKDOWN)

        return options
    }

    @Override
    String getCacheSubdir() {
        return "flexmark-multimarkdown.0_19_4"
    }
}
