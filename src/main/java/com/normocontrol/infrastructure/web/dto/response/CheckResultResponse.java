package com.normocontrol.infrastructure.web.dto.response;

import com.normocontrol.domain.model.CheckStatus;
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
public class CheckResultResponse {
    private UUID id;
    private UUID projectId;
    private CheckStatus status;
    private Integer score;
    private String targetPath;
    private String message;
    private OffsetDateTime startedAt;
    private OffsetDateTime completedAt;
}
