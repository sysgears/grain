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
        translator.translate('abc${2 + 3}def') == 'out.write("""abc${->2 + 3}def""");'
    }

    def 'check <%= block translation'() {
        expect: '<%= block should be translated unmodified'
        translator.translate('klm<%= 2 + 3 %>nop') == 'out.write("""klm${-> 2 + 3 }nop""");'
    }

    def 'check <% block translation'() {
        expect: '<% block should be translated to groovy code block'
        translator.translate('abc<% def a = 1 %>def') == 'out.write("""abc"""); def a = 1 ;out.write("""def""");'
    }

    def 'check highlighted block translation'() {
        expect: 'highlighted block should be translated into statements that disallow groovy expressions'
        translator.translate('abc```def', ['$xyz']) ==
                'out.write("""abc""");out.write(\'\'\'$xyz\'\'\');out.write("""def""");'
    }
    
    def 'check long lines truncation'() {
        def stLen = 'out.write("""""");'.length()
        expect: 'very long line should be automatically truncated'
        translator.translate('a' * 30000) ==
                'out.write("""' + ('a' * (30000 - stLen)) + '""");' + 'out.write("""' + ('a' * stLen) + '""");'
    }
}
