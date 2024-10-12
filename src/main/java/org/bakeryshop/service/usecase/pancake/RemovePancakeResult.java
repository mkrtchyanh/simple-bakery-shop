package org.bakeryshop.service.usecase.pancake;

import org.bakeryshop.common.Failures;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class RemovePancakeResult implements Result<RemovePancakeResult.RemovePancakeFailure> {

    public static final RemovePancakeResult SUCCESS_RESULT = new RemovePancakeResult(null);

    public static final RemovePancakeResult ORDER_NOT_FOUND_RESULT
            = new RemovePancakeResult(RemovePancakeFailure.ORDER_NOT_FOUND);

    private final RemovePancakeFailure failure;

    private RemovePancakeResult(RemovePancakeFailure failure) {
        this.failure = failure;
    }

    public static RemovePancakeResult success() {
        return SUCCESS_RESULT;
    }

    public static RemovePancakeResult orderNotFound() {
        return ORDER_NOT_FOUND_RESULT;
    }

    @Override
    public Optional<RemovePancakeFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof RemovePancakeResult that)) return false;
        return failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failure);
    }

    @Override
    public String toString() {
        return "RemovePancakeResult{" +
                ", failure=" + failure + '}';
    }

    public enum RemovePancakeFailure implements Failure {

        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND);

        private final String code;
        private final String reason;

        RemovePancakeFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        RemovePancakeFailure(Failure failure) {
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
