package org.bakeryshop.domain.model.order;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.address.OrderAddress;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.util.ParameterArguments;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public interface PancakesOrder {

    static PancakesOrder copyOf(PancakesOrder pancakesOrder) {
        ParameterArguments.requireNotNullParameterArgument(pancakesOrder, "pancakesOrder");
        return new SimplePancakesOrder(pancakesOrder);
    }

    static PancakesOrder newOrder(Building building, int room) {
        ParameterArguments.requireNotNullParameterArgument(building, "building");
        ParameterArguments.requirePositiveParameterArgument(room, "room");
        if (!building.hasRoom(room)) {
            throw new IllegalArgumentException("Building does have such room '%d'".formatted(room));
        }
        return new SimplePancakesOrder(new OrderAddress(building, room));
    }

    static boolean equals(PancakesOrder thisOrder, PancakesOrder thatOrder) {
        ParameterArguments.requireNotNullParameterArgument(thisOrder, "thisOrder");
        ParameterArguments.requireNotNullParameterArgument(thatOrder, "thatOrder");
        return Objects.equals(thisOrder.getAddress(), thatOrder.getAddress())
                && thisOrder.getState() == thatOrder.getState()
                && Objects.equals(thisOrder.getPancakes(), thatOrder.getPancakes());
    }

    static int hashCode(PancakesOrder pancakesOrder) {
        ParameterArguments.requireNotNullParameterArgument(pancakesOrder, "pancakesOrder");
        return Objects.hash(pancakesOrder.getAddress(), pancakesOrder.getState(), pancakesOrder.getPancakes());
    }

    UUID getId() ;

    OrderAddress getAddress();

    OrderState getState();

    List<PancakeRecipe> getPancakes();

    PancakesOrder addPancakes(int count, Set<PancakeIngredient> ingredients);

    PancakesOrder removePancakes(String description, int count);

    PancakesOrder handleOrderCanceled();

    PancakesOrder markAsPrepared();

    PancakesOrder markAsCompleted();

    List<String> pancakeDescriptions();

    PancakesOrderSnapshot snapshot();

    boolean isPrepared();

    boolean isCompleted();

}
