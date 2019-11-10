package it.nexbit.cuba.dynamicattributesext.service;

import com.haulmont.bali.util.Preconditions;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

public class TableInfo implements Serializable {

    private static final long serialVersionUID = -1466751551885324907L;

    protected String storeName;
    protected String catalog;
    protected String schema;
    protected String name;
    protected TableType type;
    protected String remarks;

    public TableInfo(@NotNull String storeName, @NotNull String name, @NotNull TableType type) {
        this(storeName, name, type, null, null, null);
    }

    public TableInfo(@NotNull String storeName, @NotNull String name, @NotNull TableType type, String catalog, String schema, String remarks) {
        Preconditions.checkNotEmptyString(storeName);
        Preconditions.checkNotEmptyString(name);
        Preconditions.checkNotNullArgument(type);

        this.storeName = storeName;
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.type = type;
        this.remarks = remarks;
    }

    public String getStoreName() {
        return storeName;
    }

    public String getCatalog() {
        return catalog;
    }

    public String getSchema() {
        return schema;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public TableType getType() {
        return type;
    }

    public String getRemarks() {
        return remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableInfo)) return false;
        TableInfo tableInfo = (TableInfo) o;
        return Objects.equals(catalog, tableInfo.catalog) &&
                Objects.equals(schema, tableInfo.schema) &&
                name.equals(tableInfo.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(catalog, schema, name);
    }
}
