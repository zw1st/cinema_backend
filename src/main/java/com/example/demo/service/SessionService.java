package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.session.LayoutRs;
import com.example.demo.api.session.SeatStatusDto;
import com.example.demo.api.session.SessionRq;
import com.example.demo.api.session.SessionRs;
import com.example.demo.configuration.AppProperties;
import com.example.demo.entity.HallEntity;
import com.example.demo.entity.MovieEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SessionEntity;
import com.example.demo.entity.TicketEntity;
import com.example.demo.entity.enumeration.TicketStatus;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.SeatRepository;
import com.example.demo.repository.SessionRepository;
import com.example.demo.repository.TicketRepository;

@Service
public class SessionService {
    private final SessionRepository repository;
    private final MovieService movieService;
    private final HallService hallService;
    private final SeatRepository seatRepository;
    private final TicketRepository ticketRepository;
    private final AppProperties appProperties;

    public SessionService(
            SessionRepository repository,
            MovieService movieService,
            HallService hallService, AppProperties appProperties, SeatRepository seatRepository,
            TicketRepository ticketRepository) {
        this.repository = repository;
        this.movieService = movieService;
        this.hallService = hallService;
        this.seatRepository = seatRepository;
        this.ticketRepository = ticketRepository;
        this.appProperties = appProperties;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SessionEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(SessionEntity.class, id));
    }

    @Transactional(readOnly = true)
    public List<SessionRs> getAll() {
        return SessionRs.fromList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public List<SessionRs> getByMovieIdAndDate(Long movieId, LocalDate date) {
        return SessionRs.fromList(repository.findByMovieIdAndDate(movieId, date));
    }

    @Transactional(readOnly = true)
    public List<SessionRs> getByMovieId(Long movieId) {
        return SessionRs.fromList(repository.findByMovieId(movieId));
    }

    @Transactional(readOnly = true)
    public SessionRs get(Long id) {
        final SessionEntity entity = getEntity(id);
        return SessionRs.from(entity);
    }

    @Transactional
    public SessionRs create(SessionRq dto) {
        MovieEntity movie = movieService.getEntity(dto.movieId());
        HallEntity hall = hallService.getEntity(dto.hallId());

        if (!appProperties.isWithinWorkingHours(dto.startTime())) {
            throw new ValidationException(
                    "Session start time %s is outside working hours [%s–%s]"
                            .formatted(dto.startTime(), appProperties.getOpenTime(), appProperties.getCloseTime()));
        }

        if (movie.getDuration() == null || movie.getDuration() <= 0) {
            throw new ValidationException("Movie duration is invalid");
        }

        LocalTime endTime = dto.startTime().plusMinutes(movie.getDuration());

        if (!appProperties.isWithinWorkingHours(endTime) && !endTime.isAfter(appProperties.getCloseTime())) {
            throw new ValidationException(
                    "Session end time %s is outside working hours"
                            .formatted(endTime));
        }

        // 🔹 5. Проверка на пересечения с учётом буфера из конфига
        int buffer = appProperties.getCleanupBufferMinutes();
        validateNoOverlap(hall.getId(), dto.date(), dto.startTime(), endTime, buffer);

        SessionEntity entity = new SessionEntity(movie, hall, dto.date(), dto.startTime(), endTime, dto.basePrice());
        return SessionRs.from(repository.save(entity));
    }

    private void validateNoOverlap(Long hallId, LocalDate date, LocalTime newStart, LocalTime newEnd,
            int bufferMinutes) {
        var existingSessions = repository.findByHallIdAndDate(hallId, date);

        for (var existing : existingSessions) {
            // Конфликт, если интервалы пересекаются с учётом буфера
            boolean hasOverlap = newStart.isBefore(existing.getEndTime().plusMinutes(bufferMinutes)) &&
                    existing.getStartTime().isBefore(newEnd.plusMinutes(bufferMinutes));

            if (hasOverlap) {
                throw new ValidationException(
                        "Session conflict: new session [%s–%s] overlaps with existing [%s–%s] (%d-min buffer required)"
                                .formatted(newStart, newEnd, existing.getStartTime(), existing.getEndTime(),
                                        bufferMinutes));
            }
        }
    }

    @Transactional(readOnly = true)
    public LayoutRs getLayout(Long sessionId) {
        SessionEntity session = repository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(SessionEntity.class, sessionId));

        List<SeatEntity> allSeats = seatRepository.findByHallId(session.getHall().getId());

        String[][] matrixScheme = new String[session.getHall().getTotalRows()][session.getHall().getTotalCols()];
        for (SeatEntity seat : allSeats) {
            // Вычитаем 1, так как в БД нумерация с 1, а в массиве с 0
            int rowIndex = seat.getRowNum() - 1;
            int colIndex = seat.getColNum() - 1;

            matrixScheme[rowIndex][colIndex] = seat.getSeatType().getName();
        }

        Map<String, TicketStatus> bookedSeats = loadSeatStatusesForSession(sessionId);

        List<SeatStatusDto> seatDtos = allSeats.stream().map(seat -> {
            String key = seat.getRowNum() + ":" + seat.getColNum();
            TicketStatus ticketStatus = bookedSeats.get(key); // null → место свободно

            // Передаём статус билета в forSession — он сам решит, доступно ли место
            return SeatStatusDto.forSession(seat, ticketStatus, session.getBasePrice());
        }).toList();

        return LayoutRs.from(session.getHall(), seatDtos, matrixScheme);
    }

    private Map<String, TicketStatus> loadSeatStatusesForSession(Long sessionId) {
        return ticketRepository.findBySessionId(sessionId).stream()
                .collect(Collectors.toMap(
                        t -> t.getRowNum() + ":" + t.getColNum(), // Ключ
                        TicketEntity::getStatus, // Значение
                        (existing, replacement) -> existing // Merge function на случай дублей
                ));
    }
}