package org.bakeryshop.service.usecase;

import org.bakeryshop.util.ParameterArguments;

public interface Failure {

    String code();

    String reason();

    static Failure of(String code, String reason) {
        ParameterArguments.requireNotBlankParameterArgument(code, "code");
        ParameterArguments.requireNotBlankParameterArgument(reason, "reason");
        return new ImmutableFailure(code, reason);
    }

    record ImmutableFailure(String code, String reason) implements Failure {
    }
}
