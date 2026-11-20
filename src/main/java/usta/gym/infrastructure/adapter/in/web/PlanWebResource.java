package usta.gym.infrastructure.adapter.in.web;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.gym.application.port.in.ManagePlansUseCase;
import usta.gym.domain.exception.BusinessRuleException;
import usta.gym.domain.model.Plan;
import usta.gym.infrastructure.adapter.in.web.Views.PlanForm;

import java.math.BigDecimal;

// Adaptador web (entrada): página de planes de membresía
@Path("/plans")
@Produces(MediaType.TEXT_HTML)
@Tag(name = "Planes", description = "Páginas para consultar, crear y activar/desactivar los planes de membresía")
public class PlanWebResource {

    @Inject
    ManagePlansUseCase plans;

    @Inject
    @Location("plans.html")
    Template plansTemplate;

    @GET
    @Operation(summary = "Listado de planes y formulario para crear uno nuevo")
    public TemplateInstance list(@QueryParam("ok") String ok, @QueryParam("error") String error) {
        return page(new PlanForm("", "", "", "30"), ok, error);
    }

    @POST
    @Operation(summary = "Crear un plan",
            description = "Valida nombre obligatorio y no repetido, precio mayor a 0 y duración entre 1 y 730 días.")
    public Response create(@FormParam("name") String name,
                           @FormParam("description") String description,
                           @FormParam("price") String price,
                           @FormParam("durationDays") String durationDays) {
        PlanForm form = new PlanForm(nz(name), nz(description), nz(price), nz(durationDays));
        try {
            Plan created = plans.create(name, description, parsePrice(price), parseDays(durationDays));
            return Pages.redirectOk("/plans", "Plan \"" + created.getName() + "\" creado");
        } catch (BusinessRuleException e) {
            return Pages.html(page(form, null, e.getMessage()));
        }
    }

    @POST
    @Path("/{id}/toggle")
    @Operation(summary = "Activar o desactivar un plan",
            description = "Los planes desactivados no aparecen al inscribir socios, pero las inscripciones existentes se conservan.")
    public Response toggle(@PathParam("id") Long id) {
        Plan plan = plans.toggleActive(id);
        return Pages.redirectOk("/plans", "Plan \"" + plan.getName() + "\" "
                + (plan.isActive() ? "activado" : "desactivado"));
    }

    private TemplateInstance page(PlanForm form, String ok, String error) {
        return Pages.of(plansTemplate, ok, error)
                .data("plans", plans.findAll().stream().map(WebMapper::toView).toList())
                .data("form", form);
    }

    private static BigDecimal parsePrice(String value) {
        try {
            return new BigDecimal(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("El precio no es válido");
        }
    }

    private static int parseDays(String value) {
        try {
            return Integer.parseInt(value == null ? "" : value.trim());
        } catch (NumberFormatException e) {
            throw new BusinessRuleException("La duración debe ser un número de días");
        }
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
