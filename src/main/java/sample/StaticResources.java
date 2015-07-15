package sample;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("static")
public class StaticResources {

    @Path("hello.txt")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public InputStream getHello() throws IOException {

        URL resource = getClass().getResource("/static/hello.txt");
        if (resource == null) {
            throw new NotFoundException();
        }

        URLConnection con = resource.openConnection();

        return con.getInputStream();
    }
}
