package com.lakecat.web.service;


import com.lakecat.web.entity.DictInfo;
import com.lakecat.web.entity.TableTagInfo;

import java.util.List;

public interface TableTagInfoService {

    boolean addTableTag(List <TableTagInfo> tableTagInfo);

    List <TableTagInfo> search(Long tableId);

    List <TableTagInfo> search(String sole);

    void deleteTags(Long id);
}
