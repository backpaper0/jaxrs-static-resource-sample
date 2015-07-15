package sample;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("static")
public class StaticResources {
    private static final ConcurrentMap<String, MediaType> types;

    @Path("{name:.+\\.(txt|js)}")
    @GET
    public Response getResource(@PathParam("name") String name)
            throws IOException {

        URL resource = getClass().getResource("/static/" + name);
        if (resource == null) {
            throw new NotFoundException();
        }

        URLConnection con = resource.openConnection();

        String ext = name.substring(name.lastIndexOf('.'));

        MediaType type = types.get(ext);

        return Response.ok(con.getInputStream(), type).build();
    }

    static {
        types = new ConcurrentHashMap<>();
        types.put(".txt", MediaType.TEXT_PLAIN_TYPE);
        types.put(".js", MediaType.valueOf("text/javascript"));
    }
}
