package com.lakecat.web.entity.owner;

import lombok.Data;

@Data
public class ActiveTable {
    private double ownerTableCount;
    private double tableCount;
    private double ownerTableRatio;
}
