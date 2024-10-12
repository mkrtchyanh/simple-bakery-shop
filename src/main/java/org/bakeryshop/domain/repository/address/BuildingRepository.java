package org.bakeryshop.domain.repository.address;

import org.bakeryshop.domain.model.address.Building;

import java.util.Optional;

public interface BuildingRepository {

    Optional<Building> find(int buildingNumber);
}
