package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.SyncNameInfo;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.mapper.DataGradeMapper;
import com.lakecat.web.mapper.DictMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.IDictService;
import com.lakecat.web.service.ISwitchService;
import com.lakecat.web.utils.DSUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;


/**
 * @author slj
 */
@Service
public class DictServiceImpl extends ServiceImpl <DictMapper, DictInfo> implements IDictService {


    @Resource
    DictMapper dictMapper;

    @Override
    public List <DictInfo> dict(String dictType) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("dict_type", dictType);
        queryWrapper.eq("status", 1);
        return this.list(queryWrapper);
    }

    @Override
    public void syncName() {
        List <SyncNameInfo> syncNameInfos = dictMapper.syncName();
        int count = 0;
        int test = 0;
        System.out.println("开始执行");
        for (SyncNameInfo syncNameInfo : syncNameInfos) {
            System.out.println(syncNameInfo.getDbNameTableName());
            count++;
            if (syncNameInfo.getDbNameTableName().contains(".") && !syncNameInfo.getDbNameTableName().contains("s3")) {

                String[] split = syncNameInfo.getDbNameTableName().split("\\.");
                String dbName = split[0];
                String tableName = split[1];
                JSONObject search = DSUtil.search(tableName, InfTraceContextHolder.get().getAuthentication(), dbName);
                if (search.size() == 0) {
                    System.out.println(syncNameInfo.getDbNameTableName());
                    System.out.println("没有查询出来结果");
                    syncNameInfo.setStatus(2);
                    dictMapper.updateByName(syncNameInfo);
                    continue;
                }
                Integer id = search.getInteger("id");
                String owner = search.getString("owner");
                System.out.println(owner);
                if (id != null && owner != null && (owner.equals("test") || owner.equals("root"))) {
                    test++;
                    System.out.println(syncNameInfo.getDbNameTableName());
                    System.out.println(id);
                    System.out.println(syncNameInfo.getOwner());
                    DSUtil.changeOwner(id, syncNameInfo.getOwner(), InfTraceContextHolder.get().getAuthentication());
                    syncNameInfo.setStatus(1);
                    dictMapper.updateByName(syncNameInfo);
                } else {
                    syncNameInfo.setStatus(2);
                    dictMapper.updateByName(syncNameInfo);
                }
            } else {
                syncNameInfo.setStatus(2);
                dictMapper.updateByName(syncNameInfo);
            }
        }

        System.out.println("总数" + count + "符合要求的数据" + test);
    }
}
