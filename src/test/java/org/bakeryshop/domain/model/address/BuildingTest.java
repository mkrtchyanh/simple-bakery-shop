package org.bakeryshop.domain.model.address;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;

class BuildingTest {

    @ParameterizedTest
    @ArgumentsSource(NonPositiveRoomProvider.class)
    void hasRoomShouldReturnFalseWhenRoomIsNonPositive(
            int nonPositiveRoom
    ) {
        final var building = new Building(1, 1);
        assertFalse(building.hasRoom(nonPositiveRoom));
    }

    @ParameterizedTest
    @ArgumentsSource(BuildingProvider.class)
    void hasRoomShouldReturnFalseWhenRoomBiggerThanAmountOfRooms(
            Building building
    ) {
        assertFalse(building.hasRoom(building.amountOfRooms() + 1));
    }

    private static class NonPositiveRoomProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(Integer.MIN_VALUE, -1, 0)
                    .map(Arguments::of);
        }
    }

    private static class BuildingProvider implements ArgumentsProvider {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext) {
            return Stream.of(
                            new Building(1, 1),
                            new Building(1, Integer.MAX_VALUE)
                    )
                    .map(Arguments::of);
        }
    }
}