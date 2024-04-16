package com.lakecat.web.controller;

import java.util.Map;

import com.lakecat.web.entity.owner.ActiveTable;
import com.lakecat.web.entity.owner.DsOwnerStatVo;
import com.lakecat.web.entity.owner.MetadataIntegrity;
import com.lakecat.web.entity.owner.ResourceUsage;
import com.lakecat.web.entity.owner.TableStorage;
import com.lakecat.web.entity.owner.TableUsageProfile;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IMetaOwnerService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
//@IdentityAuth
@Api(tags = "MetaOwnerController")
@RequestMapping("/metadata/owner")
public class MetaOwnerController {

    @Autowired
    IMetaOwnerService metaOwnerService;

    @ApiOperation(value = "dsHomePageData", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/dsHomePageData")
    public Response dsHomePageData(@RequestBody(required = true) Map<String, Object> data) {
        DsOwnerStatVo dsOwnerStatVo = new DsOwnerStatVo();
        try {
            if(data.containsKey("owner")) {
                String owner = (String) data.get("owner");
                //metaOwnerService.dsHomePageData(Long.parseLong(tableId));
                ActiveTable activeTable = metaOwnerService.activeTable(owner);
                TableStorage tableStorage = metaOwnerService.tableStorage(owner);
                ResourceUsage resourceUsage = metaOwnerService.resourceUsage(owner);
                TableUsageProfile tableUsageProfile = metaOwnerService.tableUsageProfile(owner);
                MetadataIntegrity metadataIntegrity = metaOwnerService.metadataIntegrity(owner);
                dsOwnerStatVo.setActiveTable(activeTable);
                dsOwnerStatVo.setResourceUsage(resourceUsage);
                dsOwnerStatVo.setTableStorage(tableStorage);
                dsOwnerStatVo.setTableUsageProfile(tableUsageProfile);
                dsOwnerStatVo.setMetadataIntegrity(metadataIntegrity);
                return Response.success(dsOwnerStatVo);
            }
        } catch (Exception e) {
            return Response.success(dsOwnerStatVo);
        }
        return null;
    }

    @ApiOperation(value = "activeTable", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/activeTable")
    public Response activeTable(@RequestParam(value = "owner", required = true) String owner) {
        return Response.success(metaOwnerService.activeTable(owner));
    }
    @ApiOperation(value = "tableStorage", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/tableStorage")
    public Response tableStorage(@RequestParam(value = "owner", required = true) String owner) {
        return Response.success(metaOwnerService.tableStorage(owner));
    }
    @ApiOperation(value = "resourceUsage", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/resourceUsage")
    public Response resourceUsage(@RequestParam(value = "owner", required = true) String owner) {
        return Response.success(metaOwnerService.resourceUsage(owner));
    }
    @ApiOperation(value = "tableUsageProfile", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/tableUsageProfile")
    public Response tableUsageProfile(@RequestParam(value = "owner", required = true) String owner) {
        return Response.success(metaOwnerService.tableUsageProfile(owner));
    }
    @ApiOperation(value = "metadataIntegrity", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/metadataIntegrity")
    public Response metadataIntegrity(@RequestParam(value = "owner", required = true) String owner) {
        return Response.success(metaOwnerService.metadataIntegrity(owner));
    }

    @ApiOperation(value = "refreshMetadataIntegrity", produces = "application/json;charset=UTF-8")
    @GetMapping(value = "/refreshMetadataIntegrity")
    public Response refreshMetadataIntegrity() {
        metaOwnerService.refreshMetadataIntegrity(null);
        return Response.success();
    }


}
