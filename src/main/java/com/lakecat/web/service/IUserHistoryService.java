package com.lakecat.web.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lakecat.web.entity.InputInfo;
import com.lakecat.web.entity.TableInfoSearchHistory;
import com.lakecat.web.entity.TableInfoUserInput;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
public interface IUserHistoryService extends IService<TableInfoUserInput> {

    List <TableInfoSearchHistory> getTableName(String userId,Integer size);

    List <InputInfo> getItems(String userId,Integer size);

    JSONObject getItemsJson(String userId);

    boolean deleteItems(String name);

    JSONObject getTopic();


}
