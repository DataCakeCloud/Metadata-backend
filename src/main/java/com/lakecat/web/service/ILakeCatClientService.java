package com.lakecat.web.service;

import com.github.pagehelper.PageInfo;
import com.lakecat.web.entity.DatabaseInfo;
import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.Model;
import com.lakecat.web.entity.TableInfo;
import io.lakecat.catalog.client.LakeCatClient;
import io.lakecat.catalog.common.lineage.LineageFact;
import io.lakecat.catalog.common.model.Database;
import io.lakecat.catalog.common.model.LineageInfo;
import io.lakecat.catalog.common.model.Role;
import io.lakecat.catalog.common.model.discovery.CatalogTableCount;
import io.lakecat.catalog.common.model.discovery.DatabaseSearch;
import io.lakecat.catalog.common.model.discovery.ObjectCount;
import io.lakecat.catalog.common.model.discovery.TableCategories;
import io.lakecat.catalog.common.model.glossary.Category;
import io.lakecat.catalog.common.model.glossary.Glossary;
import org.apache.hadoop.conf.Configuration;

import java.util.List;

public interface ILakeCatClientService {

    /**
     * get Lakecat client
     *
     * @return
     */

    LakeCatClient get();

    Configuration getConfiguration(String region);

    Configuration getConfiguration();

    TableInfo getTable(LakeCatParam lakeCatParam);

    TableInfo updateTable(TableInfo tableInfo);

    //对应元数据接口searchTable  通过关键字可模糊搜索catalog 库 表  描述 列名模糊匹配
    List<TableInfo> searchTable(LakeCatParam lakeCatParam);

    //对应元数据接口searchDiscoveryNames  通过关键字可模糊搜索catalog 库 表  也可以查所有的owner的表，以及所有catalog的表
    List<TableInfo> searchDiscoveryNames(LakeCatParam lakeCatParam);

    void alterDatabase(String catalogName, String dbName, String owner);

    void alterDatabase(DatabaseInfo entity);

    void toTablePrivilegeForRole(String roleName, String objectname);

    Database getDataBase(String catalogName, String dbName);

    Role getRole(String roleName);

    //模型
    Glossary getGlossary(String type);

    Glossary getCacheGlossary(String type);

    void creteGlossary(Model model);

    Category updateGlossary(Model model);

    void deleteGlossary(Integer id);

    //类目
    Category getCategory(Integer id);

    Category createCategory(Model model);

    Category updateCategory(Model model);

    void deleteCategory(Integer id);

    PageInfo<DatabaseSearch> pagesDb(DatabaseInfo databaseInfo);

    //经典模型树
    Category listDbTree();

    List<CatalogTableCount> getTableCountByCatalog(LakeCatParam lakeCatParam);

    ObjectCount getObjectCountByCategory(LakeCatParam lakeCatParam);

    TableCategories getTableRelationship(TableInfo tableInfo);

    String getLatestPartitionName(TableInfo tableInfo, String sole);

    //删除缓存
    Boolean clearCachePartitionName(TableInfo tableInfo, String sole);

    Integer getPartitionCount(TableInfo tableInfo, String sole);

    Boolean clearCachePartitionCount(TableInfo tableInfo, String sole);

    //血缘相关
    LineageInfo getLineageGraph(LakeCatParam lakeCatParam);

    LineageFact getLineageFact(LakeCatParam lakeCatParam);

}
