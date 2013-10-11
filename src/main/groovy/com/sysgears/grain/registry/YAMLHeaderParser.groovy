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

import org.yaml.snakeyaml.Yaml

import javax.inject.Inject
import javax.inject.Named

/**
 * Resource YAML header parser. 
 */
@Named
@javax.inject.Singleton
class YAMLHeaderParser implements HeaderParser {

    /** YAML parser */
    @Inject private Yaml yaml
    
    /**
     * Parses resource YAML header
     *
     * @param resourceFile resource file to parse
     *
     * @return header key -> value map
     *
     * @throws HeaderParseException in case of header parse error 
     */
    Map<String, Object> parse(File resourceFile) throws HeaderParseException {
        parse(resourceFile, new ResourceParser(resourceFile).header)
    }

    /**
     * Parses resource YAML header
     *
     * @param resourceFile resource file to parse
     * @param header contents of the resource file header
     *
     * @return header key -> value map
     *
     * @throws HeaderParseException in case of header parse error 
     */
    Map<String, Object> parse(File resourceFile, String header) throws HeaderParseException {
        try {
            yaml.load(header) as Map ?: [:]
        } catch (t) {
            throw new HeaderParseException("Error parsing header of resource at ${resourceFile}", t)
        }
    }
}
