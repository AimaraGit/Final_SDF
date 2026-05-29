-- Запустить в pgAdmin перед стартом проекта
-- Query Tool → выполнить этот скрипт

CREATE DATABASE hospital_db
    WITH ENCODING 'UTF8'
    LC_COLLATE = 'en_US.UTF-8'
    LC_CTYPE   = 'en_US.UTF-8'
    TEMPLATE   = template0;

-- После создания БД подключитесь к hospital_db и выполните:

-- Индексы (таблицы создаст Hibernate автоматически при первом запуске)
-- Запустите этот блок ПОСЛЕ первого старта приложения:

/*
CREATE INDEX IF NOT EXISTS idx_appointments_patient   ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor    ON appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_status    ON appointments(status);
CREATE INDEX IF NOT EXISTS idx_appointments_time      ON appointments(appointment_time);
CREATE INDEX IF NOT EXISTS idx_medical_records_patient ON medical_records(patient_id);
CREATE INDEX IF NOT EXISTS idx_doctors_department     ON doctors(department_id);
CREATE INDEX IF NOT EXISTS idx_wards_department       ON wards(department_id);
CREATE INDEX IF NOT EXISTS idx_test_orders_patient    ON test_orders(patient_id);
CREATE INDEX IF NOT EXISTS idx_favorite_tests_patient ON favorite_tests(patient_id);
*/
