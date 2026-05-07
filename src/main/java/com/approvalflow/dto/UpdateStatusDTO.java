package com.approvalflow.dto;

import com.approvalflow.model.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request body for updating approval request status")
public class UpdateStatusDTO {

    @NotNull(message = "Status must not be null")
    @Schema(
        description = "New status — must be APPROVED or REJECTED",
        allowableValues = {"APPROVED", "REJECTED"}
    )
    private RequestStatus status;
}