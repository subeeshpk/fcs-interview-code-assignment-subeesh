package com.fulfilment.application.monolith.stores;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class StoreService {

    @Transactional
    public void save(Store store) {
        store.persist();
    }

    @Transactional
    public Store update(Long id, Store updatedStore) {
        Store entity = Store.findById(id);
        if (entity == null) {
            throw new WebApplicationException("Store with id of " + id + " does not exist.", 404);
        }
        entity.name = updatedStore.name;
        entity.quantityProductsInStock = updatedStore.quantityProductsInStock;
        return entity;
    }
}