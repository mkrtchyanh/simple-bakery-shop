package org.bakeryshop.domain.model.pancakes;

import org.bakeryshop.util.ParameterArguments;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

final class ImmutablePancakeRecipe implements PancakeRecipe {

    private final UUID orderId;
    private final Set<PancakeIngredient> ingredients;

    ImmutablePancakeRecipe(UUID orderId, Set<PancakeIngredient> ingredients) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");
        ParameterArguments.requireNotEmptyParameterArgument(ingredients, "ingredients");
        this.orderId = orderId;
        this.ingredients = Set.copyOf(ingredients);
    }

    @Override
    public UUID getOrderId() {
        return orderId;
    }

    @Override
    public Set<PancakeIngredient> ingredients() {
        return ingredients;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PancakeRecipe that)) return false;
        return Objects.equals(getOrderId(), that.getOrderId()) && Objects.equals(ingredients(), that.ingredients());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOrderId(), ingredients());
    }
}
