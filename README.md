MedSystem — Hospital Management System
Система управления больницей на **Spring Boot + PostgreSQL + Thymeleaf**.
#Тестовые аккаунты
| Роль           | Логин           | Пароль      |
|----------------|-----------------|-------------|
| Администратор  | `admin`         | `admin123`  |
| Врач           | `doctor1`       | `doctor123` |
| Врач           | `doctor2`       | `doctor123` |
| Медсестра      | `nurse1`        | `nurse123`  |
| Регистратор    | `receptionist1` | `recept123` |
| Пациент        | `patient1`      | `patient123`|
| Пациент        | `patient2`      | `patient123`|

#Требования
- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- IntelliJ IDEA

#Настройка базы данных (pgAdmin)
1. Откройте pgAdmin
2. Нажмите ПКМ на Databases → Create → Database...
3. Введите имя: `hospital_db`, нажмите Save
4. Или выполните через Query Tool:
```sql
CREATE DATABASE hospital_db;
```
Таблицы создаются автоматически при первом запуске (`spring.jpa.hibernate.ddl-auto=update`).

#Запуск проекта
#Шаг 1 — Настройте подключение к БД

Откройте файл `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/hospital_db
spring.datasource.username=postgres      # ← ваш пользователь pgAdmin
spring.datasource.password=postgres      # ← ваш пароль pgAdmin
```
#Шаг 2 — Запуск через IntelliJ IDEA

1. Откройте папку `hospital` через File → Open
2. Подождите пока Maven загрузит зависимости
3. Найдите `HospitalApplication.java`
4. Нажмите зелёную кнопку ▶️ **Run**

#Шаг 3 — Откройте браузер

```
http://localhost:8080
```

Данные заполняются автоматически при первом запуске.

#Структура проекта

```
hospital/
├── src/main/java/com/hospital/
│   ├── HospitalApplication.java          # Точка входа
│   ├── config/
│   │   ├── SecurityConfig.java           # Spring Security
│   │   └── DataInitializer.java          # Тестовые данные
│   ├── model/                            # Сущности БД
│   │   ├── User.java
│   │   ├── Patient.java
│   │   ├── Doctor.java
│   │   ├── Department.java
│   │   ├── Ward.java
│   │   ├── Appointment.java
│   │   ├── MedicalRecord.java
│   │   ├── LabTest.java
│   │   ├── TestOrder.java
│   │   └── FavoriteTest.java
│   ├── repository/                       # Работа с БД (JPA)
│   ├── service/                          # Бизнес-логика
│   └── controller/                       # HTTP-контроллеры
│       ├── AuthController.java
│       ├── AdminController.java
│       ├── DoctorController.java
│       ├── PatientController.java
│       ├── ReceptionistController.java
│       ├── NurseController.java
│       ├── WardController.java
│       ├── AdminLabTestController.java
│       └── LabTestController.java
├── src/main/resources/
│   ├── application.properties            # Настройки
│   ├── static/css/style.css              # Стили
│   └── templates/                        # HTML страницы
│       ├── auth/login.html
│       ├── fragments.html                # Боковые панели
│       ├── admin/
│       ├── doctor/
│       ├── nurse/
│       ├── receptionist/
│       └── patient/
├── init.sql                              # SQL для pgAdmin
└── pom.xml                               # Зависимости Maven
```


#Функциональность по ролям

Администратор
- Дашборд с общей статистикой
- Управление пациентами (CRUD)
- Управление врачами (CRUD)
- Управление отделениями (CRUD)
- Управление палатами (CRUD)
- Управление каталогом анализов (CRUD)
- Просмотр и изменение статуса всех записей

Врач
- Дашборд со своими приёмами
- Завершение приёма с записью диагноза и рецепта
- Просмотр своих пациентов
- Медицинская карта пациента

Медсестра
- Просмотр расписания всех приёмов
- Список пациентов

Регистратор
- Запись пациентов на приём (оффлайн)
- Отмена записей
- Просмотр карточки пациента с историей визитов

Пациент
- Личный дашборд с предстоящими записями
- Запись к врачу онлайн / отмена записи
- Просмотр медицинской карты
- Каталог анализов с фильтрами по категориям
- Добавление анализов в **избранное** ⭐
- **Корзина** 🛒 с анализами
- **Моковая оплата** (карта / Kaspi / наличные)
- История заказов

#Стек технологий
| Технология        | Версия  | Назначение                |
|-------------------|---------|---------------------------|
| Spring Boot       | 3.2.0   | Backend фреймворк         |
| Spring Security   | 6.x     | Аутентификация и роли     |
| Spring Data JPA   | 3.x     | ORM, работа с БД          |
| Thymeleaf         | 3.x     | HTML-шаблоны              |
| PostgreSQL        | 14+     | База данных               |
| Lombok            | 1.18+   | Уменьшение boilerplate    |
| Maven             | 3.8+    | Сборка проекта            |


#Возможные проблемы

Ошибка подключения к БД:
```
Connection refused: localhost:5432
```
→ Убедитесь что PostgreSQL запущен и порт 5432 открыт

Ошибка "пользователь не найден":
→ Проверьте `username` и `password` в `application.properties`

База данных не создалась:
→ Создайте `hospital_db` вручную в pgAdmin и перезапустите

Порт 8080 занят:
→ Добавьте в `application.properties`:
```properties
server.port=8090
```
