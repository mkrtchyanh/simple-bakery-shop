package org.bakeryshop.domain.model.order;

import org.bakeryshop.domain.model.address.OrderAddress;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.service.OrderLog;
import org.bakeryshop.util.ParameterArguments;

import java.util.*;

final class SimplePancakesOrder implements PancakesOrder {

    private final UUID id;
    private final OrderAddress address;
    private OrderState state;
    private final List<PancakeRecipe> pancakes;

    SimplePancakesOrder(OrderAddress address) {
        ParameterArguments.requireNotNullParameterArgument(address, "order");
        this.id = UUID.randomUUID();
        this.address = address;
        this.state = OrderState.NEW;
        this.pancakes = new ArrayList<>();
    }

    SimplePancakesOrder(PancakesOrder pancakesOrder) {
        ParameterArguments.requireNotNullParameterArgument(pancakesOrder, "pancakesOrder");
        this.id = pancakesOrder.getId();
        this.address = pancakesOrder.getAddress();
        this.state = pancakesOrder.getState();
        this.pancakes = new ArrayList<>(pancakesOrder.getPancakes());
    }

    @Override
    public SimplePancakesOrder addPancakes(int count, Set<PancakeIngredient> ingredients) {
        for (int i = 0; i < count; i++) {
            addPancake(PancakeRecipe.of(getId(), ingredients));
        }
        return this;
    }

    private void addPancake(PancakeRecipe pancake) {
        pancakes.add(pancake);
        OrderLog.logAddPancake(this, pancake.description(), pancakes);
    }

    @Override
    public SimplePancakesOrder removePancakes(String description, int count) {
        int removedCount = 0;
        PancakeRecipe pancake;
        for (Iterator<PancakeRecipe> it = pancakes.iterator();
             removedCount < count && it.hasNext(); ) {
            pancake = it.next();
            if (pancake.description().equals(description)) {
                it.remove();
                removedCount++;
            }
        }
        if (removedCount > 0) {
            OrderLog.logRemovePancakes(this, description, removedCount, pancakes);
        }
        return this;
    }

    @Override
    public SimplePancakesOrder handleOrderCanceled() {
        OrderLog.logCancelOrder(this, pancakes);
        return this;
    }

    @Override
    public List<String> pancakeDescriptions() {
        return pancakes.stream()
                .map(PancakeRecipe::description).toList();
    }

    @Override
    public PancakesOrderSnapshot snapshot() {
        return new PancakesOrderSnapshot(this);
    }

    @Override
    public boolean isPrepared() {
        return state == OrderState.PREPARED;
    }

    @Override
    public boolean isCompleted() {
        return state == OrderState.COMPLETED;
    }

    @Override
    public SimplePancakesOrder markAsPrepared() {
        this.state = OrderState.PREPARED;
        return this;
    }

    @Override
    public SimplePancakesOrder markAsCompleted() {
        this.state = OrderState.COMPLETED;
        return this;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public OrderAddress getAddress() {
        return address;
    }

    @Override
    public OrderState getState() {
        return state;
    }

    @Override
    public List<PancakeRecipe> getPancakes() {
        return Collections.unmodifiableList(pancakes);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PancakesOrder that)) return false;
        return PancakesOrder.equals(this, that);
    }

    @Override
    public int hashCode() {
        return PancakesOrder.hashCode(this);
    }
}
