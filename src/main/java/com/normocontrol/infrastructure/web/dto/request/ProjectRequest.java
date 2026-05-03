package com.normocontrol.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Repository URL is required")
    private String repositoryUrl;

    private String branch;
}
