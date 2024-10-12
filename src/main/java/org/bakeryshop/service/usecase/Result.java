package org.bakeryshop.service.usecase;

import java.util.Optional;

public interface Result<T extends Failure> {

    Optional<T> getFailure();

    default boolean hasFailure() {
        return getFailure().isPresent();
    }

    default T requireFailure() {
        return getFailure()
                .orElseThrow(
                        () -> new IllegalStateException("Failure should be present in the current execution context."));
    }
}
