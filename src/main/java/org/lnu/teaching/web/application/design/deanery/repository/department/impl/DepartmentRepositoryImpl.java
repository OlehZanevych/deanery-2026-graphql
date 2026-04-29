package org.lnu.teaching.web.application.design.deanery.repository.department.impl;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.common.column.ColumnValue;
import org.lnu.teaching.web.application.design.deanery.entity.department.Department;
import org.lnu.teaching.web.application.design.deanery.entity.department.field.selection.DepartmentFieldSelection;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.repository.department.DepartmentRepository;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;

import static java.util.stream.Collectors.joining;
import static org.lnu.teaching.web.application.design.deanery.constants.ModelConstants.ID;
import static org.lnu.teaching.web.application.design.deanery.util.EntityUtil.assignColumnValues;
import static org.lnu.teaching.web.application.design.deanery.util.EntityUtil.formatColumnSelection;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.empty;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@AllArgsConstructor
public class DepartmentRepositoryImpl implements DepartmentRepository {

    public static final BiFunction<Row, RowMetadata, Department> DEPARTMENT_MAPPER = (row, rowMetaData) -> {
        List<ColumnValue> departmentColumnValues = new ArrayList<>();
        List<ColumnValue> facultyColumnValues = new ArrayList<>();

        rowMetaData.getColumnMetadatas().forEach(columnMetadata -> {
            String column = columnMetadata.getName();
            Object value = row.get(column);

            // Remove table alias prefix, which consists of 2 letters
            String entityColumnName = column.substring(2);
            ColumnValue columnValue = new ColumnValue(entityColumnName, value);

            if (column.startsWith("f_")) {
                facultyColumnValues.add(columnValue);
            } else {
                departmentColumnValues.add(columnValue);
            }
        });


        Department department = new Department();
        departmentColumnValues.forEach(columnValue -> {
            assignColumnValues(department, departmentColumnValues);
        });

        if (!facultyColumnValues.isEmpty()) {
            Faculty faculty = new Faculty();
            assignColumnValues(faculty, facultyColumnValues);

            department.setFaculty(faculty);
        }

        return department;
    };

    private DatabaseClient databaseClient;

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Department> create(Department department) {
        return r2dbcEntityTemplate.insert(department);
    }

    @Override
    public Flux<Department> findAll(DepartmentFieldSelection fieldSelection, int limit, long offset) {
        String baseQuery = createDepartmentSelectStatement(fieldSelection);

        StringBuilder sb = new StringBuilder(baseQuery);
        sb.append(" ORDER BY d.name");
        sb.append(" LIMIT " + limit);

        if (offset != 0) {
            sb.append(" OFFSET " + offset);
        }

        String query = sb.toString();

        return databaseClient.sql(query).map(DEPARTMENT_MAPPER).all();
    }

    @Override
    public Mono<Department> findById(Long id, DepartmentFieldSelection fieldSelection) {
        String baseQuery = createDepartmentSelectStatement(fieldSelection);
        String query = baseQuery + " WHERE d.id = :id";

        return databaseClient.sql(query).bind("id", id).map(DEPARTMENT_MAPPER)
                .one();
    }

    @Override
    public Mono<Long> count() {
        return r2dbcEntityTemplate.count(empty(), Department.class);
    }

    @Override
    public Mono<Boolean> update(Department department) {
        return r2dbcEntityTemplate.update(department)
                .map(updatedDepartment -> true)
                .onErrorReturn(TransientDataAccessResourceException.class, false);
    }

    @Override
    public Mono<Boolean> delete(Long id) {
        return r2dbcEntityTemplate.delete(Department.class)
                .matching(query(Criteria.where(ID).is(id))).all()
                .map(affectedRows -> affectedRows > 0);
    }

    @Override
    public Flux<Department> findByFacultyIds(Collection<Long> facultyIds, Collection<String> fields) {
        return r2dbcEntityTemplate.select(Department.class)
                .matching(query(where("facultyId").in(facultyIds))
                        .columns(fields)
                        .sort(by(asc("name")))).all();
    }

    private String createDepartmentSelectStatement(DepartmentFieldSelection fieldSelection) {
        List<String> fields = new ArrayList<>(fieldSelection.getRootFields().stream()
                .map(field -> formatColumnSelection(field, "d")).toList());

        List<String> facultyFields = fieldSelection.getFacultyFields();
        boolean isFacultyJoinNeeded = false;
        if (!CollectionUtils.isEmpty(facultyFields)) {
            if (facultyFields.size() == 1 && ID.equals(facultyFields.get(0))) {
                fields.add("d.faculty_id f_id");
            } else {
                isFacultyJoinNeeded = true;

                facultyFields.forEach(facultyField -> {
                    fields.add(formatColumnSelection(facultyField, "f"));
                });
            }
        }

        StringBuilder sb = new StringBuilder("SELECT ");
        sb.append(fields.stream().collect(joining(", ")));
        sb.append(" FROM " + Department.TABLE + " d");
        if (isFacultyJoinNeeded) {
            sb.append(" JOIN " + Faculty.TABLE + " f ON (f.id = d.faculty_id)");
        }

        return sb.toString();
    }
}
