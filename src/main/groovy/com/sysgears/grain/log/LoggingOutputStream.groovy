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

package com.sysgears.grain.log

import groovy.util.logging.Slf4j

import javax.inject.Named

/**
 * Output stream implementation that writes all output to the log 
 */
@Named
@Slf4j
class LoggingOutputStream extends OutputStream {
    private StringBuilder sb = new StringBuilder()

    /**
     * @inheritDoc 
     */
    @Override
    void write(int i) throws IOException {
        sb.append((char)i)
        flush()
    }

    /**
     * @inheritDoc
     */
    @Override
    void write(byte[] bytes) throws IOException {
        sb.append(new String(bytes))
        flush()
    }

    /**
     * @inheritDoc
     */
    @Override
    void write(byte[] bytes, int i, int i1) throws IOException {
        sb.append(new String(bytes, i, i1))
        flush()
    }

    /**
     * @inheritDoc
     */
    @Override
    void flush() throws IOException {
        int idx = 0
        while (idx != -1) {
            idx = sb.indexOf('\n')
            if (idx != -1) {
                log.info sb.substring(0, idx)
                sb = sb.delete(0, idx + 1)
            }
        }
    }
}
