package com.lakecat.web.service;

import com.lakecat.web.entity.GrantPrivilegeToUser;
import com.lakecat.web.entity.SensitivityLevel;

import java.util.List;

public interface OaService {
    void sendOaRequest(GrantPrivilegeToUser inputs);
    List<SensitivityLevel> sensitivityLevelList();
    void oaCallback(String requestId,Integer status);
}
