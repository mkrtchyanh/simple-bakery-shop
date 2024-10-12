package org.bakeryshop.domain.model.pancakes;

import org.bakeryshop.util.ParameterArguments;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public sealed interface PancakeRecipe permits ImmutablePancakeRecipe {

    default String description() {
        return "Delicious pancake with %s!".formatted(ingredients().stream()
                .map(PancakeIngredient::getDisplayName).sorted().collect(Collectors.joining(", ")));
    }

    UUID getOrderId();

    Set<PancakeIngredient> ingredients();

    static PancakeRecipe of(UUID orderId, Set<PancakeIngredient> ingredients) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");
        ParameterArguments.requireNotEmptyParameterArgument(ingredients, "ingredients");
        return new ImmutablePancakeRecipe(orderId, ingredients);
    }

}
