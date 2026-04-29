package org.lnu.teaching.web.application.design.deanery.controller.mutation;

import org.lnu.teaching.web.application.design.deanery.entity.mutation.departments.DepartmentMutations;
import org.lnu.teaching.web.application.design.deanery.entity.mutation.faculties.FacultyMutations;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class MutationController {
    @MutationMapping
    public FacultyMutations faculties()  {
        return new FacultyMutations();
    }
    @MutationMapping
    public DepartmentMutations departments()  {
        return new DepartmentMutations();
    }
}
