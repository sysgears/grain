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

import com.sysgears.grain.preview.ConfigChangeListener

import javax.annotation.Nullable

/**
 * Interface for code highlighter
 * <p>
 * Classes implementing this interface should be thread-safe.     
 */
public interface Highlighter {

    /**
     * Generates highlighted HTML using source code in given language. 
     * 
     * @param code a source code
     * @param language language of a source code
     * 
     * @return highlighted code HTML
     */
    String highlight(String code, String language)

    /**
     * Returns cache subdirectory of this highlighter.
     * 
     * @return cache subdirectory of this highlighter
     */
    @Nullable String getCacheSubdir()
}