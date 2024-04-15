package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.SyncNameInfo;
import com.lakecat.web.entity.TableTagInfo;
import com.lakecat.web.mapper.DictMapper;
import com.lakecat.web.mapper.TableTagInfoMapper;
import com.lakecat.web.service.IDictService;
import com.lakecat.web.service.TableTagInfoService;
import com.lakecat.web.utils.DSUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author slj
 */
@Service
public class TableTagInfoServiceImpl extends ServiceImpl <TableTagInfoMapper, TableTagInfo> implements TableTagInfoService {


    @Resource
    TableTagInfoMapper tableTagInfoMapper;

    @Override
    public boolean addTableTag(List <TableTagInfo> tableTagInfo) {
        if (!tableTagInfo.isEmpty()) {
            List<TableTagInfo> collect = tableTagInfo.stream().map(data -> {
                data.setSole(data.getSoleKey());
                return data;
            }).collect(Collectors.toList());
            tableTagInfoMapper.batchSaveForTags(collect);
        }
        return true;
    }

    @Override
    public List <TableTagInfo> search(Long tableId) {
        return tableTagInfoMapper.search(tableId);
    }

    @Override
    public List <TableTagInfo> search(String sole) {
        return tableTagInfoMapper.searchBySole(sole);
    }

    @Override
    public void deleteTags(Long id) {
        this.removeById(id);
    }


}
