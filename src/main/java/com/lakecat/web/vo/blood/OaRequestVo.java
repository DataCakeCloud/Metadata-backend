package com.lakecat.web.vo.blood;

import com.beust.jcommander.internal.Lists;
import lombok.Data;

import java.util.List;

@Data
public class OaRequestVo {
    private String sqr;//申请人
    private String sqrtdbh;//申请人团队编号
    private String sqrtdmc;//申请人团队名称
    private String sqrtdfzr;//申请人团队负责人
    private String sqly;//申请理由
    private String qxsx;//权限时效
    private String qxlx;//权限类型
    private String sjjb1;//数据级别1
    private String sjjb2;//数据级别1
    private String sjjb3;//数据级别1
    private String sjjb4;//数据级别1
    private String djys1;//定义要素
    private String djys2;//定义要素
    private String djys3;//定义要素
    private String djys4;//定义要素
    private String cyzd1;//常用字段
    private String cyzd2;//常用字段
    private String cyzd3;//常用字段
    private String cyzd4;//常用字段
    private String sqrleader;//申请人leader
    private String sjgjleader;//数据归集leader
    private String xgjbld;//相关级别领导
    private String dsjfzr;//大数据负责人
    private String qxglzy;//权限管理专员；
    private List<Row> rows= Lists.newArrayList();



    @Data
    public static class Row{
        private String km;//库名
        private String bm;//表名
        private String bmgjb;//表敏感级别
        private String mgzd;//敏感字段
        private String fl;//分类
        private String bms;//表描述
        private String sjfzr;//数据负责人
    }

    @Override
    public String toString() {
        return "OaRequestVo{" +
                "sqr='" + sqr + '\'' +
                ", sqrtdbh='" + sqrtdbh + '\'' +
                ", sqrtdmc='" + sqrtdmc + '\'' +
                ", sqrtdfzr='" + sqrtdfzr + '\'' +
                ", sqly='" + sqly + '\'' +
                ", qxsx='" + qxsx + '\'' +
                ", qxlx='" + qxlx + '\'' +
                ", sjjb1='" + sjjb1 + '\'' +
                ", sjjb2='" + sjjb2 + '\'' +
                ", sjjb3='" + sjjb3 + '\'' +
                ", sjjb4='" + sjjb4 + '\'' +
                ", djys1='" + djys1 + '\'' +
                ", djys2='" + djys2 + '\'' +
                ", djys3='" + djys3 + '\'' +
                ", djys4='" + djys4 + '\'' +
                ", cyzd1='" + cyzd1 + '\'' +
                ", cyzd2='" + cyzd2 + '\'' +
                ", cyzd3='" + cyzd3 + '\'' +
                ", cyzd4='" + cyzd4 + '\'' +
                ", sqrleader='" + sqrleader + '\'' +
                ", sjgjleader='" + sjgjleader + '\'' +
                ", xgjbld='" + xgjbld + '\'' +
                ", dsjfzr='" + dsjfzr + '\'' +
                ", qxglzy='" + qxglzy + '\'' +
                ", rows=" + rows +
                '}';
    }
}
