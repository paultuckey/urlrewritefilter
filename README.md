
# UrlRewriteFilter

### [UrlRewriteFilter Web Site](http://www.tuckey.org/urlrewrite/) | [Documentation](https://tuckey.org/urlrewrite/manual/4.0/index.html)


*Based on the popular and very useful [mod_rewrite](http://httpd.apache.org/docs-2.0/mod/mod_rewrite.html) for apache, UrlRewriteFilter is a Java Web Filter for any J2EE compliant web application server (such as [Resin](http://caucho.com), [Orion](http://www.orionserver.com/) or [Tomcat](http://tomcat.apache.org/)), which allows you to rewrite URLs before they get to your code. It is a very powerful tool just like Apache's mod_rewrite.*

URL rewriting is very common with Apache Web Server (see [mod_rewrite's rewriting guide](http://httpd.apache.org/docs-2.0/misc/rewriteguide.html)) but has not been possible in most java web application servers. The main things it is used for are:

  * URL Tidyness / [URL Abstraction](https://tuckey.org/urlrewrite/manual/4.0/guide.html#urlabs) - keep URLs tidy irrespective of the underlying technology or framework (JSP, Servlet, Struts etc).
  * Browser Detection - Allows you to rewrite URLs based on request HTTP headers (such as user-agent or charset).
  * Date based rewriting - Allows you to forward or redirect to other URL's based on the date/time (good for planned outages).
  * Moved content - enable a graceful move of content or even a change in CMS.
  * Tiny/Friendly URL's (i.e. blah.com/latest can be redirected to blah.com/download/ver1.2.46.2/setup.exe)
  * A Servlet mapping engine (see [Method Invocation](https://tuckey.org/urlrewrite/manual/4.0/guide.html#method))


!UrlRewriteFilter uses an xml file, called urlrewrite.xml (it goes into the WEB-INF directory), for configuration.  Most parameters can be Perl5 style Regular Expressions or Wildcard Expressions. This makes it very powerful indeed.

See the [manual](https://tuckey.org/urlrewrite/manual/4.0/index.html) for more information.


## Quick Start

  * Add Maven dependency below or add <a href="http://central.maven.org/maven2/org/tuckey/urlrewritefilter/5.1.3/urlrewritefilter-5.1.3.jar">urlrewritefilter-5.1.3.jar</a> directly into your <b>WEB-INF/lib</b> directory.
```xml
    <dependency>
        <groupId>org.tuckey</groupId>
        <artifactId>urlrewritefilter</artifactId>
        <version>5.1.3</version>
    </dependency>
```

For Servlet API 4 or less (javax), use the old version 4.x
```xml
    <dependency>
        <groupId>org.tuckey</groupId>
        <artifactId>urlrewritefilter</artifactId>
        <version>4.0.3</version>
    </dependency>
```
  * Add the following to your WEB-INF/web.xml (add it near the top above your servlet mappings (if you have any)): (see <a href="https://tuckey.org/urlrewrite/manual/4.0/index.html#filterparams">filter parameters</a> for more options)
```xml
    <filter>
        <filter-name>UrlRewriteFilter</filter-name>
        <filter-class>org.tuckey.web.filters.urlrewrite.UrlRewriteFilter</filter-class>
    </filter>
    <filter-mapping>
        <filter-name>UrlRewriteFilter</filter-name>
        <url-pattern>/*</url-pattern>
        <dispatcher>REQUEST</dispatcher>
        <dispatcher>FORWARD</dispatcher>
    </filter-mapping>
```
  * Add <b><a href="https://tuckey.org/urlrewrite/manual/4.0/urlrewrite.xml">urlrewrite.xml</a></b> into your WEB-INF directory. (src/main/webapp/WEB-INF/ for Maven users)
  * Restart the context.

You can visit http://127.0.0.1:8080/rewrite-status (or whatever the address of your local webapp and context) to see output (note: this page is only viewable from localhost).

Previously on Google Code: http://code.google.com/p/urlrewritefilter.
