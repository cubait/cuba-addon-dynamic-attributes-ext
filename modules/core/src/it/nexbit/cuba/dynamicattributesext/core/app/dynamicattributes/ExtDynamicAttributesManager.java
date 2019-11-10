package it.nexbit.cuba.dynamicattributesext.core.app.dynamicattributes;

import com.google.common.base.Strings;
import com.google.common.collect.Multimap;
import com.haulmont.chile.core.model.MetaClass;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.app.dynamicattributes.DynamicAttributesManager;
import com.haulmont.cuba.core.entity.CategoryAttribute;
import com.haulmont.cuba.core.entity.CategoryAttributeValue;
import com.haulmont.cuba.core.global.UuidProvider;
import com.haulmont.cuba.core.sys.persistence.DbTypeConverter;
import it.nexbit.cuba.dynamicattributesext.entity.ExtCategoryAttributeConfiguration;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ExtDynamicAttributesManager extends DynamicAttributesManager {

    @Override
    protected void handleAttributeValuesForIds(MetaClass metaClass, List<Object> currentIds, Multimap<Object, CategoryAttributeValue> attributeValuesForEntity) {
        super.handleAttributeValuesForIds(metaClass, currentIds, attributeValuesForEntity);

        if (CollectionUtils.isNotEmpty(currentIds)) {
            for (CategoryAttributeValue attributeValue : loadAttributeValuesBySql(metaClass, currentIds)) {
                attributeValuesForEntity.put(attributeValue.getObjectEntityId(), attributeValue);
            }
        }
    }

    protected List<CategoryAttributeValue> loadAttributeValuesBySql(MetaClass metaClass, List<Object> entityIds) {
        List<CategoryAttributeValue> attributeValues = new ArrayList<>();
        Collection<CategoryAttribute> categoryAttributes = getAttributesForMetaClass(metaClass);

        try (Transaction tx = persistence.getTransaction()) {
            EntityManager em = persistence.getEntityManager();
            DbTypeConverter dbTypeConverter = persistence.getDbTypeConverter();

            for (CategoryAttribute categoryAttribute : categoryAttributes) {
                ExtCategoryAttributeConfiguration configuration = (ExtCategoryAttributeConfiguration) categoryAttribute.getConfiguration();
                if (configuration != null && !Strings.isNullOrEmpty(configuration.getRawSqlSelect())) {
                    String selectQuery = configuration.getRawSqlSelect().replaceAll("#ids", entityIds.stream()
                            .map(id -> "'" + dbTypeConverter.getSqlObject(id).toString() + "'")
                            .collect(Collectors.joining(",")));
                    Query query = em.createNativeQuery(selectQuery);
                    List list = query.getResultList();
                    for (Object o : list) {
                        Object[] row = (Object[]) o;
                        Object id = row[0];
                        if (entityIds.get(0) instanceof UUID && id instanceof String) {
                            id = UuidProvider.fromString((String) id);
                        }
                        Object value = row[1];

                        CategoryAttributeValue categoryAttributeValue = metadata.create(CategoryAttributeValue.class);
                        categoryAttributeValue.setValue(value);
                        categoryAttributeValue.setObjectEntityId(id);
                        categoryAttributeValue.setCode(categoryAttribute.getCode());
                        categoryAttributeValue.setCategoryAttribute(categoryAttribute);
                        attributeValues.add(categoryAttributeValue);
                    }
                }
            }

            tx.commit();
        }
        return attributeValues;
    }

}
