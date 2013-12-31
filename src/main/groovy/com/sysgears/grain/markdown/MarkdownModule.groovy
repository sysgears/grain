package com.sysgears.grain.markdown

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.sysgears.grain.config.Config
import com.sysgears.grain.config.ImplBinder

/**
 * Package-specific IoC config
 */
class MarkdownModule extends AbstractModule {
    
    @Provides @javax.inject.Singleton
    public MarkdownProcessor provideProcessor(Config config,
                                  TxtMarkProcessor txtmark) {
        new ImplBinder<MarkdownProcessor>(MarkdownProcessor.class, config, 'features.markdown',
                [default: txtmark, txtmark: txtmark]).proxy
    }

    @Override
    protected void configure() {
    }

}