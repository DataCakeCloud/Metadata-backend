package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.entity.InputInfo;
import com.lakecat.web.entity.TableInfoSearchHistory;
import com.lakecat.web.entity.TableInfoUserInput;
import com.lakecat.web.mapper.UserHistoryMapper;
import com.lakecat.web.service.IUserHistoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
@Service
public class UserHistoryServiceImpl extends ServiceImpl <UserHistoryMapper, TableInfoUserInput> implements
        IUserHistoryService {

    @Autowired
    UserHistoryMapper userHistoryMapper;



//    @Override
//    public JSONObject getItemsJson(String userId) {
//        JSONObject jsonObject = new JSONObject();
//
//        List <InputInfo> list = userHistoryMapper.getItem(userId);
//        jsonObject.put("items", list);
//        return jsonObject;
//    }

    @Override
    public JSONObject getItemsJson(String userId) {
        JSONObject jsonObject = new JSONObject();

        List <String> list = userHistoryMapper.getItemString(userId);
        jsonObject.put("items", list);
        return jsonObject;
    }



    @Override
    public List <InputInfo> getItems(String userId,Integer limit) {
        return userHistoryMapper.getItem(userId,limit);
    }

    @Override
    public boolean deleteItems(String name) {
        return userHistoryMapper.deleteItem(name);
    }

    @Override
    public List <TableInfoSearchHistory> getTableName(String userId,Integer size) {
        List<TableInfoSearchHistory> tableName = userHistoryMapper.getTableName(userId, size);
        List<TableInfoSearchHistory> collect = tableName.stream().map(data -> {
            String sole = data.getTableInfoName();
            if (StringUtils.isNotEmpty(sole)) {
                String[] split = sole.split("\\.");
                if (split.length == 3) {
                    data.setRegion(split[0]);
                    data.setDbName(split[1]);
                    data.setTableName(split[2]);
                }
                if (split.length == 2) {
                    data.setDbName(split[0]);
                    data.setTableName(split[1]);
                }
                if (split.length == 1) {
                    data.setTableName(sole);
                }
            }
            return data;
        }).collect(Collectors.toList());
        return collect;
    }


    @Override
    public JSONObject getTopic() {
        JSONObject jsonObject = new JSONObject();
        List <String> topic = userHistoryMapper.getTopic();
        jsonObject.put("topic", topic);
        return jsonObject;
    }

}
