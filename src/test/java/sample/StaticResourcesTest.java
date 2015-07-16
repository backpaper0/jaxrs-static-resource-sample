package sample;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Assume;
import org.junit.Test;

public class StaticResourcesTest extends JerseyTest {

    private Counter counter;

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
        assertThat("Count", counter.getCount(), is(1));
    }

    /**
     * JavaScriptファイルも返される事を確認する。
     * 
     * @throws Exception
     */
    @Test
    public void testGetFibJs() throws Exception {
        Response response = target("static/fib.js").request().get();
        assertThat("Status code", response.getStatusInfo(), is(Status.OK));
        assertThat("Conetnt-Type", response.getMediaType(),
                is(MediaType.valueOf("text/javascript")));

        ScriptEngine engine = new ScriptEngineManager()
                .getEngineByMimeType("text/javascript");
        engine.eval(response.readEntity(String.class));

        assertThat("fib(0)", engine.eval("fib(0)"), is(0));
        assertThat("fib(1)", engine.eval("fib(1)"), is(1));
        assertThat("fib(2)", engine.eval("fib(2)"), is(1.0));
        assertThat("fib(3)", engine.eval("fib(3)"), is(2.0));
        assertThat("fib(4)", engine.eval("fib(4)"), is(3.0));
        assertThat("fib(5)", engine.eval("fib(5)"), is(5.0));
        assertThat("fib(6)", engine.eval("fib(6)"), is(8.0));
        assertThat("fib(7)", engine.eval("fib(7)"), is(13.0));
        assertThat("fib(8)", engine.eval("fib(8)"), is(21.0));
        assertThat("fib(9)", engine.eval("fib(9)"), is(34.0));

        assertThat("Count", counter.getCount(), is(1));
    }

    /**
     * ファイルが無かったら404
     * 
     * @throws Exception
     */
    @Test
    public void testResetCssNotFound() throws Exception {
        Response response = target("static/reset.css").request().get();
        assertThat("Status code", response.getStatusInfo(),
                is(Status.NOT_FOUND));
        assertThat("Count", counter.getCount(), is(0));
    }

    /**
     * if-modified-sinceヘッダで転送量削減
     * 
     * @throws Exception
     */
    @Test
    public void testIfModigiedSince() throws Exception {
        Response response = target("static/hello.txt").request().get();
        assertThat("Status code", response.getStatusInfo(), is(Status.OK));

        Date lastModified = response.getLastModified();
        assertThat("lastModified", lastModified, is(not(nullValue())));

        Response response2 = target("static/hello.txt").request()
                .header(HttpHeaders.IF_MODIFIED_SINCE, lastModified).get();
        assertThat("Status code", response2.getStatusInfo(),
                is(Status.NOT_MODIFIED));

        assertThat("Count", counter.getCount(), is(2));
    }

    /**
     * if-none-matchヘッダで転送量削減
     * 
     * @throws Exception
     */
    @Test
    public void testIfNoneMatch() throws Exception {

        //ハッシュファイルはGradleタスクで書き出すのでIDEでテスト実行
        //するとハッシュファイルが出来なくてアレなのでassume使っとく
        URL resource = getClass().getResource("/static/hello.txt.md5");
        Assume.assumeThat(resource, is(not(nullValue())));

        Response response = target("static/hello.txt").request().get();
        assertThat("Status code", response.getStatusInfo(), is(Status.OK));

        EntityTag eTag = response.getEntityTag();
        assertThat("ETag", eTag, is(not(nullValue())));

        Response response2 = target("static/hello.txt").request()
                .header(HttpHeaders.IF_NONE_MATCH, eTag).get();
        assertThat("Status code", response2.getStatusInfo(),
                is(Status.NOT_MODIFIED));

        assertThat("Count", counter.getCount(), is(2));
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(StaticResources.class)
                .register(counter = new Counter());
    }

    public static class Counter implements ContainerRequestFilter {

        private final AtomicInteger count = new AtomicInteger(0);

        @Override
        public void filter(ContainerRequestContext requestContext)
                throws IOException {
            count.incrementAndGet();
        }

        public int getCount() {
            return count.get();
        }
    }
}
