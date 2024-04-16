package com.lakecat.web.constant;

public interface ConfigConstants {

    /**
     * lakecat 配置设置
      */
    String LAKECAT_SERVER_URL = "lakecat.server.url";
    String LAKECAT_SERVER_PORT = "lakecat.server.port";
    String LAKECAT_SERVER_USER = "lakecat.server.user";
    String LAKECAT_SERVER_PASSWORD = "lakecat.server.password";
    String LAKECAT_SERVER_TENANT = "lakecat.server.tenant";
    String LAKECAT_SERVER_PROJECT = "lakecat.server.project";

    String LAKECAT_HMS_URIS = "lakecat.hive.metastore.uris";

    /**
     * Multiple comma separated
     */
    String LAKECAT_SERVER_LATEST_VERSION_REGION = "lakecat.server.latest.version.region";


    String OLAP_SERVICE_USER = "olap.service.user";
    String OLAP_SERVICE_PASSWORD = "olap.service.password";
    String OLAP_SERVICE_URL = "olap.service.url";



}
