package com.example.demo.api.hall;

import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.entity.HallEntity;

public record HallRs(
        Long id,
        String name) {

    public static HallRs from(HallEntity entity) {
        return new HallRs(entity.getId(), entity.getName());
    }

    public static List<HallRs> fromList(Iterable<HallEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false)
                .map(HallRs::from)
                .toList();
    }
}