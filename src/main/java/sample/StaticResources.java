package sample;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
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

/**
 * 静的ファイルを返すリソースクラス。
 *
 * クラスパス上の /static/ ディレクトリ以下のファイルを扱う。
 * 
 */
@Path("static")
public class StaticResources {
    private static final ConcurrentMap<String, MediaType> types;

    //正規表現で拡張子を.txtと.jsに制限している。
    @Path("{name:.+\\.(txt|js)}")
    @GET
    public Response getResource(@PathParam("name") String name,
            @Context Request request) throws IOException {

        URL resource = getClass().getResource("/static/" + name);
        if (resource == null) {
            throw new NotFoundException();
        }

        URLConnection con = resource.openConnection();

        //ファイルの更新日時とIf-Modified-Sinceリクエストヘッダを
        //付き合わせて同じ値ならリソースに変更は無いので304 Not Modifiedを返す。
        Date lastModified = new Date(con.getLastModified());
        ResponseBuilder builder = request.evaluatePreconditions(lastModified);
        if (builder != null) {
            return builder.build();
        }

        //ファイルのETag(ここではMD5ハッシュ値)とIf-Modified-Sinceリクエストヘッダを
        //付き合わせて同じ値ならリソースに変更は無いので304 Not Modifiedを返す。
        //このサンプルではMD5ハッシュ値を要求されたリソースのファイル名に.md5を
        //繋げた名前のファイルから読み出している。
        //そして.md5ファイルはGradleタスクで書き出しており、IDEで実行している
        //場合などは.md5ファイルが無いかもしれないのでその辺考慮してる感じ。
        EntityTag eTag = null;
        URL md5 = getClass().getResource("/static/" + name + ".md5");
        if (md5 != null) {
            StringWriter out = new StringWriter();
            out.write('"');
            try (Reader in = new InputStreamReader(md5.openStream())) {
                char[] c = new char[8192];
                int i;
                while (-1 != (i = in.read(c))) {
                    out.write(c, 0, i);
                }
            }
            out.write('"');
            eTag = EntityTag.valueOf(out.toString());
            builder = request.evaluatePreconditions(eTag);
            if (builder != null) {
                return builder.build();
            }
        }

        String ext = name.substring(name.lastIndexOf('.'));

        MediaType type = types.get(ext);

        builder = Response.ok(con.getInputStream(), type).lastModified(
                lastModified);
        if (eTag != null) {
            builder.tag(eTag);
        }
        return builder.build();
    }

    static {
        types = new ConcurrentHashMap<>();
        types.put(".txt", MediaType.TEXT_PLAIN_TYPE);
        types.put(".js", MediaType.valueOf("text/javascript"));
    }
}
