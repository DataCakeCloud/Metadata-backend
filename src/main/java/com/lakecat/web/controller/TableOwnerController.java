package com.lakecat.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ITableOwnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
//@IdentityAuth
@Api(tags = "给首页提供数据支持")
@RequestMapping("/metadata/owner")
public class TableOwnerController {

    @Autowired
    ITableOwnerService iTableOwnerService;

    @ApiOperation(value = "owner表-num,owner表-size", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/ownerTable")
    public Response dict(@RequestBody JSONObject arg) {
        return Response.success(iTableOwnerService.getTableNumber(arg.getString("owner")));
    }


    @ApiOperation(value = "高频访问情况", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/frequency")
    public Response syncName(@RequestBody JSONObject args) {
        String owner = args.getString("owner");
        String type = args.getString("type");
        return Response.success(iTableOwnerService.frequency(owner, type));
    }
}
