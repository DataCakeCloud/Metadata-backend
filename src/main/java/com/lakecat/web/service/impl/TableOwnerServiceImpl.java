package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoForOwner;
import com.lakecat.web.entity.TableOwnerInfo;
import com.lakecat.web.mapper.TableOwnerMapper;
import com.lakecat.web.service.ITableOwnerService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author slj
 */
@Service
public class TableOwnerServiceImpl extends ServiceImpl <TableOwnerMapper, TableOwnerInfo> implements ITableOwnerService {


    @Resource
    TableOwnerMapper tableOwnerMapper;




    @Override
    public JSONObject getTableNumber(String owner) {

        System.out.println("请求参数为" + owner);
        JSONObject result = new JSONObject();
        int tableNum = tableOwnerMapper.searchTableNum(owner);
        int tableNumAll = tableOwnerMapper.searchTableNumAll();
        int privilegeNum = tableOwnerMapper.privilegeNum(owner);
        int privilegeNumAll = tableOwnerMapper.privilegeNumAll();
        int privilegeUser = tableOwnerMapper.privilegeUser(owner);
        int privilegeUserAll = tableOwnerMapper.privilegeUserAll();
        Long storageSize = tableOwnerMapper.storageSize(owner);
        Long storageSizeAll = tableOwnerMapper.storageSizeAll();
        System.out.println("请求值为" + storageSize);
        result.put("tableNum", tableNum);
        result.put("tableNumAll", tableNumAll);
        result.put("storageSize", storageSize);
        result.put("storageSizeAll", storageSizeAll);
        result.put("privilegeUser", privilegeUser);
        result.put("privilegeUserAll", privilegeUserAll);
        result.put("privilegeNum", privilegeNum);
        result.put("privilegeNumAll", privilegeNumAll);
        return result;
    }

    @Override
    public List <TableInfoForOwner> frequency(String owner, String type) {


        /**
         * 1:高频访问表top10,2:全局高频访问表top10,3:owner表访问top10
         */

        if (type.equals("1")) {
            return tableOwnerMapper.ownerTableTopN(owner);
        } else if (type.equals("2")) {
            return tableOwnerMapper.tableAllTopN(owner);
        } else {
            return tableOwnerMapper.tableOwnerTopN(owner);
        }
    }
}
