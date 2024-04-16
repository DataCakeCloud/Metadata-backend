package com.lakecat.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.BatchDataGradeReq;
import com.lakecat.web.entity.DataGrade;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.vo.blood.AlterColumnVo;

import java.util.List;


public interface IDataGradeService extends IService<DataGrade> {

    void batchAddGrades(BatchDataGradeReq batchDataGradeReq) throws BusinessException;

    List<DataGrade> getByTableId(Long tableId);

    List<DataGrade> getByTableSole(String sole);

    List<DataGrade> getAll();

    /**
     * 根据数据分级查询
     * @param grade
     * @return
     */
    List<DataGrade> getByGrade(String grade);

    void updateGrade(DataGrade entity);

    void updateGradeByTableInfo(TableInfo entity) throws BusinessException;

    void updateGradeByTableInfo(AlterColumnVo alterColumnVo) throws BusinessException;

    void deleteTableById(Long tableId);

    void deleteTableBySole(String sole);
}
