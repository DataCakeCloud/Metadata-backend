package com.lakecat.web.vo.blood;

import com.google.common.collect.Lists;
import com.lakecat.web.utils.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * updonw=1 上游
 * updonw=2 下游
 */
@AllArgsConstructor
@Slf4j
public class BloodRunnable implements Runnable{

    private RestTemplate restTemplate;

    private CountDownLatch countDownLatch;

    private String url;

    private BloodRequest bloodRequest;

    private int upDown;

    private List<Response> responseList;

    @Override
    public void run() {
        try {
            StringBuffer stringBuffer=new StringBuffer(url);
            if (upDown==1){
                stringBuffer.append("?name=").append(bloodRequest.getTaskName()).append("&state=waiting").append("&depth=").append(bloodRequest.getUpDeep()).append("&upDown=")
                        .append(upDown).append("&execution_date=").append(DateUtil.getCurrentDateStrForBlood());
            }else {
                stringBuffer.append("?name=").append(bloodRequest.getTaskName()).append("&state=waiting").append("&depth=").append(bloodRequest.getDownDeep()).append("&upDown=")
                        .append(upDown).append("&execution_date=").append(DateUtil.getCurrentDateStrForBlood());
            }
            HttpHeaders httpHeaders =new HttpHeaders();
            httpHeaders.add("Authentication",bloodRequest.getAuth());
            HttpEntity httpEntity=new HttpEntity(null,httpHeaders);
            log.info("bloodUrl-->{}",stringBuffer.toString());
            ResponseEntity<Response> responseEntity=restTemplate.exchange(stringBuffer.toString(), HttpMethod.GET,httpEntity,Response.class);
            Response response=responseEntity.getBody();
            if (response!=null&&response.getData()!=null&& CollectionUtils.isNotEmpty(response.getData().getInstance())){
                if (upDown==1){
                    response.setUp(true);
                }
                if (!StringUtils.isEmpty(response.getData().getCoreTaskId())&&response.getData().getCoreTaskId().indexOf(",")>-1){
                    response.getData().setCoreTaskId(response.getData().getCoreTaskId().split(",")[0]);
                }
                if (CollectionUtils.isEmpty(response.getData().getRelation())){
                    response.getData().setRelation(Lists.newArrayList());
                }
                for (Response.Rdata.Relation relation:response.getData().getRelation()){
                    if (!StringUtils.isEmpty(relation.getSource())&&relation.getSource().indexOf(",")>-1) {
                        relation.setSource(relation.getSource().split(",")[0]);
                    }
                    if (!StringUtils.isEmpty(relation.getTarget())&&relation.getTarget().indexOf(",")>-1){
                        relation.setTarget(relation.getTarget().split(",")[0]);
                    }
                }
                for (Response.Rdata.Instance instance:response.getData().getInstance()){
                    if (!StringUtils.isEmpty(instance.getNodeId())&&instance.getNodeId().indexOf(",")>-1){
                        instance.setNodeId(instance.getNodeId().split(",")[0]);
                    }
                }
                responseList.add(response);
            }
        }catch (Exception e){
            log.error("",e);
        }finally {
            countDownLatch.countDown();
        }
    }

    public void setCountDownLatch(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
}
