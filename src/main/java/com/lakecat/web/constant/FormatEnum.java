package com.lakecat.web.constant;

/**
 * 建表格式参数枚举
 */
public enum FormatEnum {
    /**
     * hive表 存储路径
     */


    /**
     * hive表 FileFormat格式参数
     */
    FILEFORMAT_PARQUENT("Parquet"),
    FILEFORMAT_TEXTFILE("TextFile"),
    FILEFORMAT_SEQUENCEFILE("SequenceFile"),
    FILEFORMAT_RCFILE("RCFile"),
    FILEFORMAT_AVRO("AVRO"),
    FILEFORMAT_ORC("ORC"),
    /**
     * hive表 InputFormat设置参数
     */
    INPUTFORMAT_PARQUENT("org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat"),
    INPUTFORMAT_TEXTFILE("org.apache.hadoop.mapred.TextInputFormat"),
    INPUTFORMAT_SEQUENCEFILE("org.apache.hadoop.mapred.SequenceFileInputFormat"),
    INPUTFORMAT_RCFILE("org.apache.hadoop.hive.ql.io.RCFileInputFormat"),
    INPUTFORMAT_AVRO("org.apache.hadoop.hive.ql.io.avro.AvroContainerInputFormat"),
    INPUTFORMAT_ORC("org.apache.hadoop.hive.ql.io.orc.OrcInputFormat");


    private final String format;

    FormatEnum(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
