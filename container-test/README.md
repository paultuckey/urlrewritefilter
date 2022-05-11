# Container Tests

    mvn clean verify org.codehaus.cargo:cargo-maven3-plugin:run

    mvn clean verify org.codehaus.cargo:cargo-maven3-plugin:run \
        -Dcargo.maven.containerId=tomcat9x \
        -Dcargo.maven.containerUrl=https://repo.maven.apache.org/maven2/org/apache/tomcat/tomcat/9.0.45/tomcat-9.0.45.zip


    mvn clean verify org.codehaus.cargo:cargo-maven3-plugin:run
        -Dcargo.maven.containerId=wildfly20x
        -Dcargo.maven.containerUrl=https://download.jboss.org/wildfly/20.0.1.Final/wildfly-20.0.1.Final.zip
        -Dcargo.servlet.port=9000