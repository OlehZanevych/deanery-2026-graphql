package org.lnu.teaching.web.application.design.deanery.controller.query;

import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.common.graphql.schema.GraphQlSchemaDefinition;
import org.lnu.teaching.web.application.design.deanery.entity.query.departments.DepartmentQueries;
import org.lnu.teaching.web.application.design.deanery.entity.query.faculties.FacultyQueries;
import org.lnu.teaching.web.application.design.deanery.service.graphgl.schema.GraphQlSchemaService;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@AllArgsConstructor
public class QueryController {

    private final GraphQlSchemaService graphQlSchemaService;

    @QueryMapping
    public GraphQlSchemaDefinition _service() {
        return graphQlSchemaService.getSchemaDefinition();
    }
    @QueryMapping
    public FacultyQueries faculties() {
        return new FacultyQueries();
    }
    @QueryMapping
    public DepartmentQueries departments()  {
        return new DepartmentQueries();
    }
}
