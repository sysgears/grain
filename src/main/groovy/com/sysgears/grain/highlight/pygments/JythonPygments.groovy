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

import com.sysgears.grain.init.GrainSettings
import groovy.util.logging.Slf4j
import org.python.core.PyFunction
import org.python.core.PyObject
import org.python.core.PyUnicode
import org.python.util.PythonInterpreter

import javax.inject.Inject
import java.util.concurrent.CountDownLatch

/**
 * Pygments code highlighter.
 * <p>
 * This class is thread-safe.     
 */
@javax.inject.Singleton
@Slf4j
class JythonPygments extends Pygments {

    /** JPython interpreter instance */
    private PythonInterpreter python

    /** Pygments python formatter Object */
    private PyObject formatter

    /** Grain settings */
    @Inject private GrainSettings settings

    /** Mutex for pygments starting and using */
    private CountDownLatch latch

    /**
     * Initializes JPython interpreter and pygments highlighter.
     */
    @Override
    public void start() {
        latch = new CountDownLatch(1)
        Thread.start {
            try {
                log.info 'Launching bundled Jython pygments...'
                python = new PythonInterpreter()
                python.exec('import sys')
                // There are some problems with \ on Windows here, use backslashes instead
                python.exec("sys.path.append('${new File(settings.toolsHome, 'pygments-main').absolutePath.replace('\\', '/')}')")
                python.exec("from pygments import highlight")
                python.exec('from pygments.lexers import get_lexer_by_name')
                python.exec('from pygments.formatters import HtmlFormatter')
                formatter = python.eval('HtmlFormatter(encoding=\'utf-8\')')
                log.info 'Jython pygments initialized'
                latch.countDown()
            } catch (t) {
                log.error("Error launching Pygments", t)
                latch.countDown()
            }
        }
    }

    /**
     * Stops JPython interpreter
     */
    @Override
    public void stop() {
        if (latch) {
            latch.await()
            python?.cleanup()
            log.info 'Bundled Jython Pygments finished'
            python = null
            latch = null
        }
    }

    /**
     * Highlights code using Pygments 
     * 
     * @param code a code to highlight
     * @param language the language of the code
     * 
     * @return highlighted code HTML
     */
    public String highlight(String code, String language) {
        try {
            latch.await()
            PyFunction f = python.get('highlight', PyFunction.class)
            PyObject lexer = python.eval("get_lexer_by_name('${language}', encoding='utf-8')")
            return f.__call__(new PyUnicode(code), lexer, formatter).asString()
        } catch (t) {
            log.error("Error highlighting code:\n ${code}", t)
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void configChanged() {
    }
}
