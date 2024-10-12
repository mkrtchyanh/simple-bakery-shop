package org.bakeryshop.service.usecase.cancel;

import org.bakeryshop.common.Failures;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class CancelOrderResult implements Result<CancelOrderResult.CancelOrderFailure> {

    public static final CancelOrderResult SUCCESS_RESULT = new CancelOrderResult(null);

    public static final CancelOrderResult ORDER_NOT_FOUND_RESULT
            = new CancelOrderResult(CancelOrderFailure.ORDER_NOT_FOUND);

    private final CancelOrderFailure failure;

    private CancelOrderResult(CancelOrderFailure failure) {
        this.failure = failure;
    }

    public static CancelOrderResult success() {
        return SUCCESS_RESULT;
    }

    public static CancelOrderResult orderNotFound() {
        return ORDER_NOT_FOUND_RESULT;
    }

    @Override
    public Optional<CancelOrderFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CancelOrderResult that)) return false;
        return failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failure);
    }

    @Override
    public String toString() {
        return "CancelOrderResult{" +
                ", failure=" + failure + '}';
    }

    public enum CancelOrderFailure implements Failure {

        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND);

        private final String code;
        private final String reason;

        CancelOrderFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        CancelOrderFailure(Failure failure) {
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
