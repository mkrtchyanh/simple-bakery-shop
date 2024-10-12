package org.bakeryshop.domain.repository.address;

import org.bakeryshop.domain.model.address.Building;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class FixedSetBuildingRepositoryTest {

    @Test
    @DisplayName("find: should return building from provided set")
    void findShouldReturnBuildingFromProvidedSet() {
        final var buildings = Set.of(
                new Building(1, 8),
                new Building(4, 16),
                new Building(9, 32)
        );
        final var buildingRepo = new FixedSetBuildingRepository(buildings);
        final var buildingsMap = buildings.stream()
                .collect(Collectors.toMap(Building::buildingNr, Function.identity()));
        for (int i = 1; i < 10; i++) {
            if (!buildingsMap.containsKey(i)) {
                continue;
            }
            assertThat(buildingRepo.find(i)).contains(buildingsMap.get(i));
        }
    }

    @Test
    @DisplayName("find: should return nothing")
    void findShouldReturnNothing() {
        final var buildings = Set.of(
                new Building(1, 8),
                new Building(4, 16),
                new Building(9, 32)
        );
        final var buildingRepo = new FixedSetBuildingRepository(buildings);
        final var buildingNrs = buildings.stream()
                .map(Building::buildingNr)
                .collect(Collectors.toUnmodifiableSet());
        for (int i = 1; i < 10; i++) {
            if (buildingNrs.contains(i)) {
                continue;
            }
            assertThat(buildingRepo.find(i)).isEmpty();
        }
    }
}