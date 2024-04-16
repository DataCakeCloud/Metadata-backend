package com.lakecat.web;

/**
 * Created by slj on 2022/5/30.
 */

import com.google.common.collect.Lists;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReadExcel {
    public static void main(String[] args) {
        ReadExcel obj = new ReadExcel();
        // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
        File file = new File("/Users/slj/data/mode/zhanshulin.xls");
        List excelList = obj.readExcel(file);
        System.out.println("list中的数据打印出来");
        for (int i = 0; i < excelList.size(); i++) {
            List list = (List) excelList.get(0);
            System.out.println(list.size());
            for (int j = 0; j < list.size(); j++) {
                System.out.print(list.get(j));
            }
            System.out.println();
        }

    }

    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
    public List readExcel(File file) {
        try {
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                List <List> outerList = new ArrayList <List>();
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 0; i < sheet.getRows(); i++) {
                    List innerList = new ArrayList();
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if (cellinfo.isEmpty()) {
                            continue;
                        }
                        innerList.add(cellinfo);
                    }
                    outerList.add(i, innerList);
                }
                return outerList;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> readExcelUtils(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setRegion(sheet.getCell(0, j).getContents());
                    demo.setDbName(sheet.getCell(1, j).getContents());
                    demo.setTableName(sheet.getCell(2, j).getContents());
                    demo.setInterval(sheet.getCell(3, j).getContents());
                    demo.setApplication(sheet.getCell(4, j).getContents());
                    demo.setDescription(sheet.getCell(5, j).getContents());
                    demo.setHierarchical(sheet.getCell(6, j).getContents());
                    demo.setOwner(sheet.getCell(7, j).getContents());
                    demo.setSubject(sheet.getCell(8, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> readExcelUtils_TEST(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setTableName(sheet.getCell(0, j).getContents());
                    demo.setOwner(sheet.getCell(1, j).getContents());
                    demo.setNewOwner(sheet.getCell(2, j).getContents());
                    demo.setRole(sheet.getCell(3, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> readExcelOne(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setDbName(sheet.getCell(1, j).getContents());
                    demo.setTableName(sheet.getCell(2, j).getContents());
                    demo.setOwner(sheet.getCell(0, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> readExcelOneForRole(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setRole(sheet.getCell(0, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> addUsersForRole(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setRole(sheet.getCell(0, j).getContents());
                    demo.setOwner(sheet.getCell(1, j).getContents());
                    demo.setRegion(sheet.getCell(2, j).getContents());
                    demo.setDbName(sheet.getCell(3, j).getContents());
                    demo.setFlag(sheet.getCell(4, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> addUsersForRoleForRegion(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setRole(sheet.getCell(0, j).getContents());
                    demo.setOwner(sheet.getCell(1, j).getContents());
                    demo.setRegion(sheet.getCell(2, j).getContents());
                    demo.setFlag(sheet.getCell(3, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> addUsersForRoleForTable(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setRole(sheet.getCell(0, j).getContents().trim());
                    demo.setOwner(sheet.getCell(1, j).getContents().trim());
                    demo.setRegion(sheet.getCell(2, j).getContents().trim());
                    demo.setDbName(sheet.getCell(3, j).getContents().trim());
                    demo.setTableName(sheet.getCell(4, j).getContents().trim());
                    demo.setFlag(sheet.getCell(5, j).getContents().trim());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> addUsersForRoleForTableSelect(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setOwner(sheet.getCell(0, j).getContents());
                    demo.setInputGuids(sheet.getCell(1, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List <Demo> taskTable(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setKey(sheet.getCell(0, j).getContents().trim());
                    demo.setOwner(sheet.getCell(1, j).getContents().trim());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List <Demo> addUsersForRoleForTableSelectUser(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo demo = new Demo();
                    demo.setOwner(sheet.getCell(0, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List <Demo01> readExcelUtilsForClu(File file) {

        try {
            //创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            Workbook wb = Workbook.getWorkbook(is);
            int numberOfSheets = wb.getNumberOfSheets();
            for (int i = 0; i < numberOfSheets; i++) {
                ArrayList <Demo01> outerList = Lists.newArrayList();
                //每个页签创建一个sheet对象
                Sheet sheet = wb.getSheet(i);
                for (int j = 1; j < sheet.getRows(); j++) {
                    Demo01 demo = new Demo01();
                    demo.setDbName(sheet.getCell(0, j).getContents());
                    demo.setTableName(sheet.getCell(1, j).getContents());
                    demo.setName(sheet.getCell(3, j).getContents());
                    demo.setComment(sheet.getCell(4, j).getContents());
                    demo.setType(sheet.getCell(5, j).getContents());
                    demo.setRegion(sheet.getCell(6, j).getContents());
                    outerList.add(demo);
                }
                return outerList;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
