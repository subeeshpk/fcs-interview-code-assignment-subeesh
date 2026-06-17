package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOG = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final ArchiveWarehouseOperation archiveWarehouseOperation;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
          WarehouseStore warehouseStore,
          ArchiveWarehouseOperation archiveWarehouseOperation,
          LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.archiveWarehouseOperation = archiveWarehouseOperation;
    this.locationResolver = locationResolver;
  }

  @Override
  @Transactional
  public void replace(Warehouse newWarehouse) {
    if (newWarehouse == null) {
      throw new IllegalArgumentException("Warehouse must not be null.");
    }

    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      LOG.warnf("Rejected replace: no active warehouse found with code %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException(
              "No active warehouse found with code " + newWarehouse.businessUnitCode + ".");
    }

    Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location == null) {
      LOG.warnf("Rejected replace: location %s does not exist", newWarehouse.location);
      throw new IllegalArgumentException("Location " + newWarehouse.location + " does not exist.");
    }

    int usedCapacity =
            warehouseStore.getAll().stream()
                    .filter(w -> w.location.equals(newWarehouse.location))
                    .filter(w -> !w.businessUnitCode.equals(existing.businessUnitCode))
                    .mapToInt(w -> w.capacity)
                    .sum();
    if (usedCapacity + newWarehouse.capacity > location.maxCapacity) {
      LOG.warnf("Rejected replace: capacity exceeds location maximum for %s", newWarehouse.location);
      throw new IllegalArgumentException(
              "Capacity exceeds location maximum for " + newWarehouse.location + ".");
    }

    if (newWarehouse.capacity < existing.stock) {
      LOG.warnf("Rejected replace: new capacity below existing stock for %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("New capacity must be >= existing stock of " + existing.stock + ".");
    }

    if (newWarehouse.stock == null || !newWarehouse.stock.equals(existing.stock)) {
      LOG.warnf("Rejected replace: stock mismatch for %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("New stock must match existing stock of " + existing.stock + ".");
    }

    archiveWarehouseOperation.archive(existing);
    warehouseStore.create(newWarehouse);
    LOG.infof("Replaced warehouse %s: archived old, created new at %s", newWarehouse.businessUnitCode, newWarehouse.location);
  }
}