package com.lakecat.web.thread;

import com.alibaba.fastjson.JSONObject;
import com.lakecat.web.constant.CatalogNameEnum;
import com.lakecat.web.entity.TableInfo;
import com.lakecat.web.entity.TableInfoUserInput;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.mapper.TableInfoMapper;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.ITableInfoService;
import com.lakecat.web.service.impl.DataGradeServiceImpl;
import com.lakecat.web.service.impl.UserHistoryServiceImpl;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


@Component
@Log4j2
public class AlertSyncThread {
    /**
     * corePoolSize 线程池维护线程的最少数量
     * maximumPoolSize 线程池维护线程的最大数量
     * keepAliveTime 线程池维护线程所允许的空闲时间
     * unit 线程池维护线程所允许的空闲的空闲时间单位
     * workQueue 线程池所使用的缓冲队列
     * handler 线程池对拒绝任务的处理策略
     */
    private static ExecutorService pool = new ThreadPoolExecutor(200, 200,
            5, TimeUnit.SECONDS, new ArrayBlockingQueue <>(10000), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("MONITOR_ALERT_COMPLETABLEFUTURE");
            thread.setDaemon(true);
            return thread;
        }
    }, new ThreadPoolExecutor.CallerRunsPolicy());


    @Autowired
    DataGradeServiceImpl dataGradeService;

    @Autowired
    ILakeCatClientService iLakeCatClientService;

    @Autowired
    TableInfoMapper tableInfoMapper;


    @Autowired
    ITableInfoService iTableInfoService;


    //多线程执行
    public boolean inputUserHistory(TableInfo tableInfo) {
        List <CompletableFuture <Integer>> sendStatesFutureList = new ArrayList <>();


        sendStatesFutureList.add(doSyncByTables(tableInfo));
        CompletableFuture <Void> allFutures = CompletableFuture
                .allOf(sendStatesFutureList.toArray(new CompletableFuture[sendStatesFutureList.size()]));
        allFutures.thenApplyAsync(v -> {
            return sendStatesFutureList.stream().map(CompletableFuture::join).collect(Collectors.toList());
        }).whenCompleteAsync((result, t) -> {
            if (t != null) {
                log.warn("Detected a write failure:{}", t);
            }
        });
        return true;
    }




    @Autowired
    UserHistoryServiceImpl userInputItem;

    public CompletableFuture <Integer> doSyncByTables(TableInfo tableInfo) {
        return CompletableFuture.supplyAsync(() -> {
            TableInfoUserInput userInput = new TableInfoUserInput();
            if (StringUtils.isNotBlank(tableInfo.getKeyWord())) {
                userInput.setUserId(tableInfo.getUserId());
                userInput.setInput(tableInfo.getKeyWord());
                //写入用户输入表
                userInputItem.save(userInput);
            }
            return 0;
        }, pool).whenCompleteAsync((s, e) -> {
            if (e != null) {
                log.warn("Detected a write failure:{}", e);
            }

        }, pool);
    }
}
