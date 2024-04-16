package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.BillOwnerDepartment;
import org.apache.ibatis.annotations.*;
/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-13
 */
@Mapper
public interface BillOwnerMapper extends BaseMapper <BillOwnerDepartment> {


    @Select("select owner,cluster,department from bill_owner_department where owner=#{owner}")
    BillOwnerDepartment getDepartmentByOwner(@Param("owner") String owner);

}
