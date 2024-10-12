package org.bakeryshop.service;

import org.bakeryshop.domain.model.order.PancakesOrder;
import org.bakeryshop.domain.model.order.PancakesOrderSnapshot;
import org.bakeryshop.domain.model.pancakes.PancakeRecipe;
import org.bakeryshop.util.ParameterArguments;

import java.util.List;

public final class OrderLog {
    private static final ThreadLocal<StringBuilder> logHolder = ThreadLocal.withInitial(StringBuilder::new);

    private static StringBuilder log() {
        return logHolder.get();
    }

    public static void logAddPancake(PancakesOrder order, String description, List<PancakeRecipe> pancakes) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");
        ParameterArguments.requireNotBlankParameterArgument(description, "description");
        ParameterArguments.requireNotNullParameterArgument(pancakes, "pancakes");

        log().append("Added pancake with description '%s' ".formatted(description))
                .append("to order %s containing %d pancakes, ".formatted(order.getId(), pancakes.size()))
                .append("for building %d, room %d.".formatted(order.getAddress().building().buildingNr(), order.getAddress().room()));
    }

    public static void logRemovePancakes(PancakesOrder order, String description, int count, List<PancakeRecipe> pancakes) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");
        ParameterArguments.requirePositiveParameterArgument(count, "count");
        ParameterArguments.requireNotBlankParameterArgument(description, "description");
        ParameterArguments.requireNotNullParameterArgument(pancakes, "pancakes");

        log().append("Removed %d pancake(s) with description '%s' ".formatted(count, description))
                .append("from order %s now containing %d pancakes, ".formatted(order.getId(), pancakes.size()))
                .append("for building %d, room %d.".formatted(order.getAddress().buildingNr(), order.getAddress().room()));
    }

    public static void logCancelOrder(PancakesOrder order, List<PancakeRecipe> pancakes) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");
        ParameterArguments.requireNotNullParameterArgument(pancakes, "pancakes");

        log().append("Cancelled order %s with %d pancakes ".formatted(order.getId(), pancakes.size()))
                .append("for building %d, room %d.".formatted(order.getAddress().buildingNr(), order.getAddress().room()));
    }

    public static void logDeliverOrder(PancakesOrderSnapshot order, List<PancakeRecipe> pancakes) {
        ParameterArguments.requireNotNullParameterArgument(order, "order");
        ParameterArguments.requireNotNullParameterArgument(pancakes, "pancakes");

        log().append("Order %s with %d pancakes ".formatted(order.id(), order.pancakes().size()))
                .append("for building %d, room %d out for delivery.".formatted(order.address().buildingNr(), order.address().room()));
    }

    public static void flushLogs(LogWriter logWriter) {
        ParameterArguments.requireNotNullParameterArgument(logWriter, "logWriter");

        final var logBuffer = log();
        if (logBuffer.isEmpty()) {
            return;
        }
        logWriter.write(logBuffer);
        logHolder.set(new StringBuilder());
    }

}
