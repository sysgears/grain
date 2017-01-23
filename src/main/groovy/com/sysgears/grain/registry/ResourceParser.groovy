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

package com.sysgears.grain.registry

import com.google.inject.assistedinject.Assisted
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.apache.commons.io.IOUtils
import org.jetbrains.annotations.Nullable


/**
 * Low-level resource parser. Returns resource header and resource content below.
 * <p>
 * Does all the parsing on demand.
 */
@CompileStatic
@Slf4j
class ResourceParser {
    
    /** Resource reader */
    private Reader reader

    /** Resource file */
    private final File file

    /** Parsed resource header, or null if not parsed yet */
    @Nullable private String header
    
    /** Parsed resource content, or null if not parsed yet */
    @Nullable private String content

    /** First content line */
    private String firstContentLine
    
    /**
     * Creates an instance of resource parser.
     * 
     * @param file resource file
     * @param reader resource reader
     */
    public ResourceParser(@Assisted File file, @Assisted Reader reader) {
        this.reader = reader
        this.file = file
    }

    /**
     * Creates an instance of resource parser.
     *
     * @param file resource file
     */
    public ResourceParser(@Assisted File file) {
        this.reader = null
        this.file = file
    }

    /**
     * Reads header from resource
     * 
     * @param reader reader to use 
     */
    private void readHeader(Reader reader) {
        def firstLine = reader.readLine()
        if (firstLine?.trim() in ['---', '/*-']) {
            def sb = new StringBuilder()
            while (true) {
                def line = reader.readLine()
                if (line == null)
                    throw new HeaderParseException("Wrong YAML header in resource at ${file}")
                if (line.trim() in ['---', '-*/']) {
                    break
                } else {
                    sb.append(line).append('\n')
                }
            }
            firstContentLine = ''
            header = sb.toString()
        } else {
            firstContentLine = firstLine + "\n"
            header = ''
        }
    }

    /**
     * Returns header of the resource (without wrapping lines)
     * 
     * @return resource header 
     */
    public String getHeader() {
        if (header == null) {
            def reader = this.reader ?: file.newReader()
            try {
                readHeader(reader)
            } finally {
                if (this.reader == null) {
                    reader.close()
                }
            }
        }
        header
    }

    /**
     * Returns content 
     * 
     * @return resource content
     */
    public String getContent() {
        if (content == null) {
            def reader = this.reader ?: file.newReader()
            try {
                if (this.reader == null)
                    readHeader(reader)
                content = firstContentLine + IOUtils.toString(reader)
            } finally {
                if (this.reader == null) {
                    reader.close()
                }
            }
        }
        content
    }
}
