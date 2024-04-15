package com.lakecat.web.constant;

public enum CloudStorageType {
    /**
     * aws s3: STANDARD  INTELLIGENT  DEEP
     * huawei obs: STANDARD  ARCHIVE
     */
    STANDARD("标准存储"),
    INTELLIGENT("智能分层"),
    DEEP("冷冻存储"),
    ARCHIVE("归档存储"),
    ;

    public String desc;

    CloudStorageType (String desc) {
        this.desc = desc;
    }
}
