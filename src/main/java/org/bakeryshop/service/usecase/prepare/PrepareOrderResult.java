package org.bakeryshop.service.usecase.prepare;

import org.bakeryshop.common.Failures;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class PrepareOrderResult implements Result<PrepareOrderResult.PrepareOrderFailure> {

    public static final PrepareOrderResult SUCCESS_RESULT = new PrepareOrderResult(null);

    public static final PrepareOrderResult ORDER_NOT_COMPLETED_RESULT
            = new PrepareOrderResult(PrepareOrderFailure.ORDER_NOT_COMPLETED);

    public static final PrepareOrderResult ORDER_NOT_FOUND_RESULT
            = new PrepareOrderResult(PrepareOrderFailure.ORDER_NOT_FOUND);

    private final PrepareOrderFailure failure;

    private PrepareOrderResult(PrepareOrderFailure failure) {
        this.failure = failure;
    }

    public static PrepareOrderResult success() {
        return SUCCESS_RESULT;
    }

    public static PrepareOrderResult orderNotCompleted() {
        return ORDER_NOT_FOUND_RESULT;
    }

    public static PrepareOrderResult orderNotFound() {
        return ORDER_NOT_COMPLETED_RESULT;
    }

    @Override
    public Optional<PrepareOrderFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof PrepareOrderResult that)) return false;
        return failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failure);
    }

    @Override
    public String toString() {
        return "PrepareOrderResult{" +
                ", failure=" + failure + '}';
    }

    public enum PrepareOrderFailure implements Failure {

        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND),
        ORDER_NOT_COMPLETED("order_not_completed", "Order not completed");

        private final String code;
        private final String reason;

        PrepareOrderFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        PrepareOrderFailure(Failure failure) {
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
