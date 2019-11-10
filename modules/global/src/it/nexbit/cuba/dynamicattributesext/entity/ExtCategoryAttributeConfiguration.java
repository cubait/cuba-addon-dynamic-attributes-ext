package it.nexbit.cuba.dynamicattributesext.entity;

import com.google.common.base.Strings;
import com.haulmont.chile.core.annotations.MetaClass;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.CategoryAttributeConfiguration;
import com.haulmont.cuba.core.entity.annotation.Extends;
import com.haulmont.cuba.core.entity.annotation.SystemLevel;

@MetaClass(name = "dynattrext_CategoryAttributeConfiguration")
@Extends(CategoryAttributeConfiguration.class)
@SystemLevel
public class ExtCategoryAttributeConfiguration extends CategoryAttributeConfiguration {

    private static final long serialVersionUID = -7133430932689706462L;

    @MetaProperty
    protected String rawSqlSelect;

    public String getRawSqlSelect() {
        return rawSqlSelect;
    }

    public void setRawSqlSelect(String rawSqlSelect) {
        this.rawSqlSelect = rawSqlSelect;
    }

    @Override
    public Boolean isReadOnly() {
        return super.isReadOnly() && !Strings.isNullOrEmpty(rawSqlSelect);
    }
}
