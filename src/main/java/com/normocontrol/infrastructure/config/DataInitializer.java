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
        saveRuleIfMissing("Именование классов", "CLASS_NAMING", "Классы должны начинаться с заглавной буквы (CamelCase).", RuleSeverity.MEDIUM);
        saveRuleIfMissing("Длина методов", "METHOD_LENGTH", "Методы не должны превышать 30 строк кода.", RuleSeverity.MEDIUM);
        saveRuleIfMissing("Длина строки", "LINE_LENGTH", "Строка кода не должна превышать 120 символов.", RuleSeverity.LOW);
        saveRuleIfMissing("Количество команд в методе", "METHOD_COMPLEXITY", "Метод не должен содержать более 10 команд (statements).", RuleSeverity.MEDIUM);
        saveRuleIfMissing("Вторая буква заглавная", "VAR_SECOND_LETTER_CAPS", "Переменные должны иметь вторую букву заглавной (специальное правило).", RuleSeverity.LOW);
        saveRuleIfMissing("Именование методов", "METHOD_NAMING_CAMEL", "Методы должны начинаться со строчной буквы (camelCase).", RuleSeverity.MEDIUM);
        saveRuleIfMissing("Поиск TODO", "TODO_COMMENT", "Обнаружение незавершенных задач в комментариях.", RuleSeverity.LOW);
    }

    private void saveRuleIfMissing(String name, String code, String description, RuleSeverity severity) {
        if (ruleRepository.findAll().stream().noneMatch(r -> r.getCode().equals(code))) {
            ruleRepository.save(Rule.builder()
                    .name(name)
                    .code(code)
                    .description(description)
                    .severity(severity)
                    .isActive(true)
                    .build());
        }
    }
}
