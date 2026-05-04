package com.normocontrol.domain.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class StaticCodeAnalyzer {

    public List<Violation> analyzeFile(File file, List<Rule> activeRules) {
        List<Violation> violations = new ArrayList<>();
        try {
            // Check line lengths
            java.nio.file.Path path = file.toPath();
            List<String> lines = java.nio.file.Files.readAllLines(path);
            activeRules.stream()
                    .filter(r -> "LINE_LENGTH".equals(r.getCode()))
                    .findFirst()
                    .ifPresent(rule -> {
                        for (int i = 0; i < lines.size(); i++) {
                            if (lines.get(i).length() > 120) {
                                violations.add(Violation.builder()
                                        .rule(rule)
                                        .filePath(file.getName())
                                        .lineNumber(i + 1)
                                        .message("Строка слишком длинная (" + lines.get(i).length() + " символов). Максимум 120.")
                                        .build());
                            }
                        }
                    });

            CompilationUnit cu = StaticJavaParser.parse(file);
            runRules(cu, violations, file.getName(), activeRules);
        } catch (FileNotFoundException e) {
            // Skip
        } catch (Exception e) {
            // Parse error
        }
        return violations;
    }

    public List<Violation> analyzeCode(String code, List<Rule> activeRules) {
        List<Violation> violations = new ArrayList<>();
        
        // Check line lengths
        activeRules.stream()
                .filter(r -> "LINE_LENGTH".equals(r.getCode()))
                .findFirst()
                .ifPresent(rule -> {
                    String[] lines = code.split("\n");
                    for (int i = 0; i < lines.length; i++) {
                        if (lines[i].length() > 120) {
                            violations.add(Violation.builder()
                                    .rule(rule)
                                    .filePath("uploaded_file.java")
                                    .lineNumber(i + 1)
                                    .message("Строка слишком длинная (" + lines[i].length() + " символов). Максимум 120.")
                                    .build());
                        }
                    }
                });

        try {
            CompilationUnit cu = StaticJavaParser.parse(code);
            runRules(cu, violations, "uploaded_file.java", activeRules);
        } catch (Exception e) {
            // Parse error
        }
        return violations;
    }

    private void runRules(CompilationUnit cu, List<Violation> violations, String fileName, List<Rule> activeRules) {
        for (Rule rule : activeRules) {
            log.debug("Checking rule: {} ({})", rule.getName(), rule.getCode());
            
            // Hardcoded rules
            if ("CLASS_NAMING".equals(rule.getCode())) {
                cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
                    String name = cls.getNameAsString();
                    if (!Character.isUpperCase(name.charAt(0))) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(cls.getBegin().map(p -> p.line).orElse(0))
                                .message("Название класса '" + name + "' должно начинаться с заглавной буквы.")
                                .build());
                    }
                });
            } else if ("METHOD_LENGTH".equals(rule.getCode())) {
                cu.findAll(MethodDeclaration.class).forEach(method -> {
                    int lineCount = method.getEnd().map(p -> p.line).orElse(0) - 
                                    method.getBegin().map(p -> p.line).orElse(0);
                    if (lineCount > 30) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(method.getBegin().map(p -> p.line).orElse(0))
                                .message("Метод '" + method.getNameAsString() + "' слишком длинный (" + lineCount + " строк).")
                                .build());
                    }
                });
            } else if ("METHOD_COMPLEXITY".equals(rule.getCode())) {
                cu.findAll(MethodDeclaration.class).forEach(method -> {
                    long statementCount = method.findAll(com.github.javaparser.ast.stmt.Statement.class).size();
                    if (statementCount > 10) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(method.getBegin().map(p -> p.line).orElse(0))
                                .message("Метод '" + method.getNameAsString() + "' слишком сложный (" + statementCount + " команд). Максимум 10.")
                                .build());
                    }
                });
            } else if ("VAR_SECOND_LETTER_CAPS".equals(rule.getCode())) {
                cu.findAll(com.github.javaparser.ast.body.VariableDeclarator.class).forEach(var -> {
                    String name = var.getNameAsString();
                    if (name.length() >= 2 && !Character.isUpperCase(name.charAt(1))) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(var.getBegin().map(p -> p.line).orElse(0))
                                .message("Переменная '" + name + "' должна иметь вторую заглавную букву (согласно вашему спец. правилу).")
                                .build());
                    }
                });
            } else if ("METHOD_NAMING_CAMEL".equals(rule.getCode())) {
                cu.findAll(MethodDeclaration.class).forEach(method -> {
                    String name = method.getNameAsString();
                    if (Character.isUpperCase(name.charAt(0))) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(method.getBegin().map(p -> p.line).orElse(0))
                                .message("Метод '" + name + "' должен начинаться со строчной буквы (camelCase).")
                                .build());
                    }
                });
            } else if ("TODO_COMMENT".equals(rule.getCode())) {
                cu.getAllComments().forEach(comment -> {
                    if (comment.getContent().toUpperCase().contains("TODO")) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(comment.getBegin().map(p -> p.line).orElse(0))
                                .message("Обнаружен незавершенный таск (TODO): " + comment.getContent().trim())
                                .build());
                    }
                });
            } else if ("ARCHITECTURE_LAYER_VIOLATION".equals(rule.getCode())) {
                cu.getImports().forEach(imp -> {
                    String importName = imp.getNameAsString();
                    String packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
                    
                    // Domain layer should not import infrastructure or web
                    if (packageName.contains(".domain") && 
                        (importName.contains(".infrastructure") || importName.contains(".web"))) {
                        violations.add(Violation.builder()
                                .rule(rule)
                                .filePath(fileName)
                                .lineNumber(imp.getBegin().map(p -> p.line).orElse(0))
                                .message("Нарушение Clean Architecture: Доменный слой не должен зависеть от инфраструктуры (" + importName + ").")
                                .build());
                    }
                });
            } else if ("ARCHITECTURE_CONTROLLER_REPOSITORY".equals(rule.getCode())) {
                String packageName = cu.getPackageDeclaration().map(pd -> pd.getNameAsString()).orElse("");
                if (packageName.contains(".web") || packageName.contains(".controller")) {
                    cu.getImports().forEach(imp -> {
                        String importName = imp.getNameAsString();
                        if (importName.contains(".repository") || importName.contains(".persistence")) {
                            violations.add(Violation.builder()
                                    .rule(rule)
                                    .filePath(fileName)
                                    .lineNumber(imp.getBegin().map(p -> p.line).orElse(0))
                                    .message("Нарушение Clean Architecture: Контроллер не должен напрямую зависеть от репозитория (" + importName + "). Используйте сервисы.")
                                    .build());
                        }
                    });
                }
            } 
            
            // AI-generated script logic (can be combined with hardcoded or standalone)
            if (rule.getScriptLogic() != null && !rule.getScriptLogic().isBlank()) {
                executeGroovyRule(cu, violations, fileName, rule);
            }
        }
    }

    private void executeGroovyRule(CompilationUnit cu, List<Violation> violations, String fileName, Rule rule) {
        log.info("Executing Groovy script for rule: {}", rule.getName());
        try {
            groovy.lang.Binding binding = new groovy.lang.Binding();
            binding.setVariable("cu", cu);
            groovy.lang.GroovyShell shell = new groovy.lang.GroovyShell(binding);
            
            Object result = shell.evaluate(rule.getScriptLogic());
            
            if (result instanceof List) {
                List<Map<String, Object>> scriptViolations = (List<Map<String, Object>>) result;
                log.info("Script for rule '{}' returned {} findings", rule.getName(), scriptViolations.size());
                for (Map<String, Object> sv : scriptViolations) {
                    violations.add(Violation.builder()
                            .rule(rule)
                            .filePath(fileName)
                            .lineNumber(sv.get("lineNumber") instanceof Integer ? (Integer) sv.get("lineNumber") : 0)
                            .message(sv.get("message") != null ? sv.get("message").toString() : "Нарушение правила AI")
                            .build());
                }
            } else {
                log.warn("Script for rule '{}' did not return a List. Result type: {}", rule.getName(), result != null ? result.getClass().getName() : "null");
            }
        } catch (Exception e) {
            log.error("Error executing Groovy script for rule '{}'", rule.getName(), e);
            violations.add(Violation.builder()
                    .rule(rule)
                    .filePath(fileName)
                    .lineNumber(0)
                    .message("Ошибка при выполнении AI-скрипта: " + e.getMessage())
                    .build());
        }
    }
}
