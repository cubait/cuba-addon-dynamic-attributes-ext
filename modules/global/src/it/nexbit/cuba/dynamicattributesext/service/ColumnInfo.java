package it.nexbit.cuba.dynamicattributesext.service;

import com.haulmont.bali.util.Preconditions;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Types;
import java.util.Objects;

public class ColumnInfo implements Serializable {

    private static final long serialVersionUID = 6960882970448779159L;

    protected TableInfo table;
    protected String name;
    // constants from java.sql.Types class
    protected int sqlType;
    // type name as returned by the RDBMS (vendor specific)
    protected String vendorTypeName;
    protected int size;
    // may be null where not applicable
    protected Integer decimalDigits;
    protected String remarks;
    protected String defaultValue;
    // Starts from 1
    protected int ordinalPosition;
    protected boolean nullable;
    protected boolean autoIncrement;
    protected boolean generated;

    public ColumnInfo(@NotNull TableInfo table, @NotNull String name, int sqlType, String vendorTypeName,
                      int size, Integer decimalDigits, String remarks, String defaultValue,
                      int ordinalPosition, boolean nullable, boolean autoIncrement, boolean generated) {

        Preconditions.checkNotNullArgument(table);
        Preconditions.checkNotEmptyString(name);

        this.table = table;
        this.name = name;
        this.sqlType = sqlType;
        this.vendorTypeName = vendorTypeName;
        this.size = size;
        this.decimalDigits = decimalDigits;
        this.remarks = remarks;
        this.defaultValue = defaultValue;
        this.ordinalPosition = ordinalPosition;
        this.nullable = nullable;
        this.autoIncrement = autoIncrement;
        this.generated = generated;
    }

    public TableInfo getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public int getSqlType() {
        return sqlType;
    }

    public String getVendorTypeName() {
        return vendorTypeName;
    }

    public int getSize() {
        return size;
    }

    public Integer getDecimalDigits() {
        return decimalDigits;
    }

    public String getRemarks() {
        return remarks;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public int getOrdinalPosition() {
        return ordinalPosition;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public boolean isGenerated() {
        return generated;
    }

    public boolean isKeyCandidate() {
        return sqlType == Types.BIGINT ||
                sqlType == Types.CHAR ||
                sqlType == Types.INTEGER ||
                sqlType == Types.LONGNVARCHAR ||
                sqlType == Types.LONGVARCHAR ||
                sqlType == Types.NVARCHAR ||
                sqlType == Types.NCHAR ||
                sqlType == Types.ROWID ||
                sqlType == Types.VARCHAR ||
                isUuid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ColumnInfo)) return false;
        ColumnInfo that = (ColumnInfo) o;
        return table.equals(that.table) &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(table, name);
    }

    public boolean isUuid() {
        return vendorTypeName.toLowerCase().equals("uniqueidentifier") ||
                vendorTypeName.toLowerCase().equals("uuid");
    }
}
