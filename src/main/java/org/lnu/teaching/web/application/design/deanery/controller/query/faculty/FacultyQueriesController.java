package org.lnu.teaching.web.application.design.deanery.controller.query.faculty;

import graphql.GraphQLContext;
import graphql.schema.DataFetchingFieldSelectionSet;
import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.common.response.Connection;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.service.faculty.FacultyService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
@AllArgsConstructor
@SchemaMapping(typeName="FacultyQueries")
public class FacultyQueriesController {
    private final FacultyService facultyService;

    @SchemaMapping
    public Mono<Connection<Faculty>> facultyConnection(@Argument int limit, @Argument long offset,
                                                       DataFetchingFieldSelectionSet fs, GraphQLContext context) {

        return facultyService.getConnection(fs, limit, offset, context);
    }

    @SchemaMapping
    public Mono<Faculty> faculty(@Argument Long id, DataFetchingFieldSelectionSet fs, GraphQLContext context) {
        return facultyService.findById(id, fs, context);
    }
}
