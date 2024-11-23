package quarkus;

import jakarta.json.Json;
import jakarta.json.JsonObjectBuilder;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ErrorMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        int code = 500;

        if (exception instanceof WebApplicationException webApplicationException) {
            code = webApplicationException.getResponse().getStatus();
        }

        JsonObjectBuilder entityBuilder = Json.createObjectBuilder()
                .add("ExceptionType", exception.getClass().getName())
                .add("code", code);

        if (exception.getMessage() != null) {
            entityBuilder.add("error", exception.getMessage());
        }

        return Response.status(code)
                .entity(entityBuilder.build())
                .build();
    }
}
