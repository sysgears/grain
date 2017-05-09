package com.sysgears.grain.markup.markdown.flexmark

import com.vladsch.flexmark.util.options.MutableDataHolder
import com.vladsch.flexmark.util.options.MutableDataSet

/**
 * Markdown processor which uses CommonMark 0.27 implementation provided by flexmark.
 */
class FlexmarkCommonMarkProcessor extends FlexmarkBaseProcessor {

    /**
     * Since CommonMark 0.27 is the default one for Flexmark, no special options should be provided.
     */
    @Override
    MutableDataHolder getProcessorOptions() {
        return new MutableDataSet()
    }

    /**
     * @inheritDoc
     */
    @Override
    String getCacheSubdir() {
        return "flexmark-commonmark.0_19_4"
    }
}
