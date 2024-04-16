package com.lakecat.web.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.*;
import com.lakecat.web.exception.BusinessException;

import com.lakecat.web.vo.blood.PrivilegeVo;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.RolePrivilege;

public interface IAdminRoleService {
    String grantPrivilegeForPersonage(AdminRoleInfo entity, String[] scope);
    /**
     * 展示所有角色
     *
     * @param filterUserRole 过滤个人用户角色，默认为 true
     * @return
     */
    List <Role> showRoles(Boolean filterUserRole, String userName, String PROJECT_ID);

    /**
     * 创建角色
     *
     * @return
     */
    Boolean createRoleByFe(AdminRoleInfo entity);

    /**
     * @param oldName 旧角色名
     * @param newName 新角色名
     * @param comment 描述信息
     * @return
     */
    String alterRole(String oldName, String newName, String comment, String PROJECT_ID);

    /**
     * @param roleName 角色名称
     * @return
     */
    String dropRole(String roleName, String PROJECT_ID);

    /**
     * @param roleInputs 添加用户对象
     * @return
     */
    String addUsers(RoleInputs roleInputs, String PROJECT_ID);

    /**
     * @return
     */
    String removeUsers(RoleInputs roleInputs, String PROJECT_ID);

    /**
     * @param roleName  角色名称
     * @param projectId 租户名
     * @return
     */
    RolePrivilege[] showPrivileges(String roleName, String projectId, Integer maxResults, String page, String objectName);

    List <RolePrivilege> showPrivilegesByName(String roleName, String objectName, Integer maxResults, String page, String PROJECT_ID);

    Role showUsers(String roleName, String projectId, String pattern);

    List <Role> showRoleByName(String roleName, String projectId, String pattern, Boolean filterUserRole, String userName);

    /**
     * @param roleInputs 添加角色权限对象
     * @return
     */
    String grantPrivilegeToRole(RoleInputs roleInputs, String PROJECT_ID);

    /**
     * @param revokePrivilegeFromRole 移除角色权限对象
     * @return
     */
    String revokePrivilegeFromRole(RevokePrivilegeFromRole revokePrivilegeFromRole, String PROJECT_ID);

    /**
     * @param inputs 钉钉审批对象
     * @return
     */
    String grantPrivilegeToUser(GrantPrivilegeToUser inputs);

    /**
     * @param dingCallBack 钉钉回调对象
     * @return
     */
    String grantPrivilegeToUserCallback(DingCallBack dingCallBack);

    /**
     * @param roleInputs 权限列表
     * @param roleName   角色名称
     * @return
     */
    String revokePrivilegeFromUser(RoleInputs[] roleInputs, String roleName, String PROJECT_ID);

    List <ObjectTypeInfo> objectType();

    Map <String, Set <String>> showNoPrivilege(String projectId, String roleName, String userId, String region);

    PageObjectResp showObjectNames(String objectType, String keyword, Integer pageNum, Integer pageSize, String userId, String tenantName, String pageToekn) throws Exception;

    String batchGrantPrivilegeToUser(PrivilegeVo privilegeVo);


    void syncGrantPrivilege(String PROJECT_ID);

    void syncGrantPrivilegePrecise(String role, String PROJECT_ID);



    //判断角色是否存在
    boolean getRoleOrNot(AdminRoleInfo entity);

    //创建角色
    void createRole(AdminRoleInfo entity);

    //给角色添加用户
    void addUserToRole(AdminRoleInfo entity);

    //创建角色并添加用户
    void createRoleAndAddUser(AdminRoleInfo entity);
}
