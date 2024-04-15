package com.lakecat.web.service;

import java.util.List;

/**
 * Created by slj on 2022/5/25.
 */
public interface DingDingService {

    /**
     * 发送钉钉通知
     *
     * @param shareIdList 接收通知的钉钉号
     * @param message     通知内容
     */
    void notify(List <String> shareIdList, String message);

}
