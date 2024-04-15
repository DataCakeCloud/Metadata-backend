package com.lakecat.web.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.BillOwnerDepartment;
import com.lakecat.web.mapper.BillOwnerMapper;
import com.lakecat.web.service.IBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
@Service
public class BillServiceImpl extends ServiceImpl <BillOwnerMapper, BillOwnerDepartment> implements IBillService {

    @Autowired
    BillOwnerMapper billOwnerMapper;


    @Override
    public BillOwnerDepartment getTableName(String owner) {
        return billOwnerMapper.getDepartmentByOwner(owner);
    }
}
