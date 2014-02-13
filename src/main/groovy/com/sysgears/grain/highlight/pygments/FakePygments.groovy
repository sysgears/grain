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

import javax.annotation.Nullable

/**
 * Fake pygments integration, i.e. does nothing
 */
@javax.inject.Singleton
class FakePygments implements Pygments {

    /**
     * @inheritDoc
     */
    @Override
    String highlight(String code, String language) {
        code
    }

    /**
     * @inheritDoc
     */
    @Nullable String getCacheSubdir() {
        null
    }

    /**
     * @inheritDoc
     */
    @Override
    void configChanged() {
    }

    /**
     * @inheritDoc
     */
    @Override
    void start() {
    }

    /**
     * @inheritDoc
     */
    @Override
    void stop() {
    }
}
