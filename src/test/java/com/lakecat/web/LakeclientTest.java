package com.lakecat.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.AdminRoleInfo;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.entity.TableProfileInfoReq;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.utils.DateUtil;
import io.lakecat.catalog.client.CatalogUserInformation;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.LakeCatConf;
import io.lakecat.catalog.common.ObjectType;
import io.lakecat.catalog.common.Operation;
import io.lakecat.catalog.common.model.*;
import io.lakecat.catalog.common.plugin.request.*;
import io.lakecat.catalog.common.plugin.request.input.AuthorizationInput;
import io.lakecat.catalog.common.plugin.request.input.CatalogInput;
import io.lakecat.catalog.common.plugin.request.input.DatabaseInput;
import io.lakecat.catalog.common.plugin.request.input.RoleInput;
import org.apache.commons.collections.CollectionUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hive.jdbc.HiveConnection;
import org.apache.hive.jdbc.HiveStatement;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.sql.DriverManager;
import java.text.MessageFormat;
import java.util.*;

/**
 * lakecat.server.url=lakecat-catalog.datacake.cloud
 * lakecat.server.port=80
 * lakecat.server.user=bdp
 * lakecat.server.password=bdp
 */
public class LakeclientTest {
    public static LakeCatClient createClient() {
        Configuration conf = new Configuration();
        conf.set(LakeCatConf.CATALOG_HOST, "lakecat-catalog.datacake.cloud");//"lakecat-catalog.datacake.cloud");
        conf.setInt(LakeCatConf.CATALOG_PORT, 80);
        conf.set(CatalogUserInformation.LAKECAT_USER_NAME, "bdp");
        conf.set(CatalogUserInformation.LAKECAT_USER_PASSWORD, "bdp");
        LakeCatClient lakeCatClient = LakeCatClient.getInstance(conf, true);
        //lakeCatClient.getContext().setProjectId("wanglltest_authority_2");
        lakeCatClient.getContext().setProjectId("ecom");
        return lakeCatClient;
    }

    public static void listDatacake(){
        ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId("shareit");
        try {
            listDatabasesRequest.setCatalogName("googlecloud_asia-southeast1");
            PagedList<Database> databasePagedList=createClient().listDatabases(listDatabasesRequest);
            System.out.println(databasePagedList);
        } catch (Exception e) {
            System.out.println("");
        }
    }
    public static void grantPrivilegeToRole() {
        LakeCatClient lakeCatClient = createClient();
        AlterRoleRequest request = new AlterRoleRequest();
        request.setProjectId(lakeCatClient.getProjectId());
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectType(ObjectType.TABLE.name());
        roleInput.setObjectName("aws_us-east-1.tpcds_10g.promotion");
        roleInput.setRoleName("grouplcm2uqx6");
        roleInput.setOperation(Operation.DROP_TABLE);
        request.setInput(roleInput);
        lakeCatClient.grantPrivilegeToRole(request);
    }




    public static boolean doAuth(Operation operation, String catalog, String objectName, String dbName) {
        LakeCatClient lakeCatClient = createClient();
        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(operation);
        User user = new User();
        user.setUserId("wanglw");
        authorizationInput.setUser(user);

        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(lakeCatClient.getProjectId());
        catalogObject.setCatalogName(catalog);
        catalogObject.setObjectName(objectName);
        catalogObject.setDatabaseName(dbName);
        authorizationInput.setCatalogInnerObject(catalogObject);
        AuthorizationResponse authenticate = lakeCatClient.authenticate(new AuthenticationRequest(lakeCatClient.getProjectId(), false, com.google.common.collect.Lists.newArrayList(authorizationInput)));
        return authenticate.getAllowed();
    }

    public static Set<String> showRolePrivaleete(String rolename) {
        Set<String> stringList= Sets.newHashSet();
        LakeCatClient lakeCatClient = createClient();
        GetRoleRequest request = new GetRoleRequest();
        request.setProjectId(lakeCatClient.getProjectId());
        request.setRoleName(rolename);
        Role role = lakeCatClient.getRole(request);
        boolean find=false;
        for (RolePrivilege rolePrivilege : role.getRolePrivileges()) {
            if (rolePrivilege.getName().equals("shareit_ue1.ads_dmp.dwd_ads_user_applist_array")||rolePrivilege.getName().equals("shareit_ue1.ads_dmp.dwd_ads_user_applist_detail_day_v2")){
                stringList.add(rolename+"-->"+rolePrivilege.getName());
                find=true;
                break;
            }


        }
       /* if (find){
            if (role.getToUsers()!=null&&role.getToUsers().length>0){
                for (String s:role.getToUsers()){
                    stringList.add(s);
                }
            }
        }*/
        return stringList;
    }

    public static void addActor(String uuid) {
        try {
            LakeCatClient lakeCatClient = createClient();
            CreateCatalogRequest createCatalogRequest = new CreateCatalogRequest();
            createCatalogRequest.setProjectId(lakeCatClient.getProjectId());
            CatalogInput catalogInput = new CatalogInput();
            catalogInput.setCatalogName(uuid);
            catalogInput.setDescription("ue1");
            catalogInput.setOwnerType("USER");
            catalogInput.setOwner("luhongyu");
            createCatalogRequest.setInput(catalogInput);
            lakeCatClient.createCatalog(createCatalogRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deletePrivlige(String roleName, String objectName, Operation operation) {
        LakeCatClient lakeCatClient = createClient();
        AlterRoleRequest request = new AlterRoleRequest();
        request.setProjectId("shareit");
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectType("CATALOG");
        roleInput.setRoleName(roleName);
        roleInput.setObjectName(objectName);
        roleInput.setOperation(operation);
        request.setInput(roleInput);
        lakeCatClient.revokePrivilegeFromRole(request);
    }

    public static void addUserToRole(String userId, String rolename) {
        LakeCatClient lakeCatClient = createClient();
        AlterRoleRequest request = new AlterRoleRequest();
        RoleInput roleInput = new RoleInput();
        roleInput.setUserId(new String[]{userId});
        roleInput.setRoleName(rolename);
        request.setInput(roleInput);
        request.setProjectId(lakeCatClient.getProjectId());
        request.setRoleName(rolename);
        lakeCatClient.grantRoleToUser(request);
    }

    public static void testShow() {
        LakeCatClient lakeCatClient = createClient();
        List<String> uuids = Lists.newArrayList();
        ShowPermObjectsRequest showPermObjectsRequest = new ShowPermObjectsRequest();
        showPermObjectsRequest.setObjectType("CATALOG");
        showPermObjectsRequest.setProjectId(lakeCatClient.getProjectId());
        showPermObjectsRequest.setUserId("hanzenggui");
        PagedList<String> p = lakeCatClient.showPermObjectsByUser(showPermObjectsRequest);
        if (p != null && p.getObjects() != null && p.getObjects().length > 0) {
            uuids = Arrays.asList(p.getObjects());
        }
        System.out.println(uuids);
    }

    public static Role[] showRoles(){
        LakeCatClient lakeCatClient = createClient();
        ShowRolesRequest request = new ShowRolesRequest();
        request.setProjectId("shareit");
        Role[] roles = lakeCatClient.showRoles(request).getObjects();
        return  roles;
    }

    public static void testShowRole() {
        LakeCatClient lakeCatClient = createClient();
        ShowRolesRequest request = new ShowRolesRequest();
        request.setUserId("liyunsong");
        request.setProjectId(lakeCatClient.getProjectId());
        request.setUserId("liyunsong");
        Role[] roles = lakeCatClient.showRoles(request).getObjects();
        System.out.println(roles);
    }

    public static void testgetRole() {
        LakeCatClient lakeCatClient = createClient();
        GetRoleRequest request = new GetRoleRequest();
        request.setProjectId("ninebot");
        request.setRoleName("groupnnsbhtk5");
        Role role = lakeCatClient.getRole(request);
        System.out.println(role);
    }

    public static void removeUser(String  roleName,String userID){
        AlterRoleRequest request = new AlterRoleRequest();
        request.setProjectId("ninebot");
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectName("");
        roleInput.setObjectType("");
        roleInput.setOperation(Operation.ALTER_ROLE);
        roleInput.setOwnerUser("");
        roleInput.setRoleName(roleName);
        roleInput.setUserId(new String[]{userID});
        request.setInput(roleInput);
        createClient().revokeRoleFromUser(request);
    }

    public static void createRole(String roleName) {
        try {
            LakeCatClient lakeCatClient = createClient();
            CreateRoleRequest requestCreate = new CreateRoleRequest();
            requestCreate.setProjectId(lakeCatClient.getProjectId());
            RoleInput roleInputCreate = new RoleInput();
            roleInputCreate.setOwnerUser("SYSTEM");
            roleInputCreate.setRoleName(roleName);
            requestCreate.setInput(roleInputCreate);
            lakeCatClient.createRole(requestCreate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Operation> operation = new HashMap<>();

    static {
        operation.put("查询", Operation.SELECT_TABLE);
        operation.put("编辑", Operation.ALTER_TABLE);
        operation.put("描述", Operation.DESC_TABLE);
        operation.put("插入", Operation.INSERT_TABLE);
        operation.put("删除", Operation.DROP_TABLE);
    }

    public static void toDbPrivilegeForRole(String objectname, String role) {
        AlterRoleRequest requestAlter = new AlterRoleRequest();
        requestAlter.setRoleName(role);
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectName(objectname);
        roleInput.setObjectType(ObjectType.DATABASE.name());
        roleInput.setRoleName(role);
        roleInput.setOperation(Operation.CREATE_TABLE);
        requestAlter.setInput(roleInput);
        LakeCatClient lakeCatClient = createClient();
        requestAlter.setProjectId(lakeCatClient.getProjectId());

        lakeCatClient.grantPrivilegeToRole(requestAlter);
    }
    public static void toRegionPrivilegeForRole(String objectname, String role) {
        AlterRoleRequest requestAlter = new AlterRoleRequest();
        requestAlter.setRoleName(role);
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectName(objectname);
        roleInput.setObjectType(ObjectType.CATALOG.name());
        roleInput.setRoleName(role);
        roleInput.setOperation(Operation.CREATE_DATABASE);
        requestAlter.setInput(roleInput);
        LakeCatClient lakeCatClient = createClient();
        requestAlter.setProjectId(lakeCatClient.getProjectId());

        lakeCatClient.grantPrivilegeToRole(requestAlter);
    }

    public static void toTablePrivilegeForRole(String objectname, String role) {
        AlterRoleRequest requestAlter = new AlterRoleRequest();
        requestAlter.setRoleName(role);
        for (Operation value : operation.values()) {
            RoleInput roleInput = new RoleInput();
            roleInput.setObjectName(objectname);
            roleInput.setObjectType(ObjectType.TABLE.name());
            roleInput.setRoleName(role);
            roleInput.setOperation(value);
            requestAlter.setInput(roleInput);
            LakeCatClient lakeCatClient = createClient();
            requestAlter.setProjectId(lakeCatClient.getProjectId());

            lakeCatClient.grantPrivilegeToRole(requestAlter);
        }
    }
    public static BigInteger getUsageProfilesByTable(TableProfileInfoReq tableProfileInfoReq) throws BusinessException {
        LakeCatClient lakeCatClient = createClient();
        GetTableUsageProfileRequest request = new GetTableUsageProfileRequest();
        request.setProjectId(lakeCatClient.getProjectId());
        request.setCatalogName("aws_us-east-1");
        request.setDatabaseName(tableProfileInfoReq.getDatabaseName());
        request.setTableName(tableProfileInfoReq.getTableName());
        if (tableProfileInfoReq.getStartTimestamp() != null) {
            request.setStartTimestamp(tableProfileInfoReq.getStartTimestamp());
        }
        if (tableProfileInfoReq.getEndTimestamp() != null) {
            request.setEndTimestamp(tableProfileInfoReq.getEndTimestamp());
        }
        request.setUserId(tableProfileInfoReq.getUserId());
        PagedList <TableUsageProfile> tableUsageProfile = lakeCatClient.getTableUsageProfile(request);
        if (tableUsageProfile != null && tableUsageProfile.getObjects() != null && tableUsageProfile.getObjects().length > 0) {
            return tableUsageProfile.getObjects()[0].getSumCount();
        }
        return BigInteger.valueOf(0);
    }
    /**
     * BufferedReader bufferedReader=new BufferedReader(new FileReader("D:\\data\\c.txt"));
     * String line="";
     * while ((line=bufferedReader.readLine())!=null){
     * line=line.replaceAll("|","");
     * line=line.trim();
     * }
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        toRegionPrivilegeForRole("*","ecom_admin");
        toDbPrivilegeForRole("*.*","ecom_admin");
        toTablePrivilegeForRole("*.*.*","ecom_admin");
        listDatacake();
        LakeCatClient lakeCatClient=createClient();
        GetDatabaseRequest getDatabaseRequest=new GetDatabaseRequest();
        getDatabaseRequest.setProjectId(lakeCatClient.getProjectId());
        getDatabaseRequest.setCatalogName("aws_us-east-1");
        getDatabaseRequest.setDatabaseName("testDb");
        Database database=lakeCatClient.getDatabase(getDatabaseRequest);
        System.out.println(database.getOwner());
        AlterDatabaseRequest alterDatabaseRequest=new AlterDatabaseRequest();
        alterDatabaseRequest.setProjectId(lakeCatClient.getProjectId());
        alterDatabaseRequest.setCatalogName("aws_us-east-1");
        alterDatabaseRequest.setDatabaseName("testDb");
        DatabaseInput databaseInput=new DatabaseInput();
        databaseInput.setAccountId(database.getAccountId());
        databaseInput.setAuthSourceType(database.getAuthSourceType());
        databaseInput.setCatalogName(database.getCatalogName());
        databaseInput.setCreateTime(database.getCreateTime());
        databaseInput.setDatabaseName(database.getDatabaseName());
        databaseInput.setDescription(database.getDescription());
        databaseInput.setLocationUri(database.getLocationUri());
        databaseInput.setOwner("hzg");
        databaseInput.setOwnerType(database.getOwnerType());
        databaseInput.setParameters(database.getParameters());
        alterDatabaseRequest.setInput(databaseInput);
        lakeCatClient.alterDatabase(alterDatabaseRequest);
        database=lakeCatClient.getDatabase(getDatabaseRequest);
        System.out.println(database.getOwner());
        //testgetRole();
       // removeUser("groupnnsbhtk5","zhuzhe");
       // testgetRole();
        /*Role[] roles=showRoles();
        Set<String> stringList=Sets.newHashSet();
        for (Role role:roles){
            Set<String> a=showRolePrivaleete(role.getRoleName());
            if (CollectionUtils.isNotEmpty(a)){
                stringList.addAll(a);
            }
        }
        stringList.forEach(s -> {
            System.out.println(s);
        });*/
       /* TableProfileInfoReq tableProfileInfoReq=new TableProfileInfoReq();
        tableProfileInfoReq.setStartTimestamp(0l);
        tableProfileInfoReq.setEndTimestamp(System.currentTimeMillis());
        tableProfileInfoReq.setDatabaseName("testsgt345");
        tableProfileInfoReq.setRegion("aws_us-east-1");
        tableProfileInfoReq.setTableName("sgttable");
        tableProfileInfoReq.setUserId("");
        getUsageProfilesByTable(tableProfileInfoReq);*/
        //grantPrivilegeToRole();
   /*     String url = "jdbc:hive2://gateway-test.ushareit.org:10009/tpcds_10g;auth=noSasl;user={0}?kyuubi.engine.type=JDBC;kyuubi.session.cluster.tags=name:aws,region:us-east-1;kyuubi.engine.jdbc.connection.user={1};kyuubi.engine.jdbc.connection.provider=HiveConnectionProvider";
        url= MessageFormat.format(url,"groupfw2essgt","hanzenggui");
        HiveConnection conn = (HiveConnection) DriverManager.getConnection(url);
        HiveStatement stmt = (HiveStatement) conn.createStatement();

        *//*boolean hasResult = stmt.execute(sql);
        System.out.println("hasResult: " + hasResult);
*//*

        String sqlcreatedb = "CREATE DATABASE IF NOT EXISTS test_hzgdb0726 COMMENT 'This is a sample database1 for testing purposes' " ;
        boolean hasResult = stmt.execute(sqlcreatedb);

        System.out.println("hasResult: " + hasResult);

        String sqlcreatetable = "create table test_hzgdb0726.test_hzg0726(id INT)ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE";
         hasResult = stmt.execute(sqlcreatetable);
        System.out.println("hasResult: " + hasResult);
        stmt.close();
        conn.close();*/

/*        LakeCatClient lakeCatClient=createClient();
        ListTablesRequest listTablesRequest = new ListTablesRequest();
        listTablesRequest.setCatalogName("aws_us-east-1");
        listTablesRequest.setDatabaseName("testsgt345");
        listTablesRequest.setProjectId(lakeCatClient.getProjectId());
        listTablesRequest.setMaxResults(1000);
        String pageToken = "";
        int i = 0;
        while (pageToken != null) {
            if (!"".equals(pageToken)) {
                listTablesRequest.setNextToken(pageToken);
            }
            try {
                PagedList <Table> tablePagedList = lakeCatClient.listTables(listTablesRequest);
                Table[] tables = tablePagedList.getObjects();
                pageToken = tablePagedList.getNextMarker();
                i++;

            } catch (Exception e) {
                System.out.println("报错的信息" );
                e.printStackTrace();
            }

        }*/
       /* AlterRoleRequest request = new AlterRoleRequest();
        request.setProjectId(lakeCatClient.getProjectId());
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectType(ObjectType.CATALOG.name());
        //roleInput.setObjectName(regionInfo.get(objectNames[i]).getCatalogName());
        roleInput.setOperation(Operation.CREATE_DATABASE);
        //roleInput.setRoleName(roleName);
        request.setInput(roleInput);*/
        /*ShowRolesRequest request = new ShowRolesRequest();
        request.setUserId("linyang");
        request.setProjectId(lakeCatClient.getProjectId());
        Role[] roles = lakeCatClient.showRoles(request).getObjects();
        System.out.println(roles);
        lakeCatClient = createClient();
        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(Operation.ALTER_TABLE);
        User user = new User();
        user.setUserId("linyang");
        authorizationInput.setUser(user);

        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(lakeCatClient.getProjectId());
        catalogObject.setCatalogName("shareit_ue1");
        catalogObject.setObjectName("dws_user_app_install_from_applist_day");
        catalogObject.setDatabaseName("ecom_dws");
        authorizationInput.setCatalogInnerObject(catalogObject);
        AuthorizationResponse authenticate = lakeCatClient.authenticate(new AuthenticationRequest(lakeCatClient.getProjectId(), false, com.google.common.collect.Lists.newArrayList(authorizationInput)));
        boolean a= authenticate.getAllowed();
        System.out.println(a);*/
        //addActor("azcigywe5");
        //addActor("ac2s1skx6");
        //doAuth(Operation.ALTER_TABLE,"shareit_sg1","tablename223344","default");
        //createRole("Jollymax_analysis");
        //addUserToRole("libinglun","sg1_common");
        //toTablePrivilegeForRole("shareit_sg1.jol_ods.*","Jollymax_analysis");
        //toTablePrivilegeForRole("shareit_sg1.ods.*","Jollymax_analysis");
        //toTablePrivilegeForRole("shareit_sg1.analyst.*","Jollymax_analysis");
        // toDbPrivilegeForRole("shareit_sg1.*","sg1_common");
        // createRole("sg1_common");
  /*      BufferedReader bufferedReader=new BufferedReader(new FileReader("D:\\data\\c.txt"));
        String line="";
        while ((line=bufferedReader.readLine())!=null){
            line=line.replaceAll("\\|","");
            line=line.trim();
            addUserToRole(line,"sg1_common");
        }*/
        //toTablePrivilegeForRole("shareit_sg1.*.*","sg1_common");
       /* LakeCatClient lakeCatClient=createClient();
        ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId("test0612");
        listDatabasesRequest.setCatalogName("shareit_ue1");

        PagedList<Database> databasePagedList;

        try {
            databasePagedList = lakeCatClient.listDatabases(listDatabasesRequest);
        } catch (Exception exception) {
            System.out.println("1");
        }
        System.out.println("2");*/
        //addUserToRole("huangkai","privilege_single_user_xuebotao");
        //testShowRole();
        //testShow();
        //LakeCatClient lakeCatClient=new LakeCatClient("lakecat-catalog.datacake.cloud",80);
     /*   String rolename="rolename_user_privilege";
        String objectname="axa30xmgw";
        Operation operation=Operation.ALTER_CATALOG;
        showRolePrivaleete(rolename);
        deletePrivlige(rolename,objectname,operation);
        showRolePrivaleete(rolename);*/
        //testShow();


        //创建数据源 成功
/*        CreateCatalogRequest createCatalogRequest=new CreateCatalogRequest();
        createCatalogRequest.setProjectId("shareit");
        CatalogInput catalogInput=new CatalogInput();
        catalogInput.setCatalogName("ds_task");
        catalogInput.setDescription("sg2");
        catalogInput.setOwner("hanzenggui");
        catalogInput.setOwnerType("USER");
        createCatalogRequest.setInput(catalogInput);
        Catalog catalog=lakeCatClient.createCatalog(createCatalogRequest);
        System.out.println(catalog);

        AlterRoleRequest request = new AlterRoleRequest();
        request.setProjectId("shareit");
        RoleInput roleInput = new RoleInput();
        roleInput.setObjectType(ObjectType.CATALOG.name());
        roleInput.setObjectName("ds_task");
        roleInput.setOperation(Operation.ALTER_CATALOG);
        roleInput.setRoleName("rjx_test_role_01");
        request.setInput(roleInput);
        lakeCatClient.grantPrivilegeToRole(request);
        System.out.println("1");*/



/*        AuthorizationInput authorizationInput = new AuthorizationInput();
        authorizationInput.setAuthorizationType(AuthorizationType.NORMAL_OPERATION);
        authorizationInput.setOperation(Operation.DROP_CATALOG);
        User user = new User();
        user.setUserId("hanzenggui1");
        authorizationInput.setUser(user);

        CatalogInnerObject catalogObject = new CatalogInnerObject();
        catalogObject.setProjectId(lakeCatClient.getProjectId());
        catalogObject.setCatalogName("CATALOG");
        catalogObject.setObjectName("ds_task");
        authorizationInput.setCatalogInnerObject(catalogObject);
        AuthorizationResponse authenticate =lakeCatClient.authenticate(new AuthenticationRequest("shareit",false, Lists.newArrayList(authorizationInput)));
        if (!authenticate.getAllowed()){
            System.out.println("1");
            //throw new ServiceException(BaseResponseCodeEnum.LAKECLIENT_NO_SOURCE_DELETE.name(), "您没有删除数据源的权限");
        }*/

       /* ShowPermObjectsRequest showPermObjectsRequest=new ShowPermObjectsRequest();
        showPermObjectsRequest.setObjectType("CATALOG");
        showPermObjectsRequest.setProjectId("shareit");
        showPermObjectsRequest.setUserId("linyang");
        //showPermObjectsRequest.setFilter();
        PagedList<String> p=lakeCatClient.showPermObjectsByUser(showPermObjectsRequest);
        System.out.println(p);*/
        //获取权限
      /*  ShowPermObjectsRequest showPermObjectsRequest=new ShowPermObjectsRequest();
        showPermObjectsRequest.setObjectType();
        showPermObjectsRequest.setProjectId();
        showPermObjectsRequest.setUserId();
        showPermObjectsRequest.setFilter();
        lakeCatClient.showPermObjectsByUser(showPermObjectsRequest);
        //授权
        AlterRoleRequest alterRoleRequest=new AlterRoleRequest();
        alterRoleRequest.setRoleName("");
        alterRoleRequest.setProjectId("");
        RoleInput roleInput=new RoleInput();
        roleInput.setObjectType("DATASOURCE");
        roleInput.setObjectName("myfisrtsource");
        roleInput.setRoleName("analysis");
        roleInput.setOperation(Operation.Ca);
        alterRoleRequest.setInput();
        lakeCatClient.grantPrivilegeToRole();
*/

       /* ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId("qa_autotest1");
        try {
            listDatabasesRequest.setCatalogName("shareit_ue1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PagedList<Database> databasePagedList = lakeCatClient.listDatabases(listDatabasesRequest);
        System.out.println(databasePagedList);*/
     /*   ListTablesRequest listTablesRequest = new ListTablesRequest();
        listTablesRequest.setCatalogName(database.getCatalogName());
        listTablesRequest.setDatabaseName(databaseName);
        listTablesRequest.setProjectId(tenantName);
        listTablesRequest.setMaxResults(1000);
        lakeCatClient.listTableNames()*/
    }
}
