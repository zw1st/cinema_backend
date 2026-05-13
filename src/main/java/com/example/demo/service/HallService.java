package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.api.hall.HallRq;
import com.example.demo.api.hall.HallRs;
import com.example.demo.entity.HallEntity;
import com.example.demo.exception.NotFoundException;
import com.example.demo.repository.HallRepository;

@Service
public class HallService {
    private final HallRepository repository;

    public HallService(HallRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public HallEntity getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException(HallEntity.class, id));
    }

    @Transactional(readOnly = true)
    public List<HallRs> getAll() {
        return HallRs.fromList(repository.findAll());
    }

    @Transactional(readOnly = true)
    public HallRs get(Long id) {
        final HallEntity entity = getEntity(id);
        return HallRs.from(entity);
    }

    @Transactional
    public HallRs create(HallRq dto) {
        HallEntity entity = new HallEntity(dto.name(), dto.totalRows(), dto.totalCols());
        return HallRs.from(repository.save(entity));
    }

}
