package com.lakecat.web.utils;

import io.lakecat.catalog.common.model.Column;
import io.lakecat.catalog.common.model.Table;
import org.apache.hadoop.hive.metastore.api.*;

import java.util.HashMap;
import java.util.Locale;


public class LakecatTableUtils {

    public static final String TABLE_TYPE = "table_type";

    public static final String DEFAULT_TABLE_TYPE = "hive";

    private static HashMap<String, String> convertTypeMap = new HashMap() {
        {
            put("integer", "int");
        }
    };

    /**
     * 忽略大小写
     *
     * @param lmsDataType
     * @return
     */
    public static String lmsDataTypeTohmsDataType(String lmsDataType) {
        return convertTypeMap.getOrDefault(lmsDataType.toLowerCase(), lmsDataType);
    }


    public static FieldSchema lmsSchemaToMsFieldSchema(Column schemaField) {
        FieldSchema fieldSchema = new FieldSchema();
        fieldSchema.setComment(schemaField.getComment());
        fieldSchema.setName(schemaField.getColumnName());
        fieldSchema.setType(lmsDataTypeTohmsDataType(schemaField.getColType().toLowerCase()));
        return fieldSchema;
    }

    public static String getTableType(Table table) {
        String tableType = DEFAULT_TABLE_TYPE;
        if (table.getParameters() != null && table.getParameters().containsKey(TABLE_TYPE)) {
            tableType = table.getParameters().get(TABLE_TYPE);
        }
        if (tableType == null || tableType.length() == 0) {
            tableType = DEFAULT_TABLE_TYPE;
        }
        return tableType.toLowerCase(Locale.ROOT);
    }
}
