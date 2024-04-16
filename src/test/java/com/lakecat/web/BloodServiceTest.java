package com.lakecat.web;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.lakecat.web.service.BloodService;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ThreadService;
import com.lakecat.web.utils.GsonUtil;
import com.lakecat.web.vo.blood.BloodRequest;
import com.lakecat.web.vo.blood.Response;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.model.Database;
import io.lakecat.catalog.common.model.PagedList;
import io.lakecat.catalog.common.plugin.request.ListDatabasesRequest;
import io.lakecat.catalog.common.plugin.request.ListTablesRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class BloodServiceTest {

    @Autowired
    private BloodService bloodService;

    @Autowired
    ILakeCatClientService iLakeCatClientService;


    @Autowired
    private ThreadService threadService;


    @Test
    public void test3(){
        threadService.initTable();
    }
    //@Test
    public void teset2(){
        LakeCatClient lakeCatClient = iLakeCatClientService.get();

        ListDatabasesRequest listDatabasesRequest = new ListDatabasesRequest();
        listDatabasesRequest.setProjectId("qa_test3");
        try {
            listDatabasesRequest.setCatalogName("shareit_ue1");
        } catch (Exception e) {
            e.printStackTrace();
        }
        PagedList<Database> databasePagedList = lakeCatClient.listDatabases(listDatabasesRequest);
        for (Database database:databasePagedList.getObjects()){
            ListTablesRequest listTablesRequest = new ListTablesRequest();
            listTablesRequest.setCatalogName(database.getCatalogName());
            listTablesRequest.setDatabaseName(database.getDatabaseName());
            listTablesRequest.setProjectId("qa_test3");
            listTablesRequest.setMaxResults(1000);
            PagedList<String> s=lakeCatClient.listTableNames(listTablesRequest);
            String[] aa=s.getObjects();
            if (aa!=null&&aa.length>0){
                for (String c:aa){
                    System.out.println(c);
                }
            }
        }

    }


    @Test
    public void test()throws Exception{
        Map<String, Set<String>> downMap= Maps.newLinkedHashMap();
        Map<String, Set<String>> upMap= Maps.newLinkedHashMap();
        BufferedReader bufferedReader=new BufferedReader(new FileReader("D:\\data\\hw.txt"));
        String line="";
        while ((line=bufferedReader.readLine())!=null){
            String[] info=line.split("\\.");
            if (info.length>1){
                BloodRequest bloodRequest=new BloodRequest();
                bloodRequest.setDownDeep(1);
                bloodRequest.setTableName(info[1]);
                bloodRequest.setRegion("sg2");
                bloodRequest.setDatabaseName(info[0]);
                Response response =bloodService.blood(bloodRequest);
                if (response!=null&&response.getData()!=null&& CollectionUtils.isNotEmpty(response.getData().getInstance())&&response.getData().getInstance().size()>1){
                    for (Response.Rdata.Instance instance:response.getData().getInstance()){
                        if (StringUtils.isNoneBlank(instance.getOwner())&& !instance.getDagId().equals(bloodRequest.getTaskName())){
                            if (downMap.containsKey(line)){
                                downMap.get(line).add(instance.getOwner());
                            }else {
                                downMap.put(line, Sets.newHashSet(instance.getOwner()));
                            }
                        }
                    }
                }
            }
            if (info.length>1){
                BloodRequest bloodRequest=new BloodRequest();
                bloodRequest.setUpDeep(1);
                bloodRequest.setTableName(info[1]);
                bloodRequest.setRegion("sg2");
                bloodRequest.setDatabaseName(info[0]);
                Response response =bloodService.blood(bloodRequest);
                if (response!=null&&response.getData()!=null&& CollectionUtils.isNotEmpty(response.getData().getInstance())&&response.getData().getInstance().size()>1){
                    for (Response.Rdata.Instance instance:response.getData().getInstance()){
                        if (StringUtils.isNoneBlank(instance.getOwner())&& !instance.getDagId().equals(bloodRequest.getTaskName())){
                            if (upMap.containsKey(line)){
                                upMap.get(line).add(instance.getOwner());
                            }else {
                                upMap.put(line, Sets.newHashSet(instance.getOwner()));
                            }
                        }
                    }
                }
            }

        }
        if (upMap.size()>0){
            upMap.forEach((k,v)->{
                System.out.println(k+"上游的owner-->"+ GsonUtil.toJson(v,false));
            });
        }
        if (downMap.size()>0){
            downMap.forEach((k,v)->{
                System.out.println(k+"下游的owner-->"+ GsonUtil.toJson(v,false));
            });
        }
       bufferedReader.close();
    }
}
