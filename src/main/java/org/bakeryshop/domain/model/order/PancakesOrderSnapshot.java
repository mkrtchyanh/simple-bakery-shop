package org.bakeryshop.domain.model.order;

import org.bakeryshop.domain.model.address.OrderAddress;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.util.ParameterArguments;

import java.util.List;
import java.util.UUID;

public record PancakesOrderSnapshot(
        UUID id,
        OrderState state,
        OrderAddress address,
        List<PancakeRecipe> pancakes
) {

    public PancakesOrderSnapshot{
        pancakes = List.copyOf(pancakes);
    }


    public PancakesOrderSnapshot(PancakesOrder order) {
        this(order.getId(), order.getState(), order.getAddress(), order.getPancakes());
        ParameterArguments.requireNotNullParameterArgument(order, "order");
    }

    public UUID id() {
        return id;
    }

    @SuppressWarnings("unused")
    public OrderState state() {
        return state;
    }

    public OrderAddress address() {
        return address;
    }

    public List<PancakeRecipe> pancakes() {
        return pancakes;
    }

    public List<String> pancakesDescriptions() {
        return pancakes.stream().map(PancakeRecipe::description).toList();
    }

}
