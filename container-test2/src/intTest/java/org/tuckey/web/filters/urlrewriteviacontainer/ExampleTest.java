package org.tuckey.web.filters.urlrewriteviacontainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.hamcrest.MatcherAssert.assertThat;

@Testcontainers
public class ExampleTest {

    //private RedisBackedCache underTest;

    @Container
    public GenericContainer webContainer = new GenericContainer(DockerImageName.parse("jboss/wildfly:9.0.1.Final"))
            .withExposedPorts(8086);

    @BeforeEach
    public void setUp() {
        // Assume that we have Redis running locally?
        //underTest = new RedisBackedCache("localhost", 6379);
    }

    @Test
    public void testSimplePutAndGet() {
        System.out.println(webContainer.getContainerId());
        //underTest.put("test", "example");

        //String retrieved = underTest.get("test");
        //assertThat("123").isEqualTo("example");
    }
}