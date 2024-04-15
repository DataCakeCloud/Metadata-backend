package com.lakecat.web.controller;

import com.lakecat.web.entity.DictInfoTypeReq;
import com.lakecat.web.entity.TableTagInfo;
import com.lakecat.web.entity.TableTagReq;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IDictService;
import com.lakecat.web.service.TableTagInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@Slf4j
//@IdentityAuth
@Api(tags = "元数据字典")
@RequestMapping("/metadata")
public class TableTagInfoController {

    @Autowired
    TableTagInfoService tableTagInfoService;

    @ApiOperation(value = "表标签查询", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/tags/search")
    public Response search(@RequestBody TableTagReq tableTagReq) {
        return Response.success(tableTagInfoService.search(tableTagReq.getSole()));
    }


    @ApiOperation(value = " 表标签添加或添加", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/tags/add")
    public Response syncName(@RequestBody List <TableTagInfo> tableTagInfo) {
        tableTagInfoService.addTableTag(tableTagInfo);
        return Response.success();
    }

    @ApiOperation(value = " 表标签添加或添加", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/tags/delete")
    public Response delete(@RequestParam Long id) {
        tableTagInfoService.deleteTags(id);
        return Response.success();
    }
}
