package com.approvalflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request body for creating a new approval request")
public class CreateRequestDTO {

    @NotBlank(message = "Description must not be blank")
    @Schema(
        description = "What the employee is requesting"
        
    )
    private String description;
}