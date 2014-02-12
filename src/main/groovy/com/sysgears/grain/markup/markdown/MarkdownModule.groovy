package com.sysgears.grain.markup.markdown

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.sysgears.grain.config.ConfigBinder

/**
 * Package-specific IoC config
 */
class MarkdownModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public MarkdownProcessor provideProcessor(ConfigBinder binder,
                                  TxtMarkProcessor txtmark,
                                  PegdownProcessor pegdown) {
        binder.bind(MarkdownProcessor, 'features.markdown',
                [default: txtmark, txtmark: txtmark, pegdown: pegdown])
    }

    @Override
    protected void configure() {
    }

}