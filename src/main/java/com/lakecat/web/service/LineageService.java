package com.lakecat.web.service;

import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.LineageResult;
import io.lakecat.catalog.common.lineage.LineageFact;

public interface LineageService {

    LineageResult getLineageGraph(LakeCatParam lakeCatParam);

    LineageFact getLineageFact(LakeCatParam lakeCatParam);

}
