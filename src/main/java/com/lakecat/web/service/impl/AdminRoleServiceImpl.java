package com.lakecat.web.service.impl;

import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.config.DingDingConfig;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.constant.CommonConts;
import com.lakecat.web.constant.OperationType;
import com.lakecat.web.entity.*;
import com.lakecat.web.mapper.AuthGovMapper;
import com.lakecat.web.mapper.PermissionRecordMapper;
import com.lakecat.web.service.*;
import com.lakecat.web.thread.SyncThread;
import com.lakecat.web.utils.*;
import com.lakecat.web.vo.blood.*;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.RolePrivilege;
import io.lakecat.catalog.common.plugin.request.*;
import io.lakecat.catalog.common.plugin.request.input.RoleInput;
import com.lakecat.web.mapper.TableInfoMapper;
import io.lakecat.catalog.common.plugin.request.base.ProjectRequestBase;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.parquet.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.lakecat.web.utils.DateUtil.date2yyyymmddHHmmss;
import static java.util.stream.Collectors.toList;

import com.lakecat.web.constant.CatalogNameEnum.CloudRegionCatalog;

@Slf4j
@Service
public class AdminRoleServiceImpl implements IAdminRoleService {

    @Autowired
    private AccessGroupService accessGroupService;

    public static final String PRIVILEGE_SINGLE_USER_PREFIX = "privilege_single_user_";

    @Autowired
    private DSUtilForLakecat dsUtilForLakecat;

    @Autowired
    ILakeCatClientService lakeCatClientService;

    @Autowired
    TableInfoMapper tableInfoMapper;


    @Autowired
    DingDingService dingDingService;

    @Autowired
    PermissionRecordMapper permissionMapper;

    @Value("${oa.code}")
    private String code;

    private static Class<?> forName;


    @Autowired
    AuthGovMapper authGovMapper;

    @Autowired
    CatalogNameEnum CatalogNameEnum;


    @Autowired
    ITableInfoService iTableInfoService;

    @Autowired
    DingDingConfig dingDingConfig;

    @Autowired
    private OaService oaService;

    @Autowired
    ILakeCatClientService iLakeCatClientService;

    static {
        try {
            forName = Class.forName("io.lakecat.catalog.client.LakeCatClient");
        } catch (ClassNotFoundException e) {
            log.error("",e);
        }
    }


    public LakeCatClient getLsClient() {
        return lakeCatClientService.get();
    }

    @Override
    public List<Role> showRoles(Boolean filterUserRole, String userName, String PROJECT_ID) {
        try {
            ShowRolesRequest request = getRequest(ShowRolesRequest.class, PROJECT_ID);

            if (!userName.equals("")) {
                request.setUserId(userName);
            }
            Role[] roles = getLsClient().showRoles(request).getObjects();
            ArrayList<Role> rolesList = new ArrayList<>(Arrays.asList(roles));
            filterUserRole(rolesList, filterUserRole);
            return rolesList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void sync(String PROJECT_ID) {

        List<RoleOwnerRelevance> rors = new ArrayList<>();
        List<RoleTableRelevance> rtrs = new ArrayList<>();
        List<Role> roles = showRoles(false, "", PROJECT_ID);

        for (Role role : roles) {
            try {
                String roleName = role.getRoleName().replaceAll("\\.", "_");
                String projectId = role.getProjectId();
                String roleId = role.getRoleId();
                String comment = role.getComment();
                String createdTime = role.getCreatedTime();
                String[] toUsers = role.getToUsers();
                if (toUsers != null) {
                    for (String toUser : toUsers) {
                        RoleOwnerRelevance ror = new RoleOwnerRelevance();
                        ror.setProjectId(projectId);
                        ror.setComment(comment);
                        ror.setRoleId(roleId);
                        ror.setRoleName(roleName);
                        ror.setUserName(toUser);
                        ror.setCreatedTime(createdTime);
                        rors.add(ror);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (CollectionUtils.isNotEmpty(roles)) {
            List<List<RoleOwnerRelevance>> lists = SyncThread.splitList(rors, 1000);

            for (List<RoleOwnerRelevance> list : lists) {
                tableInfoMapper.batchSaveForRoles(list);
            }
        }


        List<RoleOwnerRelevance> roleOwnerRelevances = tableInfoMapper.selectRoles();
        if (CollectionUtils.isNotEmpty(roleOwnerRelevances)) {
            for (RoleOwnerRelevance roleOwnerRelevance : roleOwnerRelevances) {
                try {
                    String roleName = roleOwnerRelevance.getRoleName().replaceAll("\\.", "_");
                    Role role = showUsers(roleName, PROJECT_ID, "");
                    if (role == null) {
                        continue;
                    }
                    RolePrivilege[] rolePrivileges = role.getRolePrivileges();
                    if (rolePrivileges == null || rolePrivileges.length == 0) {
                        continue;
                    }
                    for (RolePrivilege rolePrivilege : rolePrivileges) {

                        String privilege = rolePrivilege.getPrivilege();
                        String grantedOn = rolePrivilege.getGrantedOn();
                        String name = rolePrivilege.getName();
                        RoleTableRelevance rtr = new RoleTableRelevance();
                        rtr.setRoleId(role.getRoleId());
                        rtr.setRoleName(role.getRoleName().replaceAll("\\.", "_"));
                        rtr.setName(name);
                        rtr.setGrantedOn(grantedOn);
                        rtr.setPrivilege(privilege);
                        rtrs.add(rtr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (CollectionUtils.isNotEmpty(rtrs)) {
            List<List<RoleTableRelevance>> rtrList = SyncThread.splitList(rtrs, 1000);
            for (List<RoleTableRelevance> roleTableRelevances : rtrList) {
                tableInfoMapper.batchSaveForTables(roleTableRelevances);
            }
        }

    }


    public void syncPrecise(String roleName, String PROJECT_ID) {

        List<RoleOwnerRelevance> rors = new ArrayList<>();
        List<RoleTableRelevance> rtrs = new ArrayList<>();
        try {
            Role role = showUsers(roleName, PROJECT_ID, "");
            String projectId = role.getProjectId();
            String roleId = role.getRoleId();
            String comment = role.getComment();
            String createdTime = role.getCreatedTime();
            String[] toUsers = role.getToUsers();
            for (String toUser : toUsers) {
                RoleOwnerRelevance ror = new RoleOwnerRelevance();
                ror.setProjectId(projectId);
                ror.setComment(comment);
                ror.setRoleId(roleId);
                ror.setRoleName(roleName);
                ror.setUserName(toUser);
                ror.setCreatedTime(createdTime);
                rors.add(ror);
            }
            List<List<RoleOwnerRelevance>> lists = SyncThread.splitList(rors, 10);
            for (List<RoleOwnerRelevance> list : lists) {
                tableInfoMapper.batchSaveForRoles(list);
            }
            RolePrivilege[] rolePrivileges = role.getRolePrivileges();
            for (RolePrivilege rolePrivilege : rolePrivileges) {

                String privilege = rolePrivilege.getPrivilege();
                String grantedOn = rolePrivilege.getGrantedOn();
                String name = rolePrivilege.getName();
                RoleTableRelevance rtr = new RoleTableRelevance();
                rtr.setRoleId(role.getRoleId());
                rtr.setRoleName(role.getRoleName());
                rtr.setName(name);
                rtr.setGrantedOn(grantedOn);
                rtr.setPrivilege(privilege);
                rtrs.add(rtr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<List<RoleTableRelevance>> rtrList = SyncThread.splitList(rtrs, 10);
        for (List<RoleTableRelevance> roleTableRelevances : rtrList) {
            tableInfoMapper.batchSaveForTables(roleTableRelevances);
        }
    }


    /**
     * 构造SDK请求体
     *
     * @param tClass     请求体类型
     * @param tenantName 租户名
     * @param <T>
     * @return 返回SDK请求对象
     */
    private <T extends ProjectRequestBase> T getRequest(Class<T> tClass, String tenantName) {
        T t = null;
        try {
            t = tClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert t != null;
        t.setProjectId(tenantName);
        return t;
    }

    /**
     * 剔除个人用户角色
     *
     * @param roles
     * @param filterUserRole
     */
    private void filterUserRole(List<Role> roles, Boolean filterUserRole) {
        log.info("roles: {}", roles);
        if (filterUserRole) {
            Iterator<Role> iterator = roles.iterator();
            Role next;
            while (iterator.hasNext()) {
                next = iterator.next();
                if (next.getRoleName().startsWith(PRIVILEGE_SINGLE_USER_PREFIX)) {
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public Boolean createRoleByFe(AdminRoleInfo entity) {
        try {
            if (entity.getRoleName() == null) {
                return null;
            }
            if (entity.getComment() == null) {
                entity.setComment("创建角色");
            }
            createRole(entity);
            return true;
        } catch (Exception e) {
            log.warn("Exception: {}", e.getCause().getMessage());
        }
        return false;
    }

    /**
     * 多个region lakecat 服务
     *
     * @param methodName
     * @param request
     */
    private void multiServerClientSupport(String methodName, Object request) {
        LakeCatClient lakeCatClient = getLsClient();
        try {
            Class<?> aClass = request.getClass();
            Method method = forName.getMethod(methodName, aClass);
            log.info("request-->{}",GsonUtil.toJson(request,false));
            method.invoke(lakeCatClient, new Object[]{request});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String alterRole(String oldName, String newName, String comment, String PROJECT_ID) {
        try {
            if (comment == null) {
                comment = "修改角色名";
            }
            AlterRoleRequest request = getRequest(AlterRoleRequest.class, PROJECT_ID);
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectName("");
            roleInput.setObjectType("");
            roleInput.setOperation(Operation.ALTER_ROLE);
            roleInput.setOwnerUser("");
            roleInput.setRoleName(newName);
            roleInput.setComment(comment);
            roleInput.setUserId(new String[]{});
            request.setInput(roleInput);
            request.setRoleName(oldName);
            log.info("alterRole roleInput: {}", roleInput);
            multiServerClientSupport("alterRole", request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String dropRole(String roleName, String PROJECT_ID) {
        try {
            log.info("dropRole roleName: {}", roleName);
            DropRoleRequest request = getRequest(DropRoleRequest.class, PROJECT_ID);
            request.setRoleName(roleName);
            multiServerClientSupport("dropRole", request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String addUsers(RoleInputs roleInputs, String tenantName) {
        /**
         * 给角色添加用户
         * roleName   角色名
         * userId     用户集合
         * tenantName 租户名
         *
         */
        AdminRoleInfo entity = new AdminRoleInfo();
        entity.setRoleName(roleInputs.getRoleName());
        entity.setUserId(roleInputs.getUserIds());
        if (StringUtils.isNoneBlank(roleInputs.getGroupIds())){
            List<String> userIds=accessGroupService.getUsersByGroupIds(roleInputs.getGroupIds());
            if (CollectionUtils.isNotEmpty(userIds)){
                entity.setUserId(userIds.toArray(new String[0]));
            }
        }
        entity.setTenantName(tenantName);
        if (entity.getUserId()!=null&&entity.getUserId().length>0){
            addUserToRole(entity);
        }
        return null;
    }

    @Override
    public String removeUsers(RoleInputs roleInputs, String PROJECT_ID) {
        try {
            log.info("removeUsers roleInputs: {}", roleInputs);
            AlterRoleRequest request = getRequest(AlterRoleRequest.class, PROJECT_ID);
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectName("");
            roleInput.setObjectType("");
            roleInput.setOperation(Operation.ALTER_ROLE);
            roleInput.setOwnerUser("");
            roleInput.setRoleName(roleInputs.getRoleName());
            roleInput.setUserId(roleInputs.getUserIds());
            request.setInput(roleInput);
            log.info("removeUsers roleInput: {}", roleInput);
            multiServerClientSupport("revokeRoleFromUser", request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public RolePrivilege[] showPrivileges(String roleName, String projectId, Integer maxResults, String page, String objectName) {

        log.info("showPrivileges roleName: {}", roleName);
        Role roleObject = getRole(projectId, roleName);
        log.info("roleObject: {}", roleObject);
        if (roleObject == null) {
            return null;
        }
        return roleObject.getRolePrivileges();
    }

    @Override
    public List<RolePrivilege> showPrivilegesByName(String roleName, String objectName, Integer maxResults, String page, String PROJECT_ID) {
        try {
            log.info("showPrivileges roleName: {}", roleName);
            List rolesRegion = new ArrayList();
            Role roleObject = getRole(PROJECT_ID, roleName);
            if (roleObject == null) {
                return rolesRegion;
            }
            RolePrivilege[] roles = roleObject.getRolePrivileges();
            for (int ro = 0; ro < roles.length; ro++) {
                if (roles[ro].getGrantedOn().equals("DATABASE") || roles[ro].getGrantedOn().equals("TABLE")) {
                    Map role = new HashMap();
                    role.put("privilege", roles[ro].getPrivilege());
                    role.put("grantedOn", roles[ro].getGrantedOn());
                    role.put("name", roles[ro].getName());
                    String regionName = roles[ro].getName().split("\\.")[0];
                    String regionRoleName = CatalogNameEnum.get(regionName).getCnName();
                    role.put("regionRoleName", regionRoleName);
                    rolesRegion.add(role);
                } else if (roles[ro].getGrantedOn().equals("CATALOG")) {
                    Map role = new HashMap();
                    role.put("privilege", roles[ro].getPrivilege());
                    role.put("grantedOn", "REGION");
                    role.put("name", roles[ro].getName());
                    String regionName = roles[ro].getName();
                    String regionRoleName = CatalogNameEnum.get(regionName).getCnName();
                    role.put("regionRoleName", regionRoleName);
                    rolesRegion.add(role);
                }
            }
            return rolesRegion;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Role showUsers(String roleName, String projectId, String pattern) {
        return getRole(projectId, roleName);
    }

    private Role getRole(String projectId, String roleName) {
        try {
            GetRoleRequest request = getRequest(GetRoleRequest.class, projectId);
            request.setRoleName(roleName);
            return getLsClient().getRole(request);
        } catch (Exception e) {
        }
        return null;
    }


    public PagedList<Role> showRoles(String projectId, String userId) {
        try {
            ShowRolesRequest request = new ShowRolesRequest();
            request.setProjectId(projectId);
            request.setUserId(userId);
            return getLsClient().showRoles(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

   /* @Override
    public List<String> getRoleNameListByUserName(String projectId, String userId) {
        List<String> list = new ArrayList<>();
        try {
            PagedList<Role> rolePagedList = showRoles(projectId, userId);
            Role[] objects = rolePagedList.getObjects();
            for (Role roleObject : objects) {
                String roleName = roleObject.getRoleName();
                list.add(roleName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }*/

    @Override
    public List<Role> showRoleByName(String roleName, String projectId, String pattern, Boolean filterUserRole, String userName) {
        if (roleName != null) {
            log.info("showUsers roleName: {}", roleName);
            List<Role> showRoles = showRoles(filterUserRole, userName, projectId);
            List<Role> roleByNameList = new ArrayList<>();
            for (int ro = 0; ro < showRoles.size(); ro++) {
                if (showRoles.get(ro).getRoleName().contains(roleName)) {
                    roleByNameList.add(showRoles.get(ro));
                }
            }
            return roleByNameList;
        } else {
            return showRoles(filterUserRole, userName, projectId);
        }
    }


    @Override
    public boolean getRoleOrNot(AdminRoleInfo entity) {
        log.info("showUsers roleName: {}", entity.getRoleName());
        try {
            GetRoleRequest request = new GetRoleRequest();
            request.setRoleName(entity.getRoleName());
            request.setProjectId(entity.getTenantName());
            Role role = getLsClient().getRole(request);
            if (role != null) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public String grantPrivilegeToRole(RoleInputs roleInputs, String projectId) {
        try {
            String comment="operUser="+InfTraceContextHolder.get().getUserName()+",applyUser="+InfTraceContextHolder.get().getUserName();;
            try {
                String userGroupName=tableInfoMapper.selectUserGroupName(roleInputs.getRoleName());

                if (StringUtils.isNoneBlank(userGroupName)){
                    comment=comment+",userGroup="+userGroupName;
                }
            }catch (Exception e){
                log.error("",e);
            }
            String[] objectNames = roleInputs.getObjectNames();
            String roleName = roleInputs.getRoleName();
            String[] operationsString = roleInputs.getOperation();
            List operations = Arrays.asList(operationsString);
            //数据区域
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            Map<String, CloudRegionCatalog> regionInfo = userInfo.getRegionInfo();
            boolean authDataSource = false;//是否是数据源
            if (roleInputs.getOperation() != null && roleInputs.getOperation().length > 0) {
                for (String o : roleInputs.getOperation()) {
                    if (o.equals("访问数据源") || o.equals("编辑数据源") || o.equals("删除数据源")) {
                        authDataSource = true;
                        break;
                    }
                }
            }
            if (authDataSource) {
                for (int i = 0; i <= objectNames.length - 1; i++) {
                    for (String s:roleInputs.getOperation()){
                        AlterRoleRequest request = getRequest(AlterRoleRequest.class, projectId);
                        RoleInput roleInput = new RoleInput();
                        roleInput.setObjectType(ObjectType.CATALOG.name());
                        roleInput.setObjectName(objectNames[i]);
                        if (s.equals("访问数据源")){
                            roleInput.setOperation(Operation.DESC_CATALOG);
                        }else if (s.equals("编辑数据源")){
                            roleInput.setOperation(Operation.ALTER_CATALOG);
                        }else {
                            roleInput.setOperation(Operation.DROP_CATALOG);
                        }
                        roleInput.setRoleName(roleName);
                        roleInput.setComment(comment);
                        request.setInput(roleInput);
                        multiServerClientSupport("grantPrivilegeToRole", request);
                    }
                }
            }
            else if (StringUtils.countMatches(objectNames[0],".")==0) {
                for (int i = 0; i <= objectNames.length - 1; i++) {
                    AlterRoleRequest request = getRequest(AlterRoleRequest.class, projectId);
                    RoleInput roleInput = new RoleInput();
                    roleInput.setObjectType(ObjectType.CATALOG.name());
                    roleInput.setObjectName(regionInfo.get(objectNames[i]).getCatalogName());
                    roleInput.setOperation(Operation.CREATE_DATABASE);
                    roleInput.setRoleName(roleName);
                    roleInput.setComment(comment);
                    request.setInput(roleInput);
                    multiServerClientSupport("grantPrivilegeToRole", request);
                }
            }
            //数据库
            else if (StringUtils.countMatches(objectNames[0],".")==1) {
                //挨个取数据库名称
                for (int i = 0; i <= objectNames.length - 1; i++) {
                    AlterRoleRequest request = getRequest(AlterRoleRequest.class, projectId);
                    RoleInput roleInput = new RoleInput();
                    roleInput.setObjectType(ObjectType.DATABASE.name());
                    if (InfTraceContextHolder.gcp()){
                        roleInput.setObjectName(objectNames[i]);
                    }else {
                        roleInput.setObjectName("shareit_" + objectNames[i]);
                    }
//                    roleInput.setObjectName(objectNames[i]);
                    roleInput.setRoleName(roleName);
                    roleInput.setComment(comment);
                    roleInput.setOwnerUser("");
                    roleInput.setUserId(new String[]{});
                    //挨个取该角色要添加的权限，一个一个调接口
                    for (int j = 0; j <= operations.size() - 1; j++) {
                        if (operations.get(j).equals("修改库")) {
                            roleInput.setOperation(Operation.ALTER_DATABASE);
                        } else if (operations.get(j).equals("删除库")) {
                            roleInput.setOperation(Operation.DROP_DATABASE);
                        } else if (operations.get(j).equals("描述库")) {
                            roleInput.setOperation(Operation.DESC_DATABASE);
                        } else if (operations.get(j).equals("创建表")) {
                            roleInput.setOperation(Operation.CREATE_TABLE);
                        }
                        request.setRoleName(roleName);
                        request.setInput(roleInput);
                        multiServerClientSupport("grantPrivilegeToRole", request);
                    }
                }
            }
            //数据表
            else if (StringUtils.countMatches(objectNames[0],".")==2) {
                //挨个数据表名称
                List<String> tableList= new ArrayList<>();
                for (int i = 0; i <= objectNames.length - 1; i++) {
                    AlterRoleRequest request = getRequest(AlterRoleRequest.class, projectId);
                    RoleInput roleInput = new RoleInput();
                    roleInput.setObjectType(ObjectType.TABLE.name());
                    tableList.add(objectNames[i]);
                    if (InfTraceContextHolder.gcp()){
                        roleInput.setObjectName(objectNames[i]);
                    }else {
                        roleInput.setObjectName("shareit_" + objectNames[i]);
                    }
//                    roleInput.setObjectName(objectNames[i]);
                    roleInput.setRoleName(roleName);
                    for (int j = 0; j <= operations.size() - 1; j++) {
                        if (operations.get(j).equals("修改表")) {
                            roleInput.setOperation(Operation.ALTER_TABLE);
                        } else if (operations.get(j).equals("删除表")) {
                            roleInput.setOperation(Operation.DROP_TABLE);
                        } else if (operations.get(j).equals("描述表")) {
                            roleInput.setOperation(Operation.DESC_TABLE);
                        } else if (operations.get(j).equals("查询数据")) {
                            roleInput.setOperation(Operation.SELECT_TABLE);
                        } else if (operations.get(j).equals("插入数据")) {
                            roleInput.setOperation(Operation.INSERT_TABLE);
                        }
                        roleInput.setComment(comment);
                        request.setInput(roleInput);
                        log.info("grantPrivilegetToRole-->{}", GsonUtil.toJson(request,false));
                        multiServerClientSupport("grantPrivilegeToRole", request);
                    }
                }
                PermissionRecordInfo insert = new PermissionRecordInfo();
                insert.setType("权限授予");
                insert.setTableList(StringUtils.join(tableList, ","));
                insert.setPermission(StringUtils.join(operations, ","));
                insert.setGrantUser(roleName);
                insert.setCertigier(InfTraceContextHolder.get().getUserName());
                insert.setFlag("1");
                insert.setGrantType(1);
                insert.setReason("权限授予");
                insert.setStatus(0);
                insert.setTableRecoveryState(processTableListStatus(tableList));
                insert.setCycle("5");
                insert.setCreateTime(date2yyyymmddHHmmss(0));
                insert.setUpdateTime(date2yyyymmddHHmmss(0));
                permissionMapper.insertForOrder(insert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String processTableListStatus(List<String> tableList) {
        if (tableList == null || tableList.isEmpty()) {
            return "";
        }
        List<String> collect = tableList.stream().map(data -> {
            if (data.contains(":")) {
                return data.split(":")[0] + ":0";
            }
            return data + ":0";
        }).collect(toList());
        return StringUtils.join(collect, ",");
    }

    @Override
    public String revokePrivilegeFromRole(RevokePrivilegeFromRole revokePrivilegeFromRole, String projectId) {
        //先用rolename查询该角色权限，然后我这删掉之后，在调用更改角色接口给传回去
        try {
            log.info("revokePrivilegeFromRole revokePrivilegeFromRole: {}", JSON.toJSONString(revokePrivilegeFromRole));
            String comment="operUser="+InfTraceContextHolder.get().getUserName()+",applyUser="+InfTraceContextHolder.get().getUserName();
            try {
                String userGroupName=tableInfoMapper.selectUserGroupName(revokePrivilegeFromRole.getRoleName());
                if (StringUtils.isNoneBlank(userGroupName)){
                    comment=comment+",userGroup="+userGroupName;
                }
            }catch (Exception e){
                e.printStackTrace();
                log.error("获取用户组列表失败",e);
            }
            for (int i = 0; i <= revokePrivilegeFromRole.getRoleInputs().length - 1; i++) {
                String objectname = revokePrivilegeFromRole.getRoleInputs()[i].getObjectName()[0];
                String[] operation = revokePrivilegeFromRole.getRoleInputs()[i].getOperation();
                String objectType = revokePrivilegeFromRole.getRoleInputs()[i].getObjectType();
                for (int j = 0; j <= operation.length - 1; j++) {
                    AlterRoleRequest request = getRequest(AlterRoleRequest.class, projectId);
                    RoleInput roleInput = new RoleInput();
                    roleInput.setObjectType(objectType);
                    roleInput.setRoleName(revokePrivilegeFromRole.getRoleName());
                    roleInput.setObjectName(objectname);
                    if (operation[j].equals("修改表")) {
                        roleInput.setOperation(Operation.ALTER_TABLE);
                    } else if (operation[j].equals("删除表")) {
                        roleInput.setOperation(Operation.DROP_TABLE);
                    } else if (operation[j].equals("描述表")) {
                        roleInput.setOperation(Operation.DESC_TABLE);
                    } else if (operation[j].equals("查询数据")) {
                        roleInput.setOperation(Operation.SELECT_TABLE);
                    } else if (operation[j].equals("插入数据")) {
                        roleInput.setOperation(Operation.INSERT_TABLE);
                    } else if (operation[j].equals("修改库")) {
                        roleInput.setOperation(Operation.ALTER_DATABASE);
                    } else if (operation[j].equals("删除库")) {
                        roleInput.setOperation(Operation.DROP_DATABASE);
                    } else if (operation[j].equals("描述库")) {
                        roleInput.setOperation(Operation.DESC_DATABASE);
                    } else if (operation[j].equals("创建表")) {
                        roleInput.setOperation(Operation.CREATE_TABLE);
                    } else if (operation[j].equals("创建库")) {
                        roleInput.setOperation(Operation.CREATE_DATABASE);
                    }else if (operation[j].equals("访问数据源")) {
                        roleInput.setOperation(Operation.DESC_CATALOG);
                    }else if (operation[j].equals("编辑数据源")) {
                        roleInput.setOperation(Operation.ALTER_CATALOG);
                    }else if (operation[j].equals("删除数据源")) {
                        roleInput.setOperation(Operation.DROP_CATALOG);
                    }
                    roleInput.setComment(comment);
                    request.setInput(roleInput);
                    log.info("revokePrivilegeFromRole-->{}",GsonUtil.toJson(request,false));
                    multiServerClientSupport("revokePrivilegeFromRole", request);
                }
                //更新申请记录的状态
                if (revokePrivilegeFromRole.getPermissionTableId() != null && revokePrivilegeFromRole.getPermissionTableId() > 0) {
                    PermissionRecordInfo permissionRecordInfo = permissionMapper.selectById(revokePrivilegeFromRole.getPermissionTableId());
                    String tableRecoveryState = permissionRecordInfo.getTableRecoveryState();
                    if (StringUtils.isNotEmpty(tableRecoveryState)) {
                        tableRecoveryState = processTableListStatus(Arrays.asList(permissionRecordInfo.getTableList().split(",")));
                    }
                    List<PermissionRecordInfo> permissionRecordInfos = processRecoveryState(tableRecoveryState);

                    List<String> collect = permissionRecordInfos.stream().map(data -> {
                        if (data.getObjectName().equals(objectname)) {
                            data.setRecoveryState("1");
                        }
                        return data.getObjectName() + ":" + data.getRecoveryState();
                    }).collect(toList());

                    String tableRecoveryStateRes = StringUtils.join(collect, ",");
                    permissionMapper.updateRecoveryState(tableRecoveryStateRes, revokePrivilegeFromRole.getPermissionTableId());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.error("删除权限失败",e);
        }
        return null;
    }

    public List<PermissionRecordInfo> processRecoveryState(String tableRecoveryState) {
        if (StringUtils.isEmpty(tableRecoveryState)) {
            return new ArrayList<>();
        }
        List<PermissionRecordInfo> permissionRecordInfos = new ArrayList<>();
        String[] split = tableRecoveryState.split(",");
        for (String tableStatus : split) {
            String[] split1 = tableStatus.split(":");
            PermissionRecordInfo permissionRecordInfo = new PermissionRecordInfo();
            permissionRecordInfo.setObjectName(split1[0]);
            permissionRecordInfo.setRecoveryState(split1[1]);
            permissionRecordInfos.add(permissionRecordInfo);
        }
        return permissionRecordInfos;
    }


    @Override
    public String grantPrivilegeToUserCallback(DingCallBack dingCallBack) {
        try {
            if (dingCallBack.getStatus().equals("COMPLETED") && dingCallBack.getResult().equals("agree")) {
                log.info("grantPrivilegeToUserCallback dingCallBack: {}", dingCallBack);
                String owner = dingCallBack.getFormComponentValues().get(1).get("value").toString();
                log.info("grantPrivilegeToUserCallback roleName: {}", owner);
                String comment = dingCallBack.getFormComponentValues().get(2).get("value").toString();
                String value = dingCallBack.getFormComponentValues().get(3).get("value").toString();
                String[] split = value.split(";");
                String workOrder = split[0].split(",")[0];
                String workOrderId = split[0].split(",")[1];
                AdminRoleInfo entity = new AdminRoleInfo();
                if (workOrder.contains("权限授予")) {
                    String tableName = split[1].split(":")[1];
                    String userId = split[2].split(":")[1];
                    String operations = split[3].split(":")[1];
                    String flag = split[4].split(":")[1];
                    String type = split[5].split(":")[1];
                    String cycle = split[6].split(":")[1];
                    String reason = split[7].split(":")[1];
                    entity.setTenantName("shareit");
                    entity.setType(CommonParameters.typeMap.get(type));
                    entity.setOrderType("权限授予");
                    entity.setWorkOrderId(workOrderId);
                    entity.setReason(reason);
                    entity.setCycle(CommonParameters.cycleIntMap.get(cycle));
                    entity.setFlag(flag);
                    entity.setOwner(owner);
                    entity.setOpt("授予");
                    entity.setObjectNames(tableName.split(","));
                    entity.setOperation(operations.split(","));
                    return grantPrivilegeForPersonage(entity, userId.split(","));
                } else {
                    String[] objectNames = split[1].split(":")[1].split(",");
                    String[] operations = split[2].split(":")[1].split(",");
                    String cycle = split[3].split(":")[1];
                    String reason = split[4].split(":")[1];
                    String roleName = "privilege_single_user_" + owner.replaceAll("\\.", "_");
                    //构建请求体
                    String[] userId = {owner};
                    entity.setRoleName(roleName);
                    entity.setTenantName("shareit");
                    entity.setType(1);
                    entity.setOrderType("权限申请");
                    entity.setWorkOrderId(workOrderId);
                    entity.setReason(reason);
                    entity.setCycle(CommonParameters.cycleIntMap.get(cycle));
                    entity.setFlag("否");
                    entity.setOpt("申请");
                    entity.setOwner(owner);
                    entity.setObjectNames(objectNames);
                    entity.setOperation(operations);
                    return grantPrivilegeForPersonage(entity, userId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public void alertForOneTableAndOnePer(AdminRoleInfo entity) {
        JobAlertReq req = new JobAlertReq();
        req.setUserName(entity.getUserName());
        req.setTableName(entity.getObjectName());
        req.setPermission(Strings.join(entity.getOperation(), ","));
        req.setOwner(entity.getOwner());
        alert(req);
    }

    /**
     * 给角色赋多个表的权限
     * roleName 角色名
     * userNames 用户集合
     * operations 操作权限集合
     * objectNames 表列表
     * tenantName 租户名
     * reason 原因
     * cycle 周期
     *
     * @param entity 周期
     */
    public void moreTablePrivilegeForRole(AdminRoleInfo entity) {
        String[] objectNames = entity.getObjectNames();
        for (int ina = 0; ina < objectNames.length; ina++) {
            String objectName = objectNames[ina].trim();
            if (objectName.matches("(.*)\\.(.*)\\.(.*)")) {
                entity.setObjectName(objectName);
                setMoreUserPermissions(entity);//
                insertAuthRecord(entity);
                insertForOrder(entity);
                //开始发起通知 通知该表的权限已经赋予给了该用户
                if ("是".equals(entity.getFlag())) {
                    //alertForOneTableAndOnePer(entity);
                }
            }
        }
    }


    /**
     * 给单个用户/角色或者多个用户/角色赋权限 需要生产角色名
     *
     * @param scope  用户集合
     * @param entity 权限类型
     */
    public String grantPrivilegeForPersonage(AdminRoleInfo entity, String[] scope) {
        for (String role : scope) {
            if (role == null) {
                continue;
            }
            /*if (1 == entity.getType()) {
                roleName = PRIVILEGE_SINGLE_USER_PREFIX + trim.replaceAll("\\.", "_");
            } else {
                roleName = userName.toString().trim();
            }*/
            entity.setRoleName(role);
            entity.setUserId(new String[]{entity.getUserName()});
            moreTablePrivilegeForRole(entity);
        }
        return null;
    }


    private void insertAuthRecord(AdminRoleInfo entity) {
        //插入权限记录表
        //objectName, reason, StringUtils.join(operations, ","), "", roleName, cycle, "赋予"
        AuthorityGovInfo authorityGovInfo = AuthorityGovInfo.builder().build()
                .setOperator("系统")
                .setTableName(entity.getObjectName())
                .setOperate(entity.getOpt())
                .setReason(entity.getReason())
                .setPermission(StringUtils.join(entity.getOperation(), ","))
                .setUserName(entity.getUserName())
                .setOperatedUser(entity.getRoleName())
                .setExecuteStatus(1)
                .setCycle(entity.getCycle());
        authGovMapper.insertForRecord(authorityGovInfo);
    }


    private void insertForOrder(AdminRoleInfo entity) {
        try {
            if (!"已授予".equals(entity.getOpt())){
                PermissionRecordInfo insert = new PermissionRecordInfo();
                insert.setType(entity.getOrderType());
                insert.setOrderId(entity.getWorkOrderId());
                insert.setTableList(processTableList(entity.getObjectName()));
                insert.setPermission(StringUtils.join(entity.getOperation(), ","));
                insert.setApplyUser(entity.getOwner());
                insert.setGrantUser(StringUtils.join(entity.getUserId(), ","));
                insert.setFlag(entity.getFlag());
                insert.setGrantType(entity.getType());
                //这个授权人改为第一审批人
                insert.setCertigier(entity.getSqrleader());
                insert.setStatus(0);
                if (StringUtils.isNotEmpty(entity.getObjectName())) {
                    insert.setTableRecoveryState(processTableListStatus(Arrays.asList(entity.getObjectName().split(","))));
                }
                if (entity.getCycle() != null && StringUtils.isNotEmpty(entity.getCycle().toString())) {
                    insert.setCycle(entity.getCycle().toString());
                } else {
                    insert.setCycle("5");
                }
                insert.setReason(entity.getReason());
                insert.setCreateTime(date2yyyymmddHHmmss(0));
                insert.setUpdateTime(date2yyyymmddHHmmss(0));
                permissionMapper.insertForOrder(insert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String processTableList(String objectLists) {
        if (StringUtils.isNotEmpty(objectLists)) {
            List<String> list = Arrays.asList(objectLists.split(","));
            List<String> collect = list.stream().map(data -> {
                if (data.contains(":")) {
                    data = data.split(":")[0];
                }
                return data;
            }).collect(toList());

            return StringUtils.join(collect, ",");
        }
        return null;
    }


    /**
     * 创建角色
     * roleName   角色名称
     * tenantName 租户名
     *
     * @param entity 角色名称
     */
    @Override
    public void createRole(AdminRoleInfo entity) {
        log.info("-------------create role -------------");
        CreateRoleRequest requestCreate = getRequest(CreateRoleRequest.class, entity.getTenantName());
        RoleInput roleInputCreate = new RoleInput();
        roleInputCreate.setOwnerUser("系统");
        roleInputCreate.setRoleName(entity.getRoleName());
        requestCreate.setInput(roleInputCreate);
        multiServerClientSupport("createRole", requestCreate);
    }


    /**
     * 给角色添加用户
     * roleName   角色名
     * userId     用户集合
     * tenantName 租户名
     *
     * @param entity 角色名
     */
    @Override
    public void addUserToRole(AdminRoleInfo entity) {
        AlterRoleRequest request = new AlterRoleRequest();
        RoleInput roleInput = new RoleInput();
        roleInput.setUserId(entity.getUserId());
        roleInput.setRoleName(entity.getRoleName());
        request.setInput(roleInput);
        request.setProjectId(entity.getTenantName());
        request.setRoleName(entity.getRoleName());
        log.info("addUsers roleInput: {}", roleInput);
        multiServerClientSupport("grantRoleToUser", request);
    }

    /**
     * 给角色赋表的权限
     *
     * @param entity 请求体 包括roleName objectName operations tenantName
     * @return
     */
    public long toTablePrivilegeForRole(AdminRoleInfo entity) {
        AlterRoleRequest requestAlter = new AlterRoleRequest();
        requestAlter.setProjectId(entity.getTenantName());
        requestAlter.setRoleName(entity.getRoleName());
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectName(entity.getObjectName());
        roleInput.setObjectType(ObjectType.TABLE.name());
        roleInput.setComment(entity.getComment());
        log.info(" toTablePrivilegeForRole entity is :" + entity);
        List list = Arrays.asList(entity.getOperation());
        return list.stream().filter(Objects::nonNull).map(x -> {
            Operation operation = CommonParameters.operation.get(x.toString().trim());
            roleInput.setOperation(operation);
            roleInput.setRoleName(entity.getRoleName());
            requestAlter.setInput(roleInput);
            setPrivilegeToRole(requestAlter);
            return true;
        }).count();
    }


    @Override
    public void createRoleAndAddUser(AdminRoleInfo entity) {
        //第一步创建角色
        createRole(entity);
        //第二步给角色添加用户
        addUserToRole(entity);
    }


    /**
     * 创建角色添加用户并赋权限
     * roleName   角色名
     * userNames  需要添加的用户
     * objectName 赋权结构体
     * operations 操作类型
     * tenantName 租户名
     *
     * @param entity 角色名
     */
    public void createRoleAndToPrivilege(AdminRoleInfo entity) {
        //第一步创建角色
        createRole(entity);
        //第二步给角色添加用户
        addUserToRole(entity);
        //给角色赋权
        toTablePrivilegeForRole(entity);

    }


    public void setMoreUserPermissions(AdminRoleInfo entity) {
        //boolean roleOrNot = getRoleOrNot(entity);
        toTablePrivilegeForRole(entity);
        /*if (roleOrNot) {
        } else {
            //创建角色并赋权
            createRoleAndToPrivilege(entity);
        }*/
    }

    /**
     * 给角色赋权 执行操作层
     *
     * @param requestAlter 请求对象
     */
    public void setPrivilegeToRole(AlterRoleRequest requestAlter) {
        try {
            multiServerClientSupport("grantPrivilegeToRole", requestAlter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String batchGrantPrivilegeToUser(PrivilegeVo privilegeVo) {
        AdminRoleInfo entity = new AdminRoleInfo();
        entity.setUserName(InfTraceContextHolder.get().getUserName());
        entity.setTenantName(InfTraceContextHolder.get().getTenantName());
        entity.setType(CommonParameters.typeMap.get(privilegeVo.getType()));
        entity.setOrderType("权限授予");
        entity.setWorkOrderId(UUID.randomUUID().toString());
        entity.setReason(privilegeVo.getReason());
        entity.setCycle(CommonParameters.cycleIntMap.get(privilegeVo.getCycle()));
        entity.setFlag(privilegeVo.getFlag().toString());
        entity.setOwner(privilegeVo.getOwner());
        entity.setOpt("授予");
        entity.setReason("权限授予");
        entity.setObjectNames(privilegeVo.getRoleInputs().getObjectName());
        entity.setOperation(privilegeVo.getRoleInputs().getOperation());
        return grantPrivilegeForPersonage(entity, privilegeVo.getRoleInputs().getScope());
    }

    @Override
    public void syncGrantPrivilege(String PROJECT_ID) {
        sync(PROJECT_ID);
    }

    @Override
    public void syncGrantPrivilegePrecise(String role, String PROJECT_ID) {
        syncPrecise(role, PROJECT_ID);
    }

    public boolean alert(JobAlertReq req) {
        String tableName = req.getTableName();
        String permission = req.getPermission();
        List<String> list = new ArrayList<>();
        String owner = req.getOwner();
        String userName = req.getUserName();
        String alertMarkdown = "### [权限中心 权限授予通知]\n";
        alertMarkdown += String.format("%s您好，%s的%s权限已经由%s赋予给了您，如有问题请联系%s。  \n", userName, tableName, permission, owner, owner);
        alertMarkdown += String.format("**权限授权发起时间**:%s  \n", date2yyyymmddHHmmss(0));
        list.add(userName);
        dingDingService.notify(list, alertMarkdown);
        return true;
    }


    public boolean alertForApply(JobAlertReq req) {

        List<String> list = new ArrayList<>();
        String owner = req.getOwner();
        String url = req.getUrl();
        String alertMarkdown = "### [权限中心 权限审批通知]\n";
        alertMarkdown += String.format("%s您好，您的数据权限申请流程已开始，可以点击\n [>>](%s)  \n", owner, url);
        alertMarkdown += String.format("查看,\n");
        alertMarkdown += String.format("**权限授权发起时间**:%s  \n", date2yyyymmddHHmmss(0));
        list.add(req.getOwner());
        dingDingService.notify(list, alertMarkdown);
        return true;
    }

    //发起审批
    @Override
    public String grantPrivilegeToUser(GrantPrivilegeToUser inputs) {
        oaService.sendOaRequest(inputs);


//        AdminRoleInfo entity=new AdminRoleInfo();
//        //构建请求体
//        String[] userId = {InfTraceContextHolder.get().getUserName()};
//        entity.setRoleName(inputs.getRoleInputs().getScope()[0]);
//        entity.setTenantName(InfTraceContextHolder.get().getTenantName());
//        entity.setType(1);
//        entity.setOrderType("权限申请");
//        entity.setWorkOrderId(UUID.randomUUID().toString());
//        entity.setReason(inputs.getReason());
//        entity.setCycle(CommonParameters.cycleIntMap.get(inputs.getCycle()));
//        entity.setFlag("否");
//        entity.setOpt("申请");
//        entity.setOwner(inputs.getOwner());
//        entity.setObjectNames(inputs.getRoleInputs().getObjectName());
//        entity.setOperation(inputs.getRoleInputs().getOperation());
//        return grantPrivilegeForPersonage(entity, inputs.getRoleInputs().getScope());
        return "";
    }



    @Override
    public String revokePrivilegeFromUser(RoleInputs[] roleInputs, String roleName, String PROJECT_ID) {
        try {
            String roleNameOwner = "privilege_single_user_" + roleName.replaceAll("\\.", "_");
            GetRoleRequest request = getRequest(GetRoleRequest.class, PROJECT_ID);
            request.setRoleName(roleNameOwner);
            Role roles = getLsClient().getRole(request);
            //有自己专属的组
            if (roles != null) {
                ArrayList objectNames = new ArrayList<>();
                ArrayList operations = new ArrayList<>();
                RolePrivilege[] rolesPrivileges = roles.getRolePrivileges();

                for (int i = 0; i <= roleInputs.length - 1; i++) {
                    for (int j = 0; j <= rolesPrivileges.length - 1; j++) {
                        if (roleInputs[i].getObjectName()[0] != rolesPrivileges[j].getName()) {
                            objectNames.add(rolesPrivileges[j].getName());
                            operations.add(rolesPrivileges[j].getPrivilege());
                        } else {
                            for (int r = 0; r <= roleInputs[i].getOperation().length - 1; r++) {
                                //汉字，要在弄个map取对应operation
                                if (roleInputs[i].getOperation()[r] != rolesPrivileges[j].getPrivilege()) {
                                    objectNames.add(rolesPrivileges[j].getName());
                                    operations.add(rolesPrivileges[j].getPrivilege());
                                }
                            }
                        }
                    }
                }
                //把剩下的所有权限依次写入角色权限
                for (int in = 0; in <= objectNames.size() - 1; in++) {
                    AlterRoleRequest requestInput = getRequest(AlterRoleRequest.class, PROJECT_ID);
                    RoleInput roleInput = new RoleInput();
                    roleInput.setRoleName(roleNameOwner);
                    roleInput.setObjectName(objectNames.get(in).toString());
                    roleInput.setOperation(Operation.valueOf(operations.get(in).toString()));
                    multiServerClientSupport("alterRole", requestInput);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<ObjectTypeInfo> objectType() {
        return getAllObjectType();
    }

    private List<ObjectTypeInfo> getAllObjectType() {
        List<ObjectTypeInfo> list = new ArrayList<>();
        ObjectTypeInfo objectTypeInfo;
        List<OperationTypeResp> operations;
        for (com.lakecat.web.constant.ObjectType objectType : com.lakecat.web.constant.ObjectType.values()) {
            objectTypeInfo = new ObjectTypeInfo();
            objectTypeInfo.setObjectType(objectType.printName);
            objectTypeInfo.setObjectName(objectType.cnName);
            operations = new ArrayList<>();
            for (OperationType operationType : OperationType.values()) {
                if (operationType.objectType == objectType) {
                    operations.add(new OperationTypeResp(operationType));
                }
            }
            objectTypeInfo.setOperations(operations);
            list.add(objectTypeInfo);
        }
        return list;
    }

    @Override
    public Map<String, Set<String>> showNoPrivilege(String projectId, String roleName, String userId, String region) {
        RolePrivilege[] rolePrivileges = showPrivileges(roleName, projectId, null, null, null);
        return getNoPrivilegeObjectMap(rolePrivileges, region);
    }

    @Override
    public PageObjectResp showObjectNames(String objectType, String keyword, Integer pageNum,
                                          Integer pageSize, String userId, String tenantName, String pageToken)
            throws Exception {
        PageObjectResp pageObjectResp = new PageObjectResp();
        pageObjectResp.setKeyword(keyword);
        pageObjectResp.setPageNum(pageNum);
        int pageSizeLimit = (pageNum - 1) * pageSize;
        pageObjectResp.setPageSize(pageSize);
        pageObjectResp.setObjectType(objectType.trim());
        if (objectType.trim().equalsIgnoreCase(com.lakecat.web.constant.ObjectType.REGION.printName)) {
            Set<String> list = CatalogNameEnum.getRegionList();
            pageObjectResp.setObjectNames(list);
            pageObjectResp.setTotal(list.size());
            return pageObjectResp;
        }
        if (objectType.trim().equalsIgnoreCase(com.lakecat.web.constant.ObjectType.CATALOG.printName)) {
            List<SourceRead> list = dsUtilForLakecat.getActorSource();
            if (StringUtils.isNoneBlank(keyword)){
                List<SourceRead> filterList= Lists.newArrayList();
                for (SourceRead sourceRead:list){
                    if (sourceRead.getName().indexOf(keyword)>-1){
                        filterList.add(sourceRead);
                    }
                }
                list=filterList;
            }
            if (CollectionUtils.isNotEmpty(list)) {
                pageObjectResp.setObjectNames(list.stream().map(SourceRead::getName).collect(Collectors.toSet()));
                pageObjectResp.setTotal(list.size());
                pageObjectResp.setActors(list);
            }
            return pageObjectResp;
        }
        boolean isDatabase = objectType.equalsIgnoreCase(com.lakecat.web.constant.ObjectType.DATABASE.printName);
        List<Map<String, Object>> totalMaps;
        List<Map<String, Object>> dataMaps;

        Set<String> tableSet = new HashSet<>();
        List<String> databaseList = new ArrayList<>();
        if (isDatabase) {
            //原先逻辑
            Set<String> regionList = CatalogNameEnum.getRegionList();
            for (String region : regionList) {
                List<String> dbList = iTableInfoService.getDBList(tenantName, region);
                for (String key : dbList) {
                    try {
                        databaseList.add(
                                region + CommonConts.DATA_DELIMITER + key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            pageObjectResp.setTotal(databaseList.size());
            if (keyword != null && !keyword.equals("")) {
                String finalKeyword = keyword;
                databaseList = databaseList.stream().filter(x -> x.contains(finalKeyword)).collect(toList());
            }
            List<String> list = PageUtil.startPage(databaseList, pageNum, pageSize);
            pageObjectResp.setObjectNames(list);
        } else {
            if (StringUtils.isNotEmpty(keyword)) {
                String[] split = keyword.split(CommonConts.SPLIT_DELIMITER);
                String catalog = split[0];
                if (CatalogNameEnum.getCatalogNames().contains(catalog)) {
                    keyword = keyword.replaceFirst(catalog, CatalogNameEnum.getRegion(catalog));
                }
                keyword = SqlUtils.getMysqlLike(keyword);
            }

            LakeCatParam lakeCatParam = LakeCatParam.builder().keywords(keyword)
                    .size(pageSize).pageToken(pageToken).build();

            List<TableInfo> tableInfos = iLakeCatClientService.searchDiscoveryNames(lakeCatParam);
            List<TableInfo> collect = tableInfos.stream().map(data -> {
                tableSet.add(data.getKey());
                return data;
            }).collect(toList());
            if (!tableInfos.isEmpty()) {
                TableInfo tableInfo = tableInfos.stream().findFirst().get();
                pageObjectResp.setNextMarker(tableInfo.getNextMarker());
                pageObjectResp.setPreviousMarker(tableInfo.getPreviousMarker());
            }else {
                pageObjectResp.setNextMarker(lakeCatParam.getNextMarker());
                pageObjectResp.setPreviousMarker(lakeCatParam.getPreviousMarker());
            }
            pageObjectResp.setObjectNames(tableSet);
        }
        return pageObjectResp;
    }


    private Map<String, Set<String>> getNoPrivilegeObjectMap(RolePrivilege[] rolePrivileges, String region) {
        region = CatalogNameEnum.get(region).getRegion();

        LakeCatParam lakeCatParam = LakeCatParam.builder().region(region).build();
        List<TableInfo> tableInfos = iLakeCatClientService.searchTable(lakeCatParam);
        // table privilege objectName
        Set<String> tableSet = new HashSet<>();
        Set<String> databaseSet = new HashSet<>();
        // CatalogName
        Set<String> regionSet = new HashSet<>();
        tableInfos.stream().map(m -> {
            tableSet.add(CatalogNameEnum.getCatalogName(m.getRegion()) + CommonConts.DATA_DELIMITER +
                    m.getDbName() + CommonConts.DATA_DELIMITER + m.getTableName());
            databaseSet.add(
                    CatalogNameEnum.getCatalogName(m.getRegion()) + CommonConts.DATA_DELIMITER + m.getDbName());
            regionSet.add(
                    CatalogNameEnum.getCatalogName(m.getRegion()));

            return null;
        }).count();

        Map<String, Set<String>> privilegeObjectMap = new HashMap<>();
        setPrivilegeObjectMap(tableSet, privilegeObjectMap, com.lakecat.web.constant.ObjectType.TABLE);
        setPrivilegeObjectMap(databaseSet, privilegeObjectMap, com.lakecat.web.constant.ObjectType.DATABASE);
        setPrivilegeObjectMap(regionSet, privilegeObjectMap, com.lakecat.web.constant.ObjectType.REGION);

        Map<String, Set<String>> existsPrivilegeObjectMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(Arrays.asList(rolePrivileges))) {
            String existsPrivilege;
            for (RolePrivilege rolePrivilege : rolePrivileges) {
                existsPrivilege = OperationType.getTypeValue(rolePrivilege.getPrivilege());
                if (existsPrivilegeObjectMap.containsKey(existsPrivilege)) {
                    existsPrivilegeObjectMap.get(existsPrivilege).add(rolePrivilege.getName());
                } else {
                    HashSet<String> set = new HashSet<>();
                    set.add(rolePrivilege.getName());
                    existsPrivilegeObjectMap.put(existsPrivilege, set);
                }
            }
        }

        for (Entry<String, Set<String>> entry : privilegeObjectMap.entrySet()) {
            if (existsPrivilegeObjectMap.containsKey(entry.getKey())) {
                entry.getValue().removeAll(existsPrivilegeObjectMap.get(entry.getKey()));
                //Set<String> differenceSet = MathUtils.getDifferenceSet(entry.getValue(), existsPrivilegeObjectMap.get(entry.getKey()));
                privilegeObjectMap.put(entry.getKey(), entry.getValue());
                log.info("k: {}", entry.getKey());
            }
        }

        return privilegeObjectMap;
    }

    private void setPrivilegeObjectMap(Set<String> set, Map<String, Set<String>> privilegeObjectMap,
                                       com.lakecat.web.constant.ObjectType objectType) {
        for (OperationType opType : OperationType.values()) {
            if (opType.objectType == objectType) {
                setPrivilegeObjectMapByObjectType(set, privilegeObjectMap, opType);
            }
        }
    }

    private void setPrivilegeObjectMapByObjectType(Set<String> set, Map<String, Set<String>> privilegeObjectMap, OperationType opType) {
        Set<String> tmpSet = new HashSet<String>();
        for (String objectName : set) {
            tmpSet.add(objectName);
        }
        privilegeObjectMap.put(opType.typeValue, tmpSet);
    }


}
