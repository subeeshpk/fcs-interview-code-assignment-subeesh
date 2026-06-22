package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import jakarta.ws.rs.WebApplicationException;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseRepository warehouseRepository;
  @Inject CreateWarehouseOperation createWarehouseOperation;
  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;
  @Inject ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toApiWarehouse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    if (data == null) {
      throw new IllegalArgumentException("Request body must not be null.");
    }
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = toDomainWarehouse(data);
    createWarehouseOperation.create(warehouse);
    return toApiWarehouse(warehouse);
    // HTTP status changed from 200 to 201 by WarehouseCreateStatusFilter
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
            warehouseRepository.findByNumericId(Long.parseLong(id));
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
    }
    return toApiWarehouse(warehouse);
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
            warehouseRepository.findByNumericId(Long.parseLong(id));
    if (warehouse == null) {
      throw new WebApplicationException("Warehouse with id " + id + " not found.", 404);
    }
    archiveWarehouseOperation.archive(warehouse);
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(String businessUnitCode, @NotNull Warehouse data) {
    if (data == null) {
      throw new IllegalArgumentException("Request body must not be null.");
    }
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse newWarehouse = toDomainWarehouse(data);
    newWarehouse.businessUnitCode = businessUnitCode;
    replaceWarehouseOperation.replace(newWarehouse);
    return toApiWarehouse(newWarehouse);
  }

  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomainWarehouse(Warehouse api) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    w.businessUnitCode = api.getBusinessUnitCode();
    w.location = api.getLocation();
    w.capacity = api.getCapacity();
    w.stock = api.getStock();
    return w;
  }

  private Warehouse toApiWarehouse(com.fulfilment.application.monolith.warehouses.domain.models.Warehouse w) {
    Warehouse response = new Warehouse();
    if (w.id != null) {
      response.setId(String.valueOf(w.id));
    }
    response.setBusinessUnitCode(w.businessUnitCode);
    response.setLocation(w.location);
    response.setCapacity(w.capacity);
    response.setStock(w.stock);
    return response;
  }
}