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

import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.config.Config
import com.sysgears.grain.taglib.GrainUtils
import com.sysgears.grain.taglib.Site

import javax.annotation.Nullable
import javax.inject.Inject

/**
 * Cached highlighter stores and reuses highlighting results in a Site cache,
 * each highlighter has its own sub-folder where the results are cached for a later use. 
 */
@javax.inject.Singleton
class CachedHighlighter implements Highlighter {
    
    /** Config instance */
    @Inject
    private Config config
    
    /** Uncached highlighter proxy */
    @Inject @Uncached
    private Highlighter uncachedHighlighter

    /**
     * @inheritDoc
     */
    @Override
    String highlight(String code, String language) {
        def uniqueName = uncachedHighlighter.cacheSubdir
        
        def cacheDir = new File(config.cache_dir.toString(), "highlight/" + uniqueName)
        def cacheFile = new File(cacheDir, GrainUtils.md5((code + '\001' + language).bytes) + ".html")
        if (uniqueName != null && cacheFile.exists() && cacheFile.length() > 0) {
            cacheFile.text
        } else {
            def highlightedCode = uncachedHighlighter.highlight(code, language)

            if (uniqueName != null) {
                cacheDir.mkdirs()
                cacheFile.write(highlightedCode)
            }
            
            highlightedCode
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    @Nullable String getCacheSubdir() {
        null
    }
}
