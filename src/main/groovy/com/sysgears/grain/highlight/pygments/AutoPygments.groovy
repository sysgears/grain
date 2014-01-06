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

import javax.inject.Inject
import javax.inject.Named

/**
 * Tries to use Python with fallback to JPython for utilizing Pygments  
 */
@Named
@javax.inject.Singleton
class AutoPygments extends Pygments {

    /** Pygments as a Python process integration implementation */ 
    private @Inject PythonPygments pythonPygments

    /** Pygments as a Jython process integration implementation */
    private @Inject JythonPygments jythonPygments

    private @Inject PythonFinder pythonFinder

    /** Currently used Pygments integration implementation */
    private Pygments pygments

    /**
     * @inheritDoc 
     */
    @Override
    public String highlight(String code, String language) {
        pygments.highlight(code, language)
    }

    /**
     * @inheritDoc
     */
    @Override
    public void start() {
        pygments = pythonFinder.cmd != null ? pythonPygments : jythonPygments
        pygments.start()
    }
 
    /**
     * Shuts down Compass process 
     *
     * @throws Exception in case some error occur
     */
    @Override
    public void stop() {
        pygments.stop()
    }    
}
