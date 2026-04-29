# План поэтапной разработки "Нормоконтроль"

## Текущее состояние проекта
- ✅ Spring Boot 3.2.0
- ✅ Java 17
- ✅ Spring Web, Spring Data JPA
- ✅ PostgreSQL
- ✅ Базовая структура (API, Domain, Infrastructure слои)

---

## Этап 1: Foundation (Базовая инфраструктура)

### 1.1 Настройка окружения разработки
- [ ] Добавить Lombok
- [ ] Добавить MapStruct для маппинга DTO
- [ ] Настроить application.properties для PostgreSQL
- [ ] Добавить Docker Compose (App + Postgres + Redis + Kafka)
- [ ] Создать docker-compose.yaml

### 1.2 Структура проекта Clean Architecture
- [ ] Создать пакеты: dto, mapper, model, port, service, config, git, repository
- [ ] Настроить базовые исключения (BusinessException, NotFoundException)
- [ ] Создать базовый Response DTO для API ответов

---

## Этап 2: Domain Layer (Доменный слой)

### 2.1 Сущности (Entities)
- [ ] Создать сущность Project (проект для проверки)
- [ ] Создать сущность CodeAnalysis (результат анализа)
- [ ] Создать сущность Rule (правило нормоконтроля)
- [ ] Создать сущность AuditLog (журнал аудита)

### 2.2 Порт-интерфейсы (Ports)
- [ ] Интерфейс ProjectRepository
- [ ] Интерфейс CodeAnalysisRepository
- [ ] Интерфейс RuleRepository
- [ ] Интерфейс AuditRepository

---

## Этап 3: Infrastructure Layer (Инфраструктурный слой)

### 3.1 Repository реализации
- [ ] Реализация ProjectRepository (JPA)
- [ ] Реализация CodeAnalysisRepository (JPA)
- [ ] Реализация RuleRepository (JPA)
- [ ] Реализация AuditRepository (JPA)

### 3.2 Конфигурация
- [ ] Настройка PostgreSQL (application.yml)
- [ ] Настройка Redis (CachingConfig)
- [ ] Настройка Kafka (KafkaConfig)
- [ ] Настройка Security (Spring Security)

---

## Этап 4: Security & Auth (Безопасность и авторизация)

### 4.1 Аутентификация
- [ ] Настроить Spring Security
- [ ] Создать JWT Token Provider
- [ ] Создать JwtAuthenticationFilter
- [ ] Реализовать Login endpoint (/api/auth/login)
- [ ] Реализовать Registration endpoint (/api/auth/register)

### 4.2 Авторизация (RBAC)
- [ ] Создать роли: ADMIN, LEAD, DEVELOPER, AUDITOR
- [ ] Настроить доступ к эндпоинтам на основе ролей
- [ ] Создать Permission аннотации

---

## Этап 5: API Layer (Слой API)

### 5.1 Project API
- [ ] POST /api/projects - создать проект
- [ ] GET /api/projects - список проектов
- [ ] GET /api/projects/{id} - детали проекта
- [ ] PUT /api/projects/{id} - обновить проект
- [ ] DELETE /api/projects/{id} - удалить проект

### 5.2 Code Analysis API
- [ ] POST /api/analysis/upload - загрузить код на анализ
- [ ] GET /api/analysis/{id} - получить результат анализа
- [ ] GET /api/analysis/project/{projectId} - история анализов проекта

### 5.3 Rule API
- [ ] GET /api/rules - список правил
- [ ] POST /api/rules - создать правило
- [ ] PUT /api/rules/{id} - обновить правило

### 5.4 Audit API
- [ ] GET /api/audit - журнал аудита (для админа)

---

## Этап 6: Clean Architecture Verification (Проверка архитектуры)

### 6.1 Правила валидации
- [ ] Проверка зависимостей: Controller → Service → Repository
- [ ] Проверка отсутствия циклических зависимостей
- [ ] Проверка именования пакетов и классов
- [ ] Проверка использования DTO (не сущностей в API)

### 6.2 Анализатор кода
- [ ] Парсинг Java кода (JavaParser)
- [ ] Построение графа зависимостей классов
- [ ] Валидация по правилам Clean Architecture
- [ ] Генерация отчета о нарушениях

---

## Этап 7: Git Integration (Интеграция с Git)

### 7.1 Git клиент
- [ ] Настроить JGit для работы с Git
- [ ] Создать GitService
- [ ] Получить список веток проекта
- [ ] Получить коммиты из ветки

### 7.2 Webhook обработка
- [ ] Настроить Webhook для Push events
- [ ] Обработка Merge Request events
- [ ] Автоматический запуск анализа при Push

---

## Этап 8: Reports & Notifications (Отчеты и уведомления)

### 8.1 Генерация отчетов
- [ ] PDF отчет по результатам анализа
- [ ] JSON отчет для CI/CD
- [ ] Dashboard статистика

### 8.2 Уведомления
- [ ] Интеграция с Kafka для асинхронных уведомлений
- [ ] Email уведомления о завершении анализа
- [ ] WebSocket для real-time обновлений

---

## Этап 9: Testing & CI/CD (Тестирование и CI/CD)

### 9.1 Unit Tests
- [ ] Тесты для Service слоя
- [ ] Тесты для Controller слоя
- [ ] Тесты для Domain сущностей

### 9.2 Integration Tests
- [ ] Тесты с Testcontainers (PostgreSQL)
- [ ] Тесты с Kafka (Embedded)
- [ ] Security tests

### 9.3 CI/CD
- [ ] GitHub Actions workflow
- [ ] Maven build + test stage
- [ ] Docker build + push stage
- [ ] Deploy to Kubernetes

---

## Этап 10: Performance & Monitoring (Производительность и мониторинг)

### 10.1 Кэширование
- [ ] Redis кэширование для правил
- [ ] Кэширование результатов анализа

### 10.2 Мониторинг
- [ ] Spring Boot Actuator
- [ ] Prometheus метрики
- [ ] Логирование аудита

---

## Порядок коммитов (Conventional Commits)

| # | Коммит | Описание |
|---|--------|----------|
| 1 | feat: add lombok and mapstruct dependencies | Добавить Lombok и MapStruct |
| 2 | feat: add docker-compose for local dev | Docker Compose для разработки |
| 3 | feat: add application.yml configuration | Конфигурация БД и Redis |
| 4 | feat: create domain entities | Сущности: Project, CodeAnalysis, Rule, AuditLog |
| 5 | feat: create repository interfaces | Порт-интерфейсы для репозиториев |
| 6 | feat: implement jpa repositories | JPA реализации репозиториев |
| 7 | feat: add spring security configuration | Настройка Spring Security |
| 8 | feat: implement jwt authentication | JWT аутентификация |
| 9 | feat: add role-based access control | RBAC модель |
| 10 | feat: create project api endpoints | CRUD для проектов |
| 11 | feat: create analysis api endpoints | API для анализа кода |
| 12 | feat: create rules api endpoints | API для правил |
| 13 | feat: create audit api endpoints | API для аудита |
| 14 | feat: implement clean architecture validator | Валидатор Clean Architecture |
| 15 | feat: add java parser for code analysis | Парсинг Java кода |
| 16 | feat: implement git integration | Интеграция с Git (JGit) |
| 17 | feat: add webhook handlers | Обработка Webhook событий |
| 18 | feat: add report generation | Генерация отчетов |
| 19 | feat: add kafka for async notifications | Kafka интеграция |
| 20 | feat: add unit tests | Unit тесты |
| 21 | feat: add integration tests | Integration тесты |
| 22 | feat: add github actions ci/cd | CI/CD pipeline |
| 23 | feat: add redis caching | Кэширование в Redis |
| 24 | feat: add monitoring and metrics | Мониторинг и метрики |

---

## Рекомендации по работе

1. **Ветвление**: Использовать `feature/N` для каждого коммита из таблицы выше
2. **Тесты**: Каждый коммит с новой функцией должен иметь соответствующие тесты
3. **CI**: После каждого коммита запускается сборка и тесты
4. **Review**: Объединять в `dev` ветку после успешного review

Начнем с Этапа 1 (Foundation)?