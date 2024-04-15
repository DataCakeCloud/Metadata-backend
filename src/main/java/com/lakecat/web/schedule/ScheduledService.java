package com.lakecat.web.schedule;


import com.lakecat.web.service.ThreadService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author slj
 */
@Component
public class ScheduledService {


    @Autowired
    ThreadService threadService;


    /**
     * //     * 单个表30天内活跃的人  key:tableName  value:用户集合
     * //
     */
    @Scheduled(cron = "1 * * * * ?")
    @PostConstruct
    //@SchedulerLock(name = "sync", lockAtMostFor = "50s", lockAtLeastFor = "40s") // 锁最多保持50秒，最少保持40秒
    public void initForTableOther() {
        threadService.initTableOhter(false);
    }

    @Scheduled(cron = "1 1 0 * * ?")
    @PostConstruct
    //@SchedulerLock(name = "sync", lockAtMostFor = "50s", lockAtLeastFor = "40s") // 锁最多保持50秒，最少保持40秒
    public void initForTableOtherOneDay() {
        threadService.initTableOhter(true);
    }

    /**
     * //     * 单个表30天内活跃的人  key:tableName  value:用户集合
     * //
     */
   // @Scheduled(cron = "1 1 * * * ?")
    //@PostConstruct
    //@SchedulerLock(name = "sync", lockAtMostFor = "50s", lockAtLeastFor = "40s") // 锁最多保持50秒，最少保持40秒
    public void initForTableInfo() {
        threadService.initTable();
    }


    /**
     * 单个表30天内活跃的人  key:tableName  value:用户集合
     */
    @Scheduled(cron = "10 */3 * * * ?")
    @PostConstruct
    //@SchedulerLock(name = "sync", lockAtMostFor = "50s", lockAtLeastFor = "40s") // 锁最多保持50秒，最少保持40秒
    public void initForLast() {
        threadService.syncForLast();
    }

    /**
     * 20min一次
     */
    @Scheduled(cron = "0 30 * * * ?")
    @PostConstruct
    //@SchedulerLock(name = "sync", lockAtMostFor = "50s", lockAtLeastFor = "40s") // 锁最多保持50秒，最少保持40秒
    public void syncForRole() {
        threadService.syncForRole();
    }

}
