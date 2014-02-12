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
import com.sysgears.grain.config.ConfigBinder
import com.sysgears.grain.highlight.pygments.*

/**
 * Package-specific IoC config 
 */
class HighlightModule extends AbstractModule {

    @Provides @javax.inject.Singleton
    public Pygments providePygments(ConfigBinder binder,
            PythonPygments pygments, FakePygments fake) {
        binder.bind(Pygments, 'features.pygments', 
                [python: pygments, jython: pygments,
                 shell: pygments, default: pygments, none: fake])
    }

    @Provides @javax.inject.Singleton @Uncached
    public Highlighter provideUncachedHighlighter(ConfigBinder binder,
            Pygments pygments, FakeHighlighter fake) {
        binder.bind(Highlighter, 'features.highlight',
                [pygments: pygments, default: fake])
    }

    @Provides @javax.inject.Singleton
    public Highlighter provideHighlighter(ConfigBinder binder,
            @Uncached Highlighter uncachedHighlighter,
            CachedHighlighter cachedHighlighter) {
        binder.bind(Highlighter, 'features.cache_highlight',
                [default: cachedHighlighter, false: uncachedHighlighter])
    }

    @Override
    protected void configure() {
    }
}
