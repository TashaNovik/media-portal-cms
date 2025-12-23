flowchart TB
    subgraph Client["🌐 Client Layer"]
        WEB[Web Browser]
        MOBILE[Mobile App]
        SWAGGER[Swagger UI]
    end

    subgraph Security["🔐 Security Layer"]
        JWT_FILTER[JwtAuthenticationFilter]
        JWT_PROVIDER[JwtTokenProvider]
        USER_DETAILS[CustomUserDetailsService]
    end

    subgraph Controllers["🎮 Controller Layer"]
        AUTH_CTRL[AuthController]
        ARTICLE_CTRL[ArticleController]
        VIDEO_CTRL[VideoController]
        PODCAST_CTRL[PodcastController]
        ANALYTICS_CTRL[AnalyticsController]
        RECOMMEND_CTRL[RecommendationController]
    end

    subgraph Services["⚙️ Service Layer"]
        AUTH_SVC[AuthService]
        ARTICLE_SVC[ArticleService]
        VIDEO_SVC[VideoService]
        PODCAST_SVC[PodcastService]
        ANALYTICS_SVC[AnalyticsService]
        RECOMMEND_SVC[RecommendationService]
    end

    subgraph Repositories["📦 Repository Layer"]
        USER_REPO[UserRepository]
        ARTICLE_REPO[ArticleRepository]
        VIDEO_REPO[VideoRepository]
        PODCAST_REPO[PodcastRepository]
        EPISODE_REPO[EpisodeRepository]
    end

    subgraph Storage["💾 Data Storage"]
        POSTGRES[(PostgreSQL)]
        REDIS[(Redis)]
    end

    %% Client connections
    WEB & MOBILE & SWAGGER --> JWT_FILTER

    %% Security flow
    JWT_FILTER --> JWT_PROVIDER
    JWT_FILTER --> USER_DETAILS
    JWT_FILTER --> Controllers

    %% Controller to Service connections
    AUTH_CTRL --> AUTH_SVC
    ARTICLE_CTRL --> ARTICLE_SVC
    ARTICLE_CTRL --> ANALYTICS_SVC
    VIDEO_CTRL --> VIDEO_SVC
    VIDEO_CTRL --> ANALYTICS_SVC
    PODCAST_CTRL --> PODCAST_SVC
    PODCAST_CTRL --> ANALYTICS_SVC
    ANALYTICS_CTRL --> ANALYTICS_SVC
    RECOMMEND_CTRL --> RECOMMEND_SVC

    %% Service dependencies
    AUTH_SVC --> USER_REPO
    AUTH_SVC --> JWT_PROVIDER
    ARTICLE_SVC --> ARTICLE_REPO
    ARTICLE_SVC --> USER_REPO
    VIDEO_SVC --> VIDEO_REPO
    VIDEO_SVC --> USER_REPO
    PODCAST_SVC --> PODCAST_REPO
    PODCAST_SVC --> EPISODE_REPO
    PODCAST_SVC --> USER_REPO
    
    RECOMMEND_SVC --> ARTICLE_SVC
    RECOMMEND_SVC --> VIDEO_SVC
    RECOMMEND_SVC --> PODCAST_SVC
    RECOMMEND_SVC --> REDIS

    ANALYTICS_SVC --> REDIS

    %% Repository to Storage
    USER_REPO & ARTICLE_REPO & VIDEO_REPO & PODCAST_REPO & EPISODE_REPO --> POSTGRES

    %% Redis operations
    ANALYTICS_SVC -.->|INCR, ZSET, SET| REDIS
    RECOMMEND_SVC -.->|ZREVRANGE| REDIS
    ARTICLE_SVC -.->|@Cacheable| REDIS
    VIDEO_SVC -.->|@Cacheable| REDIS
    PODCAST_SVC -.->|@Cacheable| REDIS

    %% Styling
    classDef client fill:#e1f5fe,stroke:#01579b
    classDef security fill:#fff3e0,stroke:#e65100
    classDef controller fill:#e8f5e9,stroke:#1b5e20
    classDef service fill:#f3e5f5,stroke:#4a148c
    classDef repo fill:#fce4ec,stroke:#880e4f
    classDef storage fill:#fff8e1,stroke:#ff6f00

    class WEB,MOBILE,SWAGGER client
    class JWT_FILTER,JWT_PROVIDER,USER_DETAILS security
    class AUTH_CTRL,ARTICLE_CTRL,VIDEO_CTRL,PODCAST_CTRL,ANALYTICS_CTRL,RECOMMEND_CTRL controller
    class AUTH_SVC,ARTICLE_SVC,VIDEO_SVC,PODCAST_SVC,ANALYTICS_SVC,RECOMMEND_SVC service
    class USER_REPO,ARTICLE_REPO,VIDEO_REPO,PODCAST_REPO,EPISODE_REPO repo
    class POSTGRES,REDIS storage# Media Portal CMS

> CMS-система для медиа-портала с поддержкой статей, видео и подкастов.
> Проект выполнен в рамках курсов "язык Java" и "Нереляционные базы данных" МФТИ.

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
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Health: http://localhost:8080/actuator/health

## 📚 API документация

После запуска приложения доступна интерактивная документация:
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

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
# Регистрация (требуются username, email, password)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","email":"user@example.com","password":"password123","firstName":"Test","lastName":"User"}'

# Вход (получение токена) - используется username
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'

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

### Авторизация в Swagger UI

1. Откройте http://localhost:8080/swagger-ui/index.html
2. Выполните `POST /api/auth/register` для регистрации
3. Выполните `POST /api/auth/login` с вашим username и password
4. Скопируйте значение `token` из ответа
5. Нажмите кнопку **Authorize** 🔓 в правом верхнем углу
6. Введите токен в поле (без слова "Bearer", только сам токен)
7. Нажмите **Authorize**, затем **Close**
8. Теперь все защищённые эндпоинты доступны (POST /api/videos, POST /api/podcasts и т.д.)

## 🎬 Демонстрация сценария использования

### Полный сценарий работы с API

#### Шаг 1: Запуск инфраструктуры
```bash
# Запуск PostgreSQL и Redis
docker-compose up -d

# Проверка контейнеров
docker ps
```

#### Шаг 2: Запуск приложения
```bash
mvn spring-boot:run
```

#### Шаг 3: Регистрация и авторизация
```bash
# Регистрация нового пользователя
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"demouser","email":"demo@test.com","password":"demo123","firstName":"Demo","lastName":"User"}'

# Вход и получение JWT токена
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demouser","password":"demo123"}'

# Ответ: {"token":"eyJhbGciOiJIUzI1NiJ9...","username":"demouser","email":"demo@test.com"}
```

#### Шаг 4: Создание контента (требуется токен)
```bash
# Создание статьи
curl -X POST http://localhost:8080/api/articles \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"title":"Spring Boot Guide","content":"Complete guide...","tags":["java","spring"]}'

# Создание видео
curl -X POST http://localhost:8080/api/videos \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <TOKEN>" \
  -d '{"title":"Redis Tutorial","videoUrl":"https://youtube.com/...","duration":1800}'
```

#### Шаг 5: Просмотр контента (публичный доступ)
```bash
# Получение всех статей
curl http://localhost:8080/api/articles

# Получение статьи по ID (увеличивает счётчик просмотров)
curl http://localhost:8080/api/articles/1
```

#### Шаг 6: Работа с Redis - аналитика
```bash
# Отслеживание просмотра (INCR + ZSET)
curl -X POST http://localhost:8080/api/analytics/view/ARTICLE/1
curl -X POST http://localhost:8080/api/analytics/view/ARTICLE/1
curl -X POST http://localhost:8080/api/analytics/view/ARTICLE/2

# Получение аналитики по контенту
curl http://localhost:8080/api/analytics/content/ARTICLE/1
# Ответ: {"contentType":"ARTICLE","contentId":1,"totalViews":2,"uniqueVisitorsToday":1,"currentRank":1}

# Получение топ контента (ZSET reverseRange)
curl http://localhost:8080/api/analytics/top/ARTICLE
# Ответ: {"contentType":"ARTICLE","topContentIds":[1,2],"count":2}
```

#### Шаг 7: Проверка данных в Redis напрямую
```bash
# Подключение к Redis CLI
docker exec -it media-portal-redis redis-cli

# Проверка счётчика просмотров (INCR)
GET views:total:article:1
# Ответ: "2"

# Проверка рейтинга (ZSET)
ZREVRANGE ranking:hourly:article 0 -1 WITHSCORES
# Ответ: 1) "1" 2) "2" 3) "2" 4) "1"

# Проверка уникальных посетителей (SET)
SMEMBERS visitors:daily:article:1

# Проверка TTL
TTL ranking:hourly:article
# Ответ: время в секундах до истечения

# Проверка кэша (@Cacheable)
KEYS articles::*
# Ответ: список закэшированных статей
```

#### Шаг 8: Рекомендации
```bash
# Получение топ контента всех типов
curl http://localhost:8080/api/recommendations/all
# Ответ: {"topArticles":[...],"topVideos":[...],"topPodcasts":[...]}
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
