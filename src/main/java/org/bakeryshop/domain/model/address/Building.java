package org.bakeryshop.domain.model.address;

import org.bakeryshop.util.ParameterArguments;

public record Building(int buildingNr, int amountOfRooms) {

    public Building {
        ParameterArguments.requirePositiveParameterArgument(buildingNr, "buildingNr");
        ParameterArguments.requirePositiveParameterArgument(amountOfRooms, "amountOfRooms");
    }

    public boolean hasRoom(int number) {
        return number > 0
                && amountOfRooms >= number;
    }
}
