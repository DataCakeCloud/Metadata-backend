package com.lakecat.web.controller;


import com.lakecat.web.response.Response;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * 血缘管理 Controller
 * 表级血缘 字段血缘
 * date:2023-12-07
 * @author xuebotao
 */
@Slf4j
@RestController
@Api(tags = "BloodRelationshipController", description = "血缘管理")
@RequestMapping("/blood/relationship")
public class BloodRelationshipController extends BaseController {


    /**
     * 表级血缘
     *
     * @param name
     * @return
     */
    @GetMapping("/table")
    public Response dataase(String name) {
        return Response.success();
    }

    /**
     * 字段级血缘
     *
     * @param name
     * @return
     */
    @GetMapping("/column")
    public Response getRoles(String name) {
        return Response.success();
    }

}
