package com.normocontrol.domain.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.Violation;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StaticCodeAnalyzer {

    public List<Violation> analyzeFile(File file, List<Rule> activeRules) {
        List<Violation> violations = new ArrayList<>();
        try {
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
            }
        }
    }
}
