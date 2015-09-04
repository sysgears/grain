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

import com.sysgears.grain.annotations.Uncached
import com.sysgears.grain.preview.FileChangeListener
import com.sysgears.grain.service.AgentError
import groovyx.gpars.agent.Agent

import javax.inject.Inject

/**
 * Header parser, that maintains header cache and parses unmodified file only once
 */
@javax.inject.Singleton
class CachedHeaderParser implements HeaderParser, FileChangeListener {

    /** Uncached header parser */
    @Inject @Uncached private HeaderParser headerParser

    /** Header cache state agent */
    private final Agent cache = new Agent(new HeaderCacheState())

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Object> parse(File resourceFile) throws HeaderParseException {
        cache.sendAndWait { it.get(resourceFile, { headerParser.parse(resourceFile) }) }.getOrThrowError().clone()
    }

    /**
     * @inheritDoc
     */
    @Override
    Map<String, Object> parse(File resourceFile, String header) throws HeaderParseException {
        cache.sendAndWait { it.get(resourceFile, {headerParser.parse(resourceFile, header)}) }.getOrThrowError().clone()
    }

    /**
     * @inheritDoc
     */
    @Override
    public void fileChanged(File file) {
        cache << { it.remove(file) }
    }

    /**
     * The state provides methods for accessing the header cache.
     */
    private static class HeaderCacheState {

        /* The location -> header cache map */
        private final Map<File, Map> headers = [:]

        /**
         * Returns the headers for the given file location.
         *
         * @param location a file location
         * @param closure must provide the headers that will be cached if the value is not found for the location
         * @return the cached or resolved headers for the given file location, agent error in case of exception
         */
        public StateResponse get(File location, closure) {
            try {
                if (headers[location] == null) { headers[location] = closure() }
                [response: headers[location]] as StateResponse
            } catch (e) {
                [error: new AgentError(cause: e)] as StateResponse
            }
        }

        /**
         * Removes the headers from the cache for a file location.
         * @param location a file location to remove headers for
         */
        public void remove(File location) {
            headers.remove(location)
        }

        /**
         * Agent state response holder.
         */
        static class StateResponse {

            Map response
            AgentError error

            def getOrThrowError() {
                if (error) { throw error.cause }
                response
            }
        }
    }
}
