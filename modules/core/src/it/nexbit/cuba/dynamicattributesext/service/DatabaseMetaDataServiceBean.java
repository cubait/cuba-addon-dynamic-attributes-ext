package it.nexbit.cuba.dynamicattributesext.service;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.global.Stores;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.firstNonNull;

@Service(DatabaseMetaDataService.NAME)
public class DatabaseMetaDataServiceBean implements DatabaseMetaDataService {

    @Inject
    protected Persistence persistence;

    @Override
    public Collection<TableInfo> getTables(String storeName) throws SQLException {
        List<TableInfo> tableInfos = new ArrayList<>();

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager entityManager = persistence.getEntityManager(storeName);
            Connection connection = entityManager.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            String schema = connection.getSchema();
            ResultSet rs = metaData.getTables(catalog, schema, "%", null);
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");
                String tableType = rs.getString("TABLE_TYPE");
                String tableRemarks = rs.getString("REMARKS");
                TableInfo tableInfo = new TableInfo(storeName, tableName, parseTableType(tableType), catalog, schema, tableRemarks);
                tableInfos.add(tableInfo);
            }
            rs.close();
            tx.commit();
        } finally {
            tx.end();
        }

        return tableInfos;
    }

    @Override
    public Collection<TableInfo> getTables() throws SQLException {
        return getTables(Stores.MAIN);
    }

    @Override
    public Collection<ColumnInfo> getColumns(TableInfo table) throws SQLException {
        List<ColumnInfo> columnInfos = new ArrayList<>();

        Transaction tx = persistence.createTransaction();
        try {
            EntityManager entityManager = persistence.getEntityManager(table.getStoreName());
            Connection connection = entityManager.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getColumns(
                    firstNonNull(table.getCatalog(), connection.getCatalog()),
                    firstNonNull(table.getSchema(), connection.getSchema()),
                    table.getName(),
                    "%");
            while (rs.next()) {
                String nullable = rs.getString("IS_NULLABLE");
                String autoIncrement = rs.getString("IS_AUTOINCREMENT");
                String generated = rs.getString("IS_GENERATEDCOLUMN");
                ColumnInfo columnInfo = new ColumnInfo(
                        table,
                        rs.getString("COLUMN_NAME"),
                        rs.getInt("DATA_TYPE"),
                        rs.getString("TYPE_NAME"),
                        rs.getInt("COLUMN_SIZE"),
                        rs.getInt("DECIMAL_DIGITS"),
                        rs.getString("REMARKS"),
                        rs.getString("COLUMN_DEF"),
                        rs.getInt("ORDINAL_POSITION"),
                        !Strings.isNullOrEmpty(nullable) && "YES".equals(nullable),
                        !Strings.isNullOrEmpty(autoIncrement) && "YES".equals(autoIncrement),
                        !Strings.isNullOrEmpty(generated) && "YES".equals(generated)
                );
                columnInfos.add(columnInfo);
            }
            rs.close();
            tx.commit();
        } finally {
            tx.end();
        }

        return columnInfos;
    }

    protected TableType parseTableType(String tableType) {
        switch (tableType) {
            case "TABLE":
                return TableType.TABLE;
            case "VIEW":
                return TableType.VIEW;
            case "SYSTEM TABLE":
                return TableType.SYSTEM_TABLE;
            case "GLOBAL TEMPORARY":
                return TableType.GLOBAL_TEMPORARY;
            case "LOCAL TEMPORARY":
                return TableType.LOCAL_TEMPORARY;
            case "ALIAS":
                return TableType.ALIAS;
            case "SYNONYM":
                return TableType.SYNONYM;
        }
        return null;
    }
}