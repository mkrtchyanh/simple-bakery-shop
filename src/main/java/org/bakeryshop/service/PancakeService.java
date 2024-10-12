package org.bakeryshop.service;

import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.domain.model.pancakes.PancakeIngredient;
import org.bakeryshop.domain.repository.address.BuildingRepository;
import org.bakeryshop.domain.repository.order.PancakesOrderRepository;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;
import org.bakeryshop.service.usecase.cancel.CancelOrderResult;
import org.bakeryshop.service.usecase.complete.CompleteOrderResult;
import org.bakeryshop.service.usecase.create.CreateOrderResult;
import org.bakeryshop.service.usecase.delivery.OrderDeliveryResult;
import org.bakeryshop.service.usecase.pancake.AddPancakeResult;
import org.bakeryshop.service.usecase.pancake.RemovePancakeResult;
import org.bakeryshop.service.usecase.prepare.PrepareOrderResult;
import org.bakeryshop.util.ParameterArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class PancakeService {

    private static final Logger logger = LoggerFactory.getLogger(PancakeService.class);

    private final PancakesOrderRepository pancakesOrderRepository;
    private final BuildingRepository buildingRepository;

    public PancakeService(PancakesOrderRepository pancakesOrderRepository,
                          BuildingRepository buildingRepository) {
        this.pancakesOrderRepository = pancakesOrderRepository;
        this.buildingRepository = buildingRepository;
    }

    /**
     * @param buildingNr The building number
     * @param room       The room
     * @return object encapsulating the order creation
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public CreateOrderResult createOrder(int buildingNr, int room) {
        ParameterArguments.requirePositiveParameterArgument(buildingNr, "buildingNr");
        ParameterArguments.requirePositiveParameterArgument(room, "room");
        return buildingRepository.find(buildingNr)
                .filter(thisBuilding -> thisBuilding.hasRoom(room))
                .map(thisBuilding -> supplyWitFlashingLogs(
                        () -> pancakesOrderRepository.create(thisBuilding, room)))
                .map(PancakesOrder::snapshot)
                .map(CreateOrderResult::of)
                .orElseGet(CreateOrderResult::invalidAddress);
    }

    /**
     * @param orderId     The order id
     * @param count       The amount of pancakes
     * @param ingredients The ingredients
     * @return object encapsulating the order creation
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public AddPancakeResult addPancakes(UUID orderId, int count, Set<PancakeIngredient> ingredients) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");
        ParameterArguments.requirePositiveParameterArgument(count, "count");

        return modifyOrder(
                orderId,
                pancakesOrder -> pancakesOrder.addPancakes(count, ingredients),
                any -> AddPancakeResult.success(),
                AddPancakeResult::orderNotFound
        );
    }

    /**
     * @param orderId     The order id
     * @param count       The amount of pancakes
     * @param description The description of pancakes to remove
     * @return object encapsulating the pancakes removal
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public RemovePancakeResult removePancakes(UUID orderId, String description, int count) {
        ParameterArguments.requireNotBlankParameterArgument(description, "description");
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");
        ParameterArguments.requirePositiveParameterArgument(count, "count");

        return modifyOrder(
                orderId,
                pancakesOrder -> pancakesOrder.removePancakes(description, count),
                any -> RemovePancakeResult.success(),
                RemovePancakeResult::orderNotFound
        );
    }

    /**
     * @param orderId The order id
     * @return object encapsulating the order cancellation
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public CancelOrderResult cancelOrder(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");

        return supplyWitFlashingLogs(() -> pancakesOrderRepository.find(orderId)
                .map(panCakesOrder -> {
                    panCakesOrder.handleOrderCanceled();
                    pancakesOrderRepository.remove(panCakesOrder.getId());
                    return CancelOrderResult.success();
                })
                .orElseGet(CancelOrderResult::orderNotFound)
        );
    }

    /**
     * @param orderId The order id
     * @return object encapsulating the order completion
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public CompleteOrderResult completeOrder(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");

        return modifyOrder(
                orderId,
                PancakesOrder::markAsCompleted,
                any -> CompleteOrderResult.success(),
                CompleteOrderResult::orderNotFound
        );
    }

    /**
     * @param orderId The order id
     * @return object encapsulating the order preparation
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public PrepareOrderResult prepareOrder(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");

        return modifyOrder(
                orderId,
                pancakesOrder -> pancakesOrder.isCompleted()
                        ? Optional.empty()
                        : Optional.of(PrepareOrderResult.orderNotCompleted()),
                PancakesOrder::markAsPrepared,
                any -> PrepareOrderResult.success(),
                PrepareOrderResult::orderNotFound
        );
    }

    /**
     * @param orderId The order id
     * @return object encapsulating the order delivery
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public OrderDeliveryResult deliverOrder(UUID orderId) {
        return findOrder(orderId)
                .map(pancakesOrder -> {
                    if (!pancakesOrder.isPrepared()) {
                        return supplyWitFlashingLogs(OrderDeliveryResult::notPrepared);
                    }
                    return supplyWitFlashingLogs(() -> pancakesOrderRepository.remove(orderId)
                            .map(thisPanCakesOrder -> {
                                final var orderedForDelivery = thisPanCakesOrder.snapshot();
                                OrderLog.logDeliverOrder(orderedForDelivery, orderedForDelivery.pancakes());
                                return OrderDeliveryResult.of(orderedForDelivery);
                            })
                            .orElseGet(OrderDeliveryResult::concurrentlyDelivered));
                })
                .orElseGet(OrderDeliveryResult::orderNotFound);
    }

    /**
     * @param orderId The order id
     * @return The list  of pancake descriptions
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public List<String> viewOrder(UUID orderId) {
        ParameterArguments.requireNotNullParameterArgument(orderId, "orderId");

        return supplyWitFlashingLogs(() -> pancakesOrderRepository.find(orderId)
                .map(PancakesOrder::pancakeDescriptions)
                .orElse(List.of()));
    }

    /**
     * @return The list of completed order ids
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public Set<UUID> listCompletedOrders() {
        return supplyWitFlashingLogs(pancakesOrderRepository::listCompletedOrdersIds);
    }

    /**
     * @return The list of prepared order ids
     * @throws org.bakeryshop.common.TimeoutException when configured timeout was reached
     */
    public Set<UUID> listPreparedOrders() {
        return supplyWitFlashingLogs(pancakesOrderRepository::listPreparedOrdersIds);
    }

    private <F extends Failure, R extends Result<F>> R modifyOrder(
            UUID orderId,
            UnaryOperator<PancakesOrder> modifyFunction,
            Function<PancakesOrder, R> successFunction,
            Supplier<R> orderNotFoundResultSupplier) {
        return modifyOrder(
                orderId,
                any -> Optional.empty(),
                modifyFunction,
                successFunction,
                orderNotFoundResultSupplier
        );
    }

    private <F extends Failure, R extends Result<F>> R modifyOrder(
            UUID orderId,
            Function<PancakesOrder, Optional<R>> validateFunction,
            UnaryOperator<PancakesOrder> modifyFunction,
            Function<PancakesOrder, R> successFunction,
            Supplier<R> orderNotFoundResultSupplier) {
        return supplyWitFlashingLogs(() ->
                findOrder(orderId)
                        .map(pancakesOrder ->
                                validateFunction.apply(pancakesOrder)
                                        .orElseGet(() ->
                                                modifyFunction
                                                        .andThen(pancakesOrderRepository::update)
                                                        .andThen(successFunction)
                                                        .apply(pancakesOrder)
                                        )
                        )
                        .orElseGet(orderNotFoundResultSupplier)
        );
    }

    private Optional<PancakesOrder> findOrder(UUID orderId) {
        return pancakesOrderRepository.find(orderId);
    }

    private <T> T supplyWitFlashingLogs(Supplier<T> supplier) {
        try {
            return supplier.get();
        } finally {
            flushLogs();
        }
    }

    private void flushLogs() {
        OrderLog.flushLogs(log -> logger.debug(log.toString()));
    }
}
