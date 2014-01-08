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

package com.sysgears.grain.taglib

import com.sun.xml.internal.ws.util.StringUtils

import java.security.MessageDigest

/**
 * Static methods exposed through ResourceScript into resources code.
 */
class GrainUtils {

    /**
     * Removes all XML like tags from a string:
     * <code>assert stripHtml('<em>emphasis</em>') == 'emphasis'</code>.
     *
     * @attr html string to strip
     *
     * @return the resulting string
     */
    static def stripHtml = { String html ->
        html.replaceAll('\\<.*?\\>', '')
    }

    /**
     * Calculates md5 hash of a byte array.
     *
     * @attr bytes to calculate a hash
     *
     * @return the md5 hash
     */
    static def md5 = { byte[] bytes ->
        MessageDigest md5 = MessageDigest.getInstance('MD5')
        md5.update(bytes)
        md5.digest().inject(new StringBuffer()) { sb, it ->
            sb.append(String.format('%02x', it))
        } .toString()
    }

    /**
     * Truncates string to the given word count.
     *
     * @attr string the string to truncate
     * @attr wordCount the number of words to left
     *
     * @return the resulting string
     */
    static def truncateWords = { String string, int wordCount ->
        string.split('\\s+').take(wordCount).inject(new StringBuilder()) { result, word ->
            result.append(word).append(' ')
        } .toString().trim()
    }

    /**
     * Converts title by applying Title Case capitalizing convention (capitalizes all principal words).
     *
     * @attr title the title to convert
     *
     * @return title-case string
     */
    static def titlecase = { String title ->
        def nonPrincipalWords = ['a', 'an', 'and', 'as', 'at', 'but', 'by', 'en', 'for', 'if', 'in',
                'of', 'on', 'or', 'the', 'to', 'v', 'v.', 'via', 'vs', 'vs.']
        title.split(' ').inject(new StringBuilder()) {result, word ->
            word in nonPrincipalWords ? result.append(word) : result.append(StringUtils.capitalize(word))
            result.append(' ')
        } .toString().trim()
    }

    /**
     * Converts a date to XML date time format.
     *
     * @attr date the date to convert
     *
     * @return XML date time representation of the date, for instance 2013-12-31T12:49:00+07:00
     */
    static def toXmlTime = { Date date ->
        def tz = String.format('%tz', date)
        String.format("%tFT%<tT${tz.substring(0, 3)}:${tz.substring(3)}", date)
    }
}
