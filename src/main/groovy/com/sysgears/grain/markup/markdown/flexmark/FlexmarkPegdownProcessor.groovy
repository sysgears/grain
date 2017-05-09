package com.sysgears.grain.markup.markdown.flexmark

import com.vladsch.flexmark.profiles.pegdown.Extensions
import com.vladsch.flexmark.profiles.pegdown.PegdownOptionsAdapter
import com.vladsch.flexmark.util.options.MutableDataHolder

/**
 * Contains logic for pegdown markdown processor.
 */
class FlexmarkPegdownProcessor extends FlexmarkBaseProcessor {

    /**
     * Returns the options to emulate the Multimarkdown markdown processor.
     */
    @Override
    MutableDataHolder getProcessorOptions() {
        return PegdownOptionsAdapter.flexmarkOptions(Extensions.NONE).toMutable()
    }

    /**
     * @inheritDoc
     */
    @Override
    String getCacheSubdir() {
        return "flexmark-pegdown.0_19_4"
    }
}
