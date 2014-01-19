package com.sysgears.grain.translator

import com.sysgears.grain.TestAppModule
import spock.guice.UseModules
import spock.lang.Specification

import javax.inject.Inject

@UseModules(TestAppModule)
class ScriptTranslatorSpec extends Specification {

    @Inject private ScriptTranslator translator

    def 'check $ block translation'() {
        expect: '$ block should be translated unmodified'
        translator.translate('abc${2 + 3}def').trim() == 'output.write("""abc${2 + 3}def""");' 
    }

    def 'check <%= block translation'() {
        expect: '<%= block should be translated unmodified'
        translator.translate('klm<%= 2 + 3 %>nop').trim() == 'output.write("""klm${2 + 3}nop""");'
    }

    def 'check <% block translation'() {
        expect: '<% block should be translated to groovy code block'
        translator.translate('abc<% def a = 1 %>def').trim() == 'output.write("""abc""");\ndef a = 1;\noutput.write("""def""");'
    }

    def 'check highlighted block translation'() {
        expect: 'highlighted block should be translated into statements that disallow groovy expressions'
        translator.translate('abc```def', ['$xyz']).trim() ==
                'output.write("""abc""");\noutput.write(\'\'\'$xyz\'\'\');\noutput.write("""def""");'
    }
    
    def 'check long lines truncation'() {
        def stLen = 'output.write("""""");'.length()
        expect: 'very long line should be automatically truncated'
        translator.translate('a' * 30000).trim() ==
                'output.write("""' + ('a' * (30000 - stLen)) + '""");\n' + 'output.write("""' + ('a' * stLen) + '""");'
    }

    def 'check translation of text with indivisible block'() {
        def stLen = 'output.write("""""");'.length()
        def block = '${r "/patch/to/some/icon.png"}' // indivisible block
        def text = ('a' * (30000 - stLen - (block.length() / 2))).trim() + block
        expect: 'indivisible block should not be automatically truncated'
        translator.translate(text).trim() == 'output.write("""' + ('a' * (30000 - stLen - (block.length() / 2))) +
                '""");\n' + 'output.write("""' + block + '""");'
    }
}
