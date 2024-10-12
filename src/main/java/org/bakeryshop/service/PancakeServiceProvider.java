package org.bakeryshop.service;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.repository.address.FixedSetBuildingRepository;
import org.bakeryshop.domain.repository.order.InMemoryPancakesOrderRepository;

import java.time.Duration;
import java.util.Objects;
import java.util.Set;

public final class PancakeServiceProvider {

    private PancakeServiceProvider() {
        super();
    }

    private static volatile PancakeService service;

    public static PancakeService pancakeService() {
        if (Objects.nonNull(service)) {
            return service;
        }
        synchronized (PancakeServiceProvider.class) {
            if (Objects.nonNull(service)) {
                return service;
            }
            service = new PancakeService(
                    new InMemoryPancakesOrderRepository(Duration.ofMillis(100L)),
                    new FixedSetBuildingRepository(Set.of(new Building(10, 20)))
            );
            return service;
        }
    }
}
