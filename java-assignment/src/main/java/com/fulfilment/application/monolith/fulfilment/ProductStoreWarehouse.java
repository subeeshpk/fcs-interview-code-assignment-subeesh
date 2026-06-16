package com.fulfilment.application.monolith.fulfilment;

import com.fulfilment.application.monolith.products.Product;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.DbWarehouse;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "product_store_warehouse",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"product_id", "store_id", "warehouse_id"}))
public class ProductStoreWarehouse extends PanacheEntity {

    @ManyToOne public Product product;
    @ManyToOne public Store store;
    @ManyToOne public DbWarehouse warehouse;
}