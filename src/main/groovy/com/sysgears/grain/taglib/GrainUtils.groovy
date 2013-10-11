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
     * Strips all HTML tags from the string
     */
    static def stripHtml = { String html ->
        html.replaceAll('\\<.*?\\>', '')
    }

    /**
     * Calculates md5 hash of byte array
     */
    static def md5 = { byte[] bytes ->
        MessageDigest md5 = MessageDigest.getInstance('MD5')
        md5.update(bytes)
        def messageDigest = md5.digest()
        def sb = new StringBuilder()
        messageDigest.each {
            sb.append(String.format('%02x', it))
        }
        sb.toString()
    }

    /**
     * Truncates string to the given word count
     */
    static def truncateWords = { String str, int wordCount ->
        def result = new StringBuilder()
        str.split('\\s+').take(wordCount).each { word ->
            result += word + ' '
        }
        result.toString().trim()
    }

    /**
     * Converts title to start principal words from capital letters 
     */
    static def titlecase = { String str ->
        def words = str.split(' ')
        def small_words = 'a an and as at but by en for if in of on or the to v v. via vs vs.'.split(' ')
        StringBuilder result = new StringBuilder()
        words.each { word ->
            if (word in small_words) {
                result.append(word)
            } else {
                result.append(StringUtils.capitalize(word))
            }
            result.append(' ')
        }

        result.toString().trim()
    }

    /**
     * Converts date to XML time format  
     */
    static def toXmlTime = { it ->
        def tz = String.format('%tz', it)
        String.format("%tFT%<tT${tz.substring(0, 3)}:${tz.substring(3)}", it)
    }
}
