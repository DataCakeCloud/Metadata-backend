package com.lakecat.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.entity.AuthorityGovInfo;

import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.RoleTableRelevance;
import com.lakecat.web.excel.ExcelSheetVOForGov;
import com.lakecat.web.excel.FileRenderUtil;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IAuthGovernService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.lakecat.web.common.CommonMethods.setAdmin;

@Slf4j
@RestController
@Api(tags = "权限治理")
@RequestMapping("/metadata/auth")
public class AuthGovernController extends BaseController {

    @Autowired
    private IAuthGovernService IAuthGovernService;


    @ApiOperation(value = "目录树", produces = "application/json;charset=UTF-8")
    @PostMapping("/list")
    public Response list(@RequestBody JSONObject args) {
        JSONArray list = IAuthGovernService.list(args);
        return Response.success(list);
    }

    @ApiOperation(value = "表列表", produces = "application/json;charset=UTF-8")
    @PostMapping("/tables")
    public Response tables(@RequestBody JSONObject args) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String userName = userInfo.getUserId();
        String tenantName = userInfo.getTenantName();
        args.put("currentUser", userName);
        args.put("tenantName", tenantName);
        return Response.success(IAuthGovernService.tables(args));
    }

    @ApiOperation(value = "表交接", produces = "application/json;charset=UTF-8")
    @PostMapping("/handover")
    public Response handover(@RequestBody JSONObject args) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String userName = userInfo.getUserId();
        String tenantName = userInfo.getTenantName();
        args.put("currentUser", userName);
        args.put("tenantName", tenantName);
        return IAuthGovernService.handover(args);
    }

    @ApiOperation(value = "取消权限（单个+批量）+邮箱通知", produces = "application/json;charset=UTF-8")
    @PostMapping("/cancel")
    public Response cancel(@RequestBody JSONObject args) {
        IAuthGovernService.cancel(args);
        return Response.success();
    }

    @ApiOperation(value = "取消权限（单个+批量）+邮箱通知", produces = "application/json;charset=UTF-8")
    @PostMapping("/cancellist")
    public Response cancellist(@RequestBody JSONObject args) {
        IAuthGovernService.cancelList(args);
        return Response.success();
    }

    @ApiOperation(value = "单表权限的展示", produces = "application/json;charset=UTF-8")
    @PostMapping("/searchOne")
    public Response searchOne(@RequestBody JSONObject args) {
        //增加参数分角色治理、用户治理
        List <RoleTableRelevance> roleTableRelevances = IAuthGovernService.searchOne(args);
        return Response.success(roleTableRelevances);
    }


    @ApiOperation(value = "单表权限的展示", produces = "application/json;charset=UTF-8")
    @GetMapping("/excel")
    public void excel(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                      @ApiParam(name = "tableName", value = "分数", required = false, defaultValue = "") @RequestParam(required = false) String tableName,
                      @ApiParam(name = "kwForRole", value = "逻辑关系", required = false, defaultValue = "") @RequestParam(required = false) String kwForRole,
                      @ApiParam(name = "kwForUser", value = "排序方式", required = false, defaultValue = "") @RequestParam(required = false) String kwForUser,
                      @ApiParam(name = "kwForPrivilege", value = "排序字段", required = false, defaultValue = "") @RequestParam(required = false) String kwForPrivilege,
                      @ApiParam(name = "kwForPrivilege", value = "排序字段", required = false, defaultValue = "") @RequestParam(required = false) String tenantName) {
        //增加参数分角色治理、用户治理
        CurrentUser currentUser = new CurrentUser();
        setAdmin(currentUser, tenantName);
        InfTraceContextHolder.get().setUserInfo(currentUser);
        JSONObject args = new JSONObject();
        args.put("tableName", tableName);
        args.put("kwForRole", kwForRole);
        args.put("kwForUser", kwForUser);
        args.put("kwForPrivilege", kwForPrivilege);
        List <RoleTableRelevance> roleTableRelevances = IAuthGovernService.searchOne(args);
        JSONArray index = JSON.parseArray(JSON.toJSONString(roleTableRelevances));
        excelBucketTableListForGov(index, httpServletRequest, httpServletResponse);
    }

    public static void excelBucketTableListForGov(JSONArray index, HttpServletRequest request, HttpServletResponse response) {
        ExcelSheetVOForGov excelSheetVO = new ExcelSheetVOForGov();
        excelSheetVO.setDatas(tableDetail(index));
        excelSheetVO.setName("表列表");
        List <ExcelSheetVOForGov> excelSheetVOS = Lists.newArrayList(excelSheetVO);
        FileRenderUtil.renderExcelForGov(excelSheetVOS, "表列表.xls", request, response);
    }

    private static List <Collection <Object>> tableDetail(JSONArray index) {
        if (index == null || index.isEmpty()) {
            return new ArrayList <>();
        }
        List <Collection <Object>> data = Lists.newArrayListWithCapacity(index.size() + 1);
        Set <String> headers = index.getJSONObject(0).keySet();
        data.add(Lists.newArrayList(headers));
        if (!CollectionUtils.isEmpty(index)) {
            for (int i = 0; i < index.size(); i++) {
                JSONObject result = index.getJSONObject(i);
                data.add(result.values());
            }
        }
        return data;
    }

    @ApiOperation(value = "角色下拉菜单", produces = "application/json;charset=UTF-8")
    @PostMapping("/rolelist")
    public Response roleList(@RequestBody JSONObject args) {
        List <String> list = IAuthGovernService.roleList(args);
        return Response.success(list);
    }

    @ApiOperation(value = "邮箱通知接口", produces = "application/json;charset=UTF-8")
    @PostMapping("/mail")
    public Response mail(@RequestBody JSONObject args) {
        IAuthGovernService.mail(args);
        return Response.success();
    }

    @ApiOperation(value = "治理与审批记录", produces = "application/json;charset=UTF-8")
    @PostMapping("/record")
    public Response record(@RequestBody JSONObject args) {


        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String userName = userInfo.getUserId();
        String tenantName = userInfo.getTenantName();
        args.put("currentUser", userName);
        args.put("tenantName", tenantName);
        //分页
        JSONObject record = IAuthGovernService.record(args);
        return Response.success(record.getJSONArray("index"),
                record.getInteger("total"), record.getInteger("page"), record.getInteger("limit"));
    }

    @ApiOperation(value = "撤回功能", produces = "application/json;charset=UTF-8")
    @PostMapping("/del")
    public Response del(@RequestBody AuthorityGovInfo args) {
        IAuthGovernService.del(args);
        return Response.success();
    }

    @ApiOperation(value = "修改功能", produces = "application/json;charset=UTF-8")
    @PostMapping("/update")
    public Response update(@RequestBody JSONObject args) {
        IAuthGovernService.update(args);
        return Response.success();
    }


    @ApiOperation(value = "权限生效", produces = "application/json;charset=UTF-8")
    @GetMapping("/sync")
    public Response sync() {
        IAuthGovernService.sync();
        return Response.success();
    }


}
