package com.lakecat.web.service.impl;

import com.lakecat.web.service.ISwitchService;
import org.springframework.stereotype.Service;

import java.util.HashMap;


/**
 * @author slj
 */
@Service
public class SwitchServiceImpl implements ISwitchService {


    private final HashMap <String, Boolean> CUMap = new HashMap <>();


    @Override
    public Boolean updateSwitch(Boolean CU) {
        CUMap.put("CU", CU);
        return CU;
    }

    @Override
    public Boolean getSwitch() {
        Boolean flag = false;
        if (!CUMap.isEmpty()) {
            flag = CUMap.get("CU");
        }
        return flag;
    }

}
