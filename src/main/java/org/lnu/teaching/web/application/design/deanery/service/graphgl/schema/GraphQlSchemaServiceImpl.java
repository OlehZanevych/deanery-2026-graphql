package org.lnu.teaching.web.application.design.deanery.service.graphgl.schema;

import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import org.lnu.teaching.web.application.design.deanery.entity.common.graphql.schema.GraphQlSchemaDefinition;
import org.springframework.graphql.execution.GraphQlSource;
import org.springframework.stereotype.Service;

@Service
public class GraphQlSchemaServiceImpl implements GraphQlSchemaService {

    private final GraphQlSchemaDefinition schemaDefinition;

    public GraphQlSchemaServiceImpl(GraphQlSource graphQlSource) {
        GraphQLSchema graphQlSchema = graphQlSource.schema();
        SchemaPrinter schemaPrinter = new SchemaPrinter();
        String sdl = schemaPrinter.print(graphQlSchema);
        schemaDefinition = new GraphQlSchemaDefinition(sdl);
    }


    @Override
    public GraphQlSchemaDefinition getSchemaDefinition() {
        return schemaDefinition;
    }
}
