package org.lnu.teaching.web.application.design.deanery.entity.faculty;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.List;

@Data
@Table(Faculty.TABLE)
public class Faculty {

    public static final String TABLE = "faculties";

    public static final List<String> selectableDbFields = List.of(
        "name",
        "website",
        "email",
        "phone",
        "address",
        "info"
    );

    @Id
    private Long id;

    private String name;

    private String website;

    private String email;

    private String phone;

    private String address;

    private String info;
}
