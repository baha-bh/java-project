package com.normocontrol.domain.model;

import java.util.List;
import java.util.ArrayList;

public class AnalysisReport {
    private String fileName;
    private int score;
    private List<Violation> violations;
    private List<CheckDetail> details;
    private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();
    private Project project;

    public AnalysisReport() {}

    public AnalysisReport(String fileName, int score, List<Violation> violations, List<CheckDetail> details) {
        this.fileName = fileName;
        this.score = score;
        this.violations = violations;
        this.details = details;
    }

    public static AnalysisReportBuilder builder() {
        return new AnalysisReportBuilder();
    }

    // Getters
    public String getFileName() { return fileName; }
    public int getScore() { return score; }
    public List<Violation> getViolations() { return violations; }
    public List<CheckDetail> getDetails() { return details; }
    public java.time.OffsetDateTime getCreatedAt() { return createdAt; }
    public Project getProject() { return project; }

    // Setters
    public void setFileName(String fileName) { this.fileName = fileName; }
    public void setScore(int score) { this.score = score; }
    public void setViolations(List<Violation> violations) { this.violations = violations; }
    public void setDetails(List<CheckDetail> details) { this.details = details; }
    public void setCreatedAt(java.time.OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public void setProject(Project project) { this.project = project; }

    public static class AnalysisReportBuilder {
        private String fileName;
        private int score;
        private List<Violation> violations = new ArrayList<>();
        private List<CheckDetail> details = new ArrayList<>();
        private java.time.OffsetDateTime createdAt = java.time.OffsetDateTime.now();
        private Project project;

        public AnalysisReportBuilder fileName(String fileName) { this.fileName = fileName; return this; }
        public AnalysisReportBuilder score(int score) { this.score = score; return this; }
        public AnalysisReportBuilder violations(List<Violation> violations) { this.violations = violations; return this; }
        public AnalysisReportBuilder details(List<CheckDetail> details) { this.details = details; return this; }
        public AnalysisReportBuilder createdAt(java.time.OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public AnalysisReportBuilder project(Project project) { this.project = project; return this; }
        public AnalysisReport build() {
            AnalysisReport report = new AnalysisReport(fileName, score, violations, details);
            report.setCreatedAt(createdAt);
            report.setProject(project);
            return report;
        }
    }

    public static class CheckDetail {
        private String criteria;
        private boolean passed;
        private String message;
        private String foundValue;

        public CheckDetail() {}

        public CheckDetail(String criteria, boolean passed, String message, String foundValue) {
            this.criteria = criteria;
            this.passed = passed;
            this.message = message;
            this.foundValue = foundValue;
        }

        public static CheckDetailBuilder builder() {
            return new CheckDetailBuilder();
        }

        // Getters
        public String getCriteria() { return criteria; }
        public boolean isPassed() { return passed; }
        public String getMessage() { return message; }
        public String getFoundValue() { return foundValue; }

        public static class CheckDetailBuilder {
            private String criteria;
            private boolean passed;
            private String message;
            private String foundValue;

            public CheckDetailBuilder criteria(String criteria) { this.criteria = criteria; return this; }
            public CheckDetailBuilder passed(boolean passed) { this.passed = passed; return this; }
            public CheckDetailBuilder message(String message) { this.message = message; return this; }
            public CheckDetailBuilder foundValue(String foundValue) { this.foundValue = foundValue; return this; }
            public CheckDetail build() {
                return new CheckDetail(criteria, passed, message, foundValue);
            }
        }
    }
}
