package com.example.demo.api.hall;

import java.util.List;
import java.util.stream.StreamSupport;

import com.example.demo.entity.HallEntity;
import com.fasterxml.jackson.annotation.JsonProperty;

public record HallRs(
        Long id, String name,
        @JsonProperty("total_rows") Integer totalRows,
        @JsonProperty("total_cols") Integer totalCols) {
    public static HallRs from(HallEntity e) {
        return new HallRs(e.getId(), e.getName(), e.getTotalRows(), e.getTotalCols());
    }

    public static List<HallRs> fromList(Iterable<HallEntity> entities) {
        return StreamSupport.stream(entities.spliterator(), false).map(HallRs::from).toList();
    }
}