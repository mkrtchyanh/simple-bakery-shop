package org.bakeryshop.domain.repository.address;

import org.bakeryshop.domain.model.address.Building;
import org.bakeryshop.util.ParameterArguments;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FixedSetBuildingRepository implements BuildingRepository {

    private final Map<Integer, Building> buildings;

    public FixedSetBuildingRepository(Set<Building> buildings) {
        this.buildings = buildings.stream()
                .collect(Collectors.toUnmodifiableMap(Building::buildingNr, Function.identity()));
    }

    @Override
    public Optional<Building> find(int buildingNumber) {
        ParameterArguments.requirePositiveParameterArgument(buildingNumber, "buildingNumber");
        return Optional.ofNullable(buildings.get(buildingNumber));
    }
}
