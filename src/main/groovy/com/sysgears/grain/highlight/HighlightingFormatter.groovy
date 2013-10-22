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

import javax.inject.Named

/**
 * Formats highlighted code into pretty HTML table with caption and line numbers 
 */
@Named
@javax.inject.Singleton
class HighlightingFormatter {

    /**
     * Formats highlighted code HTML as a table with
     * line numbers in the left column and code in the right column and
     * a caption above the table
     * 
     * @param highlightedCode highlighted code HTML
     * @param lang highlighted code language
     * @param caption table caption
     * @param linenos whether include line numbers
     * 
     * @return HTML table
     */
    public String formatHighlightedHtml(String highlightedCode, String lang, String caption, boolean linenos) {
        def html = (highlightedCode =~ /(?s)<pre>(.*)<\/pre>/)[0][1].replaceAll(/ *$/, '')
        caption = caption ? "<figcaption><span>${caption}</span></figcaption>" : ""
        def table = new StringBuilder()
        table.append('<div class="highlight"><table><tr>')
        def code = new StringBuilder()
        if (linenos) {
            table.append('<td class="gutter"><pre class="line-numbers">')
            html.readLines().eachWithIndex { String line, int index ->
                table.append("<span class='line-number'>${index + 1}</span>\n")
                code.append("<span class='line'>${line}\n</span>")
            }
            table.append('</pre></td>')
        } else {
            html.readLines().each { line ->
                code.append("<span class='line'>${line}\n</span>")
            }
        }
        table += "<td class='code'><pre><code class='${lang}'>${code}</code></pre></td></tr></table></div>"
        
         "<figure class='code'>${caption}${table}</figure>"
    }
}
