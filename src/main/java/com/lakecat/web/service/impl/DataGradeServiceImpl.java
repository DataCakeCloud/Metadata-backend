package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.constant.DataGradeEnum;
import com.lakecat.web.constant.DataGradeTypeEnum;
import com.lakecat.web.entity.BatchDataGradeReq;
import com.lakecat.web.entity.DataGrade;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.DataGradeMapper;
import com.lakecat.web.service.IDataGradeService;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.GsonUtil;
import com.lakecat.web.vo.blood.AlterColumnVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataGradeServiceImpl extends ServiceImpl<DataGradeMapper, DataGrade> implements IDataGradeService {

    /**
     * 前端约定字段key
     */
    public static final String DATA_GRADE_KEY = "dataGrade";

    @Resource
    DataGradeMapper dataGradeMapper;

    @Override
    public void batchAddGrades(BatchDataGradeReq batchDataGradeReq)  {
        log.info("addcolumnVo-->{}", GsonUtil.toJson(batchDataGradeReq,false));
        Long tableId = batchDataGradeReq.getTableId();
        List<BatchDataGradeReq.ColumnDataGrade> colsGrade = batchDataGradeReq.getColsGrade();
        List<DataGrade> dataGrades = new ArrayList<>();
        Set<String> duplicateCols = new HashSet<>();
        DataGrade dg;
        String name;
        // maintainer TODO
        for (BatchDataGradeReq.ColumnDataGrade cdg : colsGrade) {
            name = cdg.getName();
            if (StringUtils.isBlank(name)){
                dg = new DataGrade();
                dg.setTableId(tableId);
                dg.setSole(batchDataGradeReq.getSole());
                dg.setGradeType(DataGradeTypeEnum.TABLE.ordinal());
                dg.setName(cdg.getMaintainer());
                //dg.setMaintainer(cdg.getMaintainer());
                dg.setUpdateTime(DateUtil.getDateToStringNow());
                dg.setGrade(DataGradeEnum.checkName(cdg.getDataGrade()));
                dataGrades.add(dg);
                duplicateCols.add(cdg.getName());
            }else {
                if (!duplicateCols.contains(name)) {
                    dg = new DataGrade();
                    dg.setSole(batchDataGradeReq.getSole());
                    dg.setTableId(tableId);
                    dg.setGradeType(DataGradeTypeEnum.FIELD.ordinal());
                    dg.setName(cdg.getName());
                    dg.setMaintainer(cdg.getMaintainer());
                    dg.setUpdateTime(DateUtil.getDateToStringNow());
                    dg.setGrade(DataGradeEnum.checkName(cdg.getDataGrade()));
                    dataGrades.add(dg);
                    duplicateCols.add(cdg.getName());
                }
            }

        }

        dataGradeMapper.batchUpsert(dataGrades);
    }

    @Override
    public List<DataGrade> getByTableId(Long tableId) {
        return dataGradeMapper.getByTableId(tableId);
    }

    @Override
    public List<DataGrade> getByTableSole(String sole) {
        if (StringUtils.isNotEmpty(sole)) {
            return dataGradeMapper.getBySole(sole);
        }
        return null;
    }

    @Override
    public List<DataGrade> getAll() {
        return dataGradeMapper.getAllGrade();
    }

    @Override
    public List<DataGrade> getByGrade(String grade) {
        Map<String, Object> map = getDefaultConditionMap();
        map.put(DataGrade.FN_GRADE, grade);
        return dataGradeMapper.selectByMap(map);
    }

    @Override
    public void updateGrade(DataGrade entity) {
        dataGradeMapper.updateById(entity);
    }

    @Override
    public void updateGradeByTableInfo(TableInfo entity) throws BusinessException {
        Long tableId = entity.getId();
        JSONArray colsJson = JSON.parseArray(entity.getColumns());
        BatchDataGradeReq gradeReq = new BatchDataGradeReq();
        gradeReq.setTableId(tableId);
        gradeReq.setSole(entity.getSole());
        JSONObject colJson;
        String gradeValue;
        for (int i = 0; i < colsJson.size(); i++) {
            colJson = colsJson.getJSONObject(i);
            gradeValue = colJson.getString(DATA_GRADE_KEY);
            if (StringUtils.isNotBlank(gradeValue)) {
                gradeReq.addColumn(BatchDataGradeReq.ColumnDataGrade.builder()
                        .dataGrade(gradeValue)
                        .maintainer(colJson.getString("maintainer"))
                        .name(colJson.getString("name")).build());
            }
        }
        if (CollectionUtils.isNotEmpty(gradeReq.getColsGrade())) {
            batchAddGrades(gradeReq);
        }
    }

    @Override
    public void updateGradeByTableInfo(AlterColumnVo entity) throws BusinessException {
        Long tableId = entity.getId();
        JSONArray colsJson = JSON.parseArray(entity.getColumns());
        BatchDataGradeReq gradeReq = new BatchDataGradeReq();
        gradeReq.setTableId(tableId);
        gradeReq.setTableName(entity.getName());
        gradeReq.setDbName(entity.getDbName());
        gradeReq.setRegion(entity.getRegion());
        JSONObject colJson;
        String gradeValue;
        for (int i = 0; i < colsJson.size(); i++) {
            colJson = colsJson.getJSONObject(i);
            gradeValue = colJson.getString(DATA_GRADE_KEY);
            if (StringUtils.isNotBlank(gradeValue)) {
                gradeReq.addColumn(BatchDataGradeReq.ColumnDataGrade.builder()
                        .dataGrade(gradeValue)
                        .maintainer(colJson.getString("maintainer"))
                        .name(colJson.getString("name")).build());
            }
        }
        if (CollectionUtils.isNotEmpty(gradeReq.getColsGrade())) {
            batchAddGrades(gradeReq);
        }
    }

    @Override
    public void deleteTableById(Long tableId) {
        dataGradeMapper.deleteByTableId(tableId);
    }

    @Override
    public void deleteTableBySole(String sole) {
        dataGradeMapper.deleteByTableSole(sole);
    }

    private Map<String, Object> getDefaultConditionMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(DataGrade.FN_STATUS, 0);
        return map;
    }
}
