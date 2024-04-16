package com.lakecat.web.service.impl;


import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.config.DingDingConfig;
import com.lakecat.web.http.BaseResponse;
import com.lakecat.web.http.HttpUtil;
import com.lakecat.web.service.DingDingService;
import com.lakecat.web.utils.MD5Utill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * @author fengxiao
 * @date 2021/2/4
 */
@Slf4j
@Service
public class DingDingServiceImpl implements DingDingService {

    @Resource
    private DingDingConfig dingDingConfig;

    public static final String MSG_TYPE = "markdown";//text

    @Override
    public void notify(List <String> shareIdList, String message) {
        //获取token
        JSONObject idToken = getToken();
        if (Objects.isNull(idToken)) {
            return;
        }
        System.out.println(idToken);
        HashMap <String, String> headers = new HashMap <>(1);
        headers.put("token", idToken.getString("data"));
        JSONObject params = new JSONObject();
        params.put("shareIdList", shareIdList);
        params.put("type", MSG_TYPE);
        JSONObject messageFormat = new JSONObject();
        messageFormat.put("title", "权限中心 权限授予通知");
        messageFormat.put("text", message);
        params.put("message", messageFormat);
        System.out.println(dingDingConfig.getDingDingUrl());
        System.out.println(params.toJSONString());
        BaseResponse response = HttpUtil.postWithJson(dingDingConfig.getDingDingUrl(), params.toJSONString(), headers);
    }


    private JSONObject getToken() {
        HashMap <String, String> headers = new HashMap <>(1);
        Long timestamp = System.currentTimeMillis() / 1000;

        String password = dingDingConfig.getPassword();
        String secret = MD5Utill.md5(password + timestamp);
        String url = dingDingConfig.getDingDingTokenUrl()
                + dingDingConfig.getUsername() + "&timestamp=" + timestamp
                + "&secret=" + secret;
        System.out.println(url);
        BaseResponse response = HttpUtil.get(url, null, headers);
        return JSONObject.parseObject(response.getData().toString());
    }
}
