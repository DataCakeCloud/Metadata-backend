package com.lakecat.web.service.impl;

import com.lakecat.web.service.ISyncService;
import com.lakecat.web.service.SyncModelService;
import org.springframework.stereotype.Service;

@Service
public class SyncModelServiceImpl implements SyncModelService {


    @Override
    public boolean syncModelInfo(ISyncService iSyncService) {
        return false;
    }
}
