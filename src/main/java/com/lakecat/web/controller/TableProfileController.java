package com.lakecat.web.controller;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

import com.lakecat.web.entity.*;
import io.lakecat.catalog.common.model.TableAccessUsers;
import com.alibaba.fastjson.JSON;
import com.lakecat.web.common.CommonParameters;
import io.lakecat.catalog.common.model.TableUsageProfile;

import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ITableProfileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "表画像信息")
@RequestMapping("/metadata/tableProfile")
public class TableProfileController extends BaseController {

    @Autowired
    ITableProfileService tableProfileService;


    @ApiOperation(value = "根据表获取访问详情", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/getUsageProfileDetails")
    public Response<List<TableUsageProfile>> getUsageProfileDetails(@RequestBody TableProfileInfoReq tableProfileInfoReq, @ApiParam(value = "size", required = false, defaultValue = "10") @RequestParam(required = false)Integer size) {
        return createResponse(()->{
            try {
                return Response.success(tableProfileService.getUsageProfileDetails(tableProfileInfoReq, size));
            } catch (BusinessException e) {
                e.printStackTrace();
                return Response.fail(-1, e.getMessage());
            }
        });
    }

    @ApiOperation(value = "根据表查询被访问次数", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/getUsageProfilesByTable")
    public Response<BigInteger> getUsageProfilesByTable(@RequestBody TableProfileInfoReq tableProfileInfoReq) {
        return createResponse(()->{
            try {
                return Response.success(tableProfileService.getUsageProfilesByTable(tableProfileInfoReq));
            } catch (BusinessException e) {
                e.printStackTrace();
                return Response.fail(-1, e.getMessage());
            }
        });
    }

    @ApiOperation(value = "查询表的以用户分组使用情况", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/getUsageProfileGroupByUser")
    public Response<List<TableUsageProfileGroupByUser>> getUsageProfileGroupByUser(@RequestBody TableProfileInfoReq tableProfileInfoReq, @ApiParam(value = "访问类型，默认不填", required = false, defaultValue = "WRITE;READ") @RequestParam(required = false) String opTypes) {
        return createResponse(()->{
            try {
                CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
                String tenantName = userInfo.getTenantName();
                tableProfileInfoReq.setTenantName(tenantName);
                return Response.success(tableProfileService.getUsageProfileGroupByUser(tableProfileInfoReq, opTypes));
            } catch (Exception e) {
                e.printStackTrace();
                return Response.fail(-1, e.getMessage());
            }
        });
    }

    @ApiOperation(value = "获取最近访问用户", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/recentlyVisitedUsers")
    public Response<List<String>> recentlyVisitedUsers(@RequestBody TableInfoReq tableInfoReq) {
        return createResponse(()->{
            try {
                HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
                String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
                CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
                String tenantName = currentUser.getTenantName();
                tableInfoReq.setTenantName(tenantName);
                return Response.success(tableProfileService.recentlyVisitedUsers(tableInfoReq));
            } catch (BusinessException e) {
                e.printStackTrace();
                return Response.fail(-1, e.getMessage());
            }
        });
    }

    @ApiOperation(value = "获取最近访问用户, 仅支持一个region范围内的查询", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/recentlyVisitedUsersByList")
    public Response<List<TableAccessUsers>> recentlyVisitedUsersByList(@RequestBody List<TableInfoReq> tableInfoReqs) {
        return createResponse(()->{
            try {
                HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
                String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
                CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
                String tenantName = currentUser.getTenantName();
                return Response.success(tableProfileService.recentlyVisitedUsers(tableInfoReqs,tenantName));
            } catch (Exception e) {
                e.printStackTrace();
                return Response.fail(-1, e.getMessage());
            }
        });
    }


}
