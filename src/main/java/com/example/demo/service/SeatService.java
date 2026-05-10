package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.seat.SeatRq;
import com.example.demo.api.seat.SeatRs;
import com.example.demo.entity.HallEntity;
import com.example.demo.entity.SeatEntity;
import com.example.demo.entity.SeatType;
import com.example.demo.exception.NotFoundException;
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
}
