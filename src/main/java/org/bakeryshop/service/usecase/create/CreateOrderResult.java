package org.bakeryshop.service.usecase.create;

import org.bakeryshop.common.Failures;
import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class CreateOrderResult implements Result<CreateOrderResult.CreateOrderFailure> {

    public static final CreateOrderResult INVALID_ADDRESS_RESULT
            = new CreateOrderResult(null, CreateOrderFailure.INVALID_ADDRESS);

    private final PancakesOrderSnapshot order;

    private final CreateOrderFailure failure;

    private CreateOrderResult(PancakesOrderSnapshot order, CreateOrderFailure failure) {
        this.order = order;
        this.failure = failure;
    }

    public static CreateOrderResult of(PancakesOrderSnapshot order) {
        return new CreateOrderResult(order, null);
    }

    public static CreateOrderResult invalidAddress() {
        return INVALID_ADDRESS_RESULT;
    }

    @Override
    public Optional<CreateOrderFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    public PancakesOrderSnapshot requireOrder() {
        if (Objects.isNull(order)) {
            throw new IllegalStateException("order should be present in the current execution context");
        }
        return order;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CreateOrderResult that)) return false;
        return failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failure);
    }

    @Override
    public String toString() {
        return "CompleteOrderResult{" +
                ", failure=" + failure + '}';
    }

    public enum CreateOrderFailure implements Failure {

        INVALID_ADDRESS(Failures.INVALID_ADDRESS);

        private final String code;
        private final String reason;

        CreateOrderFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        CreateOrderFailure(Failure failure) {
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
