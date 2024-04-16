package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.*;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.InformMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.mapper.TableOwnerMapper;
import com.lakecat.web.service.*;
import com.lakecat.web.utils.CampareJson;
import com.lakecat.web.utils.DSUtilForLakecat;
import com.lakecat.web.utils.OLAPJDBCUtil;
import com.lakecat.web.vo.blood.BloodRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

import static com.lakecat.web.constant.CatalogNameEnum.regionCatalogMapping;
import static com.lakecat.web.utils.DateUtil.date2yyyymmddHHmmss;
import static java.util.stream.Collectors.toList;

/**
 * @author slj
 */
@Service
public class InformServiceImpl extends ServiceImpl <InformMapper, InformInfo> implements IInformService {


    @Resource
    InformMapper informMapper;


    @Autowired
    BloodService bloodService;


    @Autowired
    DingDingService dingDingService;

    @Autowired
    DSUtilForLakecat dsUtilForLakecat;


    @Autowired
    ITableInfoService iTableInfoService;

    @Autowired
    CampareJson campareJson;


    @Autowired
    OLAPJDBCUtil olapJdbcUtil;

    @Resource
    TableInfoMapper tableInfoMapper;


    @Value("${default.tenant.name}")
    private String defaultTenantName;


    @Autowired
    CatalogNameEnum catalogNameEnum;


    @Override
    public JSONObject inform(JSONObject args) {
        /**
         * {"region":"ue1", "databaseName":"db1", "tableName":"table1", "operationType":"create/alter/drop", "operateUser":"shareid", "operateTime": "2022-08-01 04:06:33"}
         */
        String before = args.toJSONString();
        String tableName = args.getString("tableName");
        String dbName = args.getString("dbName");
        String region = args.getString("region");
        String tenantName = args.getString("tenantName");
        //获取table_id
        TableInfo tableInfo = new TableInfo();
        tableInfo.setRegion(region);
        tableInfo.setName(tableName);
        tableInfo.setDbName(dbName);
        TableInfo search = informMapper.search(tableInfo);
        JobAlertBloReq req = new JobAlertBloReq();
        req.setMessage(getMessage(args, search, before));
        BloodRequest bloodRequest = new BloodRequest();
        bloodRequest.setTableName(tableName);
        JSONObject oneTableName = dsUtilForLakecat.getOneTableName(tableName, dbName, region,tenantName);
        if (oneTableName == null) {
            return null;
        }
        String id = oneTableName.getString("id");
        String name = oneTableName.getString("name");
        bloodRequest.setTaskName(name);
//        //获取上下游用户信息
        List <String> list = new ArrayList <>();
//        List <String> list = bloodService.bloodOwners(bloodRequest);
//        list.clear();
        if (!InfTraceContextHolder.gcp()){
            args.put("informList", StringUtils.join(list, ","));
            //发起钉钉提醒
            req.setTaskUrl("https://datastudio.ushareit.org/task/detail?id=" + id + "&name=" + name);
            req.setTableUrl(String.format("https://datastudio.ushareit.org/lakecat/meta-detail?id=%s&region=%s&databaseName=%s&tableName=%s", search.getId(), region, dbName, tableName));
            req.setTaskName(name);
            req.setTableName(tableName);
            List <JobAlertBloReqForUrl> ownerList = new ArrayList <>();
            for (String shareId : list) {
                JobAlertBloReqForUrl jobAlertReqForUrl = new JobAlertBloReqForUrl();
                jobAlertReqForUrl.setOwner(shareId);
                ownerList.add(jobAlertReqForUrl);
            }
            req.setIds(ownerList);
            getInform(req);
        }

        return args;
    }


    /**
     * 第一操作
     */
    public static Map <String, Map <String, String>> message = new HashMap <>();


    public String getMessage(JSONObject args, TableInfo search, String before) {
        StringBuilder sb = new StringBuilder();
        String tableName = args.getString("tableName");
        String dbName = args.getString("dbName");
        String region = args.getString("region");
        String operation = args.getString("operType");
        String operTime = args.getString("operTime");
        String operUser = args.getString("operUser");
        String informList = args.getString("informList");
        String owner = args.getString("operUser");
        String userName = args.getString("operUser");
        String tenantName = args.getString("tenantName");
        try {
            iTableInfoService.preciseSync(owner, region, dbName, tableName, tenantName);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
        //获取table_id
        TableInfo tableInfo = new TableInfo();
        tableInfo.setRegion(region);
        tableInfo.setName(tableName);
        tableInfo.setDbName(dbName);
        TableInfo searchNewOne = informMapper.search(tableInfo);
        if (searchNewOne == null) {
            return "表同步失败";
        }
        if (operation.equals("CREATE")) {
            sb.append(region + "." + dbName + "." + tableName + "已经创建");
        } else if (operation.equals("DROP")) {
            sb.append(region + "." + dbName + "." + tableName + "已经被删除");
            informMapper.deleteTableForBol(tableInfo);
        } else {
            String colOperType = args.getString("colOperType");

            JSONArray jsonArrayNew = JSON.parseArray(searchNewOne.getColumns());
            JSONArray jsonArrayOld = JSON.parseArray(search.getColumns());
            List <JSONObject> jsonArrayNewList = JSON.parseArray(jsonArrayNew.toJSONString(), JSONObject.class);
            List <JSONObject> jsonArrayOldList = JSON.parseArray(jsonArrayOld.toJSONString(), JSONObject.class);

            if (colOperType.equals("ADD")) {
                List <JSONObject> reduce2 = jsonArrayNewList.stream().filter(item -> !jsonArrayOldList.contains(item)).collect(toList());
                for (JSONObject column : reduce2) {
                    sb.append(setMessageADD(column));
                }
            } else {
                JSONObject jsonNew = new JSONObject();
                jsonNew.put("name", jsonArrayNew);
                JSONObject jsonOld = new JSONObject();
                jsonOld.put("name", jsonArrayOld);
                if (jsonArrayNewList.size() != jsonArrayOldList.size()) {
                    return null;
                }
                for (int i = 0; i < jsonArrayNewList.size(); i++) {
                    JSONObject newStrJson = jsonArrayNewList.get(i);
                    JSONObject oldStrJson = jsonArrayOldList.get(i);
                    String newName = newStrJson.getString("name");
                    String oldName = oldStrJson.getString("name");
                    if (!newName.equals(oldName)) {
                        continue;
                    }
                    String s = new CampareJson().campareJsonObject(oldStrJson.toJSONString(), newStrJson.toJSONString());
                    JSONObject result = JSON.parseObject(s);
                    if (result.size() == 0) {
                        continue;
                    }
                    result.put("name", newName);
                    sb.append(setMessageALTER(result));
                }

            }

        }
        //将变更记录到表变更表中
        InformInfo informInfo = new InformInfo();
        informInfo.setTableName(tableName);
        informInfo.setDbName(dbName);
        informInfo.setRegion(region);
        informInfo.setOperation(operation);
        informInfo.setCreatedTime(operTime);
        informInfo.setOperationUser(operUser);
        informInfo.setInformList(informList);
        informInfo.setMessage(sb.toString());
        informInfo.setArgs(args.toJSONString() + "###" + before);
        informMapper.insertForTableChange(informInfo);
        return sb.toString();

    }


    public String setMessageALTER(JSONObject column) {
        StringBuilder sb = new StringBuilder();
        String name = column.getString("name");
        sb.append(String.format("字段名称:%s 操作:元数据变更,", name));
        JSONObject type = column.getJSONObject("type");
        if (type != null) {
            sb.append(String.format("类型:%s-->%s ", type.getString("oldValue"), type.getString("newValue")));
        }
        JSONObject comment = column.getJSONObject("comment");
        if (comment != null) {
            sb.append(String.format("描述:%s-->%s ", comment.getString("oldValue"), comment.getString("newValue")));
        }
        JSONObject dataGrade = column.getJSONObject("dataGrade");
        if (dataGrade != null) {
            sb.append(String.format("安全等级:%s-->%s ", dataGrade.getString("oldValue"), dataGrade.getString("newValue")));
        }

        JSONObject logic = column.getJSONObject("logic");
        if (logic != null) {
            sb.append(String.format("计算逻辑:%s-->%s ", logic.getString("oldValue"), logic.getString("newValue")));
        }
        return sb.toString();
    }


    public String setMessageADD(JSONObject column) {
        String name = column.getString("name");
        String type = column.getString("type");
        String comment = column.getOrDefault("comment", "").toString();
        String dataGrade = column.getOrDefault("dataGrade", "").toString();
        String logic = column.getOrDefault("logic", "").toString();
        return String.format("字段名称:%s 类型:%s 描述:%s 安全等级:%s 计算逻辑:%s \n 操作:增加字段", name, type, comment, dataGrade, logic);
    }


    public boolean getInform(JobAlertBloReq req) {
        List <JobAlertBloReqForUrl> ids = req.getIds();
        String message = req.getMessage();
        String tableName = req.getTableName();
        String taskName = req.getTaskName();
        String tableUrl = req.getTableUrl();
        String taskUrl = req.getTaskUrl();
        for (JobAlertBloReqForUrl idsUrl : ids) {
            List <String> list = new ArrayList <>();
            String alertMarkdown = "### [元数据变更通知]\n";
            alertMarkdown += String.format("任务%s的产出数据集%s发生元数据变更 \n", taskName, tableName);
            alertMarkdown += String.format("%s \n", message);
            alertMarkdown += String.format("\n 元数据详情 [>>](%s) \n", tableUrl);
            alertMarkdown += String.format("\n 任务详情[>>](%s)  \n", taskUrl);
            alertMarkdown += String.format("**权限授权发起时间**:%s  \n", date2yyyymmddHHmmss(0));
            list.add(idsUrl.getOwner());
            dingDingService.notify(list, alertMarkdown);
        }
        return true;
    }

}
