package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final ArchiveWarehouseOperation archiveWarehouseOperation;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, ArchiveWarehouseOperation archiveWarehouseOperation) {
    this.warehouseStore = warehouseStore;
    this.archiveWarehouseOperation = archiveWarehouseOperation;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse existing = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (existing == null) {
      throw new IllegalArgumentException("No active warehouse found with code " + newWarehouse.businessUnitCode + ".");
    }

    if (newWarehouse.capacity < existing.stock) {
      throw new IllegalArgumentException("New capacity must be >= existing stock of " + existing.stock + ".");
    }

    if (!newWarehouse.stock.equals(existing.stock)) {
      throw new IllegalArgumentException("New stock must match existing stock of " + existing.stock + ".");
    }

    archiveWarehouseOperation.archive(existing);
    warehouseStore.create(newWarehouse);
  }
  }
}
