package sample;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

@Path("static")
public class StaticResources {
    private static final ConcurrentMap<String, MediaType> types;

    @Path("{name:.+\\.(txt|js)}")
    @GET
    public Response getResource(@PathParam("name") String name,
            @Context Request request) throws IOException,
            NoSuchAlgorithmException {

        URL resource = getClass().getResource("/static/" + name);
        if (resource == null) {
            throw new NotFoundException();
        }

        URLConnection con = resource.openConnection();

        Date lastModified = new Date(con.getLastModified());
        ResponseBuilder builder = request.evaluatePreconditions(lastModified);
        if (builder != null) {
            return builder.build();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (InputStream in = con.getInputStream()) {
            byte[] b = new byte[8192];
            int i;
            while (-1 != (i = in.read(b))) {
                out.write(b, 0, i);
            }
        }
        byte[] entity = out.toByteArray();
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(entity);
        StringBuilder buf = new StringBuilder();
        buf.append('"');
        for (byte b : digest) {
            buf.append(String.format("%02x", b & 0xff));
        }
        buf.append('"');
        EntityTag eTag = EntityTag.valueOf(buf.toString());
        builder = request.evaluatePreconditions(eTag);
        if (builder != null) {
            return builder.build();
        }

        String ext = name.substring(name.lastIndexOf('.'));

        MediaType type = types.get(ext);

        return Response.ok(entity, type).lastModified(lastModified).tag(eTag)
                .build();
    }

    static {
        types = new ConcurrentHashMap<>();
        types.put(".txt", MediaType.TEXT_PLAIN_TYPE);
        types.put(".js", MediaType.valueOf("text/javascript"));
    }
}
