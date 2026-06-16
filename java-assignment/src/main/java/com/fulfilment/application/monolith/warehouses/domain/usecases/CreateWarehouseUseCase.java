package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    if (warehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null.");
    }
    if (warehouse.capacity == null || warehouse.stock == null) {
      throw new IllegalArgumentException("Warehouse capacity and stock must not be null.");
    }

    LOG.debugf("Creating warehouse %s at %s", warehouse.businessUnitCode, warehouse.location);

    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      LOG.warnf("Rejected create: duplicate BU code %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse with code " + warehouse.businessUnitCode + " already exists.");
    }

    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      LOG.warnf("Rejected create: location %s does not exist", warehouse.location);
      throw new IllegalArgumentException("Location " + warehouse.location + " does not exist.");
    }

    List<Warehouse> atLocation = new ArrayList<>();
    for (Warehouse w : warehouseStore.getAll()) {
      if (w.location.equals(warehouse.location)) {
        atLocation.add(w);
      }
    }

    if (atLocation.size() >= location.maxNumberOfWarehouses) {
      LOG.warnf("Rejected create: max warehouses reached for location %s", warehouse.location);
      throw new IllegalArgumentException("Max warehouses reached for location " + warehouse.location + ".");
    }

    int usedCapacity = 0;
    for (Warehouse w : atLocation) {
      usedCapacity += w.capacity;
    }
    if (usedCapacity + warehouse.capacity > location.maxCapacity) {
      LOG.warnf("Rejected create: capacity exceeds location maximum for %s", warehouse.location);
      throw new IllegalArgumentException("Capacity exceeds location maximum for " + warehouse.location + ".");
    }

    if (warehouse.stock > warehouse.capacity) {
      LOG.warnf("Rejected create: stock exceeds capacity for %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity.");
    }

    warehouseStore.create(warehouse);
    LOG.infof("Warehouse %s created at %s", warehouse.businessUnitCode, warehouse.location);
  }
}