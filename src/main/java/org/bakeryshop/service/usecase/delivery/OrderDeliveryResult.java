package org.bakeryshop.service.usecase.delivery;

import org.bakeryshop.common.Failures;
import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;
import org.bakeryshop.util.ParameterArguments;

import java.util.Objects;
import java.util.Optional;

public final class OrderDeliveryResult implements Result<OrderDeliveryResult.OrderDeliveryFailure> {

    public static final OrderDeliveryResult NOT_PREPARED_RESULT
            = new OrderDeliveryResult(null, OrderDeliveryFailure.NOT_PREPARED);
    public static final OrderDeliveryResult ORDER_NOT_FOUND_RESULT
            = new OrderDeliveryResult(null, OrderDeliveryFailure.ORDER_NOT_FOUND);
    public static final OrderDeliveryResult CONCURRENTLY_DELIVERED_RESULT
            = new OrderDeliveryResult(null, OrderDeliveryFailure.CONCURRENTLY_DELIVERED);

    private final PancakesOrderSnapshot order;
    private final OrderDeliveryFailure failure;

    private OrderDeliveryResult(PancakesOrderSnapshot order, OrderDeliveryFailure failure) {
        this.order = order;
        this.failure = failure;
    }

    public static OrderDeliveryResult of(PancakesOrderSnapshot order) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");
        return new OrderDeliveryResult(order, null);
    }

    public static OrderDeliveryResult orderNotFound() {
        return ORDER_NOT_FOUND_RESULT;
    }

    public static OrderDeliveryResult notPrepared() {
        return NOT_PREPARED_RESULT;
    }

    public static OrderDeliveryResult concurrentlyDelivered() {
        return CONCURRENTLY_DELIVERED_RESULT;
    }

    public Optional<PancakesOrderSnapshot> order() {
        return Optional.ofNullable(order);
    }

    public PancakesOrderSnapshot requireOrder() {
        if (Objects.isNull(order)) {
            throw new IllegalStateException("Order delivery should be present in the current execution context");
        }
        return order;
    }

    @Override
    public Optional<OrderDeliveryFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof OrderDeliveryResult that)) return false;
        return Objects.equals(order, that.order) && failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, failure);
    }

    @Override
    public String toString() {
        return "OrderDeliveryResult{" +
                "order=" + order +
                ", failure=" + failure +
                '}';
    }

    public enum OrderDeliveryFailure implements Failure {
        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND),
        NOT_PREPARED("order_not_prepared", "Order not prepared"),
        CONCURRENTLY_DELIVERED("order_concurrently_delivered", "Order was concurrently delivered prepared");

        private final String code;
        private final String reason;

        OrderDeliveryFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        OrderDeliveryFailure(Failure failure) {
            this(failure.code(), failure.reason());
        }

        @Override
        public String code() {
            return code;
        }

        @Override
        public String reason() {
            return reason;
        }
    }
}
