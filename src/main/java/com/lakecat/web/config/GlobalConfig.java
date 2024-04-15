package com.lakecat.web.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.lakecat.web.constant.CommonConts;
import com.lakecat.web.constant.ConfigConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GlobalConfig {

    @Autowired
    private Environment env;

    private String lsUrl;

    private int lsPort;

    private String lsUserName;

    private String lsrPassword;

    private String lsTenant;

    private String project;

    private String hiveMetastoreUris;

    private String olapServiceUser;
    private String olapServicePassword;
    private String olapServiceUrl;

    private Set<String> lsAppRegions = new HashSet<String>();

    public Set<String> getLsAppRegions() {
        String regions = env.getProperty(ConfigConstants.LAKECAT_SERVER_LATEST_VERSION_REGION);
        if (regions != null && regions.length() > 0) {
            lsAppRegions = new HashSet<String>(Arrays.asList(regions.trim().split(",")));
        }
        return lsAppRegions;
    }

    public String getLsUrl(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_URL + CommonConts.DATA_DELIMITER  + region);
    }

    public String getLsUrl() {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_URL);
    }

    public int getLsPort(String region) {
        return Integer.parseInt(
            Objects.requireNonNull(
                env.getProperty(ConfigConstants.LAKECAT_SERVER_PORT + CommonConts.DATA_DELIMITER + region)));
    }

    public int getLsPort() {
        return Integer.parseInt(
                Objects.requireNonNull(
                        env.getProperty(ConfigConstants.LAKECAT_SERVER_PORT)));
    }

    public String getLsUserName(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_USER + CommonConts.DATA_DELIMITER  + region);
    }

    public String getLsUserName() {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_USER);
    }

    public String getLsrPassword(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_PASSWORD + CommonConts.DATA_DELIMITER  + region);
    }

    public String getLsrPassword() {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_PASSWORD);
    }

    public String getLsTenant(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_TENANT + CommonConts.DATA_DELIMITER  + region);
    }

    public String getLsTenant() {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_TENANT);
    }

    public String getProject(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_PROJECT + CommonConts.DATA_DELIMITER  + region);
    }

    public String getProject() {
        return env.getProperty(ConfigConstants.LAKECAT_SERVER_PROJECT);
    }

    public String getHiveMetastoreUris(String region) {
        return env.getProperty(ConfigConstants.LAKECAT_HMS_URIS + CommonConts.DATA_DELIMITER  + region);
    }

    public String getHiveMetastoreUris() {
        return env.getProperty(ConfigConstants.LAKECAT_HMS_URIS);
    }

    public String getOlapServiceUser(String region) {
        return getRegionSuffixValue(ConfigConstants.OLAP_SERVICE_USER, region);
    }

    public String getOlapServicePassword(String region) {
        return getRegionSuffixValue(ConfigConstants.OLAP_SERVICE_PASSWORD, region);
    }

    public String getOlapServiceUrl(String region) {
        return getRegionSuffixValue(ConfigConstants.OLAP_SERVICE_URL, region);
    }

    public String getRegionSuffixValue(String key, String region) {
        return env.getProperty(key + CommonConts.DATA_DELIMITER  + region);
    }

    public String getValue(String key) {
        return env.getProperty(key);
    }
}
