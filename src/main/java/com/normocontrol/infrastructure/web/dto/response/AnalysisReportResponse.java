package com.normocontrol.infrastructure.web.dto.response;

import java.util.List;

public class AnalysisReportResponse {
    private String fileName;
    private int score;
    private List<CheckDetailDto> details;
    private List<ViolationDto> violations;

    public AnalysisReportResponse() {}

    public AnalysisReportResponse(String fileName, int score, List<CheckDetailDto> details, List<ViolationDto> violations) {
        this.fileName = fileName;
        this.score = score;
        this.details = details;
        this.violations = violations;
    }

    // Getters and Setters
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public List<CheckDetailDto> getDetails() { return details; }
    public void setDetails(List<CheckDetailDto> details) { this.details = details; }
    public List<ViolationDto> getViolations() { return violations; }
    public void setViolations(List<ViolationDto> violations) { this.violations = violations; }

    public static class CheckDetailDto {
        private String criteria;
        private boolean passed;
        private String message;
        private String foundValue;

        public CheckDetailDto() {}

        public CheckDetailDto(String criteria, boolean passed, String message, String foundValue) {
            this.criteria = criteria;
            this.passed = passed;
            this.message = message;
            this.foundValue = foundValue;
        }

        // Getters and Setters
        public String getCriteria() { return criteria; }
        public void setCriteria(String criteria) { this.criteria = criteria; }
        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public String getFoundValue() { return foundValue; }
        public void setFoundValue(String foundValue) { this.foundValue = foundValue; }
    }

    public static class ViolationDto {
        private String ruleName;
        private String message;
        private int lineNumber;
        private String severity;

        public ViolationDto() {}

        public ViolationDto(String ruleName, String message, int lineNumber, String severity) {
            this.ruleName = ruleName;
            this.message = message;
            this.lineNumber = lineNumber;
            this.severity = severity;
        }

        // Getters and Setters
        public String getRuleName() { return ruleName; }
        public void setRuleName(String ruleName) { this.ruleName = ruleName; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getLineNumber() { return lineNumber; }
        public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
        public String getSeverity() { return severity; }
        public void setSeverity(String severity) { this.severity = severity; }
    }
}
