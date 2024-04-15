package com.lakecat.web.excel;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.ss.usermodel.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractExcelSheetVOForGov {

    protected String name;

    protected List <Collection <Object>> datas = new ArrayList <>();

    public AbstractExcelSheetVOForGov() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setDatas(List <Collection <Object>> datas) {
        this.datas = datas;
    }

    public void addData(Collection <Object> data) {
        datas.add(data);
    }

    public List <Collection <Object>> getDatas() {
        return datas;
    }

    //提供默认表头方法
    public void defaultHeaderStyle(Sheet sheet, Row row, Cell cell, CellStyle cellStyle, Font font, int cellNum) {
        font.setFontHeightInPoints((short) 16);//设置字体大小
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);//粗体显示
        font.setFontHeightInPoints((short) 12);
        row.setHeightInPoints(20);//行高
        sheet.setColumnWidth(cellNum, 256 * 18);//列宽
        cellStyle.setFont(font);
        cell.setCellStyle(cellStyle);
    }


    /**
     * 暂时用户扩展sheet上的行列合并
     * @param sheet
     */
    protected abstract void mergedRegion(Sheet sheet);

    /**
     * 设置表头格式
     * @param wb
     * @param sheet
     * @param row
     * @param cell
     * @param rowNum
     * @param cellNum
     */
    protected abstract void setHeaderStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum);

    /**
     * 设置单元格样式
     * @param wb
     * @param sheet
     * @param row
     * @param cell
     * @param rowNum
     * @param cellNum
     */
    protected abstract void setCellStyle(Workbook wb, Sheet sheet, Row row, Cell cell, int rowNum, int cellNum);

}
