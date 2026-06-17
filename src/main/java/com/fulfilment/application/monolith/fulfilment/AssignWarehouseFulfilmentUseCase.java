package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AssignWarehouseFulfilmentUseCase {

    @Transactional
    public void assign(Product product, Store store, DbWarehouse warehouse) {
        long warehousesForProductInStore =
                ProductStoreWarehouse.find("product = ?1 and store = ?2", product, store)
                        .stream()
                        .map(e -> ((ProductStoreWarehouse) e).warehouse.id)
                        .distinct()
                        .count();
        if (warehousesForProductInStore >= 2) {
            throw new IllegalArgumentException("Product already fulfilled by max 2 warehouses for this store.");
        }

        long warehousesForStore =
                ProductStoreWarehouse.find("store = ?1", store)
                        .stream()
                        .map(e -> ((ProductStoreWarehouse) e).warehouse.id)
                        .distinct()
                        .count();
        if (warehousesForStore >= 3) {
            throw new IllegalArgumentException("Store already fulfilled by max 3 warehouses.");
        }

        long productsInWarehouse =
                ProductStoreWarehouse.find("warehouse = ?1", warehouse)
                        .stream()
                        .map(e -> ((ProductStoreWarehouse) e).product.id)
                        .distinct()
                        .count();
        if (productsInWarehouse >= 5) {
            throw new IllegalArgumentException("Warehouse already stores max 5 product types.");
        }

        ProductStoreWarehouse link = new ProductStoreWarehouse();
        link.product = product;
        link.store = store;
        link.warehouse = warehouse;
        link.persist();
    }
}