package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.seatType.SeatTypeRq;
import com.example.demo.api.seatType.SeatTypeRs;
import com.example.demo.entity.SeatType;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.SeatTypeRepository;

@Service
public class SeatTypeService {
    private final SeatTypeRepository repository;

    public SeatTypeService(SeatTypeRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public SeatType getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(SeatType.class, id));
    }

    @Transactional(readOnly = true)
    public List<SeatTypeRs> getAll() {
        return SeatTypeRs.fromList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public SeatTypeRs get(Long id) {
        final SeatType entity = getEntity(id);
        return SeatTypeRs.from(entity);
    }

    @Transactional
    public SeatTypeRs create(SeatTypeRq dto) {
        SeatType entity = new SeatType(dto.name(), dto.additionalPrice());
        entity = repository.save(entity);
        return SeatTypeRs.from(entity);

    }

    @Transactional
    public SeatTypeRs update(Long id, SeatTypeRq dto) {
        final SeatType entity = getEntity(id);
        // Обновляем только цену, имя типа меняем редко (через миграцию)
        entity.setAdditionalPrice(dto.additionalPrice());
        return SeatTypeRs.from(repository.save(entity));
    }
}