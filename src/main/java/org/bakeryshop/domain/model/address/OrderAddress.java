package org.bakeryshop.domain.model.address;

import org.bakeryshop.util.ParameterArguments;

public record OrderAddress(Building building, int room) {

    public OrderAddress {
        ParameterArguments.requireNotNullParameterArgument(building, "building");
        ParameterArguments.requirePositiveParameterArgument(room, "room");
        if (!building.hasRoom(room)) {
            throw new IllegalArgumentException("Building does have such room '%d'".formatted(room));
        }
    }

    public int buildingNr(){
        return building.buildingNr();
    }
}
