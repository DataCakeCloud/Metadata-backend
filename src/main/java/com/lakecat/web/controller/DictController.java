package com.lakecat.web.controller;

import com.lakecat.web.entity.DictInfoTypeReq;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
//@IdentityAuth
@Api(tags = "元数据字典")
@RequestMapping("/metadata")
public class DictController {

    @Autowired
    IDictService iDictService;

    @ApiOperation(value = "字典查询", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/dict")
    public Response dict(@RequestBody DictInfoTypeReq dictType) {
        return Response.success(iDictService.dict(dictType.getDictType()));
    }


    @ApiOperation(value = "字典同步", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/dict/sync")
    public Response syncName() {
        iDictService.syncName();
        return Response.success();
    }

}
