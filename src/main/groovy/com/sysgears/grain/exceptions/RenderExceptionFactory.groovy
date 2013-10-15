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


package com.sysgears.grain.exceptions

import com.sysgears.grain.GrainConstants
import com.sysgears.grain.registry.ResourceParser
import com.sysgears.grain.render.RenderException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.runtime.StackTraceUtils

import javax.inject.Named



@Named
@javax.inject.Singleton
class RenderExceptionFactory {

    /**
     * Creates an instance of RenderException
     *
     * @param resource resource file
     */
    RenderException create(final String message, final Throwable cause, final File file) {
        // fetch original line number from raw source file
        int lineNumber = ErrorLineFetcher.fetchLineNumber(cause, file)
        // construct RenderException instance with all necessary information
        new RenderException(message, cause, file, lineNumber)
    }

    /**  */
    private static class ErrorLineFetcher {
        /**
         * Fetches line number of original raw file where exception occurred
         * @param cause
         * @param file
         * @return
         */
        static Integer fetchLineNumber(Throwable cause, File file) {
            def rootCause = StackTraceUtils.sanitizeRootCause(cause)
            def ste = rootCause.stackTrace.find { it.className ==~ GrainConstants.GRAIN_SCRIPT_PATTERN }
            int lineNumber = -1
            if (rootCause instanceof MultipleCompilationErrorsException) {
                MultipleCompilationErrorsException mcee = (MultipleCompilationErrorsException)rootCause
                Object message = mcee.getErrorCollector().getErrors().iterator().next()
                if (message instanceof SyntaxErrorMessage) {
                    SyntaxErrorMessage sem = (SyntaxErrorMessage)message;
                    lineNumber = sem.getCause().getLine()
                }
            } else {
                lineNumber = ste.lineNumber
            }
            def offsetIdx = 0
            def firstLine = new BufferedReader(new FileReader(file)).readLine().trim()
            if (firstLine.startsWith("---") || firstLine.startsWith("/*-")) {
                offsetIdx += 2
            }
            def resourceParser = new ResourceParser(file)
            def header = resourceParser.getHeader()
            offsetIdx += header.countLines() // header offset
            lineNumber + offsetIdx
        }
    }
}
