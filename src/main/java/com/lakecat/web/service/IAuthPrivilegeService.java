package com.lakecat.web.service;

import java.util.List;

import com.lakecat.web.entity.AuthenticationReq;

public interface IAuthPrivilegeService {

    Boolean doAuth(AuthenticationReq authenticationReq);

    Boolean doAuth(List<AuthenticationReq> authenticationReqs);

}
