# Media Portal CMS

> CMS-система для медиа-портала с поддержкой статей, видео и подкастов.
> Проект выполнен в рамках курсов "Java" и "Нереляционные базы данных" МФТИ.

## 📋 Содержание

- [Технологии](#технологии)
- [Функциональность](#функциональность)
- [Быстрый старт](#быстрый-старт)
- [API документация](#api-документация)
- [Redis возможности](#redis-возможности)
- [Структура проекта](#структура-проекта)
- [Тестирование](#тестирование)

## 🛠 Технологии

| Категория | Технология |
|-----------|------------|
| Backend   | Java 17, Spring Boot 3.2.0 |
| База данных | PostgreSQL 16 |
| NoSQL/Кэш | Redis 7 |
| Аутентификация | JWT (jjwt 0.12.3) |
| Документация | OpenAPI 3.0 / Swagger UI |
| Сборка | Maven |
| Контейнеризация | Docker, Docker Compose |

## ✨ Функциональность

### Базовый CRUD
- **Статьи**: создание, редактирование, удаление, поиск
- **Видео**: управление видеоконтентом с URL и длительностью
- **Подкасты**: серии с эпизодами

### Аутентификация
- Регистрация пользователей
- JWT-токены для авторизации
- Роли: USER, ADMIN, MODERATOR

### Redis интеграция (10 баллов по НБД)
- **INCR**: атомарные счётчики просмотров
- **ZSET**: рейтинги контента в реальном времени
- **SET**: уникальные посетители
- **TTL**: автоматическое истечение данных
- **@Cacheable**: Spring Cache с Redis

## 🚀 Быстрый старт

### Предварительные требования
- Java 17+
- Docker и Docker Compose
- Maven 3.8+

### Запуск

1. **Клонирование репозитория**
```bash
git clone <repository-url>
cd media-portal-cms
```

2. **Запуск инфраструктуры (PostgreSQL + Redis)**
```bash
docker-compose up -d
```

3. **Сборка и запуск приложения**
```bash
./mvnw spring-boot:run
```

Или через Maven:
```bash
mvn clean package
java -jar target/media-portal-cms-0.0.1-SNAPSHOT.jar
```

4. **Проверка работоспособности**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- Health: http://localhost:8080/actuator/health

## 📚 API документация

После запуска приложения доступна интерактивная документация:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

### Основные эндпоинты

| Метод | Эндпоинт | Описание | Авторизация |
|-------|----------|----------|-------------|
| POST | `/api/auth/register` | Регистрация | - |
| POST | `/api/auth/login` | Вход | - |
| GET | `/api/articles` | Список статей | - |
| POST | `/api/articles` | Создать статью | Bearer Token |
| GET | `/api/videos` | Список видео | - |
| GET | `/api/podcasts` | Список подкастов | - |
| GET | `/api/recommendations/all` | Топ контент | - |
| POST | `/api/analytics/view/{type}/{id}` | Отслеживание просмотра | - |
| GET | `/api/analytics/content/{type}/{id}` | Аналитика контента | - |

### Пример использования

```bash
# Регистрация
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","name":"Test User"}'

# Вход (получение токена)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# Создание статьи (с токеном)
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-jwt-token>" \
  -d '{"title":"My Article","content":"Content here","tags":["java","spring"]}'

# Отслеживание просмотра
curl -X POST http://localhost:8080/api/analytics/view/ARTICLE/1

# Получение топ контента
curl http://localhost:8080/api/recommendations/all
```

## 🔴 Redis возможности

### Демонстрация для курса "Нереляционные БД"

#### 1. INCR - Атомарные счётчики
```java
// AnalyticsService.java
redisTemplate.opsForValue().increment("views:total:article:1");
```

#### 2. ZSET - Сортированные множества (рейтинги)
```java
// Добавление в рейтинг
redisTemplate.opsForZSet().incrementScore("ranking:hourly:article", "1", 1);

// Получение топ-10
redisTemplate.opsForZSet().reverseRange("ranking:hourly:article", 0, 9);
```

#### 3. SET - Уникальные посетители
```java
// Добавление посетителя (возвращает 1 если новый, 0 если был)
redisTemplate.opsForSet().add("visitors:daily:article:1", visitorId);
```

#### 4. TTL - Время жизни ключей
```java
// Установка TTL
redisTemplate.expire("ranking:hourly:article", Duration.ofHours(1));
```

#### 5. @Cacheable - Spring Cache
```java
@Cacheable(value = "articles", key = "#id")
public ArticleResponse getById(Long id) { ... }

@CacheEvict(value = "articles", key = "#id")
public void delete(Long id) { ... }
```

### Конфигурация кэша (TTL)
| Кэш | TTL |
|-----|-----|
| articles | 1 час |
| videos | 1 час |
| podcasts | 1 час |
| topContent | 5 минут |
| userDetails | 30 минут |

## 📁 Структура проекта

```
media-portal-cms/
├── src/
│   ├── main/
│   │   ├── java/com/mediaportal/cms/
│   │   │   ├── config/           # Конфигурации (Redis, Security, OpenAPI)
│   │   │   ├── controller/       # REST контроллеры
│   │   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── exception/        # Обработка исключений
│   │   │   ├── model/            # JPA сущности
│   │   │   ├── repository/       # Spring Data репозитории
│   │   │   ├── security/         # JWT фильтры и провайдеры
│   │   │   └── service/          # Бизнес-логика
│   │   └── resources/
│   │       └── application.yml   # Конфигурация приложения
│   └── test/                     # Юнит и интеграционные тесты
├── docker-compose.yml            # PostgreSQL + Redis
├── Dockerfile                    # Образ приложения
├── pom.xml                       # Maven зависимости
└── README.md
```

## 🧪 Тестирование

```bash
# Запуск всех тестов
mvn test

# Запуск с отчётом о покрытии
mvn test jacoco:report

# Отчёт доступен в target/site/jacoco/index.html
```

### Покрытие тестами
- AnalyticsService: тесты INCR, ZSET, SET, TTL операций
- ArticleService: CRUD операции
- AnalyticsController: интеграционные тесты API

## 📄 Лицензия

MIT License

---

**Автор**: Студент МФТИ  
**Курсы**: Java, Нереляционные базы данных  
**Семестр**: 3
