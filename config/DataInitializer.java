package com.hospital.config;

import com.hospital.model.*;
import com.hospital.repository.*;
import com.hospital.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final WardRepository wardRepository;
    private final LabTestRepository labTestRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userService.count() > 0) return;

        // Departments
        Department cardio   = dept("Кардиология",  "Лечение заболеваний сердца и сосудов", "3", "Иванов А.П.");
        Department neuro    = dept("Неврология",   "Заболевания нервной системы",           "2", "Петрова М.С.");
        Department surgery  = dept("Хирургия",     "Оперативное лечение",                   "4", "Сидоров К.В.");
        Department therapy  = dept("Терапия",      "Общая терапия и диагностика",           "1", "Козлов Д.Н.");
        Department pediatry = dept("Педиатрия",    "Лечение детей до 18 лет",               "2", "Алимова Р.Б.");

        // Wards
        ward("301", cardio,   Ward.WardType.STANDARD,       4, 2, "Стандартная палата");
        ward("302", cardio,   Ward.WardType.INTENSIVE_CARE, 2, 1, "Палата интенсивной терапии");
        ward("201", neuro,    Ward.WardType.STANDARD,       3, 3, "Неврологическая палата");
        ward("401", surgery,  Ward.WardType.SURGERY,        2, 0, "Послеоперационная палата");
        ward("101", therapy,  Ward.WardType.PRIVATE,        1, 0, "Одноместная палата");
        ward("102", therapy,  Ward.WardType.STANDARD,       4, 1, "Терапевтическая палата");
        ward("211", pediatry, Ward.WardType.CHILDREN,       6, 4, "Детская палата");

        // Admin
        userService.saveEncoded(User.builder()
                .username("admin").password(enc("admin123"))
                .fullName("Администратор Системы").email("admin@hospital.kz")
                .role(User.Role.ADMIN).enabled(true).build());

        // Receptionist
        userService.saveEncoded(User.builder()
                .username("receptionist1").password(enc("recept123"))
                .fullName("Ахметова Гульмира Сапаровна").email("reception@hospital.kz")
                .role(User.Role.RECEPTIONIST).enabled(true).build());

        // Doctors
        User du1 = userService.saveEncoded(User.builder().username("doctor1").password(enc("doctor123"))
                .fullName("Иванов Алексей Петрович").email("ivanov@hospital.kz")
                .role(User.Role.DOCTOR).enabled(true).build());
        Doctor doc1 = doctorRepository.save(Doctor.builder().user(du1)
                .specialization("Кардиолог").licenseNumber("KZ-001").phone("+7 701 111 1111")
                .cabinet("301").experienceYears(12).department(cardio).build());

        User du2 = userService.saveEncoded(User.builder().username("doctor2").password(enc("doctor123"))
                .fullName("Петрова Мария Сергеевна").email("petrova@hospital.kz")
                .role(User.Role.DOCTOR).enabled(true).build());
        Doctor doc2 = doctorRepository.save(Doctor.builder().user(du2)
                .specialization("Невролог").licenseNumber("KZ-002").phone("+7 701 222 2222")
                .cabinet("201").experienceYears(8).department(neuro).build());

        User du3 = userService.saveEncoded(User.builder().username("doctor3").password(enc("doctor123"))
                .fullName("Сидоров Кирилл Владимирович").email("sidorov@hospital.kz")
                .role(User.Role.DOCTOR).enabled(true).build());
        doctorRepository.save(Doctor.builder().user(du3)
                .specialization("Хирург").licenseNumber("KZ-003").phone("+7 701 333 3333")
                .cabinet("401").experienceYears(15).department(surgery).build());

        // Patients
        User pu1 = userService.saveEncoded(User.builder().username("patient1").password(enc("patient123"))
                .fullName("Жумабеков Нурлан Асанович").email("patient@hospital.kz")
                .role(User.Role.PATIENT).enabled(true).build());
        Patient p1 = patientRepository.save(Patient.builder().user(pu1)
                .phone("+7 777 100 2020").dateOfBirth(LocalDate.of(1985, 5, 15))
                .address("г. Алматы, ул. Абая 10").bloodType("B+").allergies("Пенициллин")
                .gender(Patient.Gender.MALE).emergencyContact("Жумабекова А.").emergencyPhone("+7 777 100 3030").build());

        User pu2 = userService.saveEncoded(User.builder().username("patient2").password(enc("patient123"))
                .fullName("Серикова Дана Маратовна").email("dana@hospital.kz")
                .role(User.Role.PATIENT).enabled(true).build());
        Patient p2 = patientRepository.save(Patient.builder().user(pu2)
                .phone("+7 777 200 3030").dateOfBirth(LocalDate.of(1992, 9, 22))
                .address("г. Алматы, ул. Достык 45").bloodType("A+").allergies("Нет")
                .gender(Patient.Gender.FEMALE).emergencyContact("Сериков М.").emergencyPhone("+7 777 200 4040").build());

        // Appointments
        appointmentRepository.save(Appointment.builder().patient(p1).doctor(doc1)
                .appointmentTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0))
                .status(Appointment.Status.SCHEDULED).reason("Плановый осмотр").build());
        appointmentRepository.save(Appointment.builder().patient(p1).doctor(doc1)
                .appointmentTime(LocalDateTime.now().minusDays(3).withHour(14).withMinute(0))
                .status(Appointment.Status.COMPLETED).reason("Боли в груди").notes("Назначено ЭКГ").build());
        appointmentRepository.save(Appointment.builder().patient(p2).doctor(doc2)
                .appointmentTime(LocalDateTime.now().plusDays(2).withHour(11).withMinute(30))
                .status(Appointment.Status.SCHEDULED).reason("Головные боли").build());

        // Medical record
        medicalRecordRepository.save(MedicalRecord.builder().patient(p1).doctor(doc1)
                .diagnosis("Артериальная гипертензия I степени")
                .treatment("Ограничение соли, физическая активность")
                .prescription("Лизиноприл 5мг 1р/день").build());

        // Lab tests
        labTest("Общий анализ крови",       "Стандартное исследование клеток крови",        "Гематология",   1, new BigDecimal("1500"));
        labTest("Биохимия крови",           "Комплексный анализ биохимических показателей",  "Биохимия",      2, new BigDecimal("3800"));
        labTest("Общий анализ мочи",        "Исследование физических свойств мочи",          "Урология",      1, new BigDecimal("900"));
        labTest("ЭКГ",                      "Электрокардиограмма сердца",                    "Кардиология",   1, new BigDecimal("2500"));
        labTest("УЗИ брюшной полости",      "Ультразвуковое исследование органов живота",    "УЗИ",           1, new BigDecimal("5000"));
        labTest("Гормоны щитовидной железы","Т3, Т4, ТТГ",                                  "Эндокринология",3, new BigDecimal("4200"));
        labTest("МРТ головного мозга",      "Магнитно-резонансная томография",               "Неврология",    3, new BigDecimal("18000"));
        labTest("КТ грудной клетки",        "Компьютерная томография",                       "Хирургия",      2, new BigDecimal("12000"));
        labTest("Анализ на ВИЧ",            "Иммуноферментный анализ",                       "Инфекционные",  3, new BigDecimal("1200"));
        labTest("Коагулограмма",            "Исследование свёртываемости крови",             "Гематология",   2, new BigDecimal("2800"));
        labTest("Глюкоза крови",            "Определение уровня сахара в крови",             "Биохимия",      1, new BigDecimal("600"));
        labTest("Рентген грудной клетки",   "Обзорный рентген лёгких",                       "Хирургия",      1, new BigDecimal("2000"));
    }

    private Department dept(String name, String desc, String floor, String head) {
        return departmentRepository.save(Department.builder()
                .name(name).description(desc).floor(floor).headDoctorName(head).build());
    }

    private void ward(String num, Department dep, Ward.WardType type, int cap, int occ, String desc) {
        wardRepository.save(Ward.builder().number(num).department(dep)
                .type(type).capacity(cap).occupied(occ).description(desc).build());
    }

    private void labTest(String name, String desc, String cat, int days, BigDecimal price) {
        labTestRepository.save(LabTest.builder().name(name).description(desc)
                .category(cat).daysToResult(days).price(price).available(true).build());
    }

    private String enc(String raw) {
        return passwordEncoder.encode(raw);
    }
}
