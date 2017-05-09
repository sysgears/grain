package com.sysgears.grain.markup.markdown.flexmark

import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.definition.DefinitionExtension
import com.vladsch.flexmark.ext.footnotes.FootnoteExtension
import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.ext.typographic.TypographicExtension
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.parser.ParserEmulationProfile
import com.vladsch.flexmark.util.options.MutableDataHolder
import com.vladsch.flexmark.util.options.MutableDataSet

/**
 * Markdown processor which uses Kramdown implementation.
 */
class FlexmarkKramdownProcessor extends FlexmarkBaseProcessor {

    /**
     * Returns the options to emulate the Kramdown markdown processor.
     */
    @Override
    MutableDataHolder getProcessorOptions() {
        MutableDataSet options = new MutableDataSet()
        options.setFrom(ParserEmulationProfile.KRAMDOWN)

        options.set(Parser.EXTENSIONS, Arrays.asList(
                AbbreviationExtension.create(),
                DefinitionExtension.create(),
                FootnoteExtension.create(),
                TablesExtension.create(),
                TypographicExtension.create()
        ))

        return options
    }

    @Override
    String getCacheSubdir() {
        return "flexmark-kramdown.0_19_4"
    }
}
