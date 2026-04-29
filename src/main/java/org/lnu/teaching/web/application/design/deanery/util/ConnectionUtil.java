package org.lnu.teaching.web.application.design.deanery.util;

import org.lnu.teaching.web.application.design.deanery.entity.common.response.Connection;

import java.util.List;

public class ConnectionUtil {
    public static <T> Connection<T> createConnectionResponse(List<T> nodes) {
        return new Connection<>(nodes);
    }
}
