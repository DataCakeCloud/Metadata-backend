package com.lakecat.web.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.AuthorizationResponse;
import io.lakecat.catalog.common.model.AuthorizationType;
import io.lakecat.catalog.common.model.CatalogInnerObject;
import io.lakecat.catalog.common.model.User;
import io.lakecat.catalog.common.plugin.request.AuthenticationRequest;
import io.lakecat.catalog.common.plugin.request.input.AuthorizationInput;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.constant.CommonConts;
import com.lakecat.web.constant.ObjectType;
import com.lakecat.web.entity.AuthenticationReq;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.service.IAuthPrivilegeService;
import com.lakecat.web.service.ILakeCatClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthPrivilegeServiceImpl implements IAuthPrivilegeService {

    @Autowired
    ILakeCatClientService lakeCatClientService;

    @Autowired
    CatalogNameEnum CatalogNameEnum;


    @Override
    public Boolean doAuth(AuthenticationReq authenticationReq) {
        List <AuthorizationInput> list = new ArrayList <>();
        list.add(buildAuthorizationInput(authenticationReq));
        if (authenticationReq.getRegion() == null && authenticationReq.getCatalogName() != null) {
            try {
                authenticationReq.setRegion(CatalogNameEnum.getRegion(authenticationReq.getCatalogName()));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Exception: {}", e.getMessage());
                return false;
            }
        }
        return doAuthByClient(authenticationReq, list);
    }

    private boolean doAuthByClient(AuthenticationReq authenticationReq, List<AuthorizationInput> list) {
        AuthorizationResponse authenticate = null;
        try {
            String s = JSONArray.toJSONString(list);
            System.out.println(s);
            LakeCatClient lakeCatClient = lakeCatClientService.get();
            authenticate = lakeCatClient.authenticate(new AuthenticationRequest(authenticationReq.getProjectId(), false, list));
            return authenticate.getAllowed();
        }catch (Exception e) {
            e.printStackTrace();
            log.error("Exception: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Boolean doAuth(List <AuthenticationReq> authenticationReqs) {
        if (CollectionUtils.isNotEmpty(authenticationReqs)) {
            List <AuthorizationInput> list = new ArrayList <>();
            for (AuthenticationReq req : authenticationReqs) {
                list.add(buildAuthorizationInput(req));
            }
            return doAuthByClient(authenticationReqs.get(0), list);
        }
        return false;
    }

    private AuthorizationInput buildAuthorizationInput(AuthenticationReq authenticationReq) {
        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(Operation.valueOf(authenticationReq.getOperation()));
        User user = new User();
        user.setUserId(authenticationReq.getUserId());
        authorizationInput.setUser(user);
        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(authenticationReq.getProjectId());
        try {
            catalogObject.setCatalogName(authenticationReq.getCatalogName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        String qualifiedName = authenticationReq.getQualifiedName();
        String[] split = qualifiedName.split(CommonConts.SPLIT_DELIMITER);
        switch (ObjectType.getType(qualifiedName)) {
            case REGION:
                catalogObject.setObjectName(split[0]);
                break;
            case DATABASE:
                catalogObject.setDatabaseName(split[1]);
                catalogObject.setObjectName(split[1]);
                break;
            case TABLE:
                catalogObject.setDatabaseName(split[1]);
                catalogObject.setObjectName(split[2]);
                break;
            default:
                break;
        }
        authorizationInput.setCatalogInnerObject(catalogObject);
        log.debug("authorizationInput: {}", JSONObject.toJSONString(authorizationInput));
        return authorizationInput;
    }
}
