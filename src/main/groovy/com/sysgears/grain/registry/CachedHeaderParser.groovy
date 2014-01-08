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

package com.sysgears.grain.registry

import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.preview.FileChangeListener

import javax.inject.Inject

/**
 * Header parser, that maintains header cache and parses unmodified file only once
 */
@javax.inject.Singleton
class CachedHeaderParser implements HeaderParser, FileChangeListener {

    /** Uncached header parser */
    @Inject @Uncached private HeaderParser headerParser

    /** Location -> header cache */
    private final Map<File, Map> cache = [:]

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Object> parse(File resourceFile) throws HeaderParseException {
        if (!cache[resourceFile]) {
            cache[resourceFile] = headerParser.parse(resourceFile)
        }
        (Map)cache[resourceFile].clone()
    }

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Object> parse(File resourceFile, String header) throws HeaderParseException {
        if (!cache[resourceFile]) {
            cache[resourceFile] = headerParser.parse(resourceFile, header)
        }
        (Map)cache[resourceFile].clone()
    }

    /**
     * @inheritDoc
     */
    @Override
    public void fileChanged(File file) {
        cache.remove(file)
    }
}
