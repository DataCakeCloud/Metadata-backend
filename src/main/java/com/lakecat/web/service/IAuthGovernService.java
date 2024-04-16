package com.lakecat.web.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.AuthorityGovInfo;
import com.lakecat.web.entity.RoleTableRelevance;
import com.lakecat.web.entity.TableForCache;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.response.Response;

import java.util.List;

public interface IAuthGovernService {

    JSONArray list(JSONObject args);

    List <TableInfo>  tables(JSONObject args);

    Response handover(JSONObject args);


    boolean cancel(JSONObject args);


    List <RoleTableRelevance> searchOne(JSONObject args);

    List <String> roleList(JSONObject args);


    JSONObject mail(JSONObject args);


    JSONObject record(JSONObject args);


    boolean del(AuthorityGovInfo args);


    boolean update(JSONObject args);


    void sync();


    void cancelList(JSONObject args);

}
