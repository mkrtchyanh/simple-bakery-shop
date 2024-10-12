package org.bakeryshop.common;

import org.bakeryshop.service.usecase.Failure;

public final class Failures {
    private Failures() {
        super();
    }

    public static final Failure ORDER_NOT_FOUND = Failure.of("order_not_found", "Order is missing");
    public static final Failure INVALID_ADDRESS = Failure.of("invalid_address", "Invalid address");
}
