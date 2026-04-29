package org.lnu.teaching.web.application.design.deanery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends BaseException {
    public static final NotFoundException NOT_FOUND_EXCEPTION = new NotFoundException("NOT_FOUND");

    public NotFoundException(String message) {
        super(message);
    }
}
