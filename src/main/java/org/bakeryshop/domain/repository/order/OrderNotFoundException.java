package org.bakeryshop.domain.repository.order;

import java.util.UUID;

public class OrderNotFoundException extends RuntimeException {

    public OrderNotFoundException(String message) {
        super(message);
    }

    public static OrderNotFoundException byId(UUID id) {
        return new OrderNotFoundException("order with id '%s' not found".formatted(id));
    }
}
