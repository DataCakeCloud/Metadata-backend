package com.lakecat.web.vo.blood;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class RolePrivilegeVo {
    private String privilege;
    private String grantedOn;
    private String name;
    private String uuid;
}
