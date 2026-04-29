package org.lnu.teaching.web.application.design.deanery.service.faculty.impl;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingFieldSelectionSet;
import graphql.schema.SelectedField;
import org.lnu.teaching.web.application.design.deanery.constants.GraphQlSchemaConstants;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.department.Department;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyCreateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyDeleteErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyUpdateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.field.selection.FacultyFieldSelection;
import org.lnu.teaching.web.application.design.deanery.repository.faculty.FacultyRepository;
import org.lnu.teaching.web.application.design.deanery.service.common.impl.CommonEntityServiceImpl;
import org.lnu.teaching.web.application.design.deanery.service.faculty.FacultyService;
import org.lnu.teaching.web.application.design.deanery.service.file.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

import static org.lnu.teaching.web.application.design.deanery.constants.ApiConstants.FACULTIES_ROOT_URI;
import static org.lnu.teaching.web.application.design.deanery.constants.ApiConstants.FACULTY_LOGO_SUB_URI;
import static org.lnu.teaching.web.application.design.deanery.constants.GraphQlContextConstants.DEPARTMENT_SELECTED_DB_FIELDS;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse.errorCreateMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse.errorMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse.successfulMutationResponse;
import static org.lnu.teaching.web.application.design.deanery.util.FieldSelectionUtil.getSelectedDbFieldSet;
import static org.lnu.teaching.web.application.design.deanery.util.FieldSelectionUtil.getSelectedDbFields;

@Service
public class FacultyServiceImpl extends CommonEntityServiceImpl<Faculty> implements FacultyService {

    private static final String FACULTY_LOGO_FOLDER = "faculties/logos/";

    private static final CreateMutationResponse<Faculty, FacultyCreateErrorStatus> DUPLICATED_NAME_CREATE_MUTATION_RESPONSE
            = errorCreateMutationResponse(FacultyCreateErrorStatus.DUPLICATED_NAME);

    private static final CreateMutationResponse<Faculty, FacultyCreateErrorStatus> INTERNAL_SERVER_ERROR_CREATE_MUTATION_RESPONSE
            = errorCreateMutationResponse(FacultyCreateErrorStatus.INTERNAL_SERVER_ERROR);

    private static final MutationResponse<FacultyUpdateErrorStatus> FACULTY_NOT_FOUND_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(FacultyUpdateErrorStatus.FACULTY_NOT_FOUND);
    private static final MutationResponse<FacultyUpdateErrorStatus> DUPLICATED_NAME_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(FacultyUpdateErrorStatus.DUPLICATED_NAME);
    private static final MutationResponse<FacultyUpdateErrorStatus> INTERNAL_SERVER_ERROR_UPDATE_MUTATION_RESPONSE
            = errorMutationResponse(FacultyUpdateErrorStatus.INTERNAL_SERVER_ERROR);

    private static final MutationResponse<FacultyDeleteErrorStatus> FACULTY_NOT_FOUND_DELETE_MUTATION_RESPONSE
            = errorMutationResponse(FacultyDeleteErrorStatus.FACULTY_NOT_FOUND);
    private static final MutationResponse<FacultyDeleteErrorStatus> INTERNAL_SERVER_ERROR_DELETE_MUTATION_RESPONSE
            = errorMutationResponse(FacultyDeleteErrorStatus.INTERNAL_SERVER_ERROR);

    private final FacultyRepository facultyRepository;

    private final FileStorageService fileStorageService;

    private final String serviceHost;

    public FacultyServiceImpl(FacultyRepository facultyRepository, FileStorageService fileStorageService,
                              @Value("${service.host}") String serviceHost) {

        this.facultyRepository = facultyRepository;
        this.fileStorageService = fileStorageService;
        this.serviceHost = serviceHost;
    }

    @Override
    public Mono<CreateMutationResponse<Faculty, FacultyCreateErrorStatus>> create(Faculty faculty) {
        return facultyRepository.create(faculty)
                .map(createdFaculty -> CreateMutationResponse.<Faculty, FacultyCreateErrorStatus>successfulCreateMutationResponse(createdFaculty))
                .onErrorResume(e -> {
                    CreateMutationResponse<Faculty, FacultyCreateErrorStatus> errorMutationResponse = INTERNAL_SERVER_ERROR_CREATE_MUTATION_RESPONSE;

                    if (e instanceof DuplicateKeyException
                            && "duplicate key value violates unique constraint \"faculties_name_key\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = DUPLICATED_NAME_CREATE_MUTATION_RESPONSE;
                    }

                    return Mono.just(errorMutationResponse);
                });
    }

    @Override
    protected Flux<Faculty> findAll(DataFetchingFieldSelectionSet fs, int limit, long offset) {
        FacultyFieldSelection fieldSelection = createFacultyFieldSelection(fs);
        return facultyRepository.findAll(fieldSelection, limit, offset);
    }

    @Override
    public Mono<Faculty> findById(Long id, DataFetchingFieldSelectionSet fs, GraphQLContext context) {
        FacultyFieldSelection fieldSelection = createFacultyFieldSelection(fs);

        saveDepartmentSelectedDbFieldsIntoContext(fs, context);

        return facultyRepository.findById(id, fieldSelection);
    }

    @Override
    protected Mono<Long> count() {
        return facultyRepository.count();
    }

    @Override
    public Mono<MutationResponse<FacultyUpdateErrorStatus>> update(Long id, Faculty faculty) {
        faculty.setId(id);

        return facultyRepository.update(faculty).map(isDeleted ->
                        (MutationResponse<FacultyUpdateErrorStatus>) (isDeleted ? successfulMutationResponse()
                                : FACULTY_NOT_FOUND_UPDATE_MUTATION_RESPONSE))
                .onErrorResume(e -> {
                    MutationResponse<FacultyUpdateErrorStatus> errorMutationResponse = INTERNAL_SERVER_ERROR_UPDATE_MUTATION_RESPONSE;

                    if (e instanceof DuplicateKeyException
                            && "duplicate key value violates unique constraint \"faculties_name_key\"".equals(e.getCause().getMessage())) {

                        errorMutationResponse = DUPLICATED_NAME_UPDATE_MUTATION_RESPONSE;
                    }

                    return Mono.just(errorMutationResponse);
                });
    }

    @Override
    public Mono<MutationResponse<FacultyDeleteErrorStatus>> delete(Long id) {
        return facultyRepository.delete(id).map(isDeleted ->
                        (MutationResponse<FacultyDeleteErrorStatus>) (isDeleted ? successfulMutationResponse()
                                : FACULTY_NOT_FOUND_DELETE_MUTATION_RESPONSE))
                .onErrorReturn(INTERNAL_SERVER_ERROR_DELETE_MUTATION_RESPONSE);
    }

    @Override
    protected void processNodesFieldSelection(DataFetchingFieldSelectionSet nodesFs, GraphQLContext context) {
        saveDepartmentSelectedDbFieldsIntoContext(nodesFs, context);
    }

    @Override
    public Mono<String> findLogoUri(Faculty faculty) {
        Long facultyId = faculty.getId();
        String logoFileName = getFacultyLogoFileName(facultyId);

        return fileStorageService.checkIfFileExists(logoFileName).flatMap(logoExists -> logoExists
                ? Mono.just(serviceHost + FACULTIES_ROOT_URI + "/" + faculty.getId() + "/" + FACULTY_LOGO_SUB_URI)
                : Mono.empty());
    }

    @Override
    public Flux<DataBuffer> readFacultyLogo(Long facultyId) {
        String facultyLogoFileName = getFacultyLogoFileName(facultyId);
        return fileStorageService.readFile(facultyLogoFileName);
    }

    private FacultyFieldSelection createFacultyFieldSelection(DataFetchingFieldSelectionSet fs) {
        List<String> rootFields = getSelectedDbFields(Faculty.selectableDbFields, fs);
        return new FacultyFieldSelection(rootFields);
    }

    private void saveDepartmentSelectedDbFieldsIntoContext(DataFetchingFieldSelectionSet fs, GraphQLContext context) {
        List<SelectedField> departmentsFieldSearchResult = fs.getFields(GraphQlSchemaConstants.DEPARTMENTS);
        if (departmentsFieldSearchResult.size() == 1) {
            Set<String> currentDepartmentSelectedDbFields = getSelectedDbFieldSet(Department.selectableDbFields,
                    departmentsFieldSearchResult.get(0).getSelectionSet());

            Set<String> departmentSelectedDbFields = context.get(DEPARTMENT_SELECTED_DB_FIELDS);
            if (departmentSelectedDbFields == null) {
                context.put(DEPARTMENT_SELECTED_DB_FIELDS, currentDepartmentSelectedDbFields);
            } else {
                departmentSelectedDbFields.addAll(currentDepartmentSelectedDbFields);
            }
        }
    }

    private String getFacultyLogoFileName(Long facultyId) {
        return FACULTY_LOGO_FOLDER + facultyId;
    }
}
