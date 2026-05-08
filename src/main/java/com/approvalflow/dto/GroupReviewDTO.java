package com.approvalflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body for a reviewer to submit their decision")
public class GroupReviewDTO {

    @NotBlank(message = "Decision must not be blank")
    @Schema(
        description = "Reviewer's decision",
        allowableValues = {"APPROVED", "REJECTED"}
    )
    private String decision; 
}