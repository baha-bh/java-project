package com.normocontrol.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Rule {
    private UUID id;
    private String name;
    private String code;
    private String description;
    private RuleSeverity severity;
    private String category;
    private Boolean isActive;
    private OffsetDateTime createdAt;

    public Rule() {}

    public Rule(UUID id, String name, String code, String description, RuleSeverity severity, String category, Boolean isActive, OffsetDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.description = description;
        this.severity = severity;
        this.category = category;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    public static RuleBuilder builder() {
        return new RuleBuilder();
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public RuleSeverity getSeverity() { return severity; }
    public String getCategory() { return category; }
    public Boolean getIsActive() { return isActive; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(UUID id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
    public void setSeverity(RuleSeverity severity) { this.severity = severity; }
    public void setCategory(String category) { this.category = category; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    public static class RuleBuilder {
        private UUID id;
        private String name;
        private String code;
        private String description;
        private RuleSeverity severity;
        private String category;
        private Boolean isActive;
        private OffsetDateTime createdAt;

        public RuleBuilder id(UUID id) { this.id = id; return this; }
        public RuleBuilder name(String name) { this.name = name; return this; }
        public RuleBuilder code(String code) { this.code = code; return this; }
        public RuleBuilder description(String description) { this.description = description; return this; }
        public RuleBuilder severity(RuleSeverity severity) { this.severity = severity; return this; }
        public RuleBuilder category(String category) { this.category = category; return this; }
        public RuleBuilder isActive(Boolean isActive) { this.isActive = isActive; return this; }
        public RuleBuilder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Rule build() {
            return new Rule(id, name, code, description, severity, category, isActive, createdAt);
        }
    }
}
