package com.lakecat.web.service.impl;

import com.lakecat.web.service.AccessGroupService;
import com.lakecat.web.utils.DSUtilForLakecat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AccessGroupServiceImpl implements AccessGroupService {

    @Autowired
    private DSUtilForLakecat dsUtilForLakecat;

    @Override
    public List<String> getUsersByGroupIds(String groupIds) {
        return dsUtilForLakecat.getUserIdsByGroupIds(groupIds);
    }
}
