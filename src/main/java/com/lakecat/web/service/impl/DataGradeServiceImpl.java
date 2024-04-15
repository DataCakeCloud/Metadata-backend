package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.constant.DataGradeEnum;
import com.lakecat.web.constant.DataGradeTypeEnum;
import com.lakecat.web.entity.BatchDataGradeReq;
import com.lakecat.web.entity.DataGrade;
import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.DataGradeMapper;
import com.lakecat.web.service.IDataGradeService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.GsonUtil;
import com.lakecat.web.vo.blood.AlterColumnVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    ILakeCatClientService iLakeCatClientService;


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

    @Override
    public Boolean updateTableGrade() {
        List<DataGrade> allTableGrade = dataGradeMapper.getAllTableGrade();
        //更新表等级
        for (DataGrade dataGrade : allTableGrade) {
            String sole = dataGrade.getSole();
            String[] split = sole.split("\\.");
            if (split.length != 3) {
                continue;
            }
            if (StringUtils.isEmpty(dataGrade.getGrade())) {
                continue;
            }
            updateLakecatInfo(split[0], split[1], split[2], dataGrade.getGrade().trim());
        }
        return true;
    }

    public void updateLakecatInfo(String region, String dbName, String table, String grade) {
        TableInfo tableInfo = new TableInfo();
        tableInfo.setRegion(region);
        tableInfo.setDbName(dbName);
        tableInfo.setTableName(table);
        if (grade.equals("1级")) {
            grade = "一级";
        }
        if (grade.equals("2级")) {
            grade = "二级";
        }
        if (grade.equals("3级")) {
            grade = "三级";
        }
        if (grade.equals("4级")) {
            grade = "四级";
        }
        if (grade.equals("5级")) {
            grade = "五级";
        }
        tableInfo.setSecurityLevel(grade);
        LakeCatParam lakeCatParam = new LakeCatParam();
        lakeCatParam.setRegion(region);
        lakeCatParam.setDbName(dbName);
        lakeCatParam.setTableName(table);
        try {
            TableInfo lakeCatTable = iLakeCatClientService.getTable(lakeCatParam);
            String securityLevel = lakeCatTable.getSecurityLevel();
            if (StringUtils.isNotEmpty(securityLevel)) {
                log.info("table already exist securityLevel: " + dbName + "." + table);
                return;
            }
            iLakeCatClientService.updateTable(tableInfo);
            log.info("table update sucess: " + dbName + "." + table);
        } catch (Exception e) {
            log.info("table update fail: " + dbName + "." + table);
        }
    }
}
