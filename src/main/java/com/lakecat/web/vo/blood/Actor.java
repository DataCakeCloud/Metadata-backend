package com.lakecat.web.vo.blood;

import lombok.Data;

@Data
public class Actor {
    private Integer actorDefinitionId;
    private String name;
    private String region;
    private String configuration;
    private String actorType;
}
