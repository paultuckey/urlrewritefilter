package org.tuckey.web.filters.urlrewriteviacontainer;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import java.io.IOException;
import java.nio.file.Paths;


@Testcontainers
public class ExampleTest {

    String webappPath = Paths.get("..", "example-webapp", "target", "example-webapp.war")
            .toAbsolutePath().toString();

    @Container
    public GenericContainer<?> container = new GenericContainer<>("tomcat:10.1.9")
            .withReuse(true)
            .withExposedPorts(8080)
            .withFileSystemBind(webappPath, "/usr/local/tomcat/webapps/example-webapp.war")
            .waitingFor(Wait.forHttp("/example-webapp/test/test.jsp").forStatusCode(200));

    @BeforeEach
    public void setUp() {
        System.out.println("War: " + webappPath);
    }

    @Test
    public void checkContainerIsRunning() {
        System.out.println(container.getContainerId());
        assert (container.isRunning());
    }

    @Test
    public void testSimplePutAndGet() throws IOException {
        System.out.println("Container ID" + container.getContainerId());

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            ClassicHttpRequest httpGet = ClassicRequestBuilder.get("http://localhost:8080/example-webapp/test/test.jsp")
                    .build();
            httpClient.execute(httpGet, response -> {
                System.out.println(response.getCode() + " " + response.getReasonPhrase());
                return null;
            });
        }
    }
}