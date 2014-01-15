/*
 * Copyright (c) 2013 SysGears, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sysgears.grain.highlight

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Provides
import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.config.Config
import com.sysgears.grain.config.ImplBinder
import com.sysgears.grain.highlight.pygments.*

/**
 * Package-specific IoC config 
 */
class HighlightModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public Pygments providePygments(Injector injector,
            PythonPygments python, JythonPygments jython,
            ShellPygments shell, AutoPygments auto, FakePygments fake) {
        new ImplBinder<Pygments>(Pygments.class, 'features.pygments', 
                [python: python, jython: jython,
                 shell: shell, default: auto, none: fake], injector).proxy
    }

    @Provides @javax.inject.Singleton @Uncached
    public Highlighter provideUncachedHighlighter(Injector injector,
            Pygments pygments, FakeHighlighter fake) {
        new ImplBinder<Highlighter>(Highlighter.class, 'features.highlight',
                [pygments: pygments, default: fake], injector).proxy
    }

    @Provides @javax.inject.Singleton
    public Highlighter provideHighlighter(Injector injector,
            @Uncached Highlighter uncachedHighlighter,
            CachedHighlighter cachedHighlighter) {
        new ImplBinder<Highlighter>(Highlighter.class, 'features.cache_highlight',
                [default: cachedHighlighter, false: uncachedHighlighter], injector).proxy
    }

    @Override
    protected void configure() {
    }
}
