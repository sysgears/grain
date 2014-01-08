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

import javax.annotation.Nullable
import javax.inject.Named

/**
 * Highlighter that only wraps the code into <pre> element.
 */
@javax.inject.Singleton
class FakeHighlighter implements Highlighter {

    /**
     * Doesn't really highlight anything,
     * just wraps code into <pre> element.
     * 
     * @param code a code to highlight 
     * @param language language of the code
     * 
     * @return code enclosed into <pre> element
     */
    @Override
    String highlight(String code, String language) {
        "<pre>${code}</pre>"
    }

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        null // Return null to prevent any caching in CachedHighlighter
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
