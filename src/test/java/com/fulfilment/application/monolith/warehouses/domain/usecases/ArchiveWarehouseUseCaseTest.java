package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ArchiveWarehouseUseCaseTest {

    @Mock WarehouseStore warehouseStore;

    ArchiveWarehouseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ArchiveWarehouseUseCase(warehouseStore);
    }

    @Test
    void shouldSetArchivedAtTimestamp_whenArchiving() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";

        useCase.archive(warehouse);

        assertNotNull(warehouse.archivedAt);
    }

    @Test
    void shouldCallWarehouseStoreUpdate_afterSettingArchivedAt() {
        Warehouse warehouse = new Warehouse();
        warehouse.businessUnitCode = "MWH.001";

        useCase.archive(warehouse);

        verify(warehouseStore).update(warehouse);
    }

    @Test
    void shouldThrow_whenWarehouseIsNull() {
        assertThrows(IllegalArgumentException.class, () -> useCase.archive(null));
    }
}