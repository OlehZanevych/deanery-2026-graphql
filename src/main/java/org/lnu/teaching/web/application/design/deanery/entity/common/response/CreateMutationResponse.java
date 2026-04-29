package org.lnu.teaching.web.application.design.deanery.entity.common.response;

import lombok.Getter;

@Getter
public class CreateMutationResponse<Entity, ErrorStatus> extends MutationResponse<ErrorStatus> {
    public static <Entity, ErrorStatus> CreateMutationResponse<Entity, ErrorStatus> successfulCreateMutationResponse(Entity data) {
        return new CreateMutationResponse<>(true, data, null);
    }

    public static <Entity, ErrorStatus> CreateMutationResponse<Entity, ErrorStatus> errorCreateMutationResponse(ErrorStatus errorStatus) {
        return new CreateMutationResponse<>(false, null, errorStatus);
    }

    private final Entity data;

    public CreateMutationResponse(boolean isSuccess, Entity data, ErrorStatus errorStatus) {
        super(isSuccess, errorStatus);
        this.data = data;
    }
}
