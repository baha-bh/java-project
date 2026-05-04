# Нормоконтроль (Normocontrol)

Автоматизированная система проверки технических артефактов (кода и документации) на соответствие установленным регламентам и стандартам Clean Architecture.

## 🚀 Возможности
- **Статический анализ Java**: Проверка структуры слоев и зависимостей.
- **Анализ документации**: Проверка `.docx` файлов на соответствие стандартам оформления (ГОСТ).
- **Интеграция с Git**: Автоматическая проверка при пуше в репозиторий.
- **Supabase Auth**: Интеграция с облачной аутентификацией и Row Level Security.
- **Аудит**: Полное журналирование всех проверок и действий пользователей.

## 🛠 Технологический стек
- **Backend**: Java 17, Spring Boot 3.3.5
- **Persistence**: PostgreSQL (Supabase), JPA, Hibernate
- **Security**: Spring Security, Supabase Auth (JWT)
- **Frontend**: React, Vite, TypeScript
- **Analysis**: JavaParser, Apache POI, JGit

## ⚙️ Настройка и запуск

### Требования
- JDK 17+
- Maven 3.8+
- Docker & Docker Compose (для локальной разработки)

### Переменные окружения
Для работы приложения необходимо настроить следующие переменные (или прописать в `application-local.properties`):

| Переменная | Описание |
| :--- | :--- |
| `SUPABASE_URL` | URL вашего проекта Supabase |
| `SUPABASE_KEY` | Анонимный ключ Supabase (Anon Key) |
| `SUPABASE_DB_PASSWORD` | Пароль от базы данных PostgreSQL в Supabase |

### Запуск через Docker
```bash
docker-compose up -d
```

### Сборка и запуск Backend
```bash
mvn clean install
mvn spring-boot:run
```

### Запуск Frontend
```bash
cd frontend
npm install
npm run dev
```

## 🏗 Архитектура
Проект следует принципам **Clean Architecture**:
- `domain`: Чистая бизнес-логика, сущности и порты (интерфейсы).
- `application`: Сервисы приложения (User Cases).
- `infrastructure`: Реализация репозиториев, внешние API, конфигурация Spring.

## 📝 Лицензия
MIT
