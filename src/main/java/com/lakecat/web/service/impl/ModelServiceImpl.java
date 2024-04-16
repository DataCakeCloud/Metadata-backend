package com.lakecat.web.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.*;
import com.lakecat.web.mapper.DictMapper;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ModelService;
import io.lakecat.catalog.common.model.discovery.CatalogTableCount;
import io.lakecat.catalog.common.model.discovery.ObjectCount;
import io.lakecat.catalog.common.model.glossary.Category;
import io.lakecat.catalog.common.model.glossary.Glossary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


@Service
@Slf4j
public class ModelServiceImpl implements ModelService {

    private static final String MODEL = "MODEL";


    @Autowired
    public ILakeCatClientService lakeCatClientService;

    @Autowired
    public DictMapper dictMapper;

    @Autowired
    CatalogNameEnum catalogNameEnum;

    @Override
    public List<Model> listModelTree() {
        List<Model> categories = new ArrayList<>();
        //其他模型树
        Glossary glossary = lakeCatClientService.getGlossary(MODEL);

        //经典模型
//        Category classicCategory = lakeCatClientService.listDbTree();
//        Model classicModel = transformModel(classicCategory);
//        classicModel.setEffective(true);
//        categories.add(classicModel);

        //默认模型 0 是经典  其他的是其他模型id
        DictInfo dictInfo = new DictInfo();
        dictInfo.setDictType(MODEL);
        List<DictInfo> search = dictMapper.search(dictInfo);

        Map<String, List<DictInfo>> maps = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));
        if (glossary != null && !glossary.getCategories().isEmpty()) {
            List<Model> collect = glossary.getCategories().stream().map(data -> {
                Model model = transformModel(data);
                List<DictInfo> dictInfos = maps.get(data.getId().toString());
                if (dictInfos != null && !dictInfos.isEmpty()) {
                    DictInfo dbDictInfo = dictInfos.stream().findFirst().get();
                    if (dbDictInfo.getValue().equals("0")) {
                        model.setEffective(true);
                    }
                }
                model.setCreateTime(data.getCreateTime());
                return model;
            }).collect(Collectors.toList());
            categories.addAll(collect);
        }
        return categories;
    }


    public Model transformModel(Category category) {
        Model model = new Model();
        model.setId(category.getId());
        model.setName(category.getName());
        model.setParentId(category.getParentId());
        model.setChildren(category.getChildren());
        model.setDescription(category.getDescription());
        model.setCreateTime(category.getCreateTime());
        model.setUpdateTime(category.getUpdateTime());
        return model;
    }


    @Override
    public Object searchModelCount(String keyWord, String region, Integer categoryId) {
        LakeCatParam build = LakeCatParam.builder().keywords(keyWord).region(region).categoryId(categoryId).build();
        if (categoryId != null && categoryId > 0) {
            ObjectCount objectCountByCategory = lakeCatClientService.getObjectCountByCategory(build);
            return objectCountByCategory;
        }

        List<CatalogTableCount> tableCountByCatalog = lakeCatClientService.getTableCountByCatalog(build);

        //获取所有的区域  如果不传区域是全部的catalog 得过滤
        if (StringUtils.isEmpty(region)) {
            Set<String> catalogNames = CatalogNameEnum.getCatalogNames();
            tableCountByCatalog = tableCountByCatalog.stream().filter(data -> {
                return catalogNames.contains(data.getCatalogName());
            }).collect(Collectors.toList());
        }

        List<String> catalogList = tableCountByCatalog.stream().map(CatalogTableCount::getCatalogName).collect(Collectors.toList());
        Map<String, List<CatalogTableCount>> collect = tableCountByCatalog.stream().collect(Collectors.groupingBy(CatalogTableCount::getCatalogName));

        Integer rootModelSum = 0;
        List<ObjectCount> cataLogObjectCount = new ArrayList<>();
        for (String catalog : catalogList) {
            AtomicReference<Integer> sum = new AtomicReference<>(0);
            List<CatalogTableCount> catalogTableCounts = collect.get(catalog);
            CatalogTableCount catalogTableCount = catalogTableCounts.stream().findFirst().get();
            List<ObjectCount> tableCount = catalogTableCount.getDatabaseTableCountList().stream().map(data -> {
                sum.set(sum.get() + data.getTableCount());
                ObjectCount objectCount = new ObjectCount(null, data.getDatabaseName(), null, null, data.getTableCount());
                return objectCount;
            }).collect(Collectors.toList());
            if (StringUtils.isNotEmpty(region)) {
                String catalogNameByRegion = catalogNameEnum.getCatalogNameByRegion(region);
                if (StringUtils.isNotEmpty(catalogNameByRegion) && catalog.equals(catalogNameByRegion)) {
                    rootModelSum = rootModelSum + sum.get();
                    ObjectCount objectCount = new ObjectCount(null, "Hive", null, null, sum.get());
                    objectCount.setChildren(tableCount);
                    cataLogObjectCount.add(objectCount);
                    break;
                }
            } else {
                rootModelSum = rootModelSum + sum.get();
                ObjectCount objectCount = new ObjectCount(null, "Hive", null, null, sum.get());
                objectCount.setChildren(tableCount);
                cataLogObjectCount.add(objectCount);
            }
        }

        ObjectCount objectCount = new ObjectCount(0, "经典模型", null, null, rootModelSum);
        objectCount.setChildren(cataLogObjectCount);

        return objectCount;
    }

    @Override
    public void updateDefaultModel(String modelId, boolean isEffective) {
        DictInfo dictInfo = new DictInfo();
        dictInfo.setDictType(MODEL);
        List<DictInfo> search = dictMapper.search(dictInfo);
        //MODEL id 0  0
        dictInfo.setKey(modelId);
        if (isEffective) {
            dictInfo.setValue("0");
        } else {
            dictInfo.setValue("1");
        }
        if (search != null && !search.isEmpty()) {
            Map<String, List<DictInfo>> collect = search.stream().collect(Collectors.groupingBy(DictInfo::getKey));
            List<DictInfo> aDefault = collect.get(modelId);
            if (aDefault != null && !aDefault.isEmpty()) {
                DictInfo dbDictInfo = aDefault.stream().findFirst().get();
                QueryWrapper<DictInfo> queryWrapper = new QueryWrapper<DictInfo>();
                dictInfo.setId(dbDictInfo.getId());
                queryWrapper.eq("id", dbDictInfo.getId());
                dictMapper.update(dictInfo, queryWrapper);
            } else {
                dictMapper.insertDictKey(dictInfo);
            }
        } else {
            dictMapper.insertDictKey(dictInfo);
        }
    }


    @Override
    public Category getModel(Integer id) {
        Category category = lakeCatClientService.getCategory(id);
        return category;
    }

    @Override
    public Category addModel(Model model) {
        model.setGlossaryName(MODEL);
        lakeCatClientService.createCategory(model);
        return null;
    }

    @Override
    public Category updateModel(Model model) {
        lakeCatClientService.updateCategory(model);
        return null;
    }

    @Override
    public void deleteModel(Integer id) {
        lakeCatClientService.deleteCategory(id);
    }
}
