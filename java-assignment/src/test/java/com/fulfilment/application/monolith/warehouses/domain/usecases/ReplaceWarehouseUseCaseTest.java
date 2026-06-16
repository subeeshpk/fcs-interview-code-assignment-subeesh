package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReplaceWarehouseUseCaseTest {

    @Mock WarehouseStore warehouseStore;
    @Mock ArchiveWarehouseOperation archiveWarehouseOperation;
    @Mock LocationResolver locationResolver;

    ReplaceWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReplaceWarehouseUseCase(warehouseStore, archiveWarehouseOperation, locationResolver);
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
    void shouldReplaceWarehouse_whenAllValidationsPass() {
        Warehouse existing = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 10);
        Warehouse newWh = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 10);

        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        useCase.replace(newWh);

        verify(archiveWarehouseOperation).archive(existing);
        verify(warehouseStore).create(newWh);
    }

    @Test
    void shouldThrow_whenNoActiveWarehouseFoundForBusinessUnitCode() {
        when(warehouseStore.findByBusinessUnitCode("MWH.999")).thenReturn(null);

        Warehouse newWh = newWarehouse("MWH.999", "AMSTERDAM-001", 100, 10);

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWh));
    }

    @Test
    void shouldThrow_whenNewLocationDoesNotExist() {
        Warehouse existing = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("NOWHERE-000")).thenReturn(null);

        Warehouse newWh = newWarehouse("MWH.001", "NOWHERE-000", 100, 10);

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWh));
    }

    @Test
    void shouldThrow_whenNewCapacityExceedsLocationMaxCapacity() {
        Warehouse existing = newWarehouse("MWH.001", "ZWOLLE-002", 30, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("ZWOLLE-002")).thenReturn(new Location("ZWOLLE-002", 2, 50));
        when(warehouseStore.getAll())
                .thenReturn(java.util.List.of(existing, newWarehouse("MWH.002", "ZWOLLE-002", 30, 10)));

        Warehouse newWh = newWarehouse("MWH.001", "ZWOLLE-002", 40, 10);

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWh));
    }

    @Test
    void shouldThrow_whenNewCapacityLessThanExistingStock() {
        Warehouse existing = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 80);
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        Warehouse newWh = newWarehouse("MWH.001", "AMSTERDAM-001", 50, 80);

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWh));
    }

    @Test
    void shouldThrow_whenNewStockDoesNotMatchExistingStock() {
        Warehouse existing = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 10);
        when(warehouseStore.findByBusinessUnitCode("MWH.001")).thenReturn(existing);
        when(locationResolver.resolveByIdentifier("AMSTERDAM-001"))
                .thenReturn(new Location("AMSTERDAM-001", 5, 100));
        when(warehouseStore.getAll()).thenReturn(Collections.emptyList());

        Warehouse newWh = newWarehouse("MWH.001", "AMSTERDAM-001", 100, 20);

        assertThrows(IllegalArgumentException.class, () -> useCase.replace(newWh));
    }
}