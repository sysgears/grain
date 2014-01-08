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

import javax.inject.Inject
import javax.inject.Named

/**
 * Page highlighter highlights all the source code fragments of a given page
 */
@javax.inject.Singleton
public class PageHighlighter {
    
    /** Fragments highlighter to be used */
    @Inject private Highlighter highlighter

    /** Parser of highlighting markup */
    @Inject private HighlightMarkupParser markupParser

    /** Pretty formatter of highlighted code */
    @Inject private HighlightingFormatter formatter

    /**
     * Generates highlighted HTML for all the fragments of the page
     *
     * @param source page source
     *
     * @return list of highlighted code HTML fragments
     */
    public List<String> highlight(String text) {
        def defaultLang = 'html'
        def defaultLineNumbers = true
        def fragments = [] as List<String>
        
        text.findAll(/(?s)```(.*?)```/, {
            def info = markupParser.parse(it[1] as String)
            defaultLang = info.lang ?: defaultLang
            defaultLineNumbers = info.linenos ==  null ? defaultLineNumbers : info.linenos
            
            def highlightedCode = highlighter.highlight(info.code, defaultLang)
            def result = formatter.formatHighlightedHtml(
                    highlightedCode, defaultLang, info.caption, defaultLineNumbers)

            fragments += result
        })
        
        fragments
    }
}
