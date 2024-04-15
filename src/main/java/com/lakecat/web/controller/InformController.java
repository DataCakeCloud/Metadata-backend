package com.lakecat.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IInformService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.lakecat.web.constant.CatalogNameEnum.regionCatalogMapping;


@RestController
@Slf4j
@Api(tags = "给首页提供数据支持")
@RequestMapping("/metadata/table")
public class InformController {

    @Autowired
    IInformService informService;


    @Autowired
    CatalogNameEnum catalogNameEnum;

    @ApiOperation(value = "hookTrigger", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/hookTrigger")
    public Response hookTrigger(@RequestBody JSONObject args) {
        //return Response.success(null);
        log.info("执行参数" + args);
        String operUserAndTenantName = args.getString("operUser");
        String[] split = operUserAndTenantName.split("#");
        if (split.length == 2) {
            String tenantName = operUserAndTenantName.split("#")[0];
            String operUser = operUserAndTenantName.split("#")[1];
            args.put("tenantName", tenantName);
            args.put("operUser", operUser);
        } else {
            args.put("tenantName", "shareit");
            args.put("operUser", operUserAndTenantName);
        }
        Map <String, CatalogNameEnum.CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
        catalogNameEnum.setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
        CurrentUser userInfo = new CurrentUser();
        userInfo.setRegionInfo(ANY_NAME_MAP);
        userInfo.setTenantName(args.getString("tenantName"));
        InfTraceContextHolder.get().setUserInfo(userInfo);
        return Response.success(informService.inform(args));
    }

}
