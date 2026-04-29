package org.lnu.teaching.web.application.design.deanery.util;

import graphql.schema.DataFetchingFieldSelectionSet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.lnu.teaching.web.application.design.deanery.constants.ModelConstants.ID;

public class FieldSelectionUtil {
    public static List<String> ID_FIELD_ONLY = List.of(ID);

    public static List<String> getSelectedDbFields(List<String> selectableDbFields, DataFetchingFieldSelectionSet fs) {
        List<String> dbFields = new ArrayList<>();
        dbFields.add(ID);

        selectableDbFields.forEach(declaredField -> {
            if (fs.contains(declaredField)) {
                dbFields.add(declaredField);
            }
        });

        return dbFields;
    }

    public static Set<String> getSelectedDbFieldSet(List<String> selectableDbFields, DataFetchingFieldSelectionSet fs) {
        Set<String> dbFields = new HashSet<>();
        dbFields.add(ID);

        selectableDbFields.forEach(declaredField -> {
            if (fs.contains(declaredField)) {
                dbFields.add(declaredField);
            }
        });

        return dbFields;
    }
}
