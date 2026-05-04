package com.normocontrol.domain.model;

public class Violation {
    private Rule rule;
    private String message;
    private int lineNumber;
    private String filePath;
    private CheckResult checkResult;

    public Violation() {}

    public Violation(Rule rule, String message, int lineNumber, String filePath, CheckResult checkResult) {
        this.rule = rule;
        this.message = message;
        this.lineNumber = lineNumber;
        this.filePath = filePath;
        this.checkResult = checkResult;
    }

    public static ViolationBuilder builder() {
        return new ViolationBuilder();
    }

    // Getters
    public Rule getRule() { return rule; }
    public String getMessage() { return message; }
    public int getLineNumber() { return lineNumber; }
    public String getFilePath() { return filePath; }
    public CheckResult getCheckResult() { return checkResult; }

    // Setters
    public void setRule(Rule rule) { this.rule = rule; }
    public void setMessage(String message) { this.message = message; }
    public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setCheckResult(CheckResult checkResult) { this.checkResult = checkResult; }

    public static class ViolationBuilder {
        private Rule rule;
        private String message;
        private int lineNumber;
        private String filePath;
        private CheckResult checkResult;

        public ViolationBuilder rule(Rule rule) { this.rule = rule; return this; }
        public ViolationBuilder message(String message) { this.message = message; return this; }
        public ViolationBuilder lineNumber(int lineNumber) { this.lineNumber = lineNumber; return this; }
        public ViolationBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public ViolationBuilder checkResult(CheckResult checkResult) { this.checkResult = checkResult; return this; }
        public Violation build() {
            return new Violation(rule, message, lineNumber, filePath, checkResult);
        }
    }
}
