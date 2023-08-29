package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatMapDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSectorDto;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SimpleSeatMapValidator {

    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void validateForCreate(CreateSeatMapDto toValidate) throws ConflictException {
        LOGGER.trace("validateForCreate({})", toValidate);
        //TODO: do we need validation errors here?
        List<String> conflictErrors = new ArrayList<>();
        if (toValidate.getSectors() == null) {
            return;
        }
        Set<CreateSectorDto> sectors = toValidate.getSectors();
        validateSectorsForCreate(sectors, conflictErrors);
        if (!conflictErrors.isEmpty()) {
            throw new ConflictException("Creation of Seat Map failed: ", conflictErrors);
        }
    }

    private void validateSectorsForCreate(Set<CreateSectorDto> sectors, List<String> conflictErrors) {
        LOGGER.trace("validateSectorsForCreate({})", sectors);
        Set<String> sectorNames = new HashSet<>();

        for (CreateSectorDto sector : sectors) {
            //check if this sector name already exists in sectors excluding this sector
            if (sectorNames.contains(sector.getName())) {
                conflictErrors.add("Sector with name " + sector.getName() + " already exists");
            }
            if (!sector.getSeats().isEmpty()) {
                validateSeatsForCreate(sector.getSeats(), conflictErrors);
            }
            sectorNames.add(sector.getName());
        }
    }

    private void validateSeatsForCreate(Set<CreateSeatDto> seats, List<String> conflictErrors) {
        Set<Integer> seatNumbers = new HashSet<>();
        LOGGER.trace("validateSeatsForCreate({})", seats);
        for (CreateSeatDto seat : seats) {
            if (seatNumbers.contains(seat.getNumber())) {
                conflictErrors.add("Seat with number " + seat.getNumber() + " already exists");
            }
            seatNumbers.add(seat.getNumber());
        }
    }
}
