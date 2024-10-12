package org.bakeryshop.domain.repository.order;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.util.ParameterArguments;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryPancakesOrderRepository implements PancakesOrderRepository {

    private final Map<UUID, ThreadSafePancakesOrder> orders = new ConcurrentHashMap<>();

    private final Duration lockTimeout;

    public InMemoryPancakesOrderRepository(Duration lockTimeout) {
        this.lockTimeout = lockTimeout;
    }

    @Override
    public PancakesOrder create(Building building, int room) {
        ParameterArguments.requireNotNullParameterArgument(building, "building");
        ParameterArguments.requirePositiveParameterArgument(room, "room");

        final var panCakesOrder = new ThreadSafePancakesOrder(PancakesOrder.newOrder(building, room), lockTimeout);
        orders.put(panCakesOrder.getId(), panCakesOrder);
        return panCakesOrder;
    }

    @Override
    public PancakesOrder update(PancakesOrder order) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");

        final var existing = orders.get(order.getId());
        if (Objects.isNull(existing)) {
            throw OrderNotFoundException.byId(order.getId());
        }
        if (Objects.equals(existing, order)) {
            return existing;
        }
        final var persistedOrder = new ThreadSafePancakesOrder(order, lockTimeout);
        orders.put(order.getId(), persistedOrder);
        return persistedOrder;
    }

    @Override
    public Optional<PancakesOrder> find(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");

        return Optional.ofNullable(orders.get(orderId));
    }

    @Override
    public Set<UUID> listCompletedOrdersIds() {
        return orders.values().stream()
                .filter(PancakesOrder::isCompleted)
                .map(PancakesOrder::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public Set<UUID> listPreparedOrdersIds() {
        return orders.values().stream()
                .filter(PancakesOrder::isPrepared)
                .map(PancakesOrder::getId)
                .collect(Collectors.toUnmodifiableSet());
    }


    @Override
    public Optional<PancakesOrder> remove(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");
        return Optional.ofNullable(orders.remove(orderId));
    }

}
