# Hotel Booking System

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://www.java.com/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.0-green.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2025.0.0-blue.svg)](https://spring.io/projects/spring-cloud)

## 📖 Описание

**Hotel Booking System** — это распределённая микросервисная система бронирования отелей, построенная на базе Spring Cloud с использованием паттерна Service Discovery и API Gateway.

### 🏗️ Архитектура

Проект использует **микросервисную архитектуру** со следующими компонентами:

| Сервис | Порт | Описание |
|--------|------|----------|
| **Eureka Server** | 8761 | Центральный сервис регистрации и обнаружения сервисов |
| **API Gateway** | 8080 | Единая точка входа с маршрутизацией и аутентификацией |
| **Booking Service** | 8081 | Сервис управления бронированиями и пользователями |
| **Hotel Management Service** | 8082 | Сервис управления отелями и номерами |

### 🎯 Ключевые технологии

- **Spring Boot 3.5.0** — основа каждого микросервиса
- **Spring Cloud 2025.0.0** — экосистема для распределённых систем
- **Spring Cloud Gateway** — маршрутизация запросов и фильтрация
- **Netflix Eureka** — service discovery (регистрация сервисов)
- **OpenFeign** — клиент для межсервисного общения
- **JWT (JSON Web Tokens)** — аутентификация и авторизация
- **Spring Security** — защита API
- **H2 Database** — встроенная в-память база данных
- **Spring Data JPA** — работа с данными
- **Lombok + MapStruct** — упрощение кода

## 📦 Структура проекта

```
hotel-booking-system/
├── pom.xml                          # Родительский POM (dependency management)
├── eureka-server/                   # Сервис регистрации сервисов
├── api-gateway/                     # Шлюз API с маршрутизацией
├── booking-service/                 # Сервис бронирований
└── hotel-management-service/        # Сервис управления отелями
```

## 🚀 Запуск проекта

### 1. Запустить Eureka Server

```bash
cd eureka-server
mvn spring-boot:run
```

Сервис будет доступен по адресу: `http://localhost:8761`

### 2. Запустить остальные сервисы

В новых терминалах запустите:

```bash
# API Gateway
cd api-gateway
mvn spring-boot:run

# Booking Service
cd booking-service
mvn spring-boot:run

# Hotel Management Service
cd hotel-management-service
mvn spring-boot:run
```

### 3. Проверка работоспособности

После запуска всех сервисов проверьте их регистрацию в Eureka:

```bash
curl http://localhost:8761/eureka/apps
```

## 🔌 API Endpoints

### Через API Gateway (порт 8080)

#### Authentication & Authorization

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/auth/login` | Авторизация пользователя |
| POST | `/api/auth/register` | Регистрация нового пользователя |
| GET | `/api/user/bookings` | Получение бронирований текущего пользователя |

#### Бронирования

| Метод | Endpoint | Описание |
|-------|----------|----------|
| POST | `/api/bookings/create` | Создание новой брони |

#### Отели и Номера

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/hotels` | Список всех отелей |
| POST | `/api/hotels` | Создание нового отеля |
| GET | `/api/rooms` | Список всех номеров |
| POST | `/api/rooms` | Создание новой комнаты |

### Прямой доступ к сервисам

```bash
# Booking Service
curl http://localhost:8081/api/bookings/create

# Hotel Management Service
curl http://localhost:8082/api/hotels
```

## 🔐 Аутентификация

Система использует **JWT (JSON Web Tokens)** для аутентификации:

- При успешном логине возвращается JWT токен
- Токен должен быть включён в заголовок `Authorization: Bearer <token>` для последующих запросов
- Срок действия токена: 1 час (3600 секунд)

**Пример авторизации:**

```bash
# Логин
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@example.com", "password": "password123"}'

# Получение токена и использование его в других запросах
curl http://localhost:8080/api/user/bookings \
  -H "Authorization: Bearer <your_jwt_token>"
```

## 📝 Конфигурация

Основные настройки находятся в файлах `application.yml` каждого сервиса:

- **Eureka Server**: порт 8761, режим сервера
- **API Gateway**: порт 8080, реактивная маршрутизация
- **Booking Service**: порт 8081, H2 in-memory DB
- **Hotel Management Service**: порт 8082, H2 in-memory DB

### Переменные окружения

```bash
# JWT Secret (должен быть уникальным в продакшене)
export JWT_SECRET=your_secure_secret_key_here

# Запуск с переменными окружения
mvn spring-boot:run -Djwt.secret=$JWT_SECRET
```

## 🔄 Inter-Service Communication

Сервисы общаются через **OpenFeign Client**:

- **Booking Service** → **Hotel Management Service**
  - Клиент: `HotelManagementServiceClient`
  - Настройка таймаутов и повторных попыток в `feign.client.config`

## 📊 Health Checks

Каждый сервис имеет эндпоинты для проверки здоровья:

```bash
# Eureka Server
curl http://localhost:8761/actuator/health

# API Gateway
curl http://localhost:8080/actuator/gateway/health

# Booking Service
curl http://localhost:8081/actuator/health

# Hotel Management Service
curl http://localhost:8082/actuator/health
```
