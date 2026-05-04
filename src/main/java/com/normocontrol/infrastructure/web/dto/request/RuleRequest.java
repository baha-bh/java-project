package com.normocontrol.infrastructure.web.dto.request;

import com.normocontrol.domain.model.RuleSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Severity is required")
    private RuleSeverity severity;

    private String code;

    private Boolean isActive;
}
