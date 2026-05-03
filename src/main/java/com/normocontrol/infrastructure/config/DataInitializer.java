package com.normocontrol.infrastructure.config;

import com.normocontrol.domain.model.Rule;
import com.normocontrol.domain.model.RuleSeverity;
import com.normocontrol.domain.port.RuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RuleRepository ruleRepository;

    @Override
    public void run(String... args) {
        if (ruleRepository.findAll().isEmpty()) {
            ruleRepository.save(Rule.builder()
                    .name("Именование классов")
                    .code("CLASS_NAMING")
                    .description("Классы должны начинаться с заглавной буквы (CamelCase).")
                    .severity(RuleSeverity.MEDIUM)
                    .isActive(true)
                    .build());

            ruleRepository.save(Rule.builder()
                    .name("Длина методов")
                    .code("METHOD_LENGTH")
                    .description("Методы не должны превышать 30 строк кода.")
                    .severity(RuleSeverity.MEDIUM)
                    .isActive(true)
                    .build());
        }
    }
}
