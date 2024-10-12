package org.bakeryshop.util;

import java.util.Collection;
import java.util.Objects;

public final class ParameterArguments {
    private ParameterArguments() {
        super();
    }

    public static void requireNotNullParameterArgument(Object argument, String parameter) {
        if (Objects.isNull(argument)) {
            throw new IllegalArgumentException("Null was passed as an argument for parameter '%s'."
                    .formatted(parameter));
        }
    }

    public static void requireNotBlankParameterArgument(String argument, String parameter) {
        if (Objects.isNull(argument) || argument.isBlank()) {
            throw new IllegalArgumentException("Null or blank text was passed as an argument for parameter '%s'."
                    .formatted(parameter));
        }
    }

    public static void requireNotEmptyParameterArgument(Collection<?> argument, String parameter) {
        if (Objects.isNull(argument) || argument.isEmpty()) {
            throw new IllegalArgumentException("Empty collection was passed as an argument for parameter '%s'."
                    .formatted(parameter));
        }
    }

    public static void requirePositiveParameterArgument(int argument, String parameter) {
        if (argument <= 0) {
            throw new IllegalArgumentException("Non positive number %d was passed as an argument for parameter '%s'."
                    .formatted(argument, parameter));
        }
    }
}
