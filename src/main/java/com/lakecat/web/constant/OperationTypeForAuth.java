package com.lakecat.web.constant;

import com.lakecat.web.exception.BusinessException;
import io.lakecat.catalog.common.Operation;

import java.util.HashMap;
import java.util.Map;

public enum OperationTypeForAuth {

    /**
     * 修改库
     */
    CREATE_DATABASE("CREATE DATABASE", "创建库", Operation.CREATE_DATABASE, ObjectType.REGION),
    ALTER_DATABASE("ALTER DATABASE", "修改库", Operation.ALTER_DATABASE, ObjectType.DATABASE),
    DROP_DATABASE("DROP DATABASE", "删除库", Operation.DROP_DATABASE, ObjectType.DATABASE),
    DESC_DATABASE("DESC DATABASE", "描述库", Operation.DESC_DATABASE, ObjectType.DATABASE),
    CREATE_TABLE("CREATE TABLE", "创建表", Operation.CREATE_TABLE, ObjectType.DATABASE),
    ALTER_TABLE("ALTER TABLE", "修改表", Operation.ALTER_TABLE, ObjectType.TABLE),
    DROP_TABLE("DROP TABLE", "删除表", Operation.DROP_TABLE, ObjectType.TABLE),
    DESC_TABLE("DESC TABLE", "描述表", Operation.DESC_TABLE, ObjectType.TABLE),
    SELECT_TABLE("SELECT TABLE", "查询表", Operation.SELECT_TABLE, ObjectType.TABLE),
    INSERT_TABLE("INSERT TABLE", "插入表", Operation.INSERT_TABLE, ObjectType.TABLE),
    ;

    public String typeValue;
    public String cnName;
    public Operation operation;
    public ObjectType objectType;
    private static final Map <String, OperationTypeForAuth> ANY_NAME_MAP = new HashMap <>();

    static {
        for (OperationTypeForAuth e : OperationTypeForAuth.values()) {
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
    public static OperationTypeForAuth get(String anyName) throws RuntimeException {
        if (ANY_NAME_MAP.containsKey(anyName)) {
            return ANY_NAME_MAP.get(anyName);
        }
        throw new RuntimeException("传入参数有误");
    }

    OperationTypeForAuth(String typeValue, String cnName, Operation operation, ObjectType objectType) {
        this.typeValue = typeValue;
        this.cnName = cnName;
        this.operation = operation;
        this.objectType = objectType;
    }


    public static String getTypeValue(String printName) throws BusinessException {
        return get(printName).operation.getPrintName();
    }


    public static void main(String[] args) throws BusinessException {

        String cnName = OperationTypeForAuth.get("CREATE_DATABASE").cnName;
        System.out.println(cnName);

    }

}
