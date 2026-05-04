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
        // CODE RULES
        saveRuleIfMissing("Именование классов", "CLASS_NAMING", "Классы должны начинаться с заглавной буквы (CamelCase).", RuleSeverity.MEDIUM, "CODE");
        saveRuleIfMissing("Длина методов", "METHOD_LENGTH", "Методы не должны превышать 30 строк кода.", RuleSeverity.MEDIUM, "CODE");
        saveRuleIfMissing("Длина строки", "LINE_LENGTH", "Строка кода не должна превышать 120 символов.", RuleSeverity.LOW, "CODE");
        saveRuleIfMissing("Именование методов", "METHOD_NAMING_CAMEL", "Методы должны начинаться со строчной буквы (camelCase).", RuleSeverity.MEDIUM, "CODE");
        
        // DOCUMENTATION RULES (Academic/GOST)
        saveRuleIfMissing("Шрифт (Times New Roman)", "DOC_FONT_FAMILY", "В документах должен использоваться шрифт Times New Roman.", RuleSeverity.HIGH, "DOCUMENTATION");
        saveRuleIfMissing("Размер шрифта (14pt)", "DOC_FONT_SIZE", "Основной текст должен иметь размер 14пт.", RuleSeverity.HIGH, "DOCUMENTATION");
        saveRuleIfMissing("Заголовок H1", "DOC_H1_TITLE", "Документ должен начинаться с заголовка первого уровня (# Название).", RuleSeverity.MEDIUM, "DOCUMENTATION");
        saveRuleIfMissing("Запрещенные слова", "DOC_NO_FORBIDDEN_WORDS", "Документ не должен содержать пометки TODO, FIXME или ВНИМАНИЕ.", RuleSeverity.MEDIUM, "DOCUMENTATION");
        saveRuleIfMissing("Выравнивание текста", "DOC_ALIGNMENT", "Текст должен быть выровнен по ширине.", RuleSeverity.LOW, "DOCUMENTATION");
    }

    private void saveRuleIfMissing(String name, String code, String description, RuleSeverity severity, String category) {
        if (ruleRepository.findAll().stream().noneMatch(r -> r.getCode().equals(code))) {
            ruleRepository.save(Rule.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .severity(severity)
                    .category(category)
                    .isActive(true)
                    .build());
        }
    }
}
