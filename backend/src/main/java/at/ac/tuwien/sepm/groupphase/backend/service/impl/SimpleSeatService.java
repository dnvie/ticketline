package at.ac.tuwien.sepm.groupphase.backend.service.impl;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedCreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.DetailedSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import at.ac.tuwien.sepm.groupphase.backend.exception.ConflictException;
import at.ac.tuwien.sepm.groupphase.backend.exception.NotFoundException;
import at.ac.tuwien.sepm.groupphase.backend.exception.ValidationException;
import at.ac.tuwien.sepm.groupphase.backend.repository.SeatRepository;
import at.ac.tuwien.sepm.groupphase.backend.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SimpleSeatService implements SeatService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());


    private final SeatRepository seatRepository;

    private final CustomUserDetailService customUserDetailService;

    private final SeatMapper seatMapper;

    @Override
    public List<SimpleSeatDto> getAllSeats() {
        LOGGER.debug("getAllSeats()");
        return seatMapper.seatListToSeatDtoList(seatRepository.findAll());
    }

    @Override
    public SimpleSeatDto getSeatById(UUID id) throws NotFoundException {
        LOGGER.debug("getSeatById({})", id);
        return seatMapper.seatToSimpleSeatDto(seatRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Could not find seat with id " + id)));
    }

    @Override
    public SimpleSeatDto updateSeat(SimpleSeatDto seatDto, UUID id) throws NotFoundException, ConflictException {
        LOGGER.debug("updateSeat({})", seatDto);
        Seat toUpdate = seatRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Could not find seat with id " + seatDto.getId()));
        //TODO: check if seat is booked and throw ConflictException
        Seat newValues = seatMapper.seatDtoToSeat(seatDto);
        toUpdate.setSeatRow(newValues.getSeatRow());
        toUpdate.setSeatColumn(newValues.getSeatColumn());
        toUpdate.setSector(newValues.getSector());
        toUpdate.setNumber(newValues.getNumber());
        seatRepository.save(toUpdate);
        return seatMapper.seatToSimpleSeatDto(seatRepository.findById(id).orElseThrow(
            () -> new NotFoundException("Could not find seat with id " + seatDto.getId())));
    }

    @Override
    public DetailedSeatDto createSeat(DetailedCreateSeatDto seatDto) throws NotFoundException, ValidationException, ConflictException {
        LOGGER.debug("createSeat({})", seatDto);
        Seat toCreate = seatMapper.detailedCreateSeatDtoToSeat(seatDto);
        if (toCreate.getSector() == null) {
            List<String> validationError = List.of("Sector is null");
            throw new ValidationException("Seat creation failed", validationError);
        }
        if (seatRepository.existsByNumber(toCreate.getSector().getId(), toCreate.getNumber())) {
            List<String> conflictError = List.of("Seat with number " + seatDto.getNumber() + " already exists");
            throw new ConflictException("Seat creation failed", conflictError);
        }
        Seat created = seatRepository.save(toCreate);
        return seatMapper.seatToDetailedSeatDto(created);
    }
}
