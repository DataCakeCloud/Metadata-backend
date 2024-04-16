package com.lakecat.web.service;

import com.lakecat.web.entity.owner.ActiveTable;
import com.lakecat.web.entity.owner.MetadataIntegrity;
import com.lakecat.web.entity.owner.ResourceUsage;
import com.lakecat.web.entity.owner.TableStorage;
import com.lakecat.web.entity.owner.TableUsageProfile;
import org.springframework.scheduling.annotation.Scheduled;

public interface IMetaOwnerService {

    ActiveTable activeTable(String owner);

    TableStorage tableStorage(String owner);

    ResourceUsage resourceUsage(String owner);

    TableUsageProfile tableUsageProfile(String owner);

    MetadataIntegrity metadataIntegrity(String owner);

    /**
     * 定时刷新数据完整度
     */
    @Scheduled(cron = "0 0 1 * * ?")
    void refreshMetadataIntegrity(String owner);

}
