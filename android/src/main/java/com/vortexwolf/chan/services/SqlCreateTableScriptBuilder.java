package com.vortexwolf.chan.services;

import java.util.ArrayList;

public class SqlCreateTableScriptBuilder {

    private final String mTableName;
    private String mPrimaryKey;
    private final ArrayList<String> mColumns = new ArrayList<String>();

    public SqlCreateTableScriptBuilder(String tableName) {
        this.mTableName = tableName;
    }

    public SqlCreateTableScriptBuilder addPrimaryKey(String columnName) {
        this.mPrimaryKey = columnName;

        return this;
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName) {
        return this.addColumn(columnName, "text", true);
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName, String columnType) {
        return this.addColumn(columnName, columnType, true);
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName, String columnType, boolean nullable) {
        String columnString = columnName + " " + columnType + " " + (nullable ? "null" : "not null");

        this.mColumns.add(columnString);

        return this;
    }

    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table " + this.mTableName + "(");
        sqlBuilder.append(this.mPrimaryKey + " integer primary key autoincrement, ");
        for (int i = 0; i < this.mColumns.size(); i++) {
            String columnString = this.mColumns.get(i);
            sqlBuilder.append(columnString);

            if (i != this.mColumns.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(");");

        return sqlBuilder.toString();
    }
}
