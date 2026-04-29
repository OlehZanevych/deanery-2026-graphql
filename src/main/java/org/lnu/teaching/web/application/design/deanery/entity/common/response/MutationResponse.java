package org.lnu.teaching.web.application.design.deanery.entity.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MutationResponse<ErrorStatus> {
    private static MutationResponse SUCCESSFUL_MUTATION_RESPONSE = new MutationResponse<>(true, null);
    public static <ErrorStatus> MutationResponse<ErrorStatus> successfulMutationResponse() {
        return SUCCESSFUL_MUTATION_RESPONSE;
    }

    public static <ErrorStatus> MutationResponse<ErrorStatus> errorMutationResponse(ErrorStatus errorStatus) {
        return new MutationResponse<>(false, errorStatus);
    }

    private final boolean isSuccess;

    private final ErrorStatus errorStatus;

    public boolean getIsSuccess() {
        return isSuccess;
    }
}
