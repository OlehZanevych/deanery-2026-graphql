package org.lnu.teaching.web.application.design.deanery.controller.mutation.department;

import graphql.schema.DataFetchingFieldSelectionSet;
import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.department.Department;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentCreateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentDeleteErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.department.error.status.DepartmentUpdateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.service.department.DepartmentService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@AllArgsConstructor
@SchemaMapping(typeName="DepartmentMutations")
public class DepartmentMutationsController {
    private final DepartmentService departmentService;

    @SchemaMapping
    public Mono<CreateMutationResponse<Department, DepartmentCreateErrorStatus>> createDepartment(@Argument Department department, DataFetchingFieldSelectionSet fs) {
        return departmentService.create(department, fs);
    }

    @SchemaMapping
    public Mono<MutationResponse<DepartmentUpdateErrorStatus>> updateDepartment(@Argument Long id, @Argument Department department) {
        return departmentService.update(id, department);
    }

    @SchemaMapping
    public Mono<MutationResponse<DepartmentDeleteErrorStatus>> deleteDepartment(@Argument Long id) {
        return departmentService.delete(id);
    }
}
