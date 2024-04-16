package com.lakecat.web.service;


import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.DictInfo;

import java.util.List;

public interface IDictService {

    List <DictInfo> dict(String dictType);

    void syncName();

}
