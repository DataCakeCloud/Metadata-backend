package com.lakecat.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.BaseResponse;
import com.lakecat.web.constant.BaseResponseCodeEnum;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.constant.ObjectType;
import com.lakecat.web.entity.*;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IAdminRoleService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.OaService;
import com.lakecat.web.utils.DSUtilForLakecat;
import com.lakecat.web.utils.PageUtil;
import com.lakecat.web.vo.blood.PrivilegeVo;
import com.lakecat.web.vo.blood.RolePrivilegeVo;
import com.lakecat.web.vo.blood.SourceRead;
import io.lakecat.catalog.common.model.RolePrivilege;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.lakecat.web.common.CommonParameters.PER_TYPE;
import static com.lakecat.web.constant.CatalogNameEnum.regionCatalogMapping;
import static com.lakecat.web.service.impl.AdminRoleServiceImpl.PRIVILEGE_SINGLE_USER_PREFIX;

/**
 * 后台用户及角色管理 Controller
 *
 * @author hongyonggan
 */
@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "AdminRoleController", description = "后台用户角色管理")
@RequestMapping("/metadata/role")
public class AdminRoleController extends BaseController {

    @Autowired
    private IAdminRoleService adminRoleService;

    @Autowired
    CatalogNameEnum catalogNameEnum;

    @Autowired
    private DSUtilForLakecat dsUtilForLakecat;

    @Autowired
    private OaService oaService;

    @Autowired
    private ILakeCatClientService iLakeCatClientService;

    @GetMapping("/getdatabase")
    public Response dataase(String name){
        return Response.success(iLakeCatClientService.getDataBase("ksyun_cn-beijing-6",name));
    }

    @GetMapping("/getRolePrivilege")
    public Response getRoles(String name){
        RolePrivilege rolePrivilege[] = adminRoleService.showPrivileges(name, InfTraceContextHolder.get().getTenantName(), 1, "", "");
        return Response.success(rolePrivilege);
    }

    @ApiOperation(value = "展示所有角色", produces = "application/json;charset=UTF-8")
    @GetMapping("/showRoles")
    public Response showRoles(
            @ApiParam(value = "过滤用户角色，默认为:true") @RequestParam(value = "filterUserRole", defaultValue = "true") Boolean filterUserRole,
            @ApiParam(value = "userName") @RequestParam(value = "userName", defaultValue = "") String userName,
            @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region
    ) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();

        return Response.success(adminRoleService.showRoles(filterUserRole, userName, tenantName));
    }

    @ApiOperation(value = "创建角色")
    @PostMapping("/createRole")
    public Response createRole(@RequestBody RoleInfo roleInfo,
                               @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            String ownerUser = roleInfo.getOwnerUser();
            String roleName = roleInfo.getRoleName();
            String comment = roleInfo.getComment();
            AdminRoleInfo entity = new AdminRoleInfo();
            entity.setOwnerUser(ownerUser);
            entity.setRoleName(roleName);
            entity.setComment(comment);
            entity.setTenantName(tenantName);
            Boolean roleByFe = adminRoleService.createRoleByFe(entity);
            if (!roleByFe) {
                return Response.fail("角色创建失败，请检查角色名是否符合要求");
            }
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }

    }

    @ApiOperation(value = "修改角色名")//405
    @PatchMapping("/alterRole")
    public Response alterRole(@RequestBody RoleInfo roleInfo,
                              @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {

        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            String oldName = roleInfo.getOldName();
            String newName = roleInfo.getNewName();
            String comment = roleInfo.getComment();
            adminRoleService.alterRole(oldName, newName, comment, tenantName);
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    @ApiOperation(value = "根据名称删除角色")
    @DeleteMapping("/dropRole")
    public Response dropRole(@RequestParam(value = "roleName") String roleName,
                             @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            adminRoleService.dropRole(roleName, tenantName);
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    @ApiOperation(value = "给一个角色添加用户")//405
    @PostMapping("/addUsers")
    public Response addUsers(@RequestBody RoleInputs roleInputs,
                             @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.addUsers(roleInputs, tenantName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    @ApiOperation(value = "从一个角色中移除用户")//405
    @PatchMapping("removeUsers")
    public Response removeUsers(@RequestBody RoleInputs roleInputs,
                                @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.removeUsers(roleInputs, tenantName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    @ApiOperation(value = "展示角色权限")
    @GetMapping("showPrivileges")
    public Response showPrivileges(@RequestParam(value = "roleName") String roleName,
                                   @RequestParam(value = "projectId") String projectId,
                                   @RequestParam(value = "pattern", defaultValue = "") String pattern,
                                   @RequestParam(value = "maxResults") Integer maxResults,
                                   @RequestParam(value = "page") Integer page,
                                   @RequestParam(value = "objectName", required = false) String objectName,
                                   @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            RolePrivilege rolePrivilege[] = adminRoleService.showPrivileges(roleName, tenantName, maxResults, "", objectName);
            if (rolePrivilege != null && rolePrivilege.length > 0) {
                List<RolePrivilege> r = Arrays.asList(rolePrivilege);
                if (!StringUtils.isEmpty(objectName)) {
                    r = r.stream().filter(p -> p.getName().indexOf(objectName) > -1).collect(Collectors.toList());
                }
                if (!CollectionUtils.isEmpty(r)){
                    r = r.stream().filter(p -> ObjectType.TABLE.printName.equals(p.getGrantedOn()))
                            .filter(p -> !p.getName().endsWith("*"))
                            .collect(Collectors.toList());
                }
                if (!CollectionUtils.isEmpty(r)){
                    List<RolePrivilege> rolePrivilegeList=Lists.newArrayList();
                    r.stream().collect(Collectors.groupingBy(RolePrivilege::getName)).values().forEach(v->{
                        if (v.size()>2){
                            v.get(0).setPrivilege("读写");
                        }else {
                            v.get(0).setPrivilege("读取");
                        }
                        rolePrivilegeList.add(v.get(0));
                    });
                    r=rolePrivilegeList;
                }
                if (!CollectionUtils.isEmpty(r)) {
                    int size = r.size();
                    r = PageUtil.startPage(r, page == null ? 1 : page, maxResults);
                    boolean catalog = false;
                    for (RolePrivilege rp : r) {
                        if (ObjectType.CATALOG.printName.equalsIgnoreCase(rp.getGrantedOn())) {
                            catalog = true;
                            break;
                        }
                    }
                    if (catalog) {
                        try {
                            List<SourceRead> sourceReads = dsUtilForLakecat.getActorSource();
                            if (!CollectionUtils.isEmpty(sourceReads)) {
                                List<RolePrivilegeVo> rolePrivilegeVoList = Lists.newArrayList();
                                for (RolePrivilege rs : r) {
                                    RolePrivilegeVo rolePrivilegeVo = new RolePrivilegeVo();
                                    BeanUtils.copyProperties(rs, rolePrivilegeVo);
                                    rolePrivilegeVoList.add(rolePrivilegeVo);
                                    if (ObjectType.CATALOG.printName.equalsIgnoreCase(rs.getGrantedOn())) {
                                        for (SourceRead sourceRead : sourceReads) {
                                            if (!StringUtils.isEmpty(sourceRead.getUuid()) && sourceRead.getUuid().equalsIgnoreCase(rs.getName())) {
                                                rolePrivilegeVo.setUuid(sourceRead.getName());
                                            }
                                        }
                                    }

                                }
                                return Response.success(rolePrivilegeVoList, size, page == null ? 1 : page, maxResults);
                            }
                        } catch (Exception e) {

                        }
                    }
                    return Response.success(r, size, page == null ? 1 : page, maxResults);
                }
            }
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    @ApiOperation(value = "根据库表名称查询角色权限")
    @GetMapping("showPrivilegesByName")
    public Response showPrivilegesByName(@RequestParam(value = "roleName") String roleName,
                                         @RequestParam(value = "objectName") String objectName,
                                         @RequestParam(value = "maxResults") Integer maxResults,
                                         @RequestParam(value = "page") String page,
                                         @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.showPrivilegesByName(roleName, objectName, maxResults, page, tenantName));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "展示角色内用户")
    @GetMapping("showUsers")
    public Response showUsers(@RequestParam(value = "roleName") String roleName,
                              @RequestParam(value = "projectId") String projectId,
                              @RequestParam(value = "pattern", defaultValue = "") String pattern,
                              @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.showUsers(roleName, tenantName, pattern));
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @ApiOperation(value = "根据名称查找角色")
    @GetMapping("showRoleByName")
    public Response showRoleByName(@RequestParam(value = "roleName") String roleName,
                                   @RequestParam(value = "projectId") String projectId,
                                   @RequestParam(value = "pattern", defaultValue = "") String pattern,
                                   @ApiParam(value = "过滤用户角色，默认为:true") @RequestParam(value = "filterUserRole", defaultValue = "true") Boolean filterUserRole,
                                   @ApiParam(value = "userName") @RequestParam(value = "userName", defaultValue = "") String userName,
                                   @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.showRoleByName(roleName, tenantName, pattern, filterUserRole, userName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    //todo 给角色授权
    @ApiOperation(value = "给角色添加权限")
    @PostMapping("grantPrivilegeToRole")
    public Response grantPrivilegeToRole(@RequestBody RoleInputs roleInputs,
                                         @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.grantPrivilegeToRole(roleInputs, tenantName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }


    //todo 给角色授权
    @ApiOperation(value = "在建库时给角色或者个人添加权限")
    @PostMapping("grantPrivilegeToRoleOnDB")
    public Response grantPrivilegeToRoleOnDB(@RequestBody RoleInputs roleInputs) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            String[] roleNameList = roleInputs.getRoleNameList();
            String type = roleInputs.getType();
            for (int i = 0; i < roleNameList.length; i++) {
                if (roleNameList[i] == null) {
                    continue;
                }
                String roleName = roleNameList[i].trim();
                /*if (PER_TYPE.equals(type)) {
                    roleName = PRIVILEGE_SINGLE_USER_PREFIX + roleName.replaceAll("\\.", "_");
                    AdminRoleInfo entity = new AdminRoleInfo();
                    entity.setRoleName(roleName);
                    entity.setTenantName(tenantName);
                    boolean roleOrNot = adminRoleService.getRoleOrNot(entity);
                    if (!roleOrNot) {
                        entity.setUserId(roleNameList[i].trim().split(","));
                        adminRoleService.createRoleAndAddUser(entity);
                    }
                }*/
                roleInputs.setRoleName(roleName);
                adminRoleService.grantPrivilegeToRole(roleInputs, tenantName);
            }
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    //todo 给角色移除权限
    @ApiOperation(value = "给角色移除权限")
    @PatchMapping("revokePrivilegeFromRole")
    public Response revokePrivilegeFromRole(@RequestBody RevokePrivilegeFromRole revokePrivilegeFromRole,
                                            @ApiParam(value = "region", required = false)
                                            @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            log.info("控制层: {}", JSON.toJSONString(revokePrivilegeFromRole));
            return Response.success(adminRoleService.revokePrivilegeFromRole(revokePrivilegeFromRole, tenantName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    //todo 给角色移除权限
    @ApiOperation(value = "给角色移除权限")
    @PatchMapping("bathRevokePrivilegeFromRole")
    public Response bathRevokePrivilegeFromRole(@RequestBody List<RevokePrivilegeFromRole> revokePrivilegeFromRoleList,
                                                @ApiParam(value = "region", required = false)
                                                @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            String tenantName = InfTraceContextHolder.get().getTenantName();
            for (RevokePrivilegeFromRole revokePrivilegeFromRole : revokePrivilegeFromRoleList) {
                adminRoleService.revokePrivilegeFromRole(revokePrivilegeFromRole, tenantName);
            }
            return Response.success();
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }


    //todo 给用户添加权限：
    // 1、拿到权限，判断该用户有无专属role（专属role名格式待定）
    // 2、若存在，则直接调给该角色添加权限接口；（调用该接口前调用钉钉审批接口）
    // 3、若不存在，则先调用创建角色接口，rolename = 专属role名；后调用给该角色添加权限接口；（调用创建角色接口前调用钉钉审批接口）

    //调用钉钉审批 申请 发起
    @ApiOperation(value = "调用钉钉审批")
    @PostMapping("grantPrivilegeToUser")
    public BaseResponse grantPrivilegeToUser(@RequestBody GrantPrivilegeToUser inputs,
                                             @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        return BaseResponse.success(adminRoleService.grantPrivilegeToUser(inputs));
    }

    //钉钉审批回调
    @ApiOperation(value = "给用户添加权限")
    @PostMapping("grantPrivilegeToUserCallback")
    public Response grantPrivilegeToUserCallback(@RequestBody DingCallBack dingCallBack) {
        try {
           String flag = adminRoleService.grantPrivilegeToUserCallback(dingCallBack);
            command(dingCallBack);
            return Response.success("接收回调成功");
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }


    //钉钉审批回调
    @ApiOperation(value = "给用户添加权限")
    @RequestMapping("oaCallback")
    public Response oaCallback(@RequestParam("oaId") String oaId,Integer status) {
        try {
            log.info("oaCallback oaId is :" + oaId);
            oaService.oaCallback(oaId,status);
            return Response.success("接收回调成功");
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    public static ExecutorService pool = new ThreadPoolExecutor(1, 1,
            1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MONITOR_ALERT_COMPLETABLEFUTURE");
            thread.setDaemon(true);
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());

    public int command(DingCallBack dingCallBack) {
        int resultFlag = 0;
        CompletableFuture.supplyAsync(() -> {
            try {
                Map<String, CatalogNameEnum.CloudRegionCatalog> ANY_NAME_MAP = new HashMap<>();
                catalogNameEnum.setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
                CurrentUser userInfo = new CurrentUser();
                userInfo.setRegionInfo(ANY_NAME_MAP);
                userInfo.setTenantName("shareit");
                userInfo.setFlag(false);
                InfTraceContextHolder.get().setUserInfo(userInfo);
                String flag = adminRoleService.grantPrivilegeToUserCallback(dingCallBack);
                System.out.println("回调执行状态" + flag);
            } catch (Exception e) {
                System.out.println("执行线程抛出异常" + e.getMessage());
            }
            return 0;
        }, pool).whenCompleteAsync((s, e) -> {
            if (e != null) {
                System.out.println("执行线程同步执行中报错->>>>" + e.getMessage());
            }
        }, pool);
        return resultFlag;
    }

    @ApiOperation(value = "批量授予权限")
    @PostMapping("batchGrantPrivilegeToUser")
    public Response grantPrivilegeToUserCallback(@RequestBody PrivilegeVo privilegeVo) {
        try {
            return Response.success(adminRoleService.batchGrantPrivilegeToUser(privilegeVo));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    //todo 给用户移除权限：
    @ApiOperation(value = "给用户移除权限")
    @PatchMapping("revokePrivilegeFromUser")

    public Response revokePrivilegeFromUser(@RequestParam(value = "roleInputs") RoleInputs[] roleInputs,
                                            @RequestParam(value = "roleName") String roleName,
                                            @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            return Response.success(adminRoleService.revokePrivilegeFromUser(roleInputs, roleName, tenantName));
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }

    //对象类型
    @ApiOperation(value = "返回对象类型")
    @GetMapping("objectType")
    public Response objectType(
            @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region) {
        try {
            return Response.success(adminRoleService.objectType());
        } catch (Exception e) {
            return Response.fail(e.toString());
        }
    }


    @ApiOperation(value = "展示角色未拥有的所有权限", produces = "application/json;charset=UTF-8")
    @GetMapping("/showNoPrivileges")
    public Response showNoPrivilege(
            @ApiParam(value = "roleName", required = true) @RequestParam(value = "roleName", required = true) String roleName,
            @ApiParam(value = "projectId", required = true) @RequestParam(value = "projectId", required = true) String projectId,
            @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
            @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region
    ) {
        return createResponse(() -> {
            try {
                CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
                String tenantName = userInfo.getTenantName();
                return Response.success(adminRoleService.showNoPrivilege(tenantName, roleName, userId, region));
            } catch (Exception e) {
                e.printStackTrace();
                return Response.fail(e.getMessage());
            }
        });
    }

    @ApiOperation(value = "展示所有对象", produces = "application/json;charset=UTF-8")
    @GetMapping("/showObjectNames")
    public Response<PageObjectResp> showObjectNames(
            @ApiParam(value = "objectType", required = true, allowableValues = "DATABASE, TABLE, REGION") @RequestParam(value = "objectType", required = true) String objectType,
            @ApiParam(value = "keyword 检索词", required = false) @RequestParam(value = "keyword", required = true, defaultValue = "") String keyword,
            @ApiParam(value = "pageNum", required = false) @RequestParam(value = "pageNum", required = false, defaultValue = "1") Integer pageNum,
            @ApiParam(value = "pageSize", required = false) @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize,
            @ApiParam(value = "userId", required = false) @RequestParam(value = "userId", required = false) String userId,
            @ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region,
            @ApiParam(value = "pageToken", required = false) @RequestParam(value = "pageToken", required = false) String pageToken
    ) {
        return createResponse(() -> {
            try {
                CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
                String tenantName = userInfo.getTenantName();
                return Response.success(adminRoleService.showObjectNames(objectType, keyword, pageNum, pageSize, userId, tenantName, pageToken));
            } catch (Exception e) {
                e.printStackTrace();
                return Response.fail(e.getMessage());
            }
        });
    }


    @ApiOperation(value = "同步权限", produces = "application/json;charset=UTF-8")
    @GetMapping("/sync")
    public Response<PageObjectResp> showObjectNames() {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();
        return createResponse(() -> {
            adminRoleService.syncGrantPrivilege(tenantName);
            return Response.success();
        });
    }

    @ApiOperation(value = "同步权限", produces = "application/json;charset=UTF-8")
    @GetMapping("/syncPrecise")
    public Response<PageObjectResp> syncGrantPrivilegePrecise(@ApiParam(value = "roleName", required = true) @RequestParam(value = "roleName", required = true) String roleName) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();
        return createResponse(() -> {
            adminRoleService.syncGrantPrivilegePrecise(roleName, tenantName);
            return Response.success();
        });
    }

    public static void main(String[] args) {
        System.out.println("1.2.1".endsWith("*"));
    }
}
