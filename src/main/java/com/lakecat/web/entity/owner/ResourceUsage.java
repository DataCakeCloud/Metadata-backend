package com.lakecat.web.entity.owner;

import lombok.Data;

@Data
public class ResourceUsage {
    private double ownerTaskCount;
    private double avgMemoryUsed;
    private double avgCpuUsed;
}
