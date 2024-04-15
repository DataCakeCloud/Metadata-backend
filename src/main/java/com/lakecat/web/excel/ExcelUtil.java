package com.lakecat.web.excel;


import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Excel工具类
 * @author CAOXUEDONG
 */
public class ExcelUtil {

	/**
	 * 读取Excel内容，只读取第一个sheet
	 *
	 * @param fileName
	 *            文件名称（包含文件路径）
	 * @return 读取结果，可能包含null值
	 */
	public static List<String[]> readExcel(String fileName) {
		return readExcel(new File(fileName));
	}

	/**
	 * 读取Excel内容，只读取第一个sheet
	 *
	 * @param file
	 *            要读取的文件
	 * @return 读取结果，可能包含null值
	 */
	public static List<String[]> readExcel(File file) {
		List<String[]> result = new ArrayList<String[]>();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(fileInputStream);// 通用
			Sheet sheet = wb.getSheetAt(0);
			for (Row row : sheet) {
				String[] rowStrs = new String[row.getLastCellNum()];
				for (Cell cell : row) {
					switch (cell.getCellType()) {
					case Cell.CELL_TYPE_BLANK:
						rowStrs[cell.getColumnIndex()] = "";
						break;
					case Cell.CELL_TYPE_BOOLEAN:
						rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getBooleanCellValue());
						break;
					case Cell.CELL_TYPE_ERROR:
						rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getErrorCellValue());
						break;
					case Cell.CELL_TYPE_NUMERIC:
						rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getNumericCellValue());
						break;
					case Cell.CELL_TYPE_STRING:
						rowStrs[cell.getColumnIndex()] = cell.getStringCellValue().replaceAll("\t|\r|\n", "").trim();
						break;
					case Cell.CELL_TYPE_FORMULA:
						rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getNumericCellValue());
						break;
					default:
						rowStrs[cell.getColumnIndex()] = "";
						break;
					}
				}
				result.add(rowStrs);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	/**
	 * 以文本格式读取Excel内容，只读取第一个sheet
	 *
	 * @param file
	 *            要读取的文件
	 * @return 读取结果，可能包含null值
	 */
	public static List<String[]> readExcelWithText(File file) {
		List<String[]> result = new ArrayList<String[]>();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			Sheet sheet = WorkbookFactory.create(fileInputStream).getSheetAt(0);
			for (Row row : sheet) {
				String[] rowStrs = new String[row.getLastCellNum()];
				for (Cell cell : row) {
					cell.setCellType(Cell.CELL_TYPE_STRING);
					rowStrs[cell.getColumnIndex()] = cell.getStringCellValue();
				}
				result.add(rowStrs);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	public static List<String> readExcelSheetNames(File file) {
		List<String> result = new ArrayList<String>();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(fileInputStream);// 通用
			int num = wb.getNumberOfSheets();
			for (int i = 0; i < num; i++) {
				String sheetName = wb.getSheetName(i);
				result.add(sheetName);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	/**
	 * 读取excel，指定sheet页内容
	 *
	 * @param file
	 * @param sheetNum
	 *            从0开始
	 * @return
	 */
	public static List<String[]> readExcel(File file, int sheetNum) {
		List<String[]> result = new ArrayList<String[]>();
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			Workbook wb = WorkbookFactory.create(fileInputStream);// 通用
			int sheetCount = wb.getNumberOfSheets();
			if (sheetNum < sheetCount) {
				Sheet sheet = wb.getSheetAt(sheetNum);
				for (Row row : sheet) {
					String[] rowStrs = new String[row.getLastCellNum()];
					for (Cell cell : row) {
						switch (cell.getCellType()) {
						case Cell.CELL_TYPE_BLANK:
							rowStrs[cell.getColumnIndex()] = "";
							break;
						case Cell.CELL_TYPE_BOOLEAN:
							rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getBooleanCellValue());
							break;
						case Cell.CELL_TYPE_ERROR:
							rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getErrorCellValue());
							break;
						case Cell.CELL_TYPE_NUMERIC:
							rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getNumericCellValue());
							break;
						case Cell.CELL_TYPE_STRING:
							rowStrs[cell.getColumnIndex()] = cell.getStringCellValue().replaceAll("\t|\r|\n", "").trim();
							break;
						case Cell.CELL_TYPE_FORMULA:
							rowStrs[cell.getColumnIndex()] = String.valueOf(cell.getNumericCellValue());
							break;
						default:
							rowStrs[cell.getColumnIndex()] = "";
							break;
						}
					}
					result.add(rowStrs);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
				}
			}
		}
		return result;
	}

	public static void exportExcel(List<? extends AbstractExcelSheetVO> excelSheetVOs, OutputStream out) throws IOException {
		Workbook wb = new HSSFWorkbook();
		if (CollectionUtils.isNotEmpty(excelSheetVOs)){
			List<AbstractExcelSheetVO> results= Lists.newArrayList();
			for (AbstractExcelSheetVO vo : excelSheetVOs){
				if (CollectionUtils.isNotEmpty(vo.getDatas())&&vo.getDatas().size()>65530){
					List<String> headers=vo.getDatas().get(0);
					List<List<List<String>>> listList= PubMethod.subList(vo.getDatas(),65530);
					int i=0;
					for (List<List<String>> data:listList){
						i++;
						ExcelSheetVO excelSheetVO = new ExcelSheetVO();
						if (i>1){
							List<List<String>> newDatas=Lists.newArrayList();
							newDatas.add(headers);
							newDatas.addAll(data);
							excelSheetVO.setDatas(newDatas);
							excelSheetVO.setName(vo.getName()+i);
						}else {
							excelSheetVO.setDatas(data);
							excelSheetVO.setName(vo.getName()+i);
						}
						results.add(excelSheetVO);
					}
				}else {
					results.add(vo);
				}
			}
			excelSheetVOs=results;
		}
		for (AbstractExcelSheetVO vo : excelSheetVOs) {
			Sheet sheet = wb.createSheet(vo.getName());
			int rowNum = 0;
			for (List<String> datas : vo.getDatas()) {
				Row row = sheet.createRow(rowNum);
				int cellNum = 0;
				for (String str : datas) {
					str = str != null ? str : "";
					Cell cell = row.createCell(cellNum);
					cell.setCellValue(str);

					//set cell style
					vo.setCellStyle(wb, sheet, row, cell, rowNum, cellNum);
					//set header style
					vo.setHeaderStyle(wb, sheet, row, cell, rowNum, cellNum);

					cellNum++;
				}
				rowNum++;
			}
			//执行扩展的方法
			vo.mergedRegion(sheet);
		}
		wb.write(out);
	}

	public static void exportExcelGov(List<? extends AbstractExcelSheetVOForGov> excelSheetVOs, OutputStream out) throws IOException {
		Workbook wb = new HSSFWorkbook();
		if (CollectionUtils.isNotEmpty(excelSheetVOs)){
			List<AbstractExcelSheetVOForGov> results= Lists.newArrayList();
			for (AbstractExcelSheetVOForGov vo : excelSheetVOs){
				if (CollectionUtils.isNotEmpty(vo.getDatas())&&vo.getDatas().size()>65530){
                    Collection <Object> headers = vo.getDatas().get(0);
                    List <List <Collection <Object>>> lists = PubMethod.subList(vo.getDatas(), 65530);
                    int i=0;
					for (List <Collection <Object>> data:lists){
						i++;
						ExcelSheetVOForGov excelSheetVO = new ExcelSheetVOForGov();
						if (i>1){
                            List <Collection <Object>> newDatas=Lists.newArrayList();
							newDatas.add(headers);
							newDatas.addAll(data);
							excelSheetVO.setDatas(newDatas);
							excelSheetVO.setName(vo.getName()+i);
						}else {
							excelSheetVO.setDatas(data);
							excelSheetVO.setName(vo.getName()+i);
						}
						results.add(excelSheetVO);
					}
				}else {
					results.add(vo);
				}
			}
			excelSheetVOs=results;
		}
		for (AbstractExcelSheetVOForGov vo : excelSheetVOs) {
			Sheet sheet = wb.createSheet(vo.getName());
			int rowNum = 0;
			for (Collection <Object> datas : vo.getDatas()) {
				Row row = sheet.createRow(rowNum);
				int cellNum = 0;
				for (Object str : datas) {
					str = str != null ? str : "";
					Cell cell = row.createCell(cellNum);
					cell.setCellValue(str.toString());

					//set cell style
					vo.setCellStyle(wb, sheet, row, cell, rowNum, cellNum);
					//set header style
					vo.setHeaderStyle(wb, sheet, row, cell, rowNum, cellNum);

					cellNum++;
				}
				rowNum++;
			}
			//执行扩展的方法
			vo.mergedRegion(sheet);
		}
		wb.write(out);
	}


}
