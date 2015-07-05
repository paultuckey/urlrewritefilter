# Proposed Features #

## Features for 3.1 ##

  * Full mod\_rewrite configuration support (including RewriteMap)


## Later Releases ##

  * Allowing UrlRewrite to be initialized and configured in a Spring application context

  * Allowing UrlRewrite to be controlled at runtime using JMX (which could be done with almost no effort in a Spring-based env if UrlRewrite were more amenable)

  * apache httpd configuration parser for tomcat.  This would allow administrators to try tomcat without having to learn how to configure it.  mod\_rewrite style configuration is a critical component for this.

  * user in role throw 403 if no match?

  * ability to test new conf without having to reload, via status page

  * allow condition matching on get-param or post-param as well as just parameter

  * store list of robots and hide jsessionid when a robot also have condition

  * allow mounting of packages from /xxxyyy/aaa.gif to org.tuckey.xxx.static."aaa.gif" http://wiki.opensymphony.com/pages/viewpage.action?pageId=4476

  * random condition type
    * 2. A randomized condition, i.e. a condition which is true with a certain probability.
    * 3. A round robin condition, i.e. a condition which is true every n-th time.

  * backrefs in sets

  * better debugging of server name matcher

  * debugging tool (especially a googlebot client debugger, possibly add googlebot tag)

  * ability to set request parameters

  * ability to specify a $1 as $encode($1) (or something like that)

  * `<to-servlet>struts</to-servlet>`  will call context.getNamedDispatcher() to similar

  * grouping of rule for default settings

  * capture original (pre match) url into request attr so that people can use it

  * a way to compare ALL parameters with a pattern?

  * In Apache: RewriteEngine on
    * `RewriteMap upper2lower int:tolower`
    * `RewriteRule ^/(.*)$ /${upper2lower:$1`}

  * An analogue to this mod\_rewrite feature:
    * `RewriteCond      %{REQUEST_FILENAME}   !-f`
    * `RewriteCond      %{REQUEST_FILENAME}   !-d`

  * debug screen, ie, this request matches the following rules

  * capture original full url (incl query string) into request attr so it can be used later