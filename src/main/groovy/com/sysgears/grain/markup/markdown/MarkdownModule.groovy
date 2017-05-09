package com.sysgears.grain.markup.markdown

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.sysgears.grain.config.ConfigBinder
import com.sysgears.grain.markup.markdown.flexmark.FlexmarkCommonMarkProcessor
import com.sysgears.grain.markup.markdown.flexmark.FlexmarkKramdownProcessor
import com.sysgears.grain.markup.markdown.flexmark.FlexmarkMultimarkdownProcessor
import com.sysgears.grain.markup.markdown.flexmark.FlexmarkPegdownProcessor

/**
 * Package-specific IoC config
 */
class MarkdownModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public MarkdownProcessor provideProcessor(ConfigBinder binder,
                                              TxtMarkProcessor txtmark,
                                              FlexmarkCommonMarkProcessor flexmarkCommonMarkProcessor,
                                              FlexmarkKramdownProcessor flexmarkKramdownProcessor,
                                              FlexmarkPegdownProcessor flexmarkPegdownProcessor,
                                              FlexmarkMultimarkdownProcessor flexmarkMultimarkdownProcessor,
                                              PegdownProcessor pegdown) {
        binder.bind(MarkdownProcessor, 'features.markdown',
                [default: txtmark,
                 txtmark: txtmark,
                 pegdown: pegdown,
                 flexmark_pegdown:  flexmarkPegdownProcessor,
                 flexmark_kramdown: flexmarkKramdownProcessor,
                 flexmark_commonmark: flexmarkCommonMarkProcessor,
                 flexmark_multimarkdown: flexmarkMultimarkdownProcessor])
    }

    @Override
    protected void configure() {
    }

}