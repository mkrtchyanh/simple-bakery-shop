package org.bakeryshop.service.usecase.pancake;

import org.bakeryshop.common.Failures;
import org.bakeryshop.service.usecase.Failure;
import org.bakeryshop.service.usecase.Result;

import java.util.Objects;
import java.util.Optional;

public final class AddPancakeResult implements Result<AddPancakeResult.AddPancakeFailure> {

    public static final AddPancakeResult SUCCESS_RESULT = new AddPancakeResult(null);

    public static final AddPancakeResult ORDER_NOT_FOUND_RESULT
            = new AddPancakeResult(AddPancakeFailure.ORDER_NOT_FOUND);

    private final AddPancakeFailure failure;

    private AddPancakeResult(AddPancakeFailure failure) {
        this.failure = failure;
    }

    public static AddPancakeResult success() {
        return SUCCESS_RESULT;
    }

    public static AddPancakeResult orderNotFound() {
        return ORDER_NOT_FOUND_RESULT;
    }

    @Override
    public Optional<AddPancakeFailure> getFailure() {
        return Optional.ofNullable(failure);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof AddPancakeResult that)) return false;
        return failure == that.failure;
    }

    @Override
    public int hashCode() {
        return Objects.hash(failure);
    }

    @Override
    public String toString() {
        return "AddPancakeResult{" +
                ", failure=" + failure + '}';
    }

    public enum AddPancakeFailure implements Failure {

        ORDER_NOT_FOUND(Failures.ORDER_NOT_FOUND);

        private final String code;
        private final String reason;

        AddPancakeFailure(String code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        AddPancakeFailure(Failure failure) {
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
