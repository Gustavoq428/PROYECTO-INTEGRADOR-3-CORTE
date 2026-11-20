package usta.gym.infrastructure.adapter.in.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

// Utilidades comunes de las páginas: alertas, redirecciones y respuestas
public final class Pages {

    private Pages() {
    }

    // Plantilla con las alertas de éxito y error (pueden ser null)
    public static TemplateInstance of(Template template, String success, String error) {
        return template.data("success", success).data("error", error);
    }

    // Respuesta HTML 200 con la página ya construida
    public static Response html(TemplateInstance page) {
        return Response.ok(page.render(), MediaType.TEXT_HTML).build();
    }

    // Redirección (303) a otra página mostrando un mensaje de éxito
    public static Response redirectOk(String path, String message) {
        return redirect(path, "ok", message);
    }

    // Redirección (303) a otra página mostrando un mensaje de error
    public static Response redirectError(String path, String message) {
        return redirect(path, "error", message);
    }

    private static Response redirect(String path, String key, String message) {
        String encoded = URLEncoder.encode(message, StandardCharsets.UTF_8).replace("+", "%20");
        String separator = path.contains("?") ? "&" : "?";
        return Response.seeOther(URI.create(path + separator + key + "=" + encoded)).build();
    }
}
