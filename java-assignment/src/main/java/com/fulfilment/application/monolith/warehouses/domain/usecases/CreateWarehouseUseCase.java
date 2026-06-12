package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import java.util.List;
import java.util.ArrayList;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Warehouse with code " + warehouse.businessUnitCode + " already exists.");
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Location " + warehouse.location + " does not exist.");
    }

    List<Warehouse> atLocation = new ArrayList<>();
    for (Warehouse w : warehouseStore.getAll()) {
      if (w.location.equals(warehouse.location)) {
        atLocation.add(w);
      }
    }

    if (atLocation.size() >= location.maxNumberOfWarehouses) {
      throw new IllegalArgumentException("Max warehouses reached for location " + warehouse.location + ".");
    }

    int usedCapacity = 0;
    for (Warehouse w : atLocation) {
      usedCapacity += w.capacity;
    }
    if (usedCapacity + warehouse.capacity > location.maxCapacity) {
      throw new IllegalArgumentException("Capacity exceeds location maximum for " + warehouse.location + ".");
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity.");
    }

    warehouseStore.create(warehouse);
  }

}
