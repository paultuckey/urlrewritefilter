# Configuration mod\_rewrite style #

Example

```
    <filter>
        <filter-name>UrlRewriteFilter</filter-name>
        <filter-class>org.tuckey.web.filters.urlrewrite.UrlRewriteFilter</filter-class>
        <init-param>
            <param-name>modRewriteConfText</param-name>
            <param-value>

    # redirect mozilla to another area
    RewriteCond  %{HTTP_USER_AGENT}  ^Mozilla.*
    RewriteRule  ^/$                 /homepage.max.html  [L] 

            </param-value>
        </init-param>
    </filter>
```


See documentation
http://urlrewritefilter.googlecode.com/svn/trunk/src/doc/manual/3.2/index.html#mod_rewrite_conf