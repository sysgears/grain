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
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.messages.ExceptionMessage
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.runtime.StackTraceUtils



class DefaultStackTracePrinter implements StackTracePrinter {

    /**
     * {@inheritDoc}
     */
    String printStackTrace(Throwable e) {
        if (!e instanceof SourceCodeAware) {
            throw new IllegalArgumentException("Exception is not a SourceCodeAware interface implementation", e)
        }
        def rootCause = StackTraceUtils.extractRootCause(e)
        def stackTrace = rootCause.stackTrace
        def lineNumWidth = Math.max(e.lineNumber, stackTrace*.lineNumber.max()).toString().size()
        def sb = new StringBuilder()
        sb << "\nMessage: ${e.getMessage()}\n"
        printCausedByMessage(sb, rootCause)

        if (stackTrace) {
            formatHeader(sb, "${"Line".padLeft(lineNumWidth + 4)} | Method")

            stackTrace.eachWithIndex { ste, i ->
                if (ste.fileName) {
                    String fileName
                    String lineNumber
                    if (ste.className ==~ GrainConstants.GRAIN_SCRIPT_PATTERN) {
                        fileName = e.file.absolutePath
                        lineNumber = e.lineNumber.toString().padLeft(lineNumWidth)
                    } else {
                        fileName = ste.className
                        lineNumber = ste.lineNumber.toString().padLeft(lineNumWidth)
                    }
                    String methodName = ste.methodName
                    if (i == 0) {
                        printFailureLocation(sb, lineNumber, methodName, fileName)
                    } else {
                        printStackLine(sb, lineNumber, methodName, fileName)
                    }
                }
            }
        }
        sb.toString()
    }

    /**
     * Pretty prints stack trace line.
     * @param sb StringBuilder to append string to.
     * @param lineNumber line number.
     * @param methodName method name.
     * @param fileName file.
     * @return prettified String with stack trace element line.
     */
    protected void printStackLine(StringBuilder sb, String lineNumber, String methodName, String fileName) {
        sb << "|   $lineNumber | $methodName in $fileName\n"
    }

    /**
     * Pretty print failure location of stack trace.
     * @param sb StringBuilder to append string to.
     * @param lineNumber line number.
     * @param methodName method name.
     * @param fileName file.
     * @return prettified String with error stack trace element line.
     */
    protected void printFailureLocation(StringBuilder sb, String lineNumber, String methodName, String fileName) {
        sb << "->> $lineNumber | $methodName in $fileName\n"
        sb << "- " * 36
        sb << '\n'
    }

    /**
     * Method for printing "Caused by" message.
     * @param e Throwable instance.
     * @return pretty printed "Caused by" message.
     */
    protected void printCausedByMessage(StringBuilder sb, Throwable e) {
        sb << "Caused by: ${extractCauseMessage(e)}\n"
    }


    /**
     * Pretty prints head of code snippet.
     * @param sb StringBuilder to append string to.
     * @param header prepared header.
     * @return pretty printed header as a String
     */
    protected void formatHeader(StringBuilder sb, String header) {
        sb << """$header
"""
    }

    /**
     * Extracts root cause's message and pretty prints it.
     * @param e Throwable instance.
     * @return pretty printed cause message.
     */
    protected String extractCauseMessage(Throwable e) {
        def rootCause = StackTraceUtils.sanitizeRootCause(e)
        def message = ''
        if (rootCause instanceof MultipleCompilationErrorsException) {
            rootCause = (MultipleCompilationErrorsException) rootCause
            def sem = rootCause.getErrorCollector().getErrors().iterator().next();
            if (sem instanceof SyntaxErrorMessage) {
                message = sem.getCause().getOriginalMessage()
            } else if (sem instanceof ExceptionMessage) {
                message = sem.getCause().getMessage()
            }
        } else {
            StackTraceElement ste = rootCause.stackTrace.find { it ==~ GrainConstants.GRAIN_SCRIPT_PATTERN }
            message = "${rootCause.getMessage() - "for class: ${ste.fileName - '.groovy'}"}"
        }
        message
    }
}
