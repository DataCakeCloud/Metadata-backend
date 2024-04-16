package com.lakecat.web.controller;

import java.util.List;
import java.util.Objects;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.OperationType;
import com.lakecat.web.entity.*;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IAdminRoleService;
import com.lakecat.web.service.IAuthPrivilegeService;
import com.lakecat.web.utils.GsonUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "元数据鉴权")
@RequestMapping("/metadata/auth")
public class AuthPrivilegeController extends BaseController {

    @Autowired
    private IAuthPrivilegeService authPrivilegeService;

    @Value("${default.tenantName}")
    private String defaultTenantName;

    @Resource
    private TableInfoMapper tableInfoMapper;


    @ApiOperation(value = "数据权限判断", produces = "application/json;charset=UTF-8")
    @PostMapping("/doAuth")
    public Response doAuth(@RequestBody AuthenticationReq authenticationReq) {

       /* if (flag) {
            return Response.success(true);
        }*/
        authenticationReq.setUserId(InfTraceContextHolder.get().getUuid());
        authenticationReq.setProjectId(InfTraceContextHolder.get().getTenantName());
        if (authenticationReq.getRegion() == null) {
            return Response.success(true);
        }
        authenticationReq.setQualifiedName(authenticationReq.getQualifiedName().toLowerCase());
        return createResponse(() -> {
            return Response.success(authPrivilegeService.doAuth(authenticationReq));
        });
    }

    @ApiOperation(value = "数据权限判断", produces = "application/json;charset=UTF-8")
    @PostMapping("/doAuthByGroup")
    public Response doAuthByGroup(@RequestBody AuthenticationReq authenticationReq) {
        log.info("groupRequest-->{}", GsonUtil.toJson(authenticationReq,false));
        if (StringUtils.isBlank(authenticationReq.getUserId())){
            return Response.fail("用户组没传");
        }
        String uuid=tableInfoMapper.selectUserGroupUuid(authenticationReq.getUserId());
        if (StringUtils.isBlank(uuid)){
            return Response.fail("用户组不存在");
        }
        authenticationReq.setUserId(uuid);
        authenticationReq.setProjectId(defaultTenantName);
        authenticationReq.setQualifiedName(authenticationReq.getQualifiedName().toLowerCase());
        return createResponse(() -> {
            return Response.success(authPrivilegeService.doAuth(authenticationReq));
        });
    }

    @ApiOperation(value = "数据权限判断", produces = "application/json;charset=UTF-8")
    @PostMapping("/doAuths")
    public Response doAuths(@RequestBody List <AuthenticationReq> authenticationReqs) {
        return createResponse(() -> {
            HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
            CurrentUser currentUser = JSONObject.parseObject(user, CurrentUser.class);
            String userId = currentUser.getUserId();
            String tenantName = currentUser.getTenantName();
            for (AuthenticationReq authenticationReq : authenticationReqs) {
                authenticationReq.setProjectId(tenantName);
                authenticationReq.setUserId(userId);
            }
            return Response.success(authPrivilegeService.doAuth(authenticationReqs));
        });
    }


    @ApiOperation(value = "数据权限开关", produces = "application/json;charset=UTF-8")
    @PostMapping("/doSwitch")
    public Response doSwitch(@RequestBody JSONObject args) {
        return Response.success();
    }
}
