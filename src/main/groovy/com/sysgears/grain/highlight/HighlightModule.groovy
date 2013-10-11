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
import com.google.inject.Provides
import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.highlight.pygments.AutoPygments
import com.sysgears.grain.highlight.pygments.FakePygments
import com.sysgears.grain.highlight.pygments.JythonPygments
import com.sysgears.grain.highlight.pygments.Pygments
import com.sysgears.grain.highlight.pygments.PythonPygments
import com.sysgears.grain.highlight.pygments.ShellPygments
import com.sysgears.grain.config.ImplBinder
import com.sysgears.grain.taglib.Site

/**
 * Package-specific IoC config 
 */
class HighlightModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public Pygments providePygments(Site site,
            PythonPygments python, JythonPygments jython,
            ShellPygments shell, AutoPygments auto, FakePygments fake) {
        new ImplBinder<Pygments>(Pygments.class, site, 'features.pygments', 
                [python: python, jython: jython,
                 shell: shell, default: auto, none: fake]).proxy
    }

    @Provides @javax.inject.Singleton @Uncached
    public Highlighter provideUncachedHighlighter(Site site,
            Pygments pygments, FakeHighlighter fake) {
        new ImplBinder<Highlighter>(Highlighter.class, site, 'features.highlight',
                [pygments: pygments, default: fake]).proxy
    }

    @Provides @javax.inject.Singleton
    public Highlighter provideHighlighter(Site site,
            @Uncached Highlighter uncachedHighlighter,
            CachedHighlighter cachedHighlighter) {
        new ImplBinder<Highlighter>(Highlighter.class, site, 'features.cache_highlight',
                [default: cachedHighlighter, false: uncachedHighlighter]).proxy
    }

    @Override
    protected void configure() {
    }
}
