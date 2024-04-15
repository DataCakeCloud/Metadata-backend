package com.lakecat.web.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.BillOwnerDepartment;
/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
public interface IBillService extends IService<BillOwnerDepartment> {

    BillOwnerDepartment getTableName(String owner);

}
