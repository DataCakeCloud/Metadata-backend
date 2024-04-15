package com.lakecat.web.constant;


import java.util.HashMap;
import java.util.Map;

import io.lakecat.catalog.common.Operation;

/**
 * @author slj
 */

public enum OperationType {
    /**
     * 修改库
     */
    CREATE_DATABASE("CREATE_DATABASE", "创建库", Operation.CREATE_DATABASE, ObjectType.REGION),
    ALTER_DATABASE("ALTER_DATABASE", "修改库", Operation.ALTER_DATABASE, ObjectType.DATABASE),
    DROP_DATABASE("DROP_DATABASE", "删除库", Operation.DROP_DATABASE, ObjectType.DATABASE),
    DESC_DATABASE("DESC_DATABASE", "描述库", Operation.DESC_DATABASE, ObjectType.DATABASE),
    CREATE_TABLE("CREATE_TABLE", "创建表", Operation.CREATE_TABLE, ObjectType.DATABASE),
    ALTER_TABLE("ALTER_TABLE", "修改表", Operation.ALTER_TABLE, ObjectType.TABLE),
    DROP_TABLE("DROP_TABLE", "删除表", Operation.DROP_TABLE, ObjectType.TABLE),
    DESC_TABLE("DESC_TABLE", "描述表", Operation.DESC_TABLE, ObjectType.TABLE),
    SELECT_TABLE("SELECT_TABLE", "查询表", Operation.SELECT_TABLE, ObjectType.TABLE),
    INSERT_TABLE("INSERT_TABLE", "插入表", Operation.INSERT_TABLE, ObjectType.TABLE),
    ;

    public String typeValue;
    public String cnName;
    public Operation operation;
    public ObjectType objectType;
    private static final Map <String, OperationType> ANY_NAME_MAP = new HashMap <>();

    static {
        for (OperationType e : OperationType.values()) {
            ANY_NAME_MAP.put(e.typeValue, e);
            ANY_NAME_MAP.put(e.cnName, e);
            ANY_NAME_MAP.put(e.operation.getPrintName(), e);
        }
    }

    /**
     * 根据 typeValue/cnName/operation's printName 获取枚举值
     *
     * @param anyName
     * @return
     */
    public static OperationType get(String anyName) {
        if (ANY_NAME_MAP.containsKey(anyName)) {
            return ANY_NAME_MAP.get(anyName);
        }
        throw new RuntimeException("传入参数有误");
    }

    OperationType(String typeValue, String cnName, Operation operation, ObjectType objectType) {
        this.typeValue = typeValue;
        this.cnName = cnName;
        this.operation = operation;
        this.objectType = objectType;
    }


    public static String getTypeValue(String printName) {
        return get(printName).operation.getPrintName();
    }


    public static void main(String[] args) {

        String cnName = OperationType.get("CREATE_DATABASE").cnName;
        System.out.println(cnName);

    }

}
