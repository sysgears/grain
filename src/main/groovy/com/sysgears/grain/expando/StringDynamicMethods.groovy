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

package com.sysgears.grain.expando

import javax.inject.Named
import java.text.Normalizer
import java.util.regex.Pattern

/**
 * String class additional dynamic methods.
 */
@javax.inject.Singleton
class StringDynamicMethods {

    private final Pattern DIACTITIC_PATTERN = Pattern.compile('\\p{InCombiningDiacriticalMarks}+', Pattern.UNICODE_CASE)
    private final Pattern PUNCTUATION_PATTERN = Pattern.compile('\\p{Punct}+', Pattern.UNICODE_CASE)
    private final Pattern WHITESPACES_PATTERN = Pattern.compile('\\s+', Pattern.UNICODE_CASE)

    /**
     * Registers additional methods for String class
     */
    void register() {

        /**
         * Creates URL slug from string that can be used as SEO-friendly URL 
         */
        String.metaClass.encodeAsSlug = {
            String str = delegate
            if (str.isEmpty()) {
                return ""
            }

            removeWhitespaces(normalize(str.toLowerCase()))
        }
    }

    /**
     * Replaces all the whitespaces with '-' in a string
     * 
     * @param str a string
     * 
     * @return string with whitespaces replaced by '-'
     */
    private String removeWhitespaces(String str) {
        str.replaceAll(WHITESPACES_PATTERN, "-")
    }


    /**
     * Replaces all extended unicode chars and punctuation chars with whitespaces
     *
     * @param str a string
     *
     * @return string with extended chars and punctuation chars replaced by whitespaces
     */
    private String normalize(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD)
                .replaceAll(DIACTITIC_PATTERN, "")
                .replaceAll(PUNCTUATION_PATTERN, " ")
                .trim()
    }
}
