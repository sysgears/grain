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

import java.security.MessageDigest

/**
 * Static methods exposed through ResourceScript into resources code.
 */
class GrainUtils {

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
        }.toString()
    }

    /**
     * Outputs fixed block.
     * <p>
     * The fixed block is a block delimited by &&&. It is not modified by Grain and rendered as is.  
     * 
     * @param text source text
     * 
     * @return source text wrapped into fixed block
     */
    static def fixedBlock(String text) {
        '`!`' + text.replace('`!`', '`!!`') + '`!`'
    }
}
