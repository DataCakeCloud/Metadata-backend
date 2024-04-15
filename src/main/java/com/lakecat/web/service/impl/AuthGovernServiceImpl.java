package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.constant.OperationTypeForAuth;
import com.lakecat.web.entity.*;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.AuthGovMapper;
import com.lakecat.web.mapper.LastActivityMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.IAdminRoleService;
import com.lakecat.web.service.IAuthGovernService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Slf4j
@Service
public class AuthGovernServiceImpl extends ServiceImpl <AuthGovMapper, AuthorityGovInfo> implements IAuthGovernService {

    @Autowired
    ILakeCatClientService lakeCatClientService;

    @Autowired
    DSUtilForLakecat dsUtilForLakecat;

    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    TableInfoMapper tableInfoMapper;

    @Autowired
    AuthGovMapper authGovMapper;
    @Autowired
    EmailUtils emailUtils;

    @Autowired
    private IAdminRoleService adminRoleService;

    @Autowired
    CatalogNameEnum catalogNameEnum;


    @Resource
    private LastActivityMapper lastActivityMapper;

    @Value("mail.suffix")
    private String mailSuffix;

    @Override
    public JSONArray list(JSONObject args) {
        String userName = args.getString("userName");
        JSONArray data = dsUtilForLakecat.getUserTree(userName);
        if (data == null) {
            return new JSONArray();
        }
        List <AccessGroup> list = JSON.parseArray(data.toJSONString(), AccessGroup.class);
        setTableName(list);
        return JSON.parseArray(JSON.toJSONString(list));
    }

    @Override
    public List <TableInfo> tables(JSONObject args) {
        String userName = args.getString("userName");
        String currentUser = args.getString("currentUser");
        Integer type = args.getInteger("type");
        String tableNameForkw = args.getString("tableName");
        List <TableInfo> result = new ArrayList <>();
        List <TableInfo> tableInfos = tableInfoMapper.searchListByOwner(userName);
        if (tableInfos == null || tableInfos.isEmpty()) {
            return result;
        }
        //组内用户
        JSONArray dataList = dsUtilForLakecat.getUserChildren(currentUser);
        //获取组内成员
        List <String> listForInner = JSON.parseArray(JSON.toJSONString(dataList), String.class);
        if (listForInner == null || !listForInner.contains(userName)) {
            return result;
        }
        //增加标签逻辑 1:30天仅组内访问，2:外部查询权限，3:外部写权限，4:外部删除权限
        List <String> tableNames = tableInfos.stream().map(table -> {
            return catalogNameEnum.getCatalogName(table.getRegion()) + "." + table.getDbName() + "." + table.getName();
        }).collect(toList());
        if (tableNames.isEmpty()) {
            return result;
        }
        String convert = SqlUtils.convert(tableNames);
        List <String> selectTable = new ArrayList <>();
        //设置展示字段
        switch (type) {
            case 0:
                selectTable = tableNames;
                break;
            case 1:
                //近30天范围人名
                selectTable = lastActivityMapper.searchLastActivityNew(convert);
                break;
            case 2:
                //构建外部查询权限
                selectTable = tableInfoMapper.getRoleByTableSelect("SELECT TABLE", convert);
                break;
            case 3:
                //构建外部写入权限
                selectTable = tableInfoMapper.getRoleByTableSelect("INSERT TABLE", convert);
                break;
            default:
                selectTable = tableInfoMapper.getRoleByTableSelect("DROP TABLE", convert);
        }
        if (selectTable == null || selectTable.isEmpty()) {
            return result;
        }
        List <String> finalSelectTable = selectTable;
        return tableInfos.stream().filter(table -> finalSelectTable
                .contains(catalogNameEnum.getCatalogName(table.getRegion()) + "." + table.getDbName() + "." + table.getName()))
                .filter(table -> (catalogNameEnum.getCatalogName(table.getRegion()) + "." + table.getDbName() + "." + table.getName()).contains(tableNameForkw))
                .map(table -> {
                    String key = catalogNameEnum.getCatalogName(table.getRegion()) + "." + table.getDbName() + "." + table.getName();
                    table.setTableName(key);
                    return table;
                }).collect(toList());
    }

    private void setTableName(List <AccessGroup> data) {
        for (AccessGroup datum : data) {
            Boolean hasChildren = datum.getHasChildren();
            String name = datum.getName();
            datum.setLabel(name);
            datum.setId(datum.getUserId());
            if (Boolean.TRUE.equals(hasChildren)) {
                setTableName(datum.getChildren());
            }
        }
    }


    @Override
    public Response handover(JSONObject args) {

        System.out.println("开始执行");
        String userName = args.getString("userName");
        JSONArray list = args.getJSONArray("list");
        String tenantName = args.getString("tenantName");
        for (int i = 0; i < list.size(); i++) {
            JSONObject jsonObject = list.getJSONObject(i);

            String[] key = jsonObject.getString("tableName").split("\\.");
//            String catalog = jsonObject.getString("tableName").split("\\.")[0];
            String catalog = key[0];
            String region = catalogNameEnum.getRegion(catalog);
            String dbName = key[1];
            String tableName = key[2];
            TableInfo tableInfo = new TableInfo();
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);
            try {
                tableInfoService.alterOwnerByLakecat(tableInfo, userName, region, tenantName);
            } catch (BusinessException e) {
                e.printStackTrace();
            }
        }
        return Response.success();
    }


    @Override
    public void cancelList(JSONObject args) {
        String operator = args.getString("operator");
        String operate = args.getString("operate");
        String reason = args.getString("reason");
        JSONArray permission = args.getJSONArray("permission");
        List <String> listPvc = new ArrayList <>();
        for (int i = 0; i < permission.size(); i++) {
            String cnName = OperationTypeForAuth.get(permission.getString(i)).cnName;
            listPvc.add(cnName);
        }
        JSONArray tableNames = args.getJSONArray("tableNames");
        for (int i = 0; i < tableNames.size(); i++) {
            String tableName = tableNames.getString(i);
            List <RoleTableRelevance> roleListForTables = tableInfoMapper.getRoleListForTables(tableNames.getString(i));
            for (RoleTableRelevance roleListForTable : roleListForTables) {
                String roleName = roleListForTable.getRoleName();
                String userName = roleListForTable.getUserName();
                AuthorityGovInfo authorityGovInfo = AuthorityGovInfo.builder().build()
                        .setOperator(operator)
                        .setTableName(tableName)
                        .setOperate(operate)
                        .setReason(reason)
                        .setPermission(StringUtils.join(listPvc, ","))
                        .setUserName(userName)
                        .setOperatedUser(roleName)
                        .setExecuteStatus(0)
                        .setCycle(5);
                boolean b = authGovMapper.insertForRecord(authorityGovInfo);
                if (b) {
                    JSONObject text = new JSONObject();

                    StringBuilder sb = new StringBuilder();

                    sb.append(operator)
                            .append("取消了您对 ")
                            .append(tableName).append(authorityGovInfo.getPermission()).append("权限").append("\n");
                    sb.append("取消原因:").append(reason).append("\n");
                    sb.append("权限取消操作将在T+1 00:00后生效，如有问题，请您及时联系").append(operator);

                    String[] users = userName.split(",");
                    for (String user : users) {
                        text.put("mail", user + mailSuffix);
                        text.put("topic", "权限治理通知");
                        text.put("content", sb.toString());
                        this.mail(text);
                    }
                }
            }
        }
    }

    @Override
    public boolean cancel(JSONObject args) {
        String operator = args.getString("operator");
        String tableName = args.getString("tableName");
        String operate = args.getString("operate");
        String reason = args.getString("reason");

        JSONArray permission = args.getJSONArray("permission");

        List <String> listPvc = new ArrayList <>();
        for (int i = 0; i < permission.size(); i++) {
            String cnName = OperationTypeForAuth.get(permission.getString(i)).cnName;
            listPvc.add(cnName);
        }
        JSONArray list = args.getJSONArray("list");

        for (int i = 0; i < list.size(); i++) {

            JSONObject tt = list.getJSONObject(i);
            String userName = tt.getString("userName");
            String operatedUser = tt.getString("operatedUser");
            AuthorityGovInfo authorityGovInfo = AuthorityGovInfo.builder().build();
            authorityGovInfo.setOperator(operator);
            authorityGovInfo.setTableName(tableName);
            authorityGovInfo.setOperate(operate);
            authorityGovInfo.setReason(reason);
            authorityGovInfo.setPermission(StringUtils.join(listPvc, ","));
            authorityGovInfo.setUserName(userName);
            authorityGovInfo.setOperatedUser(operatedUser);
            authorityGovInfo.setExecuteStatus(0);
            authorityGovInfo.setCycle(5);
            boolean b = authGovMapper.insertForRecord(authorityGovInfo);
            if (b) {
                JSONObject text = new JSONObject();

                StringBuilder sb = new StringBuilder();

                sb.append(operator)
                        .append("取消了您对 ")
                        .append(tableName).append(authorityGovInfo.getPermission()).append("权限").append("\n");
                sb.append("取消原因:").append(reason).append("\n");
                sb.append("权限取消操作将在T+1 00:00后生效，如有问题，请您及时联系").append(operator);

                String[] users = userName.split(",");
                for (String user : users) {
                    text.put("mail", user + mailSuffix);
                    text.put("topic", "权限治理通知");
                    text.put("content", sb.toString());
                    this.mail(text);
                }
            }
        }
        return true;
    }

    @Override
    public List <RoleTableRelevance> searchOne(JSONObject args) {
        String kwForRole = args.getString("kwForRole");
        String kwForUser = args.getString("kwForUser");
        String kwForPrivilege = args.getString("kwForPrivilege");
        String tableName = args.getString("tableName");
        Integer type = args.getInteger("type");

        List <RoleTableRelevance> roleByTableName = tableInfoMapper.getRoleByTableName(tableName);
        //1 用户 2 角色
        if (type != null) {
            if (type == 1) {
                roleByTableName = roleByTableName.stream().filter(x -> x.getRoleName().startsWith("privilege_single_user_")).collect(toList());
            } else {
                roleByTableName = roleByTableName.stream().filter(x -> !x.getRoleName().startsWith("privilege_single_user_")).collect(toList());
            }
        }
        for (RoleTableRelevance roleTableRelevance : roleByTableName) {
            roleTableRelevance.setLateReadTime(DateUtil.getDateToStringNow());
            roleTableRelevance.setLateWriteTime(DateUtil.getDateToStringNow());
        }
        if (StringUtils.isNotBlank(kwForUser)) {
            roleByTableName = roleByTableName.stream().filter(x -> x.getUserName().contains(kwForUser)).collect(toList());
        }
        if (StringUtils.isNotBlank(kwForRole)) {
            roleByTableName = roleByTableName.stream().filter(x -> x.getRoleName().contains(kwForRole)).collect(toList());

        }
        if (StringUtils.isNotBlank(kwForPrivilege)) {
            roleByTableName = roleByTableName.stream().filter(x -> x.getPrivilege().toLowerCase().contains(kwForPrivilege.toLowerCase())).collect(toList());
        }

        for (RoleTableRelevance roleTableRelevance : roleByTableName) {
            String[] split = roleTableRelevance.getPrivilege().split(",");
            List <String> list = new ArrayList <>();
            for (String option : split) {
                String cnName = OperationTypeForAuth.get(option).cnName;
                list.add(cnName);
            }
            roleTableRelevance.setPrivilege(StringUtils.join(list, ","));
        }
        return roleByTableName;
    }


    @Override
    public List <String> roleList(JSONObject args) {
        return tableInfoMapper.getRoleList(args.getString("tableName"));
    }

    @Override
    public JSONObject mail(JSONObject args) {
        String mail = args.getString("mail");
        String topic = args.getString("topic");
        String content = args.getString("content");
        emailUtils.sendMessage(mail, topic, content);
        return null;
    }

    @Override
    public JSONObject record(JSONObject args) {

        // Integer pageNum, Integer pageSize
        Integer page = args.getInteger("page");
        Integer limit = args.getInteger("limit");

        String tableName = args.getString("tableName");
        //操作发起人
        String operator = args.getString("operator");
        //权限操作
        String operate = args.getString("operate");
        //被操作角色
        String operatedUser = args.getString("operatedUser");
        String executeStatus = args.getString("executeStatus");
        String currentUser = args.getString("currentUser");

        QueryWrapper <AuthorityGovInfo> wrapper = new QueryWrapper <>();
        if (StringUtils.isNotBlank(tableName)) {
            wrapper.like("table_name", tableName);
        }
        if (StringUtils.isNotBlank(operator)) {
            wrapper.like("operator", operator);
        }
        if (StringUtils.isNotBlank(operate)) {
            wrapper.like("operate", operate);
        }
        if (StringUtils.isNotBlank(operatedUser)) {
            wrapper.like("operated_user", operatedUser);
        }
        if (StringUtils.isNotBlank(executeStatus)) {
            wrapper.eq("execute_status", executeStatus);
        }
        wrapper.ne("operator", "系统");
        wrapper.eq("operator", currentUser);

        List <AuthorityGovInfo> list = this.list(wrapper);
        List <AuthorityGovInfo> authorityGovInfos = PageUtil.startPage(list, page, limit);
        JSONObject result = new JSONObject();
        result.put("total", list.size());
        result.put("page", page);
        result.put("limit", limit);
        result.put("index", authorityGovInfos);
        return result;


    }

    @Override
    public boolean del(AuthorityGovInfo args) {
        //状态描述:0:未生效,1:已生效,2:已撤回,3:已驳回
        args.setExecuteStatus(2);
        authGovMapper.updateById(args);
        return true;
    }

    @Override
    public boolean update(JSONObject args) {

        Long id = args.getLongValue("id");
        JSONArray permission = args.getJSONArray("permission");

        List <String> listPvc = new ArrayList <>();
        for (int i = 0; i < permission.size(); i++) {
            String cnName = OperationTypeForAuth.get(permission.getString(i)).cnName;
            listPvc.add(cnName);
        }
        AuthorityGovInfo authorityGovInfo = AuthorityGovInfo.builder().build();
        authorityGovInfo.setId(id);
        authorityGovInfo.setPermission(StringUtils.join(listPvc, ","));
        authGovMapper.updateById(authorityGovInfo);
        return true;
    }


    /**
     * {
     * "roleName": "privilege_single_user_sunlongjiang",
     * "roleInputs": [
     * {
     * "objectName": [
     * "shareit_ue1.ab_sys_dev.ab_table_for_server_exp_to_ck"
     * ],
     * "operation": [
     * "删除表"
     * ],
     * "objectType": "TABLE"
     * }
     * ]
     * }
     */

    @Override
    public void sync() {
        //查询需要执行的数据
//        List <AuthorityGovInfo> search = authGovMapper.search();
//        for (AuthorityGovInfo authorityGovInfo : search) {
//
//            Long id = authorityGovInfo.getId();
//            String permission = authorityGovInfo.getPermission();
//            String operatedUser = authorityGovInfo.getOperatedUser();
//            String tableName = authorityGovInfo.getTableName();
//            JSONObject args = new JSONObject();
//            JSONObject task = new JSONObject();
//            JSONArray roleInputs = new JSONArray();
//            JSONArray objectName = new JSONArray();
//            JSONArray operation = new JSONArray();
//            String objectType = "TABLE";
//            objectName.add(tableName);
//            List <String> permissions = new ArrayList <>(Arrays.asList(permission.split(",")));
//            operation.addAll(permissions);
//            task.put("objectName", objectName);
//            task.put("operation", operation);
//            task.put("objectType", objectType);
//            roleInputs.add(task);
//            args.put("roleName", operatedUser);
//            args.put("roleInputs", roleInputs);
//            System.out.println(args);
//            RevokePrivilegeFromRole currentUser = JSON.parseObject(args.toJSONString(), RevokePrivilegeFromRole.class);
//            adminRoleService.revokePrivilegeFromRole(currentUser, "shareit");
//            authGovMapper.updateByIdForSync(id);
//        }


    }


}
