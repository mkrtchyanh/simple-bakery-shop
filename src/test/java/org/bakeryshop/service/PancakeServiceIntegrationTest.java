package org.bakeryshop.service;

import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class PancakeServiceIntegrationTest {

    private final PancakeService pancakeService = PancakeServiceProvider.pancakeService();

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with hazelnuts, milk chocolate!";

    @Test
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        // setup

        // exercise
        final var order = pancakeService.createOrder(10, 20).requireOrder();

        assertEquals(10, order.address().buildingNr());
        assertEquals(20, order.address().room());

        // verify

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        // exercise
        addPancakes(order);

        // verify
        List<String> pancakeDescriptions = pancakeService.viewOrder(order.id());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION
        ), pancakeDescriptions);

        // tear down
    }

    @Test
    public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        addPancakes(order);
        // exercise
        pancakeService.removePancakes(order.id(), DARK_CHOCOLATE_PANCAKE_DESCRIPTION, 2);
        pancakeService.removePancakes(order.id(), MILK_CHOCOLATE_PANCAKE_DESCRIPTION, 3);
        pancakeService.removePancakes(order.id(), MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, 1);

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(order.id());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION
        ), ordersPancakes);

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        // exercise
        pancakeService.completeOrder(order.id());

        // verify
        Set<UUID> completedOrdersOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(order.id()));

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        pancakeService.completeOrder(order.id());
        // exercise
        pancakeService.prepareOrder(order.id());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.id()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertTrue(preparedOrders.contains(order.id()));

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        pancakeService.completeOrder(order.id());
        pancakeService.prepareOrder(order.id());
        List<String> pancakesToDeliver = pancakeService.viewOrder(order.id());
        // exercise
        PancakesOrderSnapshot deliveredOrder = pancakeService.deliverOrder(order.id()).requireOrder();

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.id()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.id()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.id());

        assertEquals(List.of(), ordersPancakes);
        assertEquals(order.id(), deliveredOrder.id());
        assertEquals(pancakesToDeliver, deliveredOrder.pancakesDescriptions());

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        final var order = pancakeService.createOrder(10, 20).requireOrder();
        addPancakes(order);

        // exercise
        pancakeService.cancelOrder(order.id());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.id()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.id()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.id());

        assertEquals(List.of(), ordersPancakes);

        // tear down
    }

    private void addPancakes(PancakesOrderSnapshot order) {
        pancakeService.addPancakes(order.id(), 3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        pancakeService.addPancakes(order.id(), 3, Set.of(PancakeIngredient.MILK_CHOCOLATE));
        pancakeService.addPancakes(order.id(), 3,
                Set.of(PancakeIngredient.MILK_CHOCOLATE, PancakeIngredient.HAZELNUTS));
    }
}
