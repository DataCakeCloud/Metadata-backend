package com.lakecat.web;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by slj on 2022/5/30.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneAttachExcel {

    @ExcelProperty(index = 0, value = "联络标识")
    private String contract_id;
    @ExcelProperty(index = 1, value = "录音地址")
    private String url;
    @ExcelProperty(index = 2, value = "手机号")
    private String phone;

}
