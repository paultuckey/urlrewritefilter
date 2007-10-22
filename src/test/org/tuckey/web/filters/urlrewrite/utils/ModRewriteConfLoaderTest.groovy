package org.tuckey.web.filters.urlrewrite.utils

import groovy.util.GroovyTestCase

class ModRewriteConfLoaderTest extends GroovyTestCase {
    def loader = new ModRewriteConfLoader()

    void setUp() {
        Log.setLevel("DEBUG")
    }

    void testEngineOn() {
        def conf = loader.process("RewriteEngine on")
        assertNotNull conf
        assertTrue conf.engineEnabled
    }

    void testEngineOff() {
        def conf = loader.process("RewriteEngine off")
        assertNotNull conf
        assertFalse conf.engineEnabled
    }

    void testSimpleRule() {
        def conf = loader.process('''
            # redirect mozilla to another area
            RewriteCond %{HTTP_USER_AGENT} ^Mozilla.*
            RewriteRule ^/$                /homepage.max.html [L]
        ''')

        assertNotNull conf
        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertNotNull rule.conditions
        assertEquals 1, rule.conditions.size()

        def condition = rule.conditions[0]
        assertEquals 'user-agent', condition.name
        assertEquals '^Mozilla.*', condition.value

        assertEquals 'redirect mozilla to another area', rule.note
        assertEquals '^/$', rule.from
        assertEquals '/homepage.max.html', rule.to
        assertTrue rule.last
    }

    void testSimpleRedirect() {
        def conf = loader.process('''
            RewriteRule ^/$     http://www.foo.com [R]
        ''')

        assertNotNull conf
        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }

    void testPermanentRedirect() {
        def conf = loader.process('''
            RewriteRule ^/$     http://www.foo.com [R=301]
        ''')

        assertNotNull conf
        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'permanent-redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }

    void testTemporaryRedirect() {
        def conf = loader.process('''
            RewriteRule ^/$     http://www.foo.com [R=302]
        ''')

        assertNotNull conf
        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'temporary-redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }
}
