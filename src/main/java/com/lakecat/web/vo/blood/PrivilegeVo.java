package com.lakecat.web.vo.blood;

import com.lakecat.web.entity.RoleInputs;
import lombok.Data;

/**
 * {
 *   "reason": "6544",
 *   "cycle": "2",
 *   "coApplicants": "",
 *   "userName": "hanzenggui",
 *   "roleInputs": {
 *     "objectName": [
 *       "ue1.ab_sys_prod.ab_all_tc_log:yuanhb"
 *     ],
 *     "operation": [
 *       "查询",
 *       "删除",
 *       "编辑",
 *       "描述"
 *     ]
 *   }
 * }
 *
 *
 * {
 *   "owner": "hanzenggui",
 *   "type": 1,
 *   "flag": 1,
 *   "cycle": "2",
 *   "roleInputs": {
 *     "objectName": [
 *       "shareit_ue1.temp_database.etst1"
 *     ],
 *     "scope": [
 *       "hanzenggui",
 *       "huangkai"
 *     ],
 *     "groupIds": "",
 *     "operation": [
 *       "查询",
 *       "删除",
 *       "描述"
 *     ]
 *   }
 * }
 */
@Data
public class PrivilegeVo {
    private String reason;
    private String cycle;
    private String userName;
    private Integer type;
    private Integer flag;
    private String owner;

    private RoleInputs roleInputs;
}
