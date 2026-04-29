package org.lnu.teaching.web.application.design.deanery.service.department.impl;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.constants.GraphQlSchemaConstants;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.department.Department;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentCreateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentDeleteErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentUpdateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.department.field.selection.DepartmentFieldSelection;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.field.selection.FacultyFieldSelection;
import org.lnu.teaching.web.application.design.deanery.repository.department.DepartmentRepository;
import org.lnu.teaching.web.application.design.deanery.repository.faculty.FacultyRepository;
import org.lnu.teaching.web.application.design.deanery.service.common.impl.CommonEntityServiceImpl;
import org.lnu.teaching.web.application.design.deanery.service.department.DepartmentService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;
import static org.lnu.teaching.web.application.design.deanery.constants.GraphQlContextConstants.DEPARTMENT_SELECTED_DB_FIELDS;
import static org.lnu.teaching.web.application.design.deanery.constants.GraphQlSchemaConstants.FACULTY;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse.errorCreateMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse.errorMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse.successfulMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.util.FieldSelectionUtil.getSelectedDbFields;

@Service
@AllArgsConstructor
public class DepartmentServiceImpl extends CommonEntityServiceImpl<Department> implements DepartmentService {

    private static final String FACULTY_ID = "facultyId";

    private static final CreateMutationResponse<Department, DepartmentCreateErrorStatus> DUPLICATED_NAME_CREATE_MUTATION_RESPONSE
            = errorCreateMutationResponse(DepartmentCreateErrorStatus.DUPLICATED_NAME);
    private static final CreateMutationResponse<Department, DepartmentCreateErrorStatus> FACULTY_NOT_FOUND_CREATE_MUTATION_RESPONSE
            = errorCreateMutationResponse(DepartmentCreateErrorStatus.FACULTY_NOT_FOUND);
    private static final CreateMutationResponse<Department, DepartmentCreateErrorStatus> INTERNAL_SERVER_ERROR_CREATE_MUTATION_RESPONSE
            = errorCreateMutationResponse(DepartmentCreateErrorStatus.INTERNAL_SERVER_ERROR);

    private static final MutationResponse<DepartmentUpdateErrorStatus> DEPARTMENT_NOT_FOUND_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentUpdateErrorStatus.DEPARTMENT_NOT_FOUND);
    private static final MutationResponse<DepartmentUpdateErrorStatus> DUPLICATED_NAME_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentUpdateErrorStatus.DUPLICATED_NAME);
    private static final MutationResponse<DepartmentUpdateErrorStatus> FACULTY_NOT_FOUND_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentUpdateErrorStatus.FACULTY_NOT_FOUND);
    private static final MutationResponse<DepartmentUpdateErrorStatus> INTERNAL_SERVER_ERROR_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentUpdateErrorStatus.INTERNAL_SERVER_ERROR);

    private static final MutationResponse<DepartmentDeleteErrorStatus> DEPARTMENT_NOT_FOUND_DELETE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentDeleteErrorStatus.DEPARTMENT_NOT_FOUND);
    private static final MutationResponse<DepartmentDeleteErrorStatus> INTERNAL_SERVER_ERROR_DELETE_MUTATION_RESPONSE
            = errorMutationResponse(DepartmentDeleteErrorStatus.INTERNAL_SERVER_ERROR);

    private final DepartmentRepository departmentRepository;
    private final FacultyRepository facultyRepository;

    @Override
    public Mono<CreateMutationResponse<Department, DepartmentCreateErrorStatus>> create(Department department, DataFetchingFieldSelectionSet fs) {
        return departmentRepository.create(department)
                .flatMap(createdDepartment -> {
                            List<String> selectedFacultyDbFields = getFacultySelectedDbFieldsInResponseData(fs);
                            if (selectedFacultyDbFields != null) {
                                return facultyRepository.findById(createdDepartment.getFacultyId(), new FacultyFieldSelection(selectedFacultyDbFields))
                                        .map(faculty -> {
                                            createdDepartment.setFaculty(faculty);
                                            return CreateMutationResponse.<Department, DepartmentCreateErrorStatus>successfulCreateMutationResponse(createdDepartment);
                                        });
                            }

                            return Mono.just(CreateMutationResponse.<Department, DepartmentCreateErrorStatus>successfulCreateMutationResponse(createdDepartment));
                        }
                )
                .onErrorResume(e -> {
                    CreateMutationResponse<Department, DepartmentCreateErrorStatus> errorMutationResponse = INTERNAL_SERVER_ERROR_CREATE_MUTATION_RESPONSE;

                    if (e instanceof DuplicateKeyException
                            && "duplicate key value violates unique constraint \"departments_name_key\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = DUPLICATED_NAME_CREATE_MUTATION_RESPONSE;
                    } else if (e instanceof DataIntegrityViolationException &&
                            "insert or update on table \"departments\" violates foreign key constraint \"departments_faculty_id_fkey\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = FACULTY_NOT_FOUND_CREATE_MUTATION_RESPONSE;
                    }

                    return Mono.just(errorMutationResponse);
                });
    }

    @Override
    protected Flux<Department> findAll(DataFetchingFieldSelectionSet fs, int limit, long offset) {
        DepartmentFieldSelection fieldSelection = createDepartmentFieldSelection(fs);
        return departmentRepository.findAll(fieldSelection, limit, offset);
    }

    @Override
    public Mono<Department> findById(Long id, DataFetchingFieldSelectionSet fs, GraphQLContext context) {
        DepartmentFieldSelection fieldSelection = createDepartmentFieldSelection(fs);
        return departmentRepository.findById(id, fieldSelection);
    }

    @Override
    protected Mono<Long> count() {
        return departmentRepository.count();
    }

    @Override
    public Mono<MutationResponse<DepartmentUpdateErrorStatus>> update(Long id, Department department) {
        department.setId(id);

        return departmentRepository.update(department).map(isDeleted ->
                        (MutationResponse<DepartmentUpdateErrorStatus>) (isDeleted ? successfulMutationResponse()
                                : DEPARTMENT_NOT_FOUND_UPDATE_MUTATION_RESPONSE))
                .onErrorResume(e -> {
                    MutationResponse<DepartmentUpdateErrorStatus> errorMutationResponse = INTERNAL_SERVER_ERROR_UPDATE_MUTATION_RESPONSE;

                    if (e instanceof DuplicateKeyException
                            && "duplicate key value violates unique constraint \"departments_name_key\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = DUPLICATED_NAME_UPDATE_MUTATION_RESPONSE;
                    } else if (e instanceof DataIntegrityViolationException &&
                            "insert or update on table \"departments\" violates foreign key constraint \"departments_faculty_id_fkey\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = FACULTY_NOT_FOUND_UPDATE_MUTATION_RESPONSE;
                    }

                    return Mono.just(errorMutationResponse);
                });
    }

    @Override
    public Mono<MutationResponse<DepartmentDeleteErrorStatus>> delete(Long id) {
        return departmentRepository.delete(id).map(isDeleted ->
                        (MutationResponse<DepartmentDeleteErrorStatus>) (isDeleted ? successfulMutationResponse()
                                : DEPARTMENT_NOT_FOUND_DELETE_MUTATION_RESPONSE))
                .onErrorReturn(INTERNAL_SERVER_ERROR_DELETE_MUTATION_RESPONSE);
    }

    @Override
    protected void processNodesFieldSelection(DataFetchingFieldSelectionSet nodesFs, GraphQLContext context) {
    }

    @Override
    public Mono<Map<Faculty, List<Department>>> findForFaculties(List<Faculty> faculties, GraphQLContext context) {
        Set<Long> facultyIds = faculties.stream().map(Faculty::getId).collect(toSet());

        Set<String> selectedDbFields = context.get(DEPARTMENT_SELECTED_DB_FIELDS);
        selectedDbFields.add(FACULTY_ID);

        return departmentRepository.findByFacultyIds(facultyIds, selectedDbFields).collectList().map(departments -> {
            Map<Long, List<Department>> facultyIdDepartmentMap = departments.stream().collect(groupingBy(Department::getFacultyId));

            return faculties.stream().collect(Collectors.toMap(Function.identity(), faculty -> {
                List<Department> facultyDepartments = facultyIdDepartmentMap.get(faculty.getId());
                return facultyDepartments == null ? List.of() : facultyDepartments;
            }));
        });
    }

    private List<String> getFacultySelectedDbFieldsInResponseData(DataFetchingFieldSelectionSet fs) {
        List<SelectedField> dataFieldSearchResult = fs.getFields(GraphQlSchemaConstants.DATA);
        if (dataFieldSearchResult.size() == 1) {
            DataFetchingFieldSelectionSet dataFs = dataFieldSearchResult.get(0).getSelectionSet();
            List<SelectedField> facultyFieldSearchResult = dataFs.getFields(FACULTY);
            if (facultyFieldSearchResult.size() == 1) {
                return getSelectedDbFields(Faculty.selectableDbFields, facultyFieldSearchResult.get(0).getSelectionSet());
            }
        }

        return null;
    }

    private DepartmentFieldSelection createDepartmentFieldSelection(DataFetchingFieldSelectionSet fs) {
        List<String> rootFields = getSelectedDbFields(Department.selectableDbFields, fs);
        DepartmentFieldSelection fieldSelection = new DepartmentFieldSelection(rootFields);

        List<SelectedField> facultyFieldSearchResult = fs.getFields(FACULTY);
        if (facultyFieldSearchResult.size() == 1) {
            List<String> facultyFields = getSelectedDbFields(Faculty.selectableDbFields, facultyFieldSearchResult.get(0).getSelectionSet());
            fieldSelection.setFacultyFields(facultyFields);
        }

        return fieldSelection;
    }
}
