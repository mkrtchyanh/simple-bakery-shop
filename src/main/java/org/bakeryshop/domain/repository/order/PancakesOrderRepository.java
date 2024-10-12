package org.bakeryshop.domain.repository.order;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.order.PancakesOrder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface PancakesOrderRepository {

    PancakesOrder create(Building building, int room);

    PancakesOrder update(PancakesOrder order);

    Optional<PancakesOrder> find(UUID orderId);

    Set<UUID> listCompletedOrdersIds();

    Set<UUID> listPreparedOrdersIds();

    Optional<PancakesOrder> remove(UUID orderId);
}
