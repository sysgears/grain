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

package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.service.Service
import com.sysgears.grain.highlight.Highlighter

/**
 * Interface for Pygments integration.
 */
interface Pygments extends Highlighter, Service {

    /**
     * Highlights code using Pygments 
     *
     * @param code a code to highlight
     * @param language the language of the code
     *
     * @return highlighted code HTML
     */
    public String highlight(String code, String language)

}
