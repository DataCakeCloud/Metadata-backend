package com.lakecat.web.controller;

import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IDataGradeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "数据等级接口控制层")
@RequestMapping("/metadata/dataGrade")
public class DataGradeController {

    @Autowired
    IDataGradeService dataGradeService;

    public Response delete(@RequestParam(value = "region", required = true) String region,
                           @RequestParam(value = "dbName", required = true) String dbName,
                           @RequestParam(value = "tablrName", required = true) String tableName) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);
        dataGradeService.deleteTableBySole(tableInfo.getSole());
        return Response.success();
    }

    @ApiOperation(value = "获取所有数据等级数据", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/getAll")
    public Response getAll() {
        return Response.success(dataGradeService.getAll());
    }
}
