package com.lakecat.web.service;


import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoForOwner;

import java.util.List;

public interface ITableOwnerService {

    JSONObject getTableNumber(String dictType);

    List <TableInfoForOwner> frequency(String owner, String type);

}
