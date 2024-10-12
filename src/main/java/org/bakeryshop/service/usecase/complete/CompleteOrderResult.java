package org.bakeryshop.service.usecase.complete;

import org.bakeryshop.common.Failures;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class CompleteOrderResult implements Result<CompleteOrderResult.CompleteOrderFailure> {

    public static final CompleteOrderResult SUCCESS_RESULT = new CompleteOrderResult(null);

    public static final CompleteOrderResult ORDER_NOT_FOUND_RESULT
            = new CompleteOrderResult(CompleteOrderFailure.ORDER_NOT_FOUND);

    private final CompleteOrderFailure failure;

    private CompleteOrderResult(CompleteOrderFailure failure) {
        this.failure = failure;
    }

    public static CompleteOrderResult success() {
        return SUCCESS_RESULT;
    }

    public static CompleteOrderResult orderNotFound() {
        return ORDER_NOT_FOUND_RESULT;
    }

    @Override
    public Optional<CompleteOrderFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CompleteOrderResult that)) return false;
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

    public enum CompleteOrderFailure implements Failure {

        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND);

        private final String code;
        private final String reason;

        CompleteOrderFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        CompleteOrderFailure(Failure failure) {
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
