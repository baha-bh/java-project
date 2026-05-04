package com.normocontrol.infrastructure.web.dto.response;

import com.normocontrol.domain.model.RuleSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleResponse {
    private UUID id;
    private String name;
    private String description;
    private RuleSeverity severity;
    private String code;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}
