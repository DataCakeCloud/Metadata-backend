package com.lakecat.web.entity;

import java.util.List;
import java.util.Map;

public class DingCallBack {
    private List<Map> formComponentValues;
    private String result;
    private String status;

    public List<Map> getFormComponentValues() {
        return this.formComponentValues;
    }

    public String getResult(){ return  this.result; }

    public String getStatus(){return  this.status; }

    public void  setResult(String result){
        this.result = result;
    }

    public void  setStatus(String status){
        this.status = status;
    }

    public void setFormComponentValues(List<Map> formComponentValues) {
        this.formComponentValues = formComponentValues;
    }
}
