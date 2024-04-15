package com.lakecat.web.proxy;

import com.lakecat.web.constant.CatalogNameEnum;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Component
@DependsOn({"catalogNameEnum"})
public class CatalogNameEnumProxy {


    public CatalogNameEnumProxy() {
//        new CatalogNameEnum().initCloudRegionCatalog();
        System.out.println("测试完成");
    }


}
