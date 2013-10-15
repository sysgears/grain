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
 * The type of the statement being written into resulting Groovy code script.
 */
enum StatementType {

    /** out.write('''....''') statement */  
    STRING_WRITE("'''"),

    /** out.write("""...""") statement */
    GSTRING_WRITE('"""'),

    /** Groovy code without wrapping into any statement */ 
    PLAIN_CODE(null)

    /** Quote chars used in statement */
    private String quoteChars

    /**
     * Creates an instance of statement enum 
     *
     * @param quoteChar quote chars used in the statement
     */
    public StatementType(String quoteChar) {
        this.quoteChars = quoteChar
    }

    /**
     * Returns statement open expression.
     *
     * @return statement open expression 
     */
    public String getOpenStr() {
        quoteChars ? "out.write(${quoteChars}".toString() : ''
    }

    /**
     * Returns statement close expression.
     *
     * @return statement close expression 
     */
    public String getCloseStr() {
        quoteChars ? "${quoteChars});".toString() : ''
    }
}
