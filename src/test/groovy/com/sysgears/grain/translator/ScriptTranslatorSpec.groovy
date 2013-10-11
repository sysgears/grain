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
        translator.translate('abc${2 + 3}def').trim() == 'out.write("""abc${2 + 3}def""");' 
    }

    def 'check <%= block translation'() {
        expect: '<%= block should be translated unmodified'
        translator.translate('klm<%= 2 + 3 %>nop').trim() == 'out.write("""klm${2 + 3}nop""");'
    }

    def 'check <% block translation'() {
        expect: '<% block should be translated to groovy code block'
        translator.translate('abc<% def a = 1 %>def').trim() == 'out.write("""abc""");\ndef a = 1;\nout.write("""def""");'
    }

    def 'check highlighted block translation'() {
        expect: 'highlighted block should be translated into statements that disallow groovy expressions'
        translator.translate('abc```def', ['$xyz']).trim() ==
                'out.write("""abc""");\nout.write(\'\'\'$xyz\'\'\');\nout.write("""def""");'
    }
    
    def 'check long lines truncation'() {
        def stLen = 'out.write("""""");'.length()
        expect: 'very long line should be automatically truncated'
        translator.translate('a' * 30000).trim() ==
                'out.write("""' + ('a' * (30000 - stLen)) + '""");\n' + 'out.write("""' + ('a' * stLen) + '""");'
    }
}
