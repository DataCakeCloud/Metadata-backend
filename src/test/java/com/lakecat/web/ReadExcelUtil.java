package com.lakecat.web;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by slj on 2022/5/30.
 */
public class ReadExcelUtil extends AnalysisEventListener<PhoneAttachExcel> {

    List <PhoneAttachExcel> list = new ArrayList <>();
    @Override
    public void invoke(PhoneAttachExcel phoneAttachExcel, AnalysisContext analysisContext) {
        list.add(phoneAttachExcel);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
}
