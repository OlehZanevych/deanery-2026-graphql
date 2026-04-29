package org.lnu.teaching.web.application.design.deanery.repository.faculty.impl;

import lombok.AllArgsConstructor;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.Faculty;
import org.lnu.teaching.web.application.design.deanery.entity.faculty.field.selection.FacultyFieldSelection;
import org.lnu.teaching.web.application.design.deanery.repository.faculty.FacultyRepository;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.lnu.teaching.web.application.design.deanery.constants.ModelConstants.ID;
import static org.springframework.data.domain.Sort.Order.asc;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.empty;
import static org.springframework.data.relational.core.query.Query.query;

@Repository
@AllArgsConstructor
public class FacultyRepositoryImpl implements FacultyRepository {

    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Faculty> create(Faculty faculty) {
        return r2dbcEntityTemplate.insert(faculty);
    }

    @Override
    public Flux<Faculty> findAll(FacultyFieldSelection fieldSelection, int limit, long offset) {
        Query query = empty().columns(fieldSelection.getRootFields()).limit(limit)
                .sort(by(asc("name")));

        if (offset != 0) {
            query = query.offset(offset);
        }

        return r2dbcEntityTemplate.select(Faculty.class)
            .matching(query)
                .all();
    }

    @Override
    public Mono<Faculty> findById(Long id, FacultyFieldSelection fieldSelection) {
        return r2dbcEntityTemplate.select(Faculty.class)
            .matching(query(where(ID).is(id)).columns(fieldSelection.getRootFields())).one();
    }

    @Override
    public Mono<Long> count() {
        return r2dbcEntityTemplate.count(empty(), Faculty.class);
    }

    @Override
    public Mono<Boolean> update(Faculty faculty) {
        return r2dbcEntityTemplate.update(faculty)
                .map(updatedFaculty -> true)
                .onErrorReturn(TransientDataAccessResourceException.class, false);
    }

    @Override
    public Mono<Boolean> delete(Long id) {
        return r2dbcEntityTemplate.delete(Faculty.class)
                .matching(query(Criteria.where(ID).is(id))).all()
                .map(affectedRows -> affectedRows > 0);
    }
}
