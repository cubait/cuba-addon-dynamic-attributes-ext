package it.nexbit.cuba.dynamicattributesext.entity;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.haulmont.chile.core.annotations.MetaProperty;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.annotation.Extends;

import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity(name = "dynattrext_CategoryAttribute")
@Extends(CategoryAttribute.class)
public class ExtCategoryAttribute extends CategoryAttribute {

    private static final long serialVersionUID = 6346336771341422312L;

    @Transient
    protected ExtCategoryAttributeConfiguration configuration;

    @Transient
    @MetaProperty
    public ExtCategoryAttributeConfiguration getConfiguration() {
        if (configuration == null) {
            if (!Strings.isNullOrEmpty(attributeConfigurationJson)) {
                configuration = new Gson().fromJson(attributeConfigurationJson, ExtCategoryAttributeConfiguration.class);
            } else {
                configuration = new ExtCategoryAttributeConfiguration();
            }
            configuration.setCategoryAttribute(this);
        }
        return configuration;
    }

}