package com.normocontrol.infrastructure.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViolationResponse {
    private UUID id;
    private UUID checkResultId;
    private RuleResponse rule;
    private String filePath;
    private Integer lineNumber;
    private String message;
}
