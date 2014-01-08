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

package com.sysgears.grain.highlight.pygments

import com.sysgears.grain.config.Service
import com.sysgears.grain.highlight.Highlighter
import com.sysgears.grain.init.GrainSettings
import com.sysgears.grain.preview.ConfigChangeListener
import com.sysgears.grain.taglib.Site
import groovy.util.logging.Slf4j

import javax.annotation.Nullable
import javax.inject.Inject

/**
 * Interface for Pygments integration.
 */
@Slf4j
abstract class Pygments implements Highlighter, ConfigChangeListener, Service {

    /** Pygments thread */
    protected Thread thread

    /** Site instance */
    private @Inject Site site

    /** Grain settings */
    private @Inject GrainSettings settings

    /**
     * Launches pygments in a separate thread 
     * <p>
     * If the thread is interrupted the process will be destroyed.
     */
    public abstract void start()

    /**
     * Terminates pygments thread 
     */
    public abstract void stop()

    /**
     * Highlights code using Pygments 
     *
     * @param code a code to highlight
     * @param language the language of the code
     *
     * @return highlighted code HTML
     */
    public abstract String highlight(String code, String language)

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        "pygments"
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
