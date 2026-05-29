package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/doctor")
@RequiredArgsConstructor
public class DoctorController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;
    private final WardService wardService;
    private final WardAssignmentService wardAssignmentService;
    private final LabTestService labTestService;
    private final TestOrderService testOrderService;

    private Optional<Doctor> getCurrentDoctor(UserDetails ud) {
        return userService.findByUsername(ud.getUsername())
                .flatMap(doctorService::findByUser);
    }

    private User getCurrentUser(UserDetails ud) {
        return userService.findByUsername(ud.getUsername()).orElse(null);
    }

    // ── DASHBOARD ──────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Optional<Doctor> doctorOpt = getCurrentDoctor(ud);
        if (doctorOpt.isEmpty()) {
            model.addAttribute("errorMsg", "Профиль врача не найден. Обратитесь к администратору.");
            return "doctor/error";
        }
        Doctor doctor = doctorOpt.get();
        var appts = appointmentService.findByDoctor(doctor.getId());
        model.addAttribute("doctor", doctor);
        model.addAttribute("scheduled",    appts.stream().filter(a -> a.getStatus() == Appointment.Status.SCHEDULED).count());
        model.addAttribute("accepted",     appts.stream().filter(a -> a.getStatus() == Appointment.Status.ACCEPTED).count());
        model.addAttribute("inProgress",   appts.stream().filter(a -> a.getStatus() == Appointment.Status.IN_PROGRESS).count());
        model.addAttribute("completed",    appts.stream().filter(a -> a.getStatus() == Appointment.Status.COMPLETED).count());
        model.addAttribute("recentAppointments", appts.stream()
                .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime()))
                .limit(5).toList());
        return "doctor/dashboard";
    }

    // ── APPOINTMENTS ───────────────────────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails ud, Model model) {
        Optional<Doctor> doctorOpt = getCurrentDoctor(ud);
        if (doctorOpt.isEmpty()) return "redirect:/doctor/dashboard";
        model.addAttribute("appointments",
                appointmentService.findByDoctor(doctorOpt.get().getId()).stream()
                        .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime()))
                        .toList());
        return "doctor/appointments";
    }

    // Изменить статус записи (Принят / На приёме / Завершён / Отменён / Не явился)
    @PostMapping("/appointments/{id}/status")
    public String changeStatus(@PathVariable Long id,
                               @RequestParam String status,
                               @AuthenticationPrincipal UserDetails ud,
                               RedirectAttributes ra) {
        Optional<Doctor> doctorOpt = getCurrentDoctor(ud);
        if (doctorOpt.isEmpty()) return "redirect:/doctor/dashboard";
        appointmentService.findById(id).ifPresent(a -> {
            a.setStatus(Appointment.Status.valueOf(status));
            appointmentService.save(a);
        });
        ra.addFlashAttribute("success", "Статус записи обновлён");
        return "redirect:/doctor/appointments";
    }

    // Завершить приём с диагнозом и записью в мед.карту
    @PostMapping("/appointments/{id}/complete")
    public String completeAppointment(@PathVariable Long id,
                                      @RequestParam(required = false) String notes,
                                      @RequestParam(required = false) String diagnosis,
                                      @RequestParam(required = false) String prescription,
                                      @AuthenticationPrincipal UserDetails ud,
                                      RedirectAttributes ra) {
        Optional<Doctor> doctorOpt = getCurrentDoctor(ud);
        if (doctorOpt.isEmpty()) return "redirect:/doctor/dashboard";
        Doctor doctor = doctorOpt.get();
        appointmentService.findById(id).ifPresent(a -> {
            a.setStatus(Appointment.Status.COMPLETED);
            a.setNotes(notes);
            appointmentService.save(a);
            if (diagnosis != null && !diagnosis.isBlank()) {
                medicalRecordService.save(MedicalRecord.builder()
                        .patient(a.getPatient()).doctor(doctor)
                        .diagnosis(diagnosis).treatment(notes)
                        .prescription(prescription).build());
            }
        });
        ra.addFlashAttribute("success", "Приём завершён, запись добавлена в мед.карту");
        return "redirect:/doctor/appointments";
    }

    // ── PATIENTS ───────────────────────────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(@AuthenticationPrincipal UserDetails ud, Model model) {
        Optional<Doctor> doctorOpt = getCurrentDoctor(ud);
        if (doctorOpt.isEmpty()) return "redirect:/doctor/dashboard";
        Doctor doctor = doctorOpt.get();
        List<Long> patientIds = appointmentService.findByDoctor(doctor.getId()).stream()
                .map(a -> a.getPatient().getId()).distinct().toList();
        List<Patient> patients = patientIds.stream()
                .map(patientService::findById)
                .filter(Optional::isPresent)
                .map(Optional::get).toList();
        model.addAttribute("patients", patients);
        model.addAttribute("wards", wardService.findAll());
        return "doctor/patients";
    }

    @GetMapping("/patients/{id}/records")
    public String patientRecords(@PathVariable Long id, Model model) {
        Optional<Patient> patientOpt = patientService.findById(id);
        if (patientOpt.isEmpty()) return "redirect:/doctor/patients";
        Patient p = patientOpt.get();
        model.addAttribute("patient", p);
        model.addAttribute("records", medicalRecordService.findByPatient(id).stream()
                .sorted((a, b) -> b.getRecordDate().compareTo(a.getRecordDate())).toList());
        model.addAttribute("activeAssignment", wardAssignmentService.findActiveByPatient(id).orElse(null));
        model.addAttribute("wards", wardService.findAll());
        model.addAttribute("testOrders", testOrderService.findByPatient(id).stream()
                .sorted((a, b) -> b.getOrderedAt().compareTo(a.getOrderedAt())).toList());
        model.addAttribute("allTests", labTestService.findAvailable());
        return "doctor/patient-records";
    }

    // ── WARD ASSIGNMENT ────────────────────────────────────────────────────────
    @PostMapping("/patients/{id}/assign-ward")
    public String assignWard(@PathVariable Long id,
                             @RequestParam Long wardId,
                             @RequestParam(required = false) String notes,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes ra) {
        User user = getCurrentUser(ud);
        patientService.findById(id).ifPresent(patient ->
                wardService.findById(wardId).ifPresent(ward ->
                        wardAssignmentService.assign(patient, ward, user, notes)));
        ra.addFlashAttribute("success", "Пациент помещён в палату");
        return "redirect:/doctor/patients/" + id + "/records";
    }

    @PostMapping("/patients/{patientId}/discharge/{assignmentId}")
    public String discharge(@PathVariable Long patientId,
                            @PathVariable Long assignmentId,
                            RedirectAttributes ra) {
        wardAssignmentService.discharge(assignmentId);
        ra.addFlashAttribute("success", "Пациент выписан из палаты");
        return "redirect:/doctor/patients/" + patientId + "/records";
    }

    // ── LAB TESTS ─────────────────────────────────────────────────────────────
    @GetMapping("/lab-tests")
    public String labTests(Model model) {
        model.addAttribute("tests", labTestService.findAvailable());
        model.addAttribute("patients", patientService.findAll());
        return "doctor/lab-tests";
    }

    @PostMapping("/lab-tests/order")
    public String orderTests(@RequestParam Long patientId,
                             @RequestParam List<Long> testIds,
                             RedirectAttributes ra) {
        patientService.findById(patientId).ifPresent(patient ->
                testOrderService.createOrderByDoctor(patient, testIds));
        ra.addFlashAttribute("success", "Анализы назначены пациенту");
        return "redirect:/doctor/lab-tests";
    }
}
