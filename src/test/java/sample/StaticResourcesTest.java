package sample;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

public class StaticResourcesTest extends JerseyTest {

    /**
     * クラスパスからhello.txtを読み込んで返す事を確認する。
     * 
     * @throws Exception
     */
    @Test
    public void testGetHelloText() throws Exception {
        Response response = target("static/hello.txt").request().get();
        assertThat("Status code", response.getStatusInfo(), is(Status.OK));
        assertThat("Conetnt-Type", response.getMediaType(),
                is(MediaType.TEXT_PLAIN_TYPE));
        assertThat("Entity body", response.readEntity(String.class),
                is("Hello, world!"));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(StaticResources.class);
    }
}
