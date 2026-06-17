package com.fulfilment.application.monolith.stores;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class StoreService {

    @Inject Event<StoreCreatedEvent> storeCreatedEvent;
    @Inject Event<StoreUpdatedEvent> storeUpdatedEvent;

    @Transactional
    public void save(Store store) {
        store.persist();
        storeCreatedEvent.fire(new StoreCreatedEvent(store));
    }

    @Transactional
    public Store update(Long id, Store updatedStore) {
        Store entity = Store.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        entity.name = updatedStore.name;
        entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
        storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));
        return entity;
    }

    @Transactional
    public Store patch(Long id, Store updatedStore) {
        Store entity = Store.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        if (updatedStore.name != null) {
            entity.name = updatedStore.name;
        }
        if (updatedStore.quantityProductsInStock != 0) {
            entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
        }
        storeUpdatedEvent.fire(new StoreUpdatedEvent(entity));
        return entity;
    }

}