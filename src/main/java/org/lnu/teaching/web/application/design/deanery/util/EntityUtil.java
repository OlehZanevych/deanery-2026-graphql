package org.lnu.teaching.web.application.design.deanery.util;

import com.google.common.base.CaseFormat;
import org.lnu.teaching.web.application.design.deanery.entity.common.column.ColumnValue;

import java.lang.reflect.Field;
import java.util.List;

public class EntityUtil {
    public static void assignColumnValues(Object target, List<ColumnValue> columnValues) {
        Class targetClass = target.getClass();

        columnValues.forEach(columnValue -> {
            try {
                String entityField = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, columnValue.getColumn());
                Field field = targetClass.getDeclaredField(entityField);
                field.setAccessible(true);

                field.set(target, columnValue.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static String formatColumnSelection(String fieldName, String tableAlias) {
        String columnName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, fieldName);
        return tableAlias + "." + columnName + " " + tableAlias + "_" + columnName;
    }
}
