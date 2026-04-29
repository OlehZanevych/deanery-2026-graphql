package org.lnu.teaching.web.application.design.deanery.entity.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class Connection<Entity> {
    private final List<Entity> nodes;
    private final PageInfo pageInfo;

    public Connection(List<Entity> nodes) {
        this.nodes = nodes;

        pageInfo = null;
    }

    public Connection(PageInfo pageInfo) {
        this.pageInfo = pageInfo;

        nodes = null;
    }
}
