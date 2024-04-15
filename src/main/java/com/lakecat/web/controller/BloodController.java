package com.lakecat.web.controller;

import com.google.common.collect.Maps;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.BloodService;
import com.lakecat.web.utils.JsonUtil;
import com.lakecat.web.vo.blood.BloodRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

@RestController
public class BloodController {
    @Autowired
    private BloodService bloodService;

    @RequestMapping(value = "/metadata/blood",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String bloodMapping(@RequestBody BloodRequest bloodRequest, HttpServletRequest httpServletRequest){
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String authentication = request.getHeader("Authentication");
        bloodRequest.setAuth(authentication);
        bloodRequest.wrapDefault();
        com.lakecat.web.vo.blood.Response response = bloodService.blood(bloodRequest);
        bloodService.wrapBloodResponse(response);
        if (response!=null){
            if (response.getData().getInstance().size()==1){
                response.getData().getInstance().get(0).setNodeId("1");
                response.getData().setCoreTaskId("1");
            }
            for (Iterator<com.lakecat.web.vo.blood.Response.Rdata.Instance> iterator = response.getData().getInstance().iterator(); iterator.hasNext(); ) {//保留下游的起始阶段 去掉上游的终止节点
                com.lakecat.web.vo.blood.Response.Rdata.Instance instance=iterator.next();
                if (StringUtils.isEmpty(instance.getNodeId())){
                    iterator.remove();
                }
            }
            Map<String,Integer> map= Maps.newHashMap();
            int max=0;
            for (com.lakecat.web.vo.blood.Response.Rdata.Relation relation:response.getData().getRelation()){
                if (map.size()==0){
                    map.put(relation.getSource(),++max);
                }
                if (!map.containsKey(relation.getSource())){
                    map.put(relation.getSource(),++max);
                }
                if (!map.containsKey(relation.getTarget())){
                    map.put(relation.getTarget(),++max);
                }
            }
            for (com.lakecat.web.vo.blood.Response.Rdata.Relation relation:response.getData().getRelation()){
                relation.setSource(map.get(relation.getSource())+"");
                relation.setTarget(map.get(relation.getTarget())+"");
            }
            for (com.lakecat.web.vo.blood.Response.Rdata.Instance instance:response.getData().getInstance()){
                instance.setNodeId(map.get(instance.getNodeId())+"");
            }
            response.getData().setCoreTaskId(map.get(response.getData().getCoreTaskId())+"");
            return JsonUtil.toJson(Response.success(response.getData()),false);
        }
        return JsonUtil.toJson(Response.success(new com.lakecat.web.vo.blood.Response.Rdata()),false);
    }
}
