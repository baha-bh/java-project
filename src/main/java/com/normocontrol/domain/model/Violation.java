package com.normocontrol.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Violation {
    private UUID id;
    private CheckResult checkResult;
    private Rule rule;
    private String filePath;
    private Integer lineNumber;
    private String message;
}
