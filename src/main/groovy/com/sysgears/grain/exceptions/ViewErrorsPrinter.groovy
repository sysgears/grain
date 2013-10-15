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

import org.codehaus.groovy.runtime.StackTraceUtils

import javax.inject.Named


/** Class for pretty printing code snippets. Optimized for html views */
@Named
@javax.inject.Singleton
class ViewErrorsPrinter extends DefaultErrorsPrinter {

    /**
     * {@inheritDoc}
     */
    @Override
    String printCodeSnippet(Throwable e) {
        if (!e instanceof SourceCodeAware) {
            throw new IllegalArgumentException("Exception is not a SourceCodeAware interface implementation", e)
        }
        File file = e.file
        int lineNumber = e.lineNumber

        StringBuilder sb = new StringBuilder()
        formatCodeSnippetStart(sb, file.name, lineNumber.toString())

        sb << '<pre class="snippet">'
        def input = new BufferedInputStream(new FileInputStream(file))
        try {
            input.withReader { fileIn ->
                def reader = new LineNumberReader(fileIn)
                int last = lineNumber + CODE_SNIPPET_OFFSET
                def range = (lineNumber - CODE_SNIPPET_OFFSET..last)
                String currentLine = reader.readLine()

                while (currentLine != null) {
                    int currentLineNumber = reader.lineNumber
                    if (currentLineNumber in range) {
                        if (currentLineNumber == lineNumber) {
                            formatCodeSnippetErrorLine(sb, currentLineNumber.toString(), currentLine)
                        } else {
                            formatCodeSnippetLine(sb, currentLineNumber.toString(), currentLine)
                        }
                    }
                    currentLine = reader.readLine()
                }
            }
        } catch (Throwable t) {
            // ignore
        } finally {
            try {
                input?.close()
            } catch (Throwable t) {
                // ignore
            }
        }

        sb << "</pre>"
        sb.toString()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void formatCodeSnippetStart(StringBuilder sb, String fileName, String lineNumber) {
        sb << "<h2>Around line $lineNumber of <span class='filename'>${fileName}</span></h2>"
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String printStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder()
        sb << '<h2>Stack trace</h2><pre class="stack">'
        sb << super.printStackTrace(e)
        sb << '</pre>'
        sb.toString()
    }

    /**
     * Method for pretty printing of message snippet.
     * @param e Throwable instance.
     * @return pretty printed message snippet.
     */
    String printMessageSnippet(Throwable e) {
        def rootCause = StackTraceUtils.extractRootCause(e)
        def sb = new StringBuilder()
        sb << "<dl class='error-details'><dt>Class</dt><dd>${rootCause.getClass().getName()}</dd>"
        sb << "<dt>Message</dt><dd>${extractCauseMessage(e)}</dd></dl>"
        sb.toString()
    }

    /**
     * {@inheritDoc}
     */
    protected void formatCodeSnippetLine(StringBuilder sb, String lineNumber, String line) {
        sb << """<code class='line'><span class='lineNumber'>${lineNumber}:</span>${line.encodeAsHTML()}</code>
"""
    }

    /**
     * {@inheritDoc}
     */
    protected void formatCodeSnippetErrorLine(StringBuilder sb, String lineNumber, String line) {
        sb << """<code class='line error'><span class='lineNumber'>${lineNumber}:</span>${line.encodeAsHTML()}</code>
"""
    }

    /**
     * Prints exception message
     * @param e
     * @return
     */
    String printExceptionMessage(Throwable e) {
        "<h1>${e.getMessage()}</h1>"
    }
}
