package org.bakeryshop.service;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.domain.repository.address.BuildingRepository;
import org.bakeryshop.domain.repository.order.PancakesOrderRepository;
import org.bakeryshop.service.usecase.Result;
import org.bakeryshop.service.usecase.cancel.CancelOrderResult;
import org.bakeryshop.service.usecase.complete.CompleteOrderResult;
import org.bakeryshop.service.usecase.create.CreateOrderResult;
import org.bakeryshop.service.usecase.delivery.OrderDeliveryResult;
import org.bakeryshop.service.usecase.pancake.AddPancakeResult;
import org.bakeryshop.service.usecase.pancake.RemovePancakeResult;
import org.bakeryshop.service.usecase.prepare.PrepareOrderResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PancakeServiceTest {

    @InjectMocks
    private PancakeService pancakeService;

    @Mock
    private PancakesOrderRepository pancakesOrderRepository;

    @Mock
    private BuildingRepository buildingRepository;

    @Test
    @DisplayName("createOrder: should fail with invalid address when building not found")
    void createOrderShouldFailWithInvalidAddressWhenBuildingIsMissing() {
        // setup
        final var buildingNumber = 1;
        when(buildingRepository.find(buildingNumber))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.createOrder(buildingNumber, 2))

                .extracting(Result::requireFailure)
                .isEqualTo(CreateOrderResult.CreateOrderFailure.INVALID_ADDRESS);
        // verify
        verify(buildingRepository).find(buildingNumber);
        verifyNoMoreInteractions(buildingRepository, buildingRepository);
    }

    @Test
    @DisplayName("createOrder: should fail with invalid address when building has not such room")
    void createOrderShouldFailWithInvalidAddressWhenBuildingHasNotSuchRoom() {
        // setup
        final var buildingNumber = 1;
        final var room = 3;
        when(buildingRepository.find(buildingNumber))
                .thenReturn(Optional.of(new Building(1, room - 1)));
        // exercise
        assertThat(pancakeService.createOrder(buildingNumber, room))

                .extracting(Result::requireFailure)
                .isEqualTo(CreateOrderResult.CreateOrderFailure.INVALID_ADDRESS);
        // verify
        verify(buildingRepository).find(buildingNumber);
        verifyNoMoreInteractions(buildingRepository, buildingRepository);
    }

    @Test
    @DisplayName("createOrder: should succeed")
    void createOrderShouldSucceed() {
        // setup
        final var building = new Building(1, 2);
        final var room = 1;
        when(buildingRepository.find(building.buildingNr()))
                .thenReturn(Optional.of(building));
        when(pancakesOrderRepository.create(building, room))
                .thenReturn(PancakesOrder.newOrder(building, room));
        // exercise
        assertThat(pancakeService.createOrder(building.buildingNr(), room))
                .matches(Predicate.not(Result::hasFailure))
                .extracting(CreateOrderResult::requireOrder)
                .hasFieldOrPropertyWithValue("address.building.buildingNr", building.buildingNr())
                .hasFieldOrPropertyWithValue("address.room", room);
        // verify
        verify(buildingRepository).find(building.buildingNr());
        verify(pancakesOrderRepository).create(building, room);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("addPancakes: should fail with order not found when order is missing")
    void addPancakesShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.addPancakes(orderId, 3, Set.of(PancakeIngredient.DARK_CHOCOLATE)))
                .isEqualTo(AddPancakeResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("addPancakes: should succeed")
    void addPancakesShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        final var amountOfPancakes = 3;
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.update(pancakesOrder))
                .thenReturn(pancakesOrder);
        // exercise
        assertThat(
                pancakeService.addPancakes(
                        pancakesOrder.getId(),
                        amountOfPancakes,
                        Set.of(PancakeIngredient.DARK_CHOCOLATE))
        )
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(AddPancakeResult.success());
        assertThat(pancakesOrder.getPancakes()).hasSize(amountOfPancakes);
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).update(pancakesOrder);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("removePancakes: should fail with order not found when order is missing")
    void removePancakesShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.removePancakes(orderId, "any", 3))
                .isEqualTo(RemovePancakeResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("removePancakes: should succeed")
    void removePancakesShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        final var initialAmountOfPancakes = 5;
        final var amountOfPancakesToRemove = 3;
        pancakesOrder.addPancakes(initialAmountOfPancakes, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.update(pancakesOrder))
                .thenReturn(pancakesOrder);
        // exercise
        assertThat(pancakeService.removePancakes(pancakesOrder.getId(),
                pancakesOrder.getPancakes().stream().findFirst().map(PancakeRecipe::description).orElseThrow(),
                amountOfPancakesToRemove)
        )
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(RemovePancakeResult.success());
        assertThat(pancakesOrder.getPancakes()).hasSize(initialAmountOfPancakes - amountOfPancakesToRemove);
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).update(pancakesOrder);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("completeOrder: should fail with order not found when order is missing")
    void completeOrderShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.completeOrder(orderId))
                .isEqualTo(CompleteOrderResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("completeOrder: should succeed")
    void completeOrderShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.update(pancakesOrder))
                .thenReturn(pancakesOrder);
        // exercise
        assertThat(pancakeService.completeOrder(pancakesOrder.getId()))
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(CompleteOrderResult.success());
        assertThat(pancakesOrder)
                .matches(PancakesOrder::isCompleted);
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).update(pancakesOrder);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("prepareOrder: should fail with order not found when order is missing")
    void prepareOrderShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.prepareOrder(orderId))
                .isEqualTo(PrepareOrderResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("prepareOrder: should fail with order not completed when order is missing")
    void prepareOrderShouldFailWithOrderNotCompletedWhenOrderNotCompleted() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        // exercise
        assertThat(pancakeService.prepareOrder(pancakesOrder.getId()))
                .isEqualTo(PrepareOrderResult.orderNotCompleted());
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("prepareOrder: should succeed")
    void prepareOrderShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        pancakesOrder.markAsCompleted();
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.update(pancakesOrder))
                .thenReturn(pancakesOrder);
        // exercise
        assertThat(pancakeService.prepareOrder(pancakesOrder.getId()))
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(PrepareOrderResult.success());
        assertThat(pancakesOrder)
                .matches(PancakesOrder::isPrepared);
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).update(pancakesOrder);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("cancelOrder: should fail with order not found when order is missing")
    void cancelOrderShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.cancelOrder(orderId))
                .isEqualTo(CancelOrderResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("completeOrder: should succeed")
    void cancelOrderOrderShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.remove(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        // exercise
        assertThat(pancakeService.cancelOrder(pancakesOrder.getId()))
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(CancelOrderResult.success());
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).remove(pancakesOrder.getId());
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("deliverOrder: should fail with order not found when order is missing")
    void deliverOrderShouldFailWithOrderNotFoundWhenOrderIsMissing() {
        // setup
        final var orderId = UUID.randomUUID();
        when(pancakesOrderRepository.find(orderId))
                .thenReturn(Optional.empty());
        // exercise
        assertThat(pancakeService.deliverOrder(orderId))
                .isEqualTo(OrderDeliveryResult.orderNotFound());
        // verify
        verify(pancakesOrderRepository).find(orderId);
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("deliverOrder: should fail with order not prepared")
    void deliverOrderShouldFailWithOrderNotPrepared() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        // exercise
        assertThat(pancakeService.deliverOrder(pancakesOrder.getId()))
                .isEqualTo(OrderDeliveryResult.notPrepared());
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

    @Test
    @DisplayName("deliverOrder: should succeed")
    void deliverOrderOrderShouldSucceed() {
        // setup
        final var pancakesOrder = PancakesOrder.newOrder(new Building(1, 2), 2);
        pancakesOrder.addPancakes(3, Set.of(PancakeIngredient.DARK_CHOCOLATE));
        pancakesOrder.markAsPrepared();
        when(pancakesOrderRepository.find(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        when(pancakesOrderRepository.remove(pancakesOrder.getId()))
                .thenReturn(Optional.of(pancakesOrder));
        // exercise
        assertThat(pancakeService.deliverOrder(pancakesOrder.getId()))
                .matches(Predicate.not(Result::hasFailure))
                .isEqualTo(OrderDeliveryResult.of(new PancakesOrderSnapshot(
                        pancakesOrder.getId(),
                        pancakesOrder.getState(),
                        pancakesOrder.getAddress(),
                        pancakesOrder.getPancakes()
                )));
        // verify
        verify(pancakesOrderRepository).find(pancakesOrder.getId());
        verify(pancakesOrderRepository).remove(pancakesOrder.getId());
        verifyNoMoreInteractions(buildingRepository, pancakesOrderRepository);
    }

}