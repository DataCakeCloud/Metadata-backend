package com.lakecat.web.service;

/**
 * Created by slj on 2023/3/7.
 */
public interface ThreadService {
    void initTable();


    void initTableOhter(boolean update);

    void syncForRole();

    void syncForPvc();

    void syncForLast();

}
