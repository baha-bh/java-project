package com.normocontrol.infrastructure.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .build();

    public String generateGroovyScript(String ruleDescription) {
        log.info("Generating Groovy script for rule description: {}", ruleDescription);
        
        String prompt = """
            You are a system architect for a project called "Normocontrol".
            Your task is to generate a Groovy script that validates Java code using the JavaParser library.
            
            Description of the rule: %s
            
            Requirements for the script:
            1. The script must be a valid Groovy script.
            2. It will have access to a variable 'cu' which is a com.github.javaparser.ast.CompilationUnit.
            3. It should return a List<Map<String, Object>> where each map represents a violation.
            4. Each violation map must contain:
               - 'message': String (description of the violation)
               - 'lineNumber': Integer
            5. Use JavaParser API (e.g., cu.findAll(com.github.javaparser.ast.body.ClassOrInterfaceDeclaration.class)).
            
            Example output format (ONLY the script, no markdown blocks):
            def violations = []
            cu.findAll(com.github.javaparser.ast.body.MethodDeclaration.class).each { method ->
                if (method.nameAsString.startsWith('test')) {
                    violations << [message: "Method name starts with test", lineNumber: method.range.get().begin.line]
                }
            }
            return violations
            
            Generate only the script code. No commentary. No markdown.
            """.formatted(ruleDescription);

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            String cleanKey = apiKey != null ? apiKey.trim() : "";
            if (cleanKey.isEmpty()) {
                throw new RuntimeException("Gemini API Key is empty!");
            }
            
            // Reverting to gemini-flash-latest as requested
            String absoluteUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + cleanKey;
            log.info("Requesting Gemini (flash-latest) via v1beta...");

            Map result = null;
            int maxRetries = 3;
            int retryCount = 0;
            
            while (retryCount < maxRetries) {
                try {
                    result = webClient.post()
                            .uri(absoluteUrl)
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                            .bodyValue(requestBody)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .block();
                    break; // Success
                } catch (org.springframework.web.reactive.function.client.WebClientResponseException.TooManyRequests e) {
                    retryCount++;
                    if (retryCount >= maxRetries) throw e;
                    log.warn("Rate limit hit (429), retrying in 5 seconds... (Attempt {}/{})", retryCount, maxRetries);
                    try { Thread.sleep(5000); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
                }
            }

            log.debug("Gemini response received.");

            if (result != null && result.containsKey("candidates")) {
                List candidates = (List) result.get("candidates");
                if (!candidates.isEmpty()) {
                    Map firstCandidate = (Map) candidates.get(0);
                    Map content = (Map) firstCandidate.get("content");
                    if (content != null && content.containsKey("parts")) {
                        List parts = (List) content.get("parts");
                        if (!parts.isEmpty()) {
                            String text = (String) ((Map) parts.get(0)).get("text");
                            if (text == null || text.isBlank()) {
                                throw new RuntimeException("Gemini returned empty text part.");
                            }
                            // Cleanup markdown if AI included it
                            String cleanScript = text.replaceAll("(?s)```groovy\\s*", "")
                                                   .replaceAll("(?s)```\\s*", "")
                                                   .trim();
                            
                            log.info("Successfully generated script (length: {})", cleanScript.length());
                            return cleanScript;
                        }
                    }
                }
            }
            log.error("Gemini response is empty or missing candidates. Full response: {}", result);
            throw new RuntimeException("AI returned an empty response or invalid structure.");
        } catch (Exception e) {
            log.error("Failed to generate script via AI: {}", e.getMessage());
            throw new RuntimeException("Failed to generate script via AI: " + e.getMessage());
        }
    }
}
