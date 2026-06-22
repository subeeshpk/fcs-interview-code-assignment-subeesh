package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
public class WarehouseCreateStatusFilter implements ContainerResponseFilter {

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if ("POST".equals(requestContext.getMethod())
                && "/warehouse".equals(requestContext.getUriInfo().getPath())
                && responseContext.getStatus() == 200) {
            responseContext.setStatus(201);
        }
    }
}