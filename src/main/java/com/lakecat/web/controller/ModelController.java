package com.lakecat.web.controller;


import com.lakecat.web.entity.Model;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.ModelService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 模型管理
 * date:2023-12-07
 *
 * @author xuebotao
 */
@Slf4j
@RestController
@Api(tags = "ModelController", description = "模型管理")
@RequestMapping("/metadata/model")
public class ModelController extends BaseController {


    @Autowired
    public ModelService modelService;

    //1.列出树结构的模型
    @GetMapping("/listModelTree")
    public Response listModelTree() {
        return Response.success(modelService.listModelTree());
    }

    //1.设置默认模型
    @GetMapping("/updateEffectiveModel")
    public Response updateDefaultModel(@RequestParam String modelId,
                                       @RequestParam boolean isEffective) {
        modelService.updateDefaultModel(modelId,isEffective);
        return Response.success();
    }

    /**
     * 检索表count
     */
    @RequestMapping(value = "/searchModelCount")
    public Response searchModelCount(String keyWord, String region, Integer categoryId) {
        Response success = Response.success(modelService.searchModelCount(keyWord, region, categoryId));
        return success;

    }

    //2.获取模型详情
    @GetMapping("/getModel")
    public Response getModel(@RequestParam Integer id) {
        return Response.success(modelService.getModel(id));
    }

    //3.添加模型详情
    @PostMapping("/addModel")
    public Response addModel(@RequestBody Model model) {
        modelService.addModel(model);
        return Response.success();
    }

    //4.更新模型详情
    @PostMapping("/updateModel")
    public Response getModel(@RequestBody Model model) {
        modelService.updateModel(model);
        return Response.success();
    }

    //5.删除模型详情
    @DeleteMapping("/deleteModel")
    public Response deleteModel(@RequestParam Integer id) {
        modelService.deleteModel(id);
        return Response.success();
    }


}
