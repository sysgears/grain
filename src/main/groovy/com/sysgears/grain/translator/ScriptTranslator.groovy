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

package com.sysgears.grain.translator

import com.sysgears.grain.translator.writer.ScriptWriter
import com.sysgears.grain.translator.writer.StatementType

import javax.inject.Inject
import javax.inject.Named
import javax.inject.Provider

/**
 * Grain script code to Groovy source code translator.
 */
public class ScriptTranslator {

    /** ScriptWriter factory */
    @Inject private Provider<ScriptWriter> scriptPrototype

    /**
     * Translates Grain script code into Groovy source code.
     * 
     * @param scriptSource Grain script source code
     * @param insertions list of text chunks, each chunk should be placed instead of ```, 
     *                   without translation
     * 
     * @return Groovy source code 
     */
    public String translate(String scriptSource, List insertions = []) {
        def codeIdx = 0
        def script = scriptPrototype.get()
        while (scriptSource.length() > 0) {
            def m = scriptSource =~ /```|\$\{|<%=|<%/

            def found = m.find()

            script.write(scriptSource.substring(0, found ? m.start() : scriptSource.length())
                    .replaceAll('\r', '').replaceAll(/([\$"\\])/, '\\\\$1'))

            scriptSource = found ? scriptSource.substring(m.end()) : ""

            if (found) {
                def op = m[0]

                if (op == '```') {
                    def code = insertions[codeIdx++] as String
                    script.write(code.replaceAll("\\\\", "\\\\\\\\").replaceAll("'", "\\\\'"), StatementType.STRING_WRITE)
                } else if (op == '<%') {
                    def m2 = scriptSource =~ /%>/
                    if (m2.find()) {
                        script.write(scriptSource.substring(0, m2.start()).trim() + ";\n", StatementType.PLAIN_CODE, true)
                        scriptSource = scriptSource.substring(m2.end())
                    }
                } else if (op == '<%=') {
                    def m2 = scriptSource =~ /%>/
                    if (m2.find()) {
                        script.write('${' + scriptSource.substring(0, m2.start()).trim() + '}',
                                StatementType.GSTRING_WRITE, true)
                        scriptSource = scriptSource.substring(m2.end())
                    }
                } else if (op == '${') {
                    def m2 = scriptSource =~ /}/
                    if (m2.find()) {
                        script.write('${' + scriptSource.substring(0, m2.start())
                                .replaceAll('[\r\n]', '').trim() + '}', StatementType.GSTRING_WRITE, true)
                        scriptSource = scriptSource.substring(m2.end())
                    }
                }
            }
        }
        script.toString()
    }
    
}
