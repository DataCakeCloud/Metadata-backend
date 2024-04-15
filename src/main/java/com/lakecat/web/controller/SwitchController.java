package com.lakecat.web.controller;

import com.lakecat.web.response.Response;
import com.lakecat.web.service.ISwitchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
//@IdentityAuth
@RequestMapping("/metadata")
public class SwitchController {

    @Autowired
    ISwitchService switchService;

    @RequestMapping(value = "/switch")
    public Response updateSwitch(@RequestParam Boolean CU) {
        return Response.success(switchService.updateSwitch(CU));
    }

    @RequestMapping(value = "/version")
    public Response test(){
        return Response.success("V1");
    }
}
