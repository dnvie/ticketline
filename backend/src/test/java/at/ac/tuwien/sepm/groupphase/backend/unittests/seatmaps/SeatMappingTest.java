package at.ac.tuwien.sepm.groupphase.backend.unittests.seatmaps;

import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.CreateSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.dto.SimpleSeatDto;
import at.ac.tuwien.sepm.groupphase.backend.endpoint.mapper.SeatMapper;
import at.ac.tuwien.sepm.groupphase.backend.entity.Seat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

//TODO: refactor because of changes with seatmap
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class SeatMappingTest {

    private final Seat seat = Seat.builder()
        .seatRow(1)
        .number(1)
        .seatColumn(1)
        .sector(null)
        .id(UUID.fromString("f227921b-4605-43a2-8a1d-a617629c55e1"))
        .build();

    @InjectMocks
    private SeatMapper seatMapper = Mappers.getMapper(SeatMapper.class);

    @Test
    public void simpleSeatDtoToSeat() {
        SimpleSeatDto simpleSeatDto = new SimpleSeatDto(
            UUID.fromString("f227921b-4605-43a2-8a1d-a617629c55e1"),
            1,
            null,
            1,
            1,
            false);
        Seat mappedSeat = seatMapper.seatDtoToSeat(simpleSeatDto);
        assertNotNull(mappedSeat);
        assertAll(
            () -> assertEquals(seat.getId(), mappedSeat.getId()),
            () -> assertEquals(seat.getSeatColumn(), mappedSeat.getSeatColumn()),
            () -> assertEquals(seat.getSeatRow(), mappedSeat.getSeatRow()),
            () -> assertEquals(seat.getNumber(), mappedSeat.getNumber())
        );
    }

    @Test
    public void seatToSimpleSeatDto() {
        SimpleSeatDto mappedSimpleSeatDto = seatMapper.seatToSimpleSeatDto(seat);
        assertNotNull(mappedSimpleSeatDto);
        assertAll(
            () -> assertEquals(seat.getId(), mappedSimpleSeatDto.getId()),
            () -> assertEquals(seat.getSeatColumn(), mappedSimpleSeatDto.getSeatColumn()),
            () -> assertEquals(seat.getSeatRow(), mappedSimpleSeatDto.getSeatRow()),
            () -> assertEquals(seat.getNumber(), mappedSimpleSeatDto.getNumber())
/*
            () -> assertEquals(seat.getReserved().getId(), mappedSimpleSeatDto.getReservedFor())
*/
        );
    }

    @Test
    public void createSeatDtoToSeat() {
        CreateSeatDto createSeatDto = new CreateSeatDto(
            1,
            1,
            1);
        Seat mappedSeat = seatMapper.createSeatDtoToSeat(createSeatDto);
        assertNotNull(mappedSeat);
        assertAll(
            () -> assertEquals(1, mappedSeat.getSeatColumn()),
            () -> assertEquals(1, mappedSeat.getSeatRow()),
            () -> assertEquals(1, mappedSeat.getNumber())
        );
    }

    @Test
    public void seatListToSimpleSeatDtoList() {
        List<Seat> seats = new ArrayList<>();
        seats.add(seat);
        seats.add(Seat.builder()
            .seatRow(1)
            .number(2)
            .seatColumn(1)
            .sector(null)
            .id(UUID.fromString("7cfe43cd-799f-40cb-b552-2e2de68bd39e"))
            .build());

        List<SimpleSeatDto> mappedSeats = seatMapper.seatListToSeatDtoList(seats);
        assertNotNull(mappedSeats);
        assertEquals(2, mappedSeats.size());
        assertAll(
            () -> assertEquals(seats.get(0).getId(), mappedSeats.get(0).getId()),
            () -> assertEquals(seats.get(0).getSeatColumn(), mappedSeats.get(0).getSeatColumn()),
            () -> assertEquals(seats.get(0).getSeatRow(), mappedSeats.get(0).getSeatRow()),
            () -> assertEquals(seats.get(0).getNumber(), mappedSeats.get(0).getNumber()),
            () -> assertEquals(seats.get(1).getId(), mappedSeats.get(1).getId()),
            () -> assertEquals(seats.get(1).getSeatColumn(), mappedSeats.get(1).getSeatColumn()),
            () -> assertEquals(seats.get(1).getSeatRow(), mappedSeats.get(1).getSeatRow()),
            () -> assertEquals(seats.get(1).getNumber(), mappedSeats.get(1).getNumber())
        );
    }
}
