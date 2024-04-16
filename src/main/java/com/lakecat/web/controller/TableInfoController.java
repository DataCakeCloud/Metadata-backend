package com.lakecat.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.BaseResponseCodeEnum;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.*;
import com.lakecat.web.entity.table.TableOutputInfo;
import com.lakecat.web.entity.table.TablePrivilegeInfo;
import com.lakecat.web.entity.table.TableStorageInfo;
import com.lakecat.web.entity.table.TableSummaryDescInfo;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.response.Response;
import com.lakecat.web.service.*;
import com.lakecat.web.utils.SqlUtils;
import com.lakecat.web.vo.blood.AddColumnVo;
import com.lakecat.web.vo.blood.AlterColumnVo;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.RolePrivilege;
import io.lakecat.catalog.common.plugin.request.GetRoleRequest;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

@RestController
@Slf4j
//@IdentityAuth
@RequestMapping("/metadata/table")
public class TableInfoController {

    @Autowired
    private IAuthPrivilegeService authPrivilegeService;

    @Autowired
    ISwitchService switchService;

    @Autowired
    ITableInfoByHMSService tableInfoByHMSService;

    @Autowired
    ITableInfoService tableInfoService;

    @Autowired
    IDataGradeService dataGradeService;

    @Autowired
    ITableInfoSearchHistoryService tableInfoSearchHistoryService;


    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Autowired
    CatalogNameEnum CatalogNameEnum;

    @Autowired
    private OaService oaService;


    @ApiOperation(value = "数据敏感级别", produces = "application/json;charset=UTF-8")
    @GetMapping("/sensitivityLevel")
    public Response<SensitivityLevel> level() {
        return Response.success(oaService.sensitivityLevelList());
    }

    @ApiOperation(value = "数据敏感级别", produces = "application/json;charset=UTF-8")
    @PostMapping("/addCloumnLevel")
    public Response addCloumnLevel(@RequestBody AddColumnVo addColumnVo) {
        tableInfoService.addCloumnLevel(addColumnVo);
        return Response.success();
    }


    @PostMapping(value = "/add")
    public Response create(@RequestBody TableInfo entity) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            entity.setTenantName(tenantName);
            return tableInfoService.createTable(entity);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail("", e.getMessage(), 500);
        }
    }

    @ApiOperation(value = "校验创建表数据", produces = "application/json;charset=UTF-8")
    @PostMapping(value = "/checkCreate")
    public Response checkCreate(@RequestBody TableInfo entity) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String catalogName = CatalogNameEnum.getCatalogNameByRegion(entity.getRegion());
            entity.setTenantName(userInfo.getTenantName());
            entity.setCatalogName(catalogName);

            GetRoleRequest getRoleRequest=new GetRoleRequest();
            getRoleRequest.setProjectId(entity.getTenantName());
            getRoleRequest.setRoleName(InfTraceContextHolder.get().getUuid());
            LakeCatClient lakeCatClient=iLakeCatClientService.get();
            lakeCatClient.getContext().setProjectId(entity.getTenantName());
            Role role=lakeCatClient.getRole(getRoleRequest);
            RolePrivilege[] rolePrivileges=role.getRolePrivileges();
            boolean allow=false;
            if (rolePrivileges!=null&&rolePrivileges.length>0){
                for (RolePrivilege rolePrivilege:rolePrivileges){
                    if (rolePrivilege.getPrivilege().equalsIgnoreCase(Operation.CREATE_TABLE.getPrintName())&&rolePrivilege.getName().equals(entity.getRegion()+"."+entity.getDbName())){
                        allow=true;
                    }
                }
            }
            if (!allow){
                return Response.fail("", "当前用户组没有该数据库的创建表权限", 500);
            }
            if (tableInfoService.existTable(entity.getRegion(),entity.getDbName(),entity.getName())){
                return Response.fail("", "表已存在，无需重复创建", 500);
            }
            return Response.success(tableInfoByHMSService.checkCreate(entity));
        } catch (Exception e) {
            //e.printStackTrace();
            return Response.fail("", e.getMessage(), 500);
        }
    }

    @PostMapping(value = "/createdb")
    public Response createDB(@RequestBody DatabaseInfo entity) {

        //entity.setUserId(userInfo.getUserId());
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            //return tableInfoService.createDatabase(entity);
            return tableInfoService.createDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/updatedb")
    public Response updateDB(@RequestBody DatabaseInfo entity) {
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            return tableInfoService.updateDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/deletedb")
    public Response deleteDB(@RequestBody DatabaseInfo entity) {
        entity.setProjectId(InfTraceContextHolder.get().getTenantName());
        try {
            return tableInfoService.deleteDatabaseByGateWay(entity);
        } catch (Exception e) {
            return Response.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/dbList")
    @ApiOperation(value = "数据库下拉列表", produces = "application/json;charset=UTF-8")
    public Response getDBList(@ApiParam(name = "region", value = "数据区域", required = true) @RequestParam String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            if (region.equals("")) {
                return Response.success();
            }
            return Response.success(tableInfoService.getDBList(tenantName, region));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }



    @GetMapping(value = "/searchdb")
    public Response searchDbList(@RequestParam String region) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            if (region.equals("")) {
                return Response.success(new ArrayList<>());
            }
            return Response.success(tableInfoService.searchDbList(tenantName, region));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


    @GetMapping(value = "/getdb")
    public Response getDbInfo(@RequestParam String region, @RequestParam String dbName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            if (region.equals("")) {
                return Response.success(new ArrayList<>());
            }
            return Response.success(tableInfoService.getDBByName(tenantName, region, dbName));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }




    @PostMapping(value = "/collect")
    @ApiOperation(value = "收藏接口", produces = "application/json;charset=UTF-8")
    public Response getCollect(@RequestBody JSONObject entity) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            entity.put("userId", userName);
            return Response.success(tableInfoService.setCollect(entity));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


    @RequestMapping(value = "/tableList")
    public Response getDBList(@RequestParam String region, String dbName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            return Response.success(tableInfoByHMSService.getTableList(region, dbName, tenantName, userName));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


    /**
     * 检索 接口
     *
     * @param keyWord 检索字段， region 创建区域， subject 主题， pageNum页面数， pageSize 每页item数
     */
    @RequestMapping(value = "/search")
    public Response search(@RequestParam Integer pageNum, String keyWord, String region, String subject, String dbName,
                           Integer pageSize, String pageToken,Integer categoryId, String secondaryKeywords) {
        try {
            long start = System.currentTimeMillis();
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setUserId(userName);
            tableInfo.setKeyWord(keyWord);
            tableInfo.setRegion(region);
            tableInfo.setSubject(subject);
            tableInfo.setDbName(dbName);
            tableInfo.setPage(pageNum);
            tableInfo.setLimit(pageSize);
            tableInfo.setPageToken(pageToken);
            tableInfo.setCategoryId(categoryId);
            tableInfo.setSecondaryKeywords(secondaryKeywords);
            tableInfo.setTenantName(tenantName);
            Response success = Response.success(tableInfoService.search(tableInfo));
//            Response success = Response.success();
            long end = System.currentTimeMillis();
            System.out.println("请求时长" + (end - start));
            return success;
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }



    @GetMapping(value = "/searchOne")
    public Response searchOne(@RequestParam String region,
                              @RequestParam String dbName,
                              @RequestParam String tableName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            TableInfo tableInfo = new TableInfo();
//            tableInfo.setId(id);
            tableInfo.setUserId(userName);
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);
            return Response.success(tableInfoService.searchOne(tableInfo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/getTable")
    public Response getTable(@RequestParam String region,
                              @RequestParam String dbName,
                              @RequestParam String tableName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setUserId(userName);
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);
            return Response.success(tableInfoService.getTable(tableInfo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/getTableOtherInfo")
    public Response getTableOtherInfo(@RequestParam String region,
                             @RequestParam String dbName,
                             @RequestParam String tableName,
                             @RequestParam(value = "clearCache", defaultValue = "false")  boolean clearCache) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setUserId(userName);
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName).setClearCache(clearCache);
            return Response.success(tableInfoService.getTableOtherInfo(tableInfo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


    @PostMapping(value = "/updateTable")
    public Response updateTable(@RequestBody TableInfo tableInfo) {
        try {
            return Response.success(tableInfoService.updateTable(tableInfo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @RequestMapping(value = "/collectList")
    public Response collectList(@ApiParam(name = "size", value = "展示条数", required = false) @RequestParam(required = false) Long size) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setUserId(userName);
            tableInfo.setSize(size);
            return Response.success(tableInfoService.collectList(tableInfo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/collectPages")
    public Response collectPages(@RequestParam Integer pageNum,
                                 Integer pageSize) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String userName = userInfo.getUserId();
        CollectInfo collectInfo = new CollectInfo();
        collectInfo.setUserId(userName);
        collectInfo.setPageSize(pageSize);
        collectInfo.setPageNum(pageNum);
        return Response.success(tableInfoService.collectPages(collectInfo));
    }



    @RequestMapping(value = "/sync")
    public Response synchronization(@ApiParam(value = "region", required = false) @RequestParam(value = "region", required = false, defaultValue = "ue1") String region,
                                    @ApiParam(value = "flag", required = false) @RequestParam(value = "flag", required = false, defaultValue = "false") Boolean flag) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            tableInfoService.synchronization(tenantName, region, flag,false);
            return Response.success("同步成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


    @GetMapping(value = "/detail")
    public Response detail(String userId, String region, String dbName, String tableName) {
//        TableInfo tableDetail = tableInfoService.getTableDetail(id);
        TableInfo tableIno = new TableInfo();
        tableIno.setRegion(region).setDbName(dbName).setTableName(tableName);
        TableInfo tableDetail = tableInfoService.getTableDetail(tableIno);
//        tableInfoSearchHistoryService.addSearchHistory(id, userId);
        tableInfoSearchHistoryService.addSearchHistory(tableIno, userId);
        return Response.success(tableDetail);

    }


    @GetMapping(value = "/column")
    @ApiOperation(value = "获取表的字段信息", produces = "application/json;charset=UTF-8")
    public Response column(@RequestParam String region, @RequestParam String dbName, @RequestParam String tableName) {

        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String userName = userInfo.getUserId();
        TableInfo tableIno = new TableInfo();
        tableIno.setRegion(region).setDbName(dbName).setName(tableName).setTableName(tableName);
        TableInfo tableDetail = tableInfoService.column(tableIno);
        tableInfoSearchHistoryService.addSearchHistory(tableIno, userName);
        return Response.success(tableDetail);

    }

    @GetMapping(value = "/data")
    public Response data(@RequestParam String table, String region,
                         @ApiParam(name = "size", defaultValue = "5") Integer size) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            return Response.success(tableInfoService.getData(table, region, size, tenantName, userName));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.success(e.getMessage());
        }
    }

    /**
     * 修改表详情
     *
     * @param entity
     * @return
     */
    @PostMapping(value = "/changeDetail")
    public Response changeBusiness(@RequestBody TableInfo entity) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();
        entity.setTenantName(tenantName);
        try {
            boolean flag = tableInfoService.changeTableDetail(entity);
            if (flag) {
                return Response.success("修改成功");
            } else {
                return Response.fail("修改失败");
            }
        } catch (BusinessException e) {
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/check")
    public Response check(@RequestParam String sql, String region) {

        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            String userSql = URLDecoder.decode(new String(Base64.getDecoder().decode(sql)), "UTF-8");
            userSql = SqlUtils.replaceLastSemicolon(userSql);
            String lowCaseSql = SqlUtils.trimLine(userSql).toLowerCase(Locale.ROOT);
            Boolean flag = false;
            if (SqlUtils.matchKeyword(lowCaseSql, " stored as ") || SqlUtils.matchKeyword(lowCaseSql, " location ")) {
                flag = true;
            }
            if (!SqlUtils.matchKeyword(lowCaseSql, " location ")) {
                //为了校验通过，不作路径解析
                userSql += " location 'dummypath'";
            }
            tableInfoService.isRightSQL(userSql, region, tenantName, userName);
            if (flag) {
                return Response.success("校验通过，但不推荐在SQL框内指定[location]及[stored as]配置！");
            }
            return Response.success("校验通过");
        } catch (BusinessException | UnsupportedEncodingException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/addColumn")
    public Response addColumn(@RequestBody TableInfo entity) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            entity.setUserName(userName);
            entity.setTenantName(tenantName);
            dataGradeService.updateGradeByTableInfo(entity);
            boolean b = tableInfoService.addColumns(entity);
            if (b) {
                return Response.success(b);
            } else {
                return Response.fail("字段重复，请检查后添加");
            }
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

/*    *//**
     * 修改表详情字段
     *
     * @param entity
     * @return
     *//*
    @PostMapping(value = "/alterColumn")
    public Response alterColumn(@RequestBody TableInfo entity) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            entity.setUserName(userName);
            entity.setTenantName(tenantName);
            dataGradeService.updateGradeByTableInfo(entity);
            return Response.success(tableInfoByHMSService.alterColumnsByHMS(entity));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }*/

    /**
     * 修改表详情字段
     *
     * @param
     * @return
     */
    @PostMapping(value = "/alterColumn")
    public Response alterColumnNinebot(@RequestBody AlterColumnVo alterColumnVo) {
        try {
            dataGradeService.updateGradeByTableInfo(alterColumnVo);
            return Response.success(tableInfoService.alterColumns(alterColumnVo));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/alterOwner")
    public Response alterOwner(
            @RequestParam(required = false, defaultValue = "ue1") String region,
            @RequestParam(required = false, defaultValue = "") String owner,
            @RequestParam(required = false) String dbName,
            @RequestParam(required = false) String tableName) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            TableInfo tableInfo = new TableInfo();
            tableInfo.setRegion(region).setDbName(dbName).setTableName(tableName);
//            return Response.success(tableInfoService.alterOwnerByLakecat(id, owner, region, tenantName));
            return Response.success(tableInfoService.alterOwnerByLakecat(tableInfo, owner, region, tenantName));
        } catch (BusinessException e) {
            return Response.fail(e.getMessage());
        }
    }


    @GetMapping(value = "/migration")
    public Response migration(@RequestParam(required = true) Long id, @RequestParam(required = false, defaultValue = "ue1") String region, @RequestParam(required = false, defaultValue = "") String owner,
                              @RequestParam(required = false, defaultValue = "payment") String tenantName) {
        try {
            //创建库及表
            return Response.success(tableInfoService.createTableByLakecat(id, owner, region, tenantName));
        } catch (BusinessException e) {
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/batchUpdateGrade")
    public Response batchUpdateGrade(@RequestBody BatchDataGradeReq batchDataGradeReq) {
        try {
            dataGradeService.batchAddGrades(batchDataGradeReq);
            return Response.success();
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping(value = "/test")
    public void test() {
        if (switchService.getSwitch()) {
            System.out.println("LAKECAT");
        } else {
            System.out.println("HMS AND OLAP");
        }

    }

    @RequestMapping(value = "/route")
    public Response route(@RequestBody JSONObject data) {
        JSONObject route = tableInfoService.route(data);
        if (route == null) {
            return Response.fail("请联系管理员添加目录配置");
        }
        return Response.success(route);
    }

    @PostMapping(value = "/tableSummaryDescInfo")
    @ApiOperation(value = "获取表的综合信息", produces = "application/json;charset=UTF-8")
    public Response <TableSummaryDescInfo> tableSummaryDescInfo(@RequestBody TableInfoReq tableInfoReq, @ApiParam(value = "可以只给表ID", required = false) @RequestParam(required = false) Long id) {
        try {
            String tenantName = InfTraceContextHolder.get().getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tableSummaryDescInfo(tableInfoReq, id));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/tableProfileInfo")
    @ApiOperation(value = "获取表的访问信息", produces = "application/json;charset=UTF-8")
    public Response <List <TableUsageProfileGroupByUser>> tableProfileInfo(@RequestBody TableInfoReq tableInfoReq, @ApiParam(value = "最近天数，可不填默认30天", required = false, defaultValue = "30") @RequestParam(required = false) Integer recentlyDays) {
        try {
            if (SqlUtils.containsSqlInjection(tableInfoReq.getDatabaseName())||SqlUtils.containsSqlInjection(tableInfoReq.getTableName())||
                    SqlUtils.containsSqlInjection(tableInfoReq.getRegion())){
                return Response.fail(BaseResponseCodeEnum.SQLINJECTION.getMessage());
            }
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tableProfileInfo(tableInfoReq, recentlyDays));
        } catch (Exception e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/tableStorageInfo")
    @ApiOperation(value = "获取表的存储信息", produces = "application/json;charset=UTF-8")
    public Response <TableStorageInfo> tableStorageInfo(@RequestBody TableInfoReq tableInfoReq, @ApiParam(value = "可以只给表ID", required = false) @RequestParam(required = false) Long id) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tableStorageInfo(tableInfoReq, id));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/tableOutputInfo")
    @ApiOperation(value = "获取表的产出信息", produces = "application/json;charset=UTF-8")
    public Response <TableOutputInfo> tableOutputInfo(@RequestBody TableInfoReq tableInfoReq) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String tenantName = userInfo.getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tableOutputInfo(tableInfoReq));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/tablePrivilegeInfo")
    @ApiOperation(value = "获取表的权限信息", produces = "application/json;charset=UTF-8")
    public Response <List <TablePrivilegeInfo>> tablePrivilegeInfo(@RequestBody TableInfoReq tableInfoReq) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tablePrivilegeInfo(tableInfoReq));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }

    @PostMapping(value = "/tablePrivilegeInfoPages")
    @ApiOperation(value = "获取表的权限信息", produces = "application/json;charset=UTF-8")
    public Response <List <TablePrivilegeInfo>> tablePrivilegeInfoPages(@RequestBody TableInfoReq tableInfoReq) {
        try {
            CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
            String userName = userInfo.getUserId();
            String tenantName = userInfo.getTenantName();
            tableInfoReq.setTenantName(tenantName);
            return Response.success(tableInfoService.tablePrivilegeInfoPages(tableInfoReq));
        } catch (BusinessException e) {
            e.printStackTrace();
            return Response.fail(e.getMessage());
        }
    }


}
