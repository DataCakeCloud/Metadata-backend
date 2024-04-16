package com.lakecat.web.service;

import com.alibaba.fastjson.JSONObject;

public interface ISyncService {

    /**
     * 设置发送报警方式
     * @return
     */
    boolean sync();


    Integer sync(JSONObject context);
}
