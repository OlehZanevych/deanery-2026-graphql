package org.lnu.teaching.web.application.design.deanery.controller.mutation.faculty;

import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.CreateMutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.MutationResponse;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyCreateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyDeleteErrorStatus;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.error.status.FacultyUpdateErrorStatus;
import org.lnu.teaching.web.application.design.deanery.service.faculty.FacultyService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@AllArgsConstructor
@SchemaMapping(typeName="FacultyMutations")
public class FacultyMutationsController {
    private final FacultyService facultyService;

    @SchemaMapping
    public Mono<CreateMutationResponse<Faculty, FacultyCreateErrorStatus>> createFaculty(@Argument Faculty faculty) {
        return facultyService.create(faculty);
    }

    @SchemaMapping
    public Mono<MutationResponse<FacultyUpdateErrorStatus>> updateFaculty(@Argument Long id, @Argument Faculty faculty) {
        return facultyService.update(id, faculty);
    }

    @SchemaMapping
    public Mono<MutationResponse<FacultyDeleteErrorStatus>> deleteFaculty(@Argument Long id) {
        return facultyService.delete(id);
    }
}
