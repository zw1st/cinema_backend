package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.h;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.seat.SeatRq;
import com.example.demo.api.seat.SeatRs;
import com.example.demo.api.session.LayoutRq;
import com.example.demo.api.session.LayoutRs;
import com.example.demo.api.session.SeatStatusDto;
import com.example.demo.entity.HallEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SeatType;
import com.example.demo.exception.NotFoundException;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.SeatRepository;

@Service
public class SeatService {
    private final SeatRepository repository;
    private final HallService hallService;
    private final SeatTypeService seatTypeService;

    public SeatService(
            SeatRepository repository,
            HallService hallService,
            SeatTypeService seatTypeService) {
        this.repository = repository;
        this.hallService = hallService;
        this.seatTypeService = seatTypeService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SeatEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(SeatEntity.class, id));
    }

    @Transactional(readOnly = true)
    public List<SeatRs> getAllByHallId(Long hallId) {
        return SeatRs.fromList(repository.findByHallId(hallId));
    }

    @Transactional(readOnly = true)
    public SeatRs get(Long id) {
        final SeatEntity entity = getEntity(id);
        return SeatRs.from(entity);
    }

    @Transactional
    public SeatRs create(SeatRq dto) {

        // Валидация связей
        HallEntity hall = hallService.getEntity(dto.hallId());
        SeatType seatType = seatTypeService.getEntity(dto.seatTypeId());

        SeatEntity entity = new SeatEntity(dto.rowNum(), dto.colNum(), seatType, hall);
        entity = repository.save(entity);
        return SeatRs.from(entity);
    }

    @Transactional
    public LayoutRs createLayout(LayoutRq rq) {
        HallEntity hall = hallService.getEntity(rq.hallId());
        List<List<Integer>> matrix = rq.seatTypeMatrix();

        // 🔹 1. Валидация: количество рядов
        if (matrix.size() != hall.getTotalRows()) {
            throw new ValidationException(
                    "Row count mismatch: expected %d, got %d"
                            .formatted(hall.getTotalRows(), matrix.size()));
        }

        // 🔹 2. Валидация: количество колонок в каждом ряду
        for (int i = 0; i < matrix.size(); i++) {
            List<Integer> row = matrix.get(i);
            if (row.size() != hall.getTotalCols()) {
                throw new ValidationException(
                        "Column count mismatch at row %d: expected %d, got %d"
                                .formatted(i + 1, hall.getTotalCols(), row.size()));
            }
        }

        // TODO расскомменить когда допишу ticket
        // boolean hasActiveBookings =
        // sessionRepository.findByHallId(hall.getId()).stream()
        // .anyMatch(session -> ticketRepository.existsBySessionId(session.getId()));
        // if (hasActiveBookings) {
        // throw new ValidationException("Cannot update layout: Hall has active sessions
        // with tickets.");
        // }

        // 3. Удаляем старую схему
        repository.deleteByHallId(hall.getId());

        List<SeatEntity> seatsToSave = new ArrayList<>();
        int rowIdx = 1;

        for (List<Integer> row : matrix) {
            int colIdx = 1;
            for (Integer typeId : row) {
                if (typeId != null && typeId > 0) { // >0 = место, 0/null = неявный проход
                    SeatType type = seatTypeService.getEntity(typeId.longValue());
                    seatsToSave.add(new SeatEntity(rowIdx, colIdx, type, hall));
                }
                colIdx++;
            }
            rowIdx++;
        }

        repository.saveAll(seatsToSave);

        List<SeatStatusDto> seatDtos = seatsToSave.stream()
                .map(SeatStatusDto::forLayout)
                .toList();

        return LayoutRs.from(hall, seatDtos);
    }
}
