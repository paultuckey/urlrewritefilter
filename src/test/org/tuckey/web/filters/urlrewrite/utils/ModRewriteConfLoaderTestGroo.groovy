package org.tuckey.web.filters.urlrewrite.utils

import groovy.util.GroovyTestCase
import org.tuckey.web.filters.urlrewrite.Conf

class ModRewriteConfLoaderTestGroo extends GroovyTestCase {
    def loader = new ModRewriteConfLoader()
    def conf

    void setUp() {
        Log.setLevel("DEBUG")
        conf = new Conf()
    }

    void testEngineOn() {
        loader.process("RewriteEngine on", conf)
        assertTrue conf.engineEnabled
    }

    void testEngineOff() {
        loader.process("RewriteEngine off", conf)
        assertFalse conf.engineEnabled
    }

    void testSimpleRule() {
        loader.process('''
            # redirect mozilla to another area
            RewriteCond %{HTTP_USER_AGENT} ^Mozilla.*
            RewriteRule ^/$                /homepage.max.html [L]
        ''', conf)

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
        loader.process('''
            RewriteRule ^/$     http://www.foo.com [R]
        ''', conf)

        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }

    void testPermanentRedirect() {
        loader.process('''
            RewriteRule ^/$     http://www.foo.com [R=301]
        ''', conf)

        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'permanent-redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }

    void testTemporaryRedirect() {
        loader.process('''
            RewriteRule ^/$     http://www.foo.com [R=302]
        ''', conf)

        assertNotNull conf.rules
        assertEquals 1, conf.rules.size()

        def rule = conf.rules[0]
        assertNotNull rule
        assertEquals 'temporary-redirect', rule.toType
        assertEquals '^/$', rule.from
        assertEquals 'http://www.foo.com', rule.to
    }
}
