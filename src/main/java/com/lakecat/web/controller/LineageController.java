package com.lakecat.web.controller;


import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.LineageService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 血缘管理
 * date:2024-01-02
 *
 * @author xuebotao
 */
@Slf4j
@RestController
@Api(tags = "LineageController", description = "血缘管理")
@RequestMapping("/metadata/lineage")
public class LineageController extends BaseController {

    @Autowired
    public LineageService lineageService;

    @PostMapping("/getLineageGraph")
    public Response getLineageGraph(@RequestBody LakeCatParam lakeCatParam) {
        return Response.success(lineageService.getLineageGraph(lakeCatParam));
    }

    @PostMapping("/getLineageFact")
    public Response getLineageFact(@RequestBody LakeCatParam lakeCatParam) {
        return Response.success(lineageService.getLineageFact(lakeCatParam));
    }



}
