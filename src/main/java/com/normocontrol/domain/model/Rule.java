package com.normocontrol.domain.model;

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
public class Rule {
    private UUID id;
    private String name;
    private String description;
    private RuleSeverity severity;
    private Boolean isActive;
    private OffsetDateTime createdAt;
}
