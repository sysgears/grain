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

package com.sysgears.grain.translator.writer

/**
 * Builds a sequence of output.write() statements that have arbitrary groovy code between them.
 */
class ScriptWriter {

    /**
     * Maximum allowed line length 
     */
    private static final def MAX_LINE_LENGTH = 30000
    
    /** String writer that contains resulting groovy code */
    private StringWriter sw

    /** The type of the statement being written */
    private StatementType curStatement

    /** Current line length */
    private int lineLength

    /**
     * Creates an instance of output composer. 
     */
    public ScriptWriter() {
        sw = new StringWriter()
        curStatement = StatementType.PLAIN_CODE
        lineLength = 0
    }

    /**
     * Calculates the maximum number of characters that can be placed in the current line wrapped by the
     * specified statement.
     *
     * @param statement a type of statement to wrap the text in
     * @return maximum number of characters that can be placed in the current line
     */
    private def calculateMaxChars(StatementType statement) {
        def maxChars = MAX_LINE_LENGTH - statement.closeStr.trim().length() - lineLength
        if (curStatement != statement) {
            maxChars -= curStatement.closeStr.trim().length() + statement.openStr.length()
        }

        maxChars
    }

    /**
     * Closes a previous statement type and opens a new one.
     *
     * @param statement a type of statement to wrap the text in
     */
    private void openNewStatement(StatementType statement) {
        sw.write(curStatement.closeStr)
        curStatement = statement
        sw.write(statement.openStr)
        lineLength = statement.openStr.length()
    }

    /**
     * Writes text wrapped by the specified statement.
     *
     * @param statement a type of statement to wrap the text in
     * @param text a text to write
     * @param indivisible indicates whether text truncation prohibited or permitted
     */
    public void write(String text, StatementType statement = StatementType.GSTRING_WRITE, Boolean indivisible = false) {
        if (text.length() > 0) {
            def maxChars = calculateMaxChars(statement)
            def remText = text
            while (remText.length() > 0) {
                def chunk
                if (remText.length() > maxChars && !indivisible) {
                    def m = remText.substring(0, maxChars) =~ /[\\]+$/
                    maxChars = m.find() ? m.start() : maxChars

                    chunk = remText.substring(0, maxChars)
                } else {
                    chunk = remText
                }

                if (curStatement != statement || (indivisible && (chunk.length() > maxChars))) {
                    openNewStatement(statement)
                }
                sw.write(chunk)
                lineLength += chunk.length()
                if (lineLength + statement.closeStr.length() >= MAX_LINE_LENGTH) {
                    openNewStatement(statement)
                }
                remText = remText.substring(chunk.length())
            }
        }
    }
    
    /**
     * Returns sequence of output.write statements intermixed with groovy code as a string
     * 
      * @return sequence of output.write statements intermixed with groovy code as a string
     */
    public String toString() {
        if (curStatement != StatementType.PLAIN_CODE) {
            sw.write(curStatement.closeStr)
            curStatement = StatementType.PLAIN_CODE
        }
        sw.toString()
    }
}
