package com.lakecat.web.constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.lakecat.web.entity.CurrentUser;
import com.lakecat.web.entity.InfTraceContextHolder;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.utils.DSUtilForLakecat;
import com.lakecat.web.utils.GsonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.lakecat.web.common.CommonMethods.isAdmin;

@Data
@Slf4j
@Configuration
@Component
public class CatalogNameEnum {

    @Autowired
    DSUtilForLakecat dsUtilForLakecat;

    public static String regionCatalogMapping;

    public static String defaultCatalogName;

    @Value("${cloud.region.catalog.mapping}")
    public void setRegionCatalogMapping(String regionCatalogMappingStr) {
        regionCatalogMapping = regionCatalogMappingStr;
    }


    @Value("${task.resource.flag}")
    private Boolean taskResource;

    @Value("${cloud.region.default}")
    public void setDefaultCatalogName(String defaultCatalogNameStr) {
        defaultCatalogName = defaultCatalogNameStr;
    }

    @Data
    public static class CloudRegionCatalog {
        private String catalogName;
        private String region;
        private String cnName;
        private String storage;

        public CloudRegionCatalog(String catalogName, String region, String cnName, String storage) {
            this.catalogName = catalogName;
            this.region = region;
            this.cnName = cnName;
            this.storage = storage;
        }
    }

    public Map <String, CloudRegionCatalog> initCloudRegionCatalog(String tenantName, String authentication) {

        Map <String, CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
        if (!taskResource || isAdmin(tenantName)) {
            setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
        } else {
            try {
                String resource = dsUtilForLakecat.getResource(authentication);
                setAnyNameMap(ANY_NAME_MAP, resource);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ANY_NAME_MAP;
    }


    public void setAnyNameMap(Map <String, CloudRegionCatalog> ANY_NAME_MAP, String regionCatalogMapping) {
        if (regionCatalogMapping != null && ANY_NAME_MAP.size() == 0) {
            String[] regionsInfo = regionCatalogMapping.split(",");
            for (String regionInfos : regionsInfo) {
                if (regionInfos.trim().length() > 0) {
                    String[] regionInfo = regionInfos.trim().split("#");
                    if (regionInfo.length == 4) {
                        CloudRegionCatalog cloudRegionCatalog = new CloudRegionCatalog(regionInfo[2].trim(),
                                regionInfo[1].trim(), regionInfo[0].trim(), regionInfo[3].trim());
                        ANY_NAME_MAP.put(cloudRegionCatalog.catalogName, cloudRegionCatalog);
                        ANY_NAME_MAP.put(cloudRegionCatalog.region, cloudRegionCatalog);
                        ANY_NAME_MAP.put(cloudRegionCatalog.cnName, cloudRegionCatalog);
                    }
                }
            }
        }
    }


    /**
     * 根据任何名称获取枚举值
     *
     * @param anyName
     * @return
     */
    public CloudRegionCatalog get(String anyName) {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        String tenantName = userInfo.getTenantName();
        Map <String, CloudRegionCatalog> regionInfo = userInfo.getRegionInfo();
        if (regionInfo != null && regionInfo.containsKey(anyName)) {
            return regionInfo.get(anyName);
        }
        Map <String, CloudRegionCatalog> ANY_NAME_MAP = new HashMap <>();
        setAnyNameMap(ANY_NAME_MAP, regionCatalogMapping);
        if (ANY_NAME_MAP.containsKey(anyName)) {
            return ANY_NAME_MAP.get(anyName);
        }
        throw new RuntimeException(tenantName + " Exception: [" + anyName + "] region or catalogName does not exist.");
    }

    public static Set <String> getCatalogNames() {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        Map <String, CloudRegionCatalog> regionInfo = userInfo.getRegionInfo();
        return regionInfo.values().stream().map(CloudRegionCatalog::getCatalogName).collect(Collectors.toSet());
    }


    public static Set <String> getRegionList() {
        CurrentUser userInfo = InfTraceContextHolder.get().getUserInfo();
        Map <String, CloudRegionCatalog> regionInfo = userInfo.getRegionInfo();
        return regionInfo.values().stream().map(CloudRegionCatalog::getRegion).collect(Collectors.toSet());
    }


    public String getCatalogNameByRegion(String region) {
        return get(region).getCatalogName();
    }


    public String getStorageByRegion(String region) {
        return get(region).getStorage();
    }

    public String getCatalogName(String region) {
        return get(region).getCatalogName();
    }

    public String getRegion(String catalogName) {
        return get(catalogName).getRegion();
    }


    public String getDefaultRegion() {
        return this.get(defaultCatalogName).region;
    }
}
