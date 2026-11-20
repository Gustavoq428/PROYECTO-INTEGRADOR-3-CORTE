package usta.gym.infrastructure.adapter.in.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import usta.gym.application.exception.ResourceNotFoundException;

// Muestra una página amigable (404) cuando se pide algo que no existe
@Provider
public class NotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Inject
    @Location("error.html")
    Template errorPage;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        String html = Pages.of(errorPage, null, null)
                .data("message", exception.getMessage())
                .render();
        return Response.status(Response.Status.NOT_FOUND).type(MediaType.TEXT_HTML).entity(html).build();
    }
}
