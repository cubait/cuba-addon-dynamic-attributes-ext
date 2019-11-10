package it.nexbit.cuba.dynamicattributesext.web.screens;

import com.google.common.base.Strings;
import com.haulmont.cuba.core.entity.CategoryAttributeConfiguration;
import com.haulmont.cuba.core.global.AppBeans;
import com.haulmont.cuba.core.global.Messages;
import com.haulmont.cuba.gui.app.core.categories.AttributeEditor;
import com.haulmont.cuba.gui.components.HasValue;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.cuba.gui.data.impl.DatasourceImplementation;
import it.nexbit.cuba.dynamicattributesext.entity.ExtCategoryAttributeConfiguration;
import it.nexbit.cuba.dynamicattributesext.service.ColumnInfo;
import it.nexbit.cuba.dynamicattributesext.service.DatabaseMetaDataService;
import it.nexbit.cuba.dynamicattributesext.service.TableInfo;
import it.nexbit.cuba.dynamicattributesext.service.TableType;

import javax.inject.Inject;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ExtAttributeEditor extends AttributeEditor {

    protected static final Pattern SQL_SELECT_PATTERN = Pattern.compile("(?i)^\\s*select\\s+([^\\s,]+),\\s*(\\S+)\\s+from\\s+(\\S+).*");

    @Inject
    protected LookupField<TableInfo> tableLookupField;
    @Inject
    protected LookupField<ColumnInfo> fkColumnLookupField;
    @Inject
    protected LookupField<ColumnInfo> valueColumnLookupField;

    @Inject
    protected DatabaseMetaDataService databaseMetaDataService;

    protected boolean isSettingInitialValues;

    @Override
    protected void initAttributesFieldGroup() {
        super.initAttributesFieldGroup();

        // initialize lookup fields options
        try {
            Collection<TableInfo> tableInfos = databaseMetaDataService.getTables();

            tableLookupField.setOptionsList(
                    tableInfos.stream()
                            .filter(tableInfo -> tableInfo.getType() == TableType.TABLE || tableInfo.getType() == TableType.VIEW)
                            .sorted(Comparator.comparing(TableInfo::getName))
                            .collect(Collectors.toList())
            );
            tableLookupField.setOptionCaptionProvider(TableInfo::getName);
            tableLookupField.setNullOptionVisible(false);
            tableLookupField.addValueChangeListener(changeEvent -> {
                setConfigurationDsAsModified();
                TableInfo selectedTable = changeEvent.getValue();

                // populate columns lookup fields
                try {
                    Collection<ColumnInfo> columnInfos = databaseMetaDataService.getColumns(selectedTable);
                    List<ColumnInfo> fkColumnOptions =
                            columnInfos.stream()
                                    .filter(ColumnInfo::isKeyCandidate)
                                    .sorted(Comparator.comparing(ColumnInfo::getName))
                                    .collect(Collectors.toList());
                    List<ColumnInfo> valueColumnOptions =
                            columnInfos.stream()
                                    .sorted(Comparator.comparing(ColumnInfo::getName))
                                    .collect(Collectors.toList());

                    setupLookupField(fkColumnLookupField, fkColumnOptions);
                    setupLookupField(valueColumnLookupField, valueColumnOptions);

                } catch (SQLException e) {

                    setupLookupField(fkColumnLookupField, null);
                    setupLookupField(valueColumnLookupField, null);
                }
            });

            final Consumer<HasValue.ValueChangeEvent<ColumnInfo>> columnChangeListener = changeEvent -> setConfigurationDsAsModified();
            fkColumnLookupField.addValueChangeListener(columnChangeListener);
            valueColumnLookupField.addValueChangeListener(columnChangeListener);

        } catch (SQLException e) {
            tableLookupField.setNullOptionVisible(true);
            tableLookupField.setNullSelectionCaption(getMessage("errorRetrievingTables"));
        }

    }

    protected static void setupLookupField(LookupField<ColumnInfo> columnLookupField, List<ColumnInfo> columnOptions) {
        if (columnOptions != null) {
            columnLookupField.setOptionCaptionProvider(ColumnInfo::getName);
            columnLookupField.setOptionsList(columnOptions);
            columnLookupField.setNullOptionVisible(false);
            columnLookupField.setValue(columnOptions.iterator().next());
        } else {
            columnLookupField.setNullOptionVisible(true);
            columnLookupField.setNullSelectionCaption(AppBeans.get(Messages.class).getMessage(ExtAttributeEditor.class, "errorRetrievingColumns"));
            columnLookupField.setValue(null);
        }
    }

    @Override
    protected void postInit() {
        super.postInit();

        // load initial values (if any)
        ParseRawSqlSelectResult parseResult = parseRawSqlSelect();
        if (parseResult != null) {
            isSettingInitialValues = true;

            TableInfo tableToSelect = tableLookupField.getOptions().getOptions()
                    .filter(tableInfo -> tableInfo.getName().equals(parseResult.tableName))
                    .findFirst().orElse(null);
            tableLookupField.setValue(tableToSelect);

            ColumnInfo columnToSelect = fkColumnLookupField.getOptions().getOptions()
                    .filter(columnInfo -> columnInfo.getName().equals(parseResult.fkColumnName))
                    .findFirst().orElse(null);
            fkColumnLookupField.setValue(columnToSelect);

            columnToSelect = valueColumnLookupField.getOptions().getOptions()
                    .filter(columnInfo -> columnInfo.getName().equals(parseResult.valueColumnName))
                    .findFirst().orElse(null);
            valueColumnLookupField.setValue(columnToSelect);

            isSettingInitialValues = false;
        }
    }

    @Override
    public boolean preCommit() {
        if (fkColumnLookupField.getValue() != null && valueColumnLookupField.getValue() != null) {
            ExtCategoryAttributeConfiguration configuration = (ExtCategoryAttributeConfiguration) configurationDs.getItem();
            String sqlSelect = MessageFormat.format(
                    "SELECT {1},{2} FROM {0} WHERE {1} IN (#ids)",
                    tableLookupField.getValue().getName(),
                    fkColumnLookupField.getValue().getName(),
                    valueColumnLookupField.getValue().getName()
            );
            configuration.setRawSqlSelect(sqlSelect);
        }

        return super.preCommit();
    }

    protected void setConfigurationDsAsModified() {
        if (!isSettingInitialValues)
            ((DatasourceImplementation<CategoryAttributeConfiguration>) configurationDs).modified(configurationDs.getItem());
    }

    protected ParseRawSqlSelectResult parseRawSqlSelect() {
        ExtCategoryAttributeConfiguration configuration = (ExtCategoryAttributeConfiguration) configurationDs.getItem();
        if (!Strings.isNullOrEmpty(configuration.getRawSqlSelect())) {
            Matcher matcher = SQL_SELECT_PATTERN.matcher(configuration.getRawSqlSelect());
            if (matcher.matches()) {
                ParseRawSqlSelectResult result = new ParseRawSqlSelectResult();
                result.fkColumnName = matcher.group(1);
                result.valueColumnName = matcher.group(2);
                result.tableName = matcher.group(3);
                return result;
            }
        }
        return null;
    }

    protected static class ParseRawSqlSelectResult {
        public String tableName;
        public String fkColumnName;
        public String valueColumnName;
    }
}