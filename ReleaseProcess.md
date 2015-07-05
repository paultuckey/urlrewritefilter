# Release Procedures #


## Test in container ##

```
cd container-test
mvn clean verify

mvn integration-test -Dcargo.maven.containerId=tomcat5x    -Dcargo.maven.containerUrl=http://www.apache.org/dist/tomcat/tomcat-5/v5.5.35/bin/apache-tomcat-5.5.35.zip
# last status: ok

mvn integration-test -Dcargo.maven.containerId=tomcat6x    -Dcargo.maven.containerUrl=http://mirror.olnevhost.net/pub/apache/tomcat/tomcat-6/v6.0.35/bin/apache-tomcat-6.0.35.zip
# last status: ok

mvn integration-test -Dcargo.maven.containerId=tomcat7x    -Dcargo.maven.containerUrl=http://mirror.olnevhost.net/pub/apache/tomcat/tomcat-7/v7.0.28/bin/apache-tomcat-7.0.28.zip
# last status: ok

mvn integration-test -Dcargo.maven.containerId=glassfish3x -Dcargo.maven.containerUrl=http://download.java.net/glassfish/3.1.1/release/glassfish-3.1.1.zip
# last status: delete not a supported http method, otherwise ok

mvn integration-test -Dcargo.maven.containerId=resin31x -Dcargo.maven.containerUrl=http://www.caucho.com/download/resin-3.1.12.zip
# last status: resin doesn't start??

mvn integration-test -Dcargo.maven.containerId=jetty8x -Dcargo.maven.containerUrl=http://dist.codehaus.org/jetty/jetty-hightide-8.1.4/jetty-hightide-8.1.4.v20120524.zip
# last status: testNoDecode fails, otherwise ok

mvn integration-test -Dcargo.maven.containerId=jboss71x -Dcargo.maven.containerUrl=http://download.jboss.org/jbossas/7.1/jboss-as-7.1.1.Final/jboss-as-7.1.1.Final.zip

```


## Update documentation and version number ##

```
pom.xml
annotation/pom.xml
container-test/pom.xml

mvn package                 # builds the jar
ant docs-version-update     # updates the version in the docs
```

Update changelog: src/doc/manual/4.0/introduction.html

## Check ~/.m2/settings xml has in it ##

```
<servers>
    <server>
      <id>sonatype-nexus-snapshots</id>
      <username>uuuuuu</username>
      <password>xxxxxx</password>
    </server>
    <server>
      <id>sonatype-nexus-staging</id>
      <username>uuuuuu</username>
      <password>xxxxxx</password>
    </server>
  </servers>
</settings>
```


## Do the release ##

```
mvn install release:prepare
mvn release:perform -Dgpg.passphrase=XXXXX
```

Go to: https://oss.sonatype.org/

Close and release the release

## Update Sites ##

Update project web site: http://tuckey.org/urlrewrite/

Update project home page: http://code.google.com/p/urlrewritefilter/

Upload to http://code.google.com/p/urlrewritefilter/downloads/list
  * jar
  * sources
