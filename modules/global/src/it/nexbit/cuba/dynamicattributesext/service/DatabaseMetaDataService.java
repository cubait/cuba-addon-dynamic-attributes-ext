package it.nexbit.cuba.dynamicattributesext.service;

import java.sql.SQLException;
import java.util.Collection;

public interface DatabaseMetaDataService {
    String NAME = "dynattrext_DatabaseMetaDataService";

    Collection<TableInfo> getTables(String storeName) throws SQLException;
    Collection<TableInfo> getTables() throws SQLException;

    Collection<ColumnInfo> getColumns(TableInfo table) throws SQLException;
}