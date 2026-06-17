package com.fulfilment.application.monolith.fulfilment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AssignWarehouseFulfilmentUseCaseTest {

    @Inject AssignWarehouseFulfilmentUseCase useCase;
    @Inject ProductRepository productRepository;
    @Inject WarehouseRepository warehouseRepository;

    private Product newProduct(String name) {
        Product p = new Product(name);
        productRepository.persist(p);
        return p;
    }

    private Store newStore(String name) {
        Store s = new Store(name);
        s.persist();
        return s;
    }

    private DbWarehouse newWarehouse(String buCode) {
        DbWarehouse w = new DbWarehouse();
        w.businessUnitCode = buCode;
        w.location = "ZWOLLE-001";
        w.capacity = 100;
        w.stock = 0;
        warehouseRepository.persist(w);
        return w;
    }

    @Test
    @TestTransaction
    void shouldAssignWarehouse_whenWithinAllLimits() {
        Product product = newProduct("ASSIGN-TEST-PRODUCT-1");
        Store store = newStore("ASSIGN-TEST-STORE-1");
        DbWarehouse warehouse = newWarehouse("ASSIGN-TEST-WH-1");

        useCase.assign(product, store, warehouse);

        long count =
                ProductStoreWarehouse.count(
                        "product = ?1 and store = ?2 and warehouse = ?3", product, store, warehouse);
        assertEquals(1, count);
    }

    @Test
    @TestTransaction
    void shouldThrow_whenProductAlreadyFulfilledByTwoWarehousesInStore() {
        Product product = newProduct("ASSIGN-TEST-PRODUCT-2");
        Store store = newStore("ASSIGN-TEST-STORE-2");
        DbWarehouse warehouseA = newWarehouse("ASSIGN-TEST-WH-2A");
        DbWarehouse warehouseB = newWarehouse("ASSIGN-TEST-WH-2B");
        DbWarehouse warehouseC = newWarehouse("ASSIGN-TEST-WH-2C");

        useCase.assign(product, store, warehouseA);
        useCase.assign(product, store, warehouseB);

        assertThrows(IllegalArgumentException.class, () -> useCase.assign(product, store, warehouseC));
    }

    @Test
    @TestTransaction
    void shouldThrow_whenStoreAlreadyFulfilledByThreeWarehouses() {
        Store store = newStore("ASSIGN-TEST-STORE-3");
        DbWarehouse warehouseA = newWarehouse("ASSIGN-TEST-WH-3A");
        DbWarehouse warehouseB = newWarehouse("ASSIGN-TEST-WH-3B");
        DbWarehouse warehouseC = newWarehouse("ASSIGN-TEST-WH-3C");
        DbWarehouse warehouseD = newWarehouse("ASSIGN-TEST-WH-3D");

        useCase.assign(newProduct("ASSIGN-TEST-PRODUCT-3A"), store, warehouseA);
        useCase.assign(newProduct("ASSIGN-TEST-PRODUCT-3B"), store, warehouseB);
        useCase.assign(newProduct("ASSIGN-TEST-PRODUCT-3C"), store, warehouseC);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.assign(newProduct("ASSIGN-TEST-PRODUCT-3D"), store, warehouseD));
    }

    @Test
    @TestTransaction
    void shouldThrow_whenWarehouseAlreadyStoresFiveProductTypes() {
        DbWarehouse warehouse = newWarehouse("ASSIGN-TEST-WH-4");

        for (int i = 1; i <= 5; i++) {
            useCase.assign(
                    newProduct("ASSIGN-TEST-PRODUCT-4-" + i),
                    newStore("ASSIGN-TEST-STORE-4-" + i),
                    warehouse);
        }

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.assign(
                                newProduct("ASSIGN-TEST-PRODUCT-4-6"), newStore("ASSIGN-TEST-STORE-4-6"), warehouse));
    }
}