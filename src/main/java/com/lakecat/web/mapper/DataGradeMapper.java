package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.DataGrade;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface DataGradeMapper extends BaseMapper <DataGrade> {

    /**
     * 根据表ID获取 数据等级对象
     *
     * @param tableId
     * @return
     */
    @Select("SELECT * from data_grade where table_id=${tableId} and status=0")
    List <DataGrade> getByTableId(@Param("tableId") Long tableId);

    @Select("SELECT * from data_grade where sole=#{sole} and status=0")
    List <DataGrade> getBySole(@Param("sole") String sole);

    /**
     * 根据表ID获取 数据等级对象
     *
     * @param tableId
     * @return
     */
    @Delete("delete from data_grade where table_id=#{tableId}")
    boolean deleteByTableId(@Param("tableId") Long tableId);

    @Delete("delete from data_grade where sole=#{sole}")
    boolean deleteByTableSole(@Param("sole") String sole);

    /**
     * 根据数据分级查询
     *
     * @param grade
     * @return
     */
    @Select("SELECT * from data_grade where grade=${grade} and status=0 order by id")
    List <DataGrade> getByGrade(@Param("grade") String grade);

    @Select("SELECT * from data_grade where status=0 order by id")
    List <DataGrade> getAllGrade();

    /**
     * 批量插入更新
     *
     * @param list
     * @return
     */
    @Select({"<script>" +
            "insert into data_grade\n" +
            "        <trim prefix=\"(\" suffix=\")\" >\n" +
            "            sole, grade_type, grade, name, maintainer, status, update_time\n" +
            "        </trim>\n" +
            "        values\n" +
            "        <foreach  collection=\"list\" item=\"item\" index=\"index\" separator=\",\">\n" +
            "            <trim prefix=\" (\" suffix=\")\" suffixOverrides=\",\" >\n" +
            "                #{item.sole,jdbcType=VARCHAR},\n" +
            "                #{item.gradeType,jdbcType=INTEGER},\n" +
            "                #{item.grade,jdbcType=VARCHAR},\n" +
            "                #{item.name,jdbcType=VARCHAR},\n" +
            "                #{item.maintainer,jdbcType=VARCHAR},\n" +
            "                #{item.status,jdbcType=INTEGER},\n" +
            "                #{item.updateTime,jdbcType=TIMESTAMP}\n" +
            "            </trim>\n" +
            "        </foreach>\n" +
            "        on duplicate key update\n" +
            "        grade_type= VALUES(grade_type),\n" +
            "        grade=VALUES(grade),\n" +
            "        maintainer=VALUES(maintainer),\n" +
            "        status=VALUES(status),\n" +
            "        update_time=VALUES(update_time)" +
            "</script>"})
    Integer batchUpsert(@Param("list") List <DataGrade> list);

    @Select("SELECT * from data_grade where sole is not null and grade_type =1  and status=0 ")
    List<DataGrade> getAllTableGrade();
}
