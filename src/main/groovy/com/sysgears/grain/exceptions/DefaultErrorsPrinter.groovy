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

import javax.inject.Named

/**
 * Class with basic method for pretty printing code snippets.
 */
@Named
@javax.inject.Singleton
class DefaultErrorsPrinter extends DefaultStackTracePrinter implements CodeSnippetPrinter {

    /**
     * Constant for determination of lines count to be shown before and after error line.
     */
    protected static final int CODE_SNIPPET_OFFSET = 5

    /**
     * {@inheritDoc}
     */
    @Override
    String printCodeSnippet(Throwable e) {
        if (!e instanceof SourceCodeAware) {
            throw new IllegalArgumentException("Exception instance is not a SourceCodeAware implementation", e)
        }
        File file = e.file
        int lineNumber = e.lineNumber

        def lineNumWidth = file.text.countLines().toString().size()

        StringBuilder sb = new StringBuilder()
        formatCodeSnippetStart(sb, file.name, lineNumber.toString())
        formatHeader(sb, "${"Line".padLeft(lineNumWidth + 4)} | Code")
        def input = new BufferedInputStream(new FileInputStream(file))
        try {
            input.withReader { fileIn ->
                def reader = new LineNumberReader(fileIn)
                int last = lineNumber + CODE_SNIPPET_OFFSET
                def range = (lineNumber - CODE_SNIPPET_OFFSET..last)
                String currentLine = reader.readLine()

                while (currentLine != null) {
                    int currentLineNumber = reader.getLineNumber()
                    if (currentLineNumber in range) {
                        boolean isErrorLine = currentLineNumber == lineNumber
                        if (isErrorLine) {
                            formatCodeSnippetErrorLine(sb, currentLineNumber.toString().padLeft(lineNumWidth),
                                    currentLine)
                        } else {
                            formatCodeSnippetLine(sb, currentLineNumber.toString().padLeft(lineNumWidth),
                                    currentLine)
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

        formatCodeSnippetEnd(sb)
        sb.toString()
    }

    /**
     * Method for formatting code snippet line.
     * @param sb StringBuilder to append string to.
     * @param lineNumber number of a line.
     * @param line contents of a line.
     * @return formatted line of code snippet.
     */
    protected void formatCodeSnippetLine(StringBuilder sb, String lineNumber, String line) {
        sb << """|   $lineNumber | $line
"""
    }

    /**
     * Method for formatting start of code snippet.
     * @param sb StringBuilder to append string to.
     * @param fileName where code snippet is taken from.
     * @param lineNumber current line in code snippet.
     * @return
     */
    protected void formatCodeSnippetStart(StringBuilder sb, String fileName, String lineNumber) {
        sb << """
Around line $lineNumber of $fileName
"""
        sb << '- ' * 36
        sb << '\n'
    }

    /**
     * Method for formatting end of code snippet.
     * @param sb StringBuilder to append string to.
     * @return
     */
    protected void formatCodeSnippetEnd(StringBuilder sb) {
        sb << "- " * 36
        sb << '\n'
    }

    /**
     * Method for formatting code snippet error line.
     * @param sb StringBuilder to append string to.
     * @param lineNumber number of an error line.
     * @param line contents of an error line.
     * @return formatted error line of code snippet.
     */
    protected void formatCodeSnippetErrorLine(StringBuilder sb, String lineNumber, String line) {
        sb << """->> $lineNumber | $line
"""
    }
}
