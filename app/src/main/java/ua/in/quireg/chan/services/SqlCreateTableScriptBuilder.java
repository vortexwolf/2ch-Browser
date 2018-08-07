package ua.in.quireg.chan.services;

import java.util.ArrayList;

public class SqlCreateTableScriptBuilder {

    private final String mTableName;
    private String mPrimaryKey;
    private final ArrayList<String> mColumns = new ArrayList<String>();

    public SqlCreateTableScriptBuilder(String tableName) {
        mTableName = tableName;
    }

    public SqlCreateTableScriptBuilder addPrimaryKey(String columnName) {
        mPrimaryKey = columnName;

        return this;
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName) {
        return addColumn(columnName, "text", true);
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName, String columnType) {
        return addColumn(columnName, columnType, true);
    }

    public SqlCreateTableScriptBuilder addColumn(String columnName, String columnType, boolean nullable) {
        String columnString = columnName + " " + columnType + " " + (nullable ? "null" : "not null");

        mColumns.add(columnString);

        return this;
    }

    public String toSql() {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("create table " + mTableName + "(");
        sqlBuilder.append(mPrimaryKey + " integer primary key autoincrement, ");
        for (int i = 0; i < mColumns.size(); i++) {
            String columnString = mColumns.get(i);
            sqlBuilder.append(columnString);

            if (i != mColumns.size() - 1) {
                sqlBuilder.append(", ");
            }
        }
        sqlBuilder.append(");");

        return sqlBuilder.toString();
    }
}
