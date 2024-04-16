package com.lakecat.web.controller;


import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.DatabaseInfo;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


/**
 * 数据库管理 Controller
 * date:2023-12-07
 *
 * @author xuebotao
 */
@Slf4j
@RestController
@Api(tags = "DataBasesController", description = "数据库管理")
@RequestMapping("/metadata/database")
public class DataBasesController extends BaseController {

    @Autowired
    private ILakeCatClientService iLakeCatClientService;

    @Autowired
    ITableInfoService tableInfoService;

    //1.列出所有云
    @GetMapping("/listRegion")
    public Response listRegion() {
        List<String> regions = new ArrayList<>();
        //内部云资源获取
        regions.add("ksyun_cn-beijing-6");
        return Response.success(regions);
    }

    //3.列出某个类型下所有的库
    @PostMapping(value = "/pagesDb")
    @ApiOperation(value = "数据库下拉列表", produces = "application/json;charset=UTF-8")
    public Response pagesDb(@RequestBody DatabaseInfo databaseInfo) {
        return Response.success(iLakeCatClientService.pagesDb(databaseInfo));

    }


    //4.创建数据库
    @PostMapping(value = "/createdb")
    public Response createDB(@RequestBody DatabaseInfo entity) {

        //entity.setUserId(userInfo.getUserId());
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            //return tableInfoService.createDatabase(entity);
            return tableInfoService.createDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }


    //5.获取数据库
    @GetMapping(value = "/getdb")
    public Response getDbInfo(@RequestParam String region, @RequestParam String dbName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            if (region.equals("")) {
                return Response.success(new ArrayList<>());
            }
            return Response.success(tableInfoService.getDBByName(tenantName, region, dbName));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    //6.修改数据库
    @PostMapping(value = "/updatedb")
    public Response updateDB(@RequestBody DatabaseInfo entity) {
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            return tableInfoService.updateDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    //7.删除数据库
    @PostMapping(value = "/deletedb")
    public Response deleteDB(@RequestBody DatabaseInfo entity) {
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            return tableInfoService.deleteDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }


}
