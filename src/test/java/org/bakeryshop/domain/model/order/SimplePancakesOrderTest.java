package org.bakeryshop.domain.model.order;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.address.OrderAddress;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.service.OrderLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimplePancakesOrderTest {

    @Test
    @DisplayName("addPancake: should add pancakes")
    void addPancake() {
        // setup
        final var address = new OrderAddress(new Building(1, 2), 2);
        final var order = new SimplePancakesOrder(address);
        final var count = 3;
        final var pancakeRecipe = PancakeRecipe.of(order.getId(), Set.of(PancakeIngredient.DARK_CHOCOLATE));
        // exercise
        assertThat(order.addPancakes(count, pancakeRecipe.ingredients()))
                .extracting(PancakesOrder::getPancakes)
                .matches(pancakes -> pancakes.size() == count
                        && pancakes.stream()
                        .distinct()
                        .filter(pancakeRecipe::equals).count() == 1);
    }

    @Test
    @DisplayName("removePancakes: should remove pancakes")
    void removeGetPancakes() {
        // setup
        final var initialCount = 5;
        final var pancakesAmountToRemove = 3;
        final var address = new OrderAddress(new Building(1, 2), 2);
        final var order = new SimplePancakesOrder(address);
        final var pancakeRecipe = PancakeRecipe.of(order.getId(), Set.of(PancakeIngredient.DARK_CHOCOLATE));
        final var pancakesOrder = new SimplePancakesOrder(order);
        pancakesOrder.addPancakes(initialCount, pancakeRecipe.ingredients());
        // exercise
        assertThat(pancakesOrder.removePancakes(pancakeRecipe.description(), pancakesAmountToRemove))
                .extracting(PancakesOrder::getPancakes)
                .matches(pancakes -> pancakes.size() == initialCount - pancakesAmountToRemove);
    }

    @Test
    @DisplayName("handleOrderCanceled: should only log cancellation details")
    void handleGetAddressCanceled() {
        // setup
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        final var copy = PancakesOrder.copyOf(pancakesOrder);
        final var logCaptor = new StringWriter();
        // exercise
        assertThat(pancakesOrder.handleOrderCanceled())
                .isEqualTo(copy);
        OrderLog.flushLogs(logCaptor::append);
        assertThat(logCaptor.toString())
                .isNotBlank()
                .containsIgnoringCase("cancelled")
                .contains(pancakesOrder.getId().toString());
    }

    @Test
    @DisplayName("pancakeDescriptions: should return pancakes descriptions")
    void pancakeDescriptions(
    ) {
        // setup
        final var count = 3;
        final var address = new OrderAddress(new Building(1, 2), 2);
        final var order = new SimplePancakesOrder(address);
        final var pancakeRecipe = PancakeRecipe.of(order.getId(), Set.of(PancakeIngredient.DARK_CHOCOLATE));
        final var pancakesOrder = new SimplePancakesOrder(order);
        pancakesOrder.addPancakes(count, pancakeRecipe.ingredients());
        // exercise
        assertThat(pancakesOrder.pancakeDescriptions())
                .matches(descriptions -> descriptions.size() == count)
                .contains(pancakeRecipe.description());
    }

    @Test
    @DisplayName("snapshot: should return couple of order and pancakes")
    void getAddressForDelivery() {
        // setup
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        // exercise
        assertThat(pancakesOrder.snapshot())
                .hasFieldOrPropertyWithValue("address", pancakesOrder.getAddress())
                .hasFieldOrPropertyWithValue("pancakes", pancakesOrder.getPancakes());
    }

    @Test
    @DisplayName("markAsPrepared: should set status to PREPARED")
    void markAsPrepared() {
        // setup
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        assertThat(pancakesOrder.isPrepared())
                .isFalse();
        // exercise
        pancakesOrder.markAsPrepared();
        assertThat(pancakesOrder.isPrepared())
                .isTrue();
    }

    @Test
    @DisplayName("markAsPrepared: should set status to COMPLETE")
    void markAsCompleted() {
        // setup
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        assertThat(pancakesOrder.isCompleted())
                .isFalse();
        // exercise
        pancakesOrder.markAsCompleted();
        assertThat(pancakesOrder.isCompleted())
                .isTrue();
    }

    @Test
    @DisplayName("isPrepared: should return true when state is PREPARED")
    void isPrepared() {
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        assertThat(pancakesOrder.isPrepared())
                .isFalse();
        pancakesOrder.markAsPrepared();
        // exercise
        assertThat(pancakesOrder)
                .matches(SimplePancakesOrder::isPrepared)
                .extracting(PancakesOrder::getState)
                .isEqualTo(OrderState.PREPARED);
    }

    @Test
    @DisplayName("isCompleted: should return true when state is COMPLETED")
    void isCompleted() {
        // setup
        final var pancakesOrder = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        assertThat(pancakesOrder.isCompleted())
                .isFalse();
        pancakesOrder.markAsCompleted();
        // exercise
        assertThat(pancakesOrder)
                .matches(SimplePancakesOrder::isCompleted)
                .extracting(PancakesOrder::getState)
                .isEqualTo(OrderState.COMPLETED);
    }

    @Test
    @DisplayName("snapshot: should return equal objects when order Was not mutated")
    void snapshotShouldReturnEqualObjectsWhenOrderWasNotMutated() {
        final var order = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        order.addPancakes(2, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        final var snapshot = order.snapshot();
        assertThat(snapshot)
                .hasFieldOrPropertyWithValue("id", order.getId())
                .hasFieldOrPropertyWithValue("pancakes", order.getPancakes())
                .hasFieldOrPropertyWithValue("address", order.getAddress())
                .isEqualTo(order.snapshot());
        order.addPancakes(2, Set.of(PancakeIngredient.MILK_CHOCOLATE));
        assertThat(snapshot)
                .hasFieldOrPropertyWithValue("id", order.getId())
                .hasFieldOrPropertyWithValue("address", order.getAddress())
                .isNotEqualTo(order.snapshot());
    }

    @Test
    @DisplayName("snapshot: should return Changed object when order was not mutated")
    void snapshotShouldReturnChangedObjectWhenOrderWasNotMutated() {
        final var order = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        order.addPancakes(2, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        final var snapshot = order.snapshot();
        order.addPancakes(2, Set.of(PancakeIngredient.MILK_CHOCOLATE));
        assertThat(snapshot)
                .hasFieldOrPropertyWithValue("id", order.getId())
                .hasFieldOrPropertyWithValue("address", order.getAddress())
                .isNotEqualTo(order.snapshot());
    }

    @Test
    @DisplayName("snapshot: should be immutable")
    void snapshotShouldBeImmutable() {
        final var order = new SimplePancakesOrder(new OrderAddress(new Building(1, 2), 2));
        order.addPancakes(2, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        final var pancakes = order.snapshot().pancakes();
        final var pancakeRecipe = PancakeRecipe.of(order.getId(), Set.of(PancakeIngredient.MILK_CHOCOLATE));
        assertThatThrownBy(() -> pancakes.add(pancakeRecipe))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}