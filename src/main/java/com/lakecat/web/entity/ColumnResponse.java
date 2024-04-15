package com.lakecat.web.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ColumnResponse {

    private String name;

    private String comment;

    private String type;

    public static ColumnResponse convertColumn(io.lakecat.catalog.common.model.Column column) {
        ColumnResponse build = ColumnResponse.builder().comment(column.getComment()).name(column.getColumnName())
                .type(column.getColType()).build();
        return build;
    }

}