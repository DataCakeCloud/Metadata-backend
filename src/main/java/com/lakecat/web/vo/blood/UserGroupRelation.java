package com.lakecat.web.vo.blood;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class UserGroupRelation {
    /**
     * id是主键，自动生成
     */

    private Integer id;
    private Integer userId;//用户id
    private String userName;
    private Integer userGroupId;//用户组id
    private Integer owner; //0不是  1 是
    private Timestamp createTime;
}
