package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateWarehouseUseCaseTest {

    @Mock WarehouseStore warehouseStore;
    @Mock LocationResolver locationResolver;

    CreateWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
    }

    private Warehouse newWarehouse(String code, String location, int capacity, int stock) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = code;
        w.location = location;
        w.capacity = capacity;
        w.stock = stock;
        return w;
    }

    @Test
    void shouldCreateWarehouse_whenAllValidationsPass() {
        Warehouse warehouse = newWarehouse("MWH.100", "AMSTERDAM-001", 50, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        useCase.create(warehouse);

        verify(warehouseStore).create(warehouse);
    }

    @Test
    void shouldThrow_whenBusinessUnitCodeAlreadyExists() {
        Warehouse existing = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);

        Warehouse newWh = newWarehouse("MWH.001", "AMSTERDAM-001", 50, 10);

        assertThrows(IllegalArgumentException.class, () -> useCase.create(newWh));
    }

    @Test
    void shouldThrow_whenLocationDoesNotExist() {
        Warehouse warehouse = newWarehouse("MWH.100", "NOWHERE-000", 50, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("NOWHERE-000")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    }

    @Test
    void shouldThrow_whenMaxNumberOfWarehousesReachedAtLocation() {
        Warehouse warehouse = newWarehouse("MWH.100", "ZWOLLE-001", 10, 5);
        when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-001")).thenReturn(new Location("ZWOLLE-001", 1, 40));
        when(warehouseStore.getAll())
                .thenReturn(List.of(newWarehouse("MWH.001", "ZWOLLE-001", 30, 5)));

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    }

    @Test
    void shouldThrow_whenCapacityExceedsLocationMaxCapacity() {
        Warehouse warehouse = newWarehouse("MWH.100", "ZWOLLE-002", 30, 5);
        when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("ZWOLLE-002")).thenReturn(new Location("ZWOLLE-002", 2, 50));
        when(warehouseStore.getAll())
                .thenReturn(List.of(newWarehouse("MWH.001", "ZWOLLE-002", 30, 5)));

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    }

    @Test
    void shouldThrow_whenStockExceedsWarehouseCapacity() {
        Warehouse warehouse = newWarehouse("MWH.100", "AMSTERDAM-001", 20, 50);
        when(warehouseStore.findByBusinessUnitCode("MWH.100")).thenReturn(null);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> useCase.create(warehouse));
    }
}