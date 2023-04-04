package org.tuckey.web.filters.urlrewriteviacontainer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.testcontainers.shaded.org.apache.commons.lang3.BooleanUtils.isTrue;

@Testcontainers
public class ExampleTest {

    private final MountableFile warFile = MountableFile.forHostPath("/Users/paul/co/urlrewritefilter/container-test2/example-webapp/target/webapp.war");

    @Container
    public GenericContainer<?> container = new GenericContainer(DockerImageName.parse("payara/micro"))
            .withExposedPorts(8087)
            .withCopyFileToContainer(warFile, "/opt/payara/deployments/webapp.war")
            .withCommand("--noCluster --deploy /opt/payara/deployments/webapp.war --contextRoot /")
            .waitingFor(Wait.forHttp("/get").forStatusCode(200));

    @BeforeEach
    public void setUp() {
        // Assume that we have Redis running locally?
        //underTest = new RedisBackedCache("localhost", 6379);
    }

    @Test
    public void checkContainerIsRunning(){
        assert(isTrue(container.isRunning()));
    }

    @Test
    public void testSimplePutAndGet() {
        //System.out.println(webContainer.getContainerId());
        //underTest.put("test", "example");

        //String retrieved = underTest.get("test");
        //assertThat("123").isEqualTo("example");


    }
}