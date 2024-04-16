package com.lakecat.web.service;

import com.lakecat.web.entity.Model;
import io.lakecat.catalog.common.model.glossary.Category;

import java.util.List;
import java.util.Map;

public interface ModelService {

    List<Model> listModelTree();

    void updateDefaultModel(String modelId, boolean isEffective);

    Object searchModelCount(String keyWord, String region, Integer categoryId);

    Category getModel(Integer id);

    Category addModel(Model model);

    Category updateModel(Model model);

    void deleteModel(Integer id);

}
