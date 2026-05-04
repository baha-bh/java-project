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
public class CheckResult {
    private UUID id;
    private Project project;
    private CheckStatus status;
    private Integer score;
    private String targetPath;
    private String message;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
}
