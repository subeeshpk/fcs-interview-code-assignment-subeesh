package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.Map;
import org.jboss.logging.Logger;

@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    private static final Logger LOG = Logger.getLogger(WarehouseExceptionMapper.class);

    @Override
    public Response toResponse(IllegalArgumentException exception) {
        LOG.warnf("Bad request: %s", exception.getMessage());
        return Response.status(400)
                .entity(Map.of("error", exception.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}