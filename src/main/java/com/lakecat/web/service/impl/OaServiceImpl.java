package com.lakecat.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lakecat.web.common.CommonParameters;
import com.lakecat.web.constant.BaseResponseCodeEnum;
import com.lakecat.web.constant.ObjectType;
import com.lakecat.web.entity.*;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.exception.ServiceException;
import com.lakecat.web.mapper.AuthGovMapper;
import com.lakecat.web.mapper.OaAuthMapper;
import com.lakecat.web.mapper.PermissionRecordMapper;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.IAdminRoleService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.service.OaService;
import com.lakecat.web.utils.DSUtilForLakecat;
import com.lakecat.web.utils.DateUtil;
import com.lakecat.web.utils.GsonUtil;
import com.lakecat.web.vo.blood.*;
import io.lakecat.catalog.common.model.Database;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.RolePrivilege;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lakecat.web.constant.BaseResponseCodeEnum.EXTERNAL_OA_INVOKE_FAIL;
import static com.lakecat.web.utils.DateUtil.date2yyyymmddHHmmss;
import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class OaServiceImpl implements OaService {

    public static String COMMENT="applyUser={0},operUser={1},userGroup={2}";

    public static final String FAIL = "fail";

    @Resource
    private TableInfoMapper tableInfoMapper;

    @Autowired
    ITableInfoService tableInfoService;

    @Resource
    private DSUtilForLakecat dsUtilForLakecat;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${oa.url}")
    private String url;

    @Value("${oa.appkey}")
    private String token;

    @Resource
    private OaAuthMapper oaAuthMapper;

    @Resource
    private IAdminRoleService iAdminRoleService;

    @Resource
    private AuthGovMapper authGovMapper;
    @Resource
    private PermissionRecordMapper permissionRecordMapper;

    @Value("${default.tenantName}")
    private String defaultTenantName;

    @Autowired
    private ILakeCatClientService iLakeCatClientService;
    @Autowired
    private DataGradeServiceImpl dataGradeService;

    public void sendOaRequest(GrantPrivilegeToUser inputs){
        //InfTraceContextHolder.get().setUuid("groupdvquveu8");
        String inputJson= GsonUtil.toJson(inputs,false);
        List<OaRequestVo> list=oaRequestVo(inputs);
        if (CollectionUtils.isNotEmpty(list)){
            for (OaRequestVo oaRequestVo:list){
                OaResponseVo oaResponseVo=requestOa(oaRequestVo);
                OaAuth oaAuth=new OaAuth();
                oaAuth.setCreateTime(DateUtil.getCurrentDateStr());
                oaAuth.setInputJson(inputJson);
                if (oaResponseVo!=null&&StringUtils.isNoneBlank(oaResponseVo.getOaid())){
                    oaAuth.setOaId(oaResponseVo.getOaid());
                    oaAuth.setId(oaResponseVo.getOaid());
                }else {
                    oaAuth.setId(FAIL+UUID.randomUUID().toString());
                }
                oaAuth.setOaRequestJson(GsonUtil.toJson(oaRequestVo,false));
                oaAuth.setUuid(InfTraceContextHolder.get().getUuid());
                oaAuth.setUserGroup("");
                oaAuth.setUser(InfTraceContextHolder.get().getUserName());
                oaAuthMapper.insert(oaAuth);
                if (oaResponseVo == null) {
                    throw new ServiceException(BaseResponseCodeEnum.EXTERNAL_OA_INVOKE_FAIL);
                }
                if (StringUtils.isBlank(oaResponseVo.getOaid())) {
                    throw new ServiceException(BaseResponseCodeEnum.EXTERNAL_OA_INVOKE_FAIL, oaResponseVo.getMessage());
                }
                for (OaRequestVo.Row row:oaRequestVo.getRows()){
                    insertAuthorityGovInfo(inputs,oaRequestVo,row,oaAuth);
                    insertPermission(inputs,oaRequestVo,row,oaAuth);
                }
            }
        }
    }
    public void insertAuthorityGovInfo(GrantPrivilegeToUser inputs,OaRequestVo oaRequestVo,OaRequestVo.Row row,OaAuth oaAuth){
        AuthorityGovInfo authorityGovInfo = AuthorityGovInfo.builder().build()
                .setOperator("系统")
                .setTableName(row.getBm())
                .setOperate("查询")
                .setReason(oaRequestVo.getSqly())
                .setPermission("查询")
                .setUserName(InfTraceContextHolder.get().getUserName())
                .setOperatedUser(InfTraceContextHolder.get().getUuid())
                .setExecuteStatus(1)
                .setCycle(CommonParameters.cycleIntMap.get(inputs.getCycle()));
        authGovMapper.insertForRecord(authorityGovInfo);
    }

    public void insertPermission(GrantPrivilegeToUser inputs,OaRequestVo oaRequestVo,OaRequestVo.Row row,OaAuth oaAuth){
        PermissionRecordInfo insert = new PermissionRecordInfo();
        insert.setType("权限申请");
        insert.setOrderId(oaAuth.getOaId());
        String tableList = inputs.getRoleInputs().getObjectName()[0].split("\\.")[0] + "." + row.getKm() + "." + row.getBm();
        insert.setTableList(tableList);
        if (StringUtils.isNotEmpty(tableList)) {
            insert.setTableRecoveryState(processTableListStatus(Arrays.asList(tableList.split(","))));
        }
        insert.setPermission("查询");
        String userGroupUUid = inputs.getRoleInputs().getScope()[0];
        insert.setApplyUser(userGroupUUid);
        insert.setProposer(InfTraceContextHolder.get().getUserName());
//        insert.setGrantUser(InfTraceContextHolder.get().getUserName());
        insert.setFlag("1");
        if (StringUtils.isNotEmpty(oaRequestVo.getQxsx())) {
            insert.setCycle(CommonParameters.cycleIntMap.get(oaRequestVo.getQxsx()).toString());
        } else {
            insert.setCycle("5");
        }
        insert.setGrantType(1);
        insert.setReason(inputs.getReason());
        insert.setStatus(1);
        insert.setCreateTime(date2yyyymmddHHmmss(0));
        insert.setUpdateTime(date2yyyymmddHHmmss(0));
        permissionRecordMapper.insertForOrder(insert);
    }

    public String processTableListStatus(List<String> tableList) {
        if (tableList == null || tableList.isEmpty()) {
            return "";
        }
        List<String> collect = tableList.stream().map(data -> {
            return data + ":0";
        }).collect(toList());
        return StringUtils.join(collect, ",");
    }

    public OaResponseVo requestOa(OaRequestVo oaRequestVo){
        HttpHeaders httpHeaders =new HttpHeaders();
        httpHeaders.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON_UTF8));
        httpHeaders.setContentType(MediaType.APPLICATION_JSON_UTF8);
        httpHeaders.add("appkey",token);
        try {
            OaResponseVo oaResponseVo=restTemplate.postForObject(url,new HttpEntity<>(oaRequestVo,httpHeaders),OaResponseVo.class);
            return oaResponseVo;
        }catch (Exception e){
            log.error("",e);
        }
        return null;
    }

    private List<OaRequestVo> oaRequestVo(GrantPrivilegeToUser inputs){
        List<OaRequestVo> oaRequestVoList= Lists.newArrayList();
        List<SensitivityLevel> sensitivityLevelList=tableInfoMapper.selectSensitivityLevel();
        List<UserGroupVo> userGroupVos=dsUtilForLakecat.getAllUserGroup();
        Role role=iLakeCatClientService.getRole(InfTraceContextHolder.get().getUuid());
        if (CollectionUtils.isNotEmpty(userGroupVos)){
            for (String objectName:inputs.getRoleInputs().getObjectName()){
                TableVo tableVo=new TableVo(objectName);
                if (role.getRolePrivileges()!=null&&role.getRolePrivileges().length>0){
                    for (RolePrivilege rolePrivilege:role.getRolePrivileges()){
                        if (ObjectType.TABLE.printName.equals(rolePrivilege.getGrantedOn())){
                            String name=rolePrivilege.getName();
                            if (name.indexOf("*") > -1) {
                                if (name.toLowerCase().indexOf(tableVo.getDbName().toLowerCase()) > -1) {
                                    throw new ServiceException(BaseResponseCodeEnum.TABLE_AUTH_EXIST, tableVo.getTableName() + "表已经有权限，不用重复申请");
                                }
                            } else {
                                if (name.equalsIgnoreCase(tableVo.getRegion() + "." + tableVo.getDbName() + "." + tableVo.getTableName())) {
                                    throw new ServiceException(BaseResponseCodeEnum.TABLE_AUTH_EXIST, tableVo.getTableName() + "表已经有权限，不用重复申请");
                                }
                            }
                        }
                    }
                }
                OaRequestVo oaRequestVo=new OaRequestVo();
                oaRequestVo.setSqr(InfTraceContextHolder.get().getUserInfo().getUserName());
                oaRequestVo.setSqly(inputs.getReason());
                oaRequestVo.setQxsx("永久");
                oaRequestVo.setQxlx("查询");
                wrapLeaders(userGroupVos,oaRequestVo);
                log.info("after wrapLeaders=" + oaRequestVo);
                wrapSensitivityLevel(oaRequestVo,sensitivityLevelList);
                log.info("after wrapSensitivityLevel=" + oaRequestVo);
                wrapRows(tableVo,oaRequestVo);
                log.info("after wrapRows=" + oaRequestVo);
                oaRequestVoList.add(oaRequestVo);
            }
        }
        return wrapoaRequestList(oaRequestVoList);
    }




    private List<OaRequestVo> wrapoaRequestList(List<OaRequestVo> oaRequestVos){
        List<OaRequestVo> list=Lists.newArrayList();
        Map<String,List<OaRequestVo>> map= Maps.newHashMap();
        if (CollectionUtils.isNotEmpty(oaRequestVos)){
            for (OaRequestVo oaRequestVo:oaRequestVos){
                String key=""+oaRequestVo.getSqrleader()+"-"+oaRequestVo.getSjgjleader()+"-"+oaRequestVo.getXgjbld()+"-"+oaRequestVo.getDsjfzr();
                if (map.containsKey(key)){
                    map.get(key).add(oaRequestVo);
                }else {
                    map.put(key,Lists.newArrayList(oaRequestVo));
                }
            }
        }
        for (List<OaRequestVo> oaRequestVoList:map.values()){
            if (oaRequestVoList.size()>1){
                OaRequestVo oaRequestVo=oaRequestVoList.get(0);
                for (int i=1;i<oaRequestVoList.size();i++){
                    oaRequestVo.getRows().addAll(oaRequestVoList.get(i).getRows());
                }
                list.add(oaRequestVo);
            }else {
                list.addAll(oaRequestVoList);
            }
        }
        return list;
    }

    private void wrapRows(TableVo tableVo,OaRequestVo oaRequestVo){
        OaRequestVo.Row row=new OaRequestVo.Row();
        oaRequestVo.getRows().add(row);
        row.setKm(tableVo.getDbName());
        row.setBm(tableVo.getTableName());
        LakeCatParam build = LakeCatParam.builder().region(tableVo.getRegion()).dbName(tableVo.getDbName())
                .tableName(tableVo.getTableName())
                .build();
        TableInfo tableInfo = iLakeCatClientService.getTable(build);

        List<UserGroupRelation> userGroupRelations=Lists.newArrayList();
        if (tableVo.getTableName().toLowerCase().startsWith("stage_")){
            List<UserGroupRelation> actorLeader=tableInfoMapper.actorLeader(tableVo.getTableName());
            if (CollectionUtils.isEmpty(actorLeader)){
                throw new RuntimeException("当前表不是标准的stage表");
            }
            userGroupRelations=actorLeader;
            oaRequestVo.setSjgjleader(actorLeader.get(0).getUserName());
        }else {
            if (StringUtils.isNoneBlank(tableInfo.getSubject())){
                List<UserGroupRelation> leaders = tableInfoMapper.selectUserGroupLeader(tableInfo.getSubject());
                if (CollectionUtils.isNotEmpty(leaders)){
                    oaRequestVo.setSjgjleader(leaders.get(0).getUserName());
                    userGroupRelations=leaders;
                }
            }else {
                Database database=iLakeCatClientService.getDataBase(tableVo.getRegion(),tableVo.getDbName());
                if (database!=null&&StringUtils.isNoneBlank(database.getOwner())){
                    List<UserGroupRelation> userGroupLeaders = tableInfoMapper.selectUserGroupLeaderByUserGroupName(database.getOwner());
                    if (CollectionUtils.isNotEmpty(userGroupLeaders)){
                        oaRequestVo.setSjgjleader(userGroupLeaders.get(0).getUserName());
                        userGroupRelations=userGroupLeaders;
                    }
                }
            }
        }

       // oaRequestVo.setSjgjleader(tableInfo.getSubject());//用subject表示数据归集的leader
        if (StringUtils.isBlank(oaRequestVo.getSjgjleader())){
            throw new RuntimeException("数据归集负责人为空");
        }
        row.setSjfzr(oaRequestVo.getSjgjleader());
        row.setBms(tableInfo.getDescription());

        List <DataGrade> colsGrade = dataGradeService.getByTableSole(tableVo.getSole());
        if (CollectionUtils.isNotEmpty(colsGrade)){
            for (DataGrade dataGrade:colsGrade){
                if (dataGrade.getGradeType()!=null&&dataGrade.getGradeType()==1){
                    row.setBmgjb("L"+dataGrade.getGrade().substring(0,1));
                }
            }
        }
        if (StringUtils.isNoneBlank(tableInfo.getColumns())){
            try {
                List<Column> columnList=GsonUtil.parseFromJson(tableInfo.getColumns(),new TypeToken<List<Column>>(){}.getType());
                if (CollectionUtils.isNotEmpty(columnList)){
                    StringBuffer  columnBuffer=new StringBuffer();
                    for (Column column:columnList){
                        columnBuffer.append(column.getName());
                        if (StringUtils.isNoneBlank(column.getComment())){
                            columnBuffer.append(" [").append(column.getComment()).append("]");
                        }
                        columnBuffer.append("<br>");
                    }
                    row.setMgzd(columnBuffer.toString());
                }
            }catch (Exception e){

            }
        }
        if (StringUtils.isNoneBlank(row.getBmgjb())&&("L3".equals(row.getBmgjb())||"L4".equals(row.getBmgjb()))&&CollectionUtils.isNotEmpty(userGroupRelations)){
            List<String> orgLeader=tableInfoMapper.selectUserGroupOrgLeaderByUserGroupId(userGroupRelations.get(0).getUserGroupId());
            if (CollectionUtils.isNotEmpty(orgLeader)){
                oaRequestVo.setXgjbld(orgLeader.get(0));
            }
        }
    }

    private void wrapLeaders(List<UserGroupVo> userGroupVos,OaRequestVo oaRequestVo){
        log.info("111111oaRequestVo=" + oaRequestVo);
        log.info("InfTraceContextHolder.get().getUuid()=" + InfTraceContextHolder.get().getUuid());
        UserGroupVo currentUserGroup=null; //当前用户组
        UserGroupRelation currentUserGroupLeader=null;//当前用户组的负责人
        UserGroupRelation dpmUserGroupLeader=null; //dsjfzr 大数据负责人
        UserGroupRelation dpmUserGroupTeamer=null;//qxglzy 权限管理专员
        for (UserGroupVo userGroupVo:userGroupVos){
            if (userGroupVo.getUuid().equals(InfTraceContextHolder.get().getUuid())){
                currentUserGroup=userGroupVo;
                if (CollectionUtils.isNotEmpty(userGroupVo.getUserGroupRelationList())){
                    for (UserGroupRelation userGroupRelation:userGroupVo.getUserGroupRelationList()){
                        if (userGroupRelation.getOwner()!=null&&userGroupRelation.getOwner()==0){
                            currentUserGroupLeader=userGroupRelation;
                            break;
                        }
                    }
                }

            }
            if (userGroupVo.getName().equals("dpm")){
                currentUserGroup=userGroupVo;
                if (CollectionUtils.isNotEmpty(userGroupVo.getUserGroupRelationList())){
                    Collections.shuffle(userGroupVo.getUserGroupRelationList());
                    for (UserGroupRelation userGroupRelation:userGroupVo.getUserGroupRelationList()){
                        if (userGroupRelation.getOwner()!=null&&userGroupRelation.getOwner()==0){
                            dpmUserGroupLeader=userGroupRelation;
                        } else {
                            dpmUserGroupTeamer=userGroupRelation;
                        }
                    }
                }

            }
            log.info(userGroupVo.getName() + "    :    " + oaRequestVo);
        }
        oaRequestVo.setSqrtdbh(currentUserGroup.getName());
        oaRequestVo.setSqrtdmc(currentUserGroup.getDescription());
        if (currentUserGroupLeader!=null){
            oaRequestVo.setSqrtdfzr(currentUserGroupLeader.getUserName());
            oaRequestVo.setSqrleader(currentUserGroupLeader.getUserName());
        }else {
            throw new RuntimeException("申请人leader为空");
        }
        if (dpmUserGroupLeader!=null){
            oaRequestVo.setDsjfzr(dpmUserGroupLeader.getUserName());
        }else {
            throw new RuntimeException("大数据负责人为空");
        }
        if (dpmUserGroupTeamer!=null){
            oaRequestVo.setQxglzy(dpmUserGroupTeamer.getUserName());
        }else {
            throw new RuntimeException("权限管理人员为空");
        }
        log.info("99999oaRequestVo=" + oaRequestVo);
    }

    /**
     * 封装数据要素
     * @param oaRequestVo
     * @param sensitivityLevelList
     */
    private void wrapSensitivityLevel(OaRequestVo oaRequestVo,List<SensitivityLevel> sensitivityLevelList){
        if (CollectionUtils.isNotEmpty(sensitivityLevelList)){
            for (SensitivityLevel sensitivityLevel:sensitivityLevelList){
                if (sensitivityLevel.getDataLevel().endsWith("1")){
                    oaRequestVo.setSjjb1(sensitivityLevel.getDataLevel());
                    oaRequestVo.setDjys1(sensitivityLevel.getDefiningElements());
                    oaRequestVo.setCyzd1(sensitivityLevel.getCommonFields());
                }
                if (sensitivityLevel.getDataLevel().endsWith("2")){
                    oaRequestVo.setSjjb2(sensitivityLevel.getDataLevel());
                    oaRequestVo.setDjys2(sensitivityLevel.getDefiningElements());
                    oaRequestVo.setCyzd2(sensitivityLevel.getCommonFields());
                }
                if (sensitivityLevel.getDataLevel().endsWith("3")){
                    oaRequestVo.setSjjb3(sensitivityLevel.getDataLevel());
                    oaRequestVo.setDjys3(sensitivityLevel.getDefiningElements());
                    oaRequestVo.setCyzd3(sensitivityLevel.getCommonFields());
                }
                if (sensitivityLevel.getDataLevel().endsWith("4")){
                    oaRequestVo.setSjjb4(sensitivityLevel.getDataLevel());
                    oaRequestVo.setDjys4(sensitivityLevel.getDefiningElements());
                    oaRequestVo.setCyzd4(sensitivityLevel.getCommonFields());
                }
            }
        }
    }

    @Override
    public List<SensitivityLevel> sensitivityLevelList() {
        return tableInfoMapper.selectSensitivityLevel();
    }


    @Override
    public void oaCallback(String requestId,Integer status) {
        OaAuth oaAuth=oaAuthMapper.selectById(requestId);
        if (oaAuth!=null){
            log.info(" oaCallback oaAuth is :" + GsonUtil.toJson(oaAuth,true));
            log.info(" oaCallback oaAuth is :" + oaAuth);
            oaAuth.setStatus(status==null?2:status);
            oaAuth.setUpdateTime(DateUtil.getCurrentDateStr());
            if (status!=null&&status==1){
                OaRequestVo oaRequestVo=GsonUtil.parse(oaAuth.getOaRequestJson(),OaRequestVo.class);
                GrantPrivilegeToUser grantPrivilegeToUser=GsonUtil.parse(oaAuth.getInputJson(),GrantPrivilegeToUser.class);
                if (CollectionUtils.isNotEmpty(oaRequestVo.getRows())){
                    for (OaRequestVo.Row row:oaRequestVo.getRows()){
                        AdminRoleInfo entity = new AdminRoleInfo();
                        entity.setUserName(oaAuth.getUser());
                        entity.setType(CommonParameters.typeMap.get(1));
                        entity.setOrderType("权限授予");
                        entity.setWorkOrderId(oaAuth.getOaId());
                        entity.setReason(oaRequestVo.getSqly());
                        entity.setCycle(CommonParameters.cycleIntMap.get(oaRequestVo.getQxsx()));
                        entity.setFlag("0");
                        entity.setOwner("部门");
                        entity.setOpt("已授予");
                        entity.setSqrleader(oaRequestVo.getSqrleader());
                        entity.setTenantName(defaultTenantName);
                        entity.setObjectNames(new String[]{grantPrivilegeToUser.getRoleInputs().getObjectName()[0].split("\\.")[0]+"."+row.getKm()+"."+row.getBm()});
                        entity.setOperation(grantPrivilegeToUser.getRoleInputs().getOperation());
                        String comment= MessageFormat.format(COMMENT, oaRequestVo.getSqr(),oaAuth.getOaId(),tableInfoMapper.selectUserGroupName(oaAuth.getUuid()));//applyUser=xxx,operUser=xxx,fsUser=xxx
                        entity.setComment(comment);
                        iAdminRoleService.grantPrivilegeForPersonage(entity, new String[]{oaAuth.getUuid()});
                    }
                }
            }
            oaAuthMapper.updateById(oaAuth);
        }
    }

    public static void main(String[] args) {
        String comment= MessageFormat.format(COMMENT, 1,2,3);//applyUser=xxx,operUser=xxx,fsUser=xxx
        System.out.println(comment);
    }
}
