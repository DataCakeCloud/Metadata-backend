package com.lakecat.web.controller;

import com.alibaba.fastjson.JSON;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.service.IUserHistoryService;
import com.lakecat.web.service.ThreadService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;


@RestController
@Slf4j
@RequestMapping("/metadata")
public class UserHistoryController {

    @Autowired
    IUserHistoryService userHistoryService;
    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    ThreadService threadService;
    /**
     * 历史浏览接口 接口
     */
    @RequestMapping(value = "/history/table")
    public Response getTableName(@RequestParam Integer size) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
        CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
        String userId = currentUser.getUserId();
        return Response.success(userHistoryService.getTableName(userId, size));
    }


    /**
     * 历史输入接口 接口
     */
    @RequestMapping(value = "/history/input")
    public Response getItems(@RequestParam Integer size) {
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String user = request.getHeader(CommonParameters.CURRENT_LOGIN_USER);
        CurrentUser currentUser = JSON.parseObject(user, CurrentUser.class);
        String userId = currentUser.getUserId();
        return Response.success(userHistoryService.getItems(userId, size));
    }


    /**
     * 历史输入接口 接口
     */
    @RequestMapping(value = "/history/del")
    public Response getDel(@RequestParam String name) {
        return Response.success(userHistoryService.deleteItems(name));
    }

    /**
     * 主题域 接口
     */
    @RequestMapping(value = "/history/topic")
    public Response getTopic() {
        return Response.success(userHistoryService.getTopic());
    }



    @GetMapping(value = "/table/preciseSync")
    @ApiOperation(value = "精准同步", produces = "application/json;charset=UTF-8")
    public Response preciseSync(
            @ApiParam(name = "userId", value = "用户ID", required = false) @RequestParam(required = false) String userId,
            @ApiParam(name = "region", value = "所属区域", defaultValue = "ue1", required = false) @RequestParam(required = false) String region,
            @ApiParam(name = "databaseName", value = "库名", required = true) @RequestParam(required = true) String databaseName,
            @ApiParam(name = "tableName", value = "表名", required = true) @RequestParam(required = true) String tableName,
            @ApiParam(name = "tenantName", value = "租户名称", required = true) @RequestParam(required = true) String tenantName) {
        try {

            return Response.success(tableInfoService.preciseSync(userId, region, databaseName, tableName,tenantName));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/health")
    public Response health() {
        return Response.success();
    }


    @RequestMapping(value = "/sync")
    public Response sync() {
        threadService.initTable();
        return Response.success();
    }

}
