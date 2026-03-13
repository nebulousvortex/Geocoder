# Geocoder

**Личный геокод на базе Open Street Maps**

---

## Возможности

- Добавление адресов с автоматическим геокодированием
- Обработка дубликатов: добавление адреса как псевдонима при совпадении координат
- Статусы геокодирования: «В процессе», «Готово», «Ошибка»
- Хранение псевдонимов
- Построение маршрутов на основе координат адресов
- Полностью контейнеризированное развертывание (Docker + PostgreSQL)

---

## Стек технологий

- **Backend**: Java 21, Spring Boot 3.3.5
- **Database**: PostgreSQL 16
- **Template Engine**: Thymeleaf
- **Geocoding**: geocode.maps.co
- **Routing**: project-osrm.org
- **Дополнительно**: Spring Data JPA, Async, Docker, Maven

---

## Требования

- Docker и Docker Compose
- `.env` файл (см. `.env.example`)
- Ключ API geocode.maps.co

---

## Установка и запуск

### 1. Клонирование и подготовка

```bash
git clone <repository-url>
cd geocoder
cp .env.example .env
# Заполните .env (GEOCODE_API_KEY и данные БД)
```

### 2. Запуск через Docker Compose

```bash
docker-compose up --build
```

### 3. Локальный запуск (для разработки)

```bash
mvn spring-boot:run
```
