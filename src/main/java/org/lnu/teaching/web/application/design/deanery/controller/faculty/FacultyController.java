package org.lnu.teaching.web.application.design.deanery.controller.faculty;

import graphql.GraphQLContext;
import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.department.Department;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.service.department.DepartmentService;
import org.lnu.teaching.web.application.design.deanery.service.faculty.FacultyService;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.graphql.data.method.annotation.BatchMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.lnu.teaching.web.application.design.deanery.constants.ApiConstants.FACULTIES_ROOT_URI;
import static org.lnu.teaching.web.application.design.deanery.constants.ApiConstants.FACULTY_LOGO_SUB_URI;

@Controller
@AllArgsConstructor
@SchemaMapping(typeName="Faculty")
@RequestMapping(FACULTIES_ROOT_URI)
public class FacultyController {
    private final FacultyService facultyService;
    private final DepartmentService departmentService;

    @BatchMapping
    public Mono<Map<Faculty, List<Department>>> departments(List<Faculty> faculties, GraphQLContext context) {
        return departmentService.findForFaculties(faculties, context);
    }

    @SchemaMapping
    public Mono<String> logoUri(Faculty faculty) {
        return facultyService.findLogoUri(faculty);
    }

    @SchemaMapping(typeName = "FacultyItem", field = "logoUri")
    public Mono<String> facultyItemLogoUri(Faculty faculty) {
        return facultyService.findLogoUri(faculty);
    }

    @ResponseBody
    @GetMapping(value = "{facultyId}/" + FACULTY_LOGO_SUB_URI, produces = {
        MediaType.IMAGE_PNG_VALUE,
        MediaType.IMAGE_JPEG_VALUE,
        MediaType.IMAGE_GIF_VALUE
    })
    public Flux<DataBuffer> findLogo(@PathVariable Long facultyId) {
        return facultyService.readFacultyLogo(facultyId);
    }
}
