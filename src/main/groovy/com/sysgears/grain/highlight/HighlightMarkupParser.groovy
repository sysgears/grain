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
/**
 * Parses highlighting block and returns its components.
 */
@javax.inject.Singleton
class HighlightMarkupParser {
    /**
     * Parses highlight fragment and returns highlight info.
     * 
     * @param highlightBlock highlighting fragment
     * 
     * @return highlight fragment info
     */
    public HighlightInfo parse(String highlightBlock) {
        def info = new HighlightInfo()
        
        if (highlightBlock.contains("\n")) {
            def idx = highlightBlock.indexOf('\n') 
            def firstLine = highlightBlock.substring(0, idx)  
            def m = firstLine =~ /(?s)[ \t]*([^\s]+)?[ \t]*([^\s]+)?(.*)/
            def langDef = (m[0][1] as String)?.split(':')
            if (langDef) {
                info.lang = langDef?.getAt(0)
                info.linenos = !(langDef.length > 1 && langDef[1] == 'nl')
            }
            info.caption = m[0][2]
            info.code = highlightBlock.substring(idx + 1)
        } else {
            info.code = highlightBlock
        }
        
        info
    } 
}
