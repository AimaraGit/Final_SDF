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

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final DepartmentService departmentService;
    private final AppointmentService appointmentService;
    private final WardService wardService;
    private final LabTestService labTestService;
    private final WardAssignmentService wardAssignmentService;

    // ── DASHBOARD ──────────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalPatients",       patientService.count());
        model.addAttribute("totalDoctors",        doctorService.count());
        model.addAttribute("totalDepartments",    departmentService.count());
        model.addAttribute("totalAppointments",   appointmentService.count());
        long activeAppts = appointmentService.countByStatus(Appointment.Status.SCHEDULED) + appointmentService.countByStatus(Appointment.Status.ACCEPTED) + appointmentService.countByStatus(Appointment.Status.IN_PROGRESS);
        model.addAttribute("scheduledAppointments", activeAppts);
        model.addAttribute("completedAppointments", appointmentService.countByStatus(Appointment.Status.COMPLETED));
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("recentAppointments",
                appointmentService.findAll().stream()
                        .filter(a -> a.getCreatedAt() != null)
                        .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                        .limit(5).toList());
        return "admin/dashboard";
    }

    // ── PATIENTS ───────────────────────────────────────────────────────────────
    @GetMapping("/patients")
    public String patients(Model model) {
        var patients = patientService.findAll();
        model.addAttribute("patients", patients);
        model.addAttribute("wards", wardService.findAll());
        model.addAttribute("activePage", "patients");
        return "admin/patients";
    }

    @PostMapping("/patients/save")
    public String savePatient(@ModelAttribute User user,
                              @RequestParam String phone,
                              @RequestParam(required = false) String dateOfBirth,
                              @RequestParam(required = false) String address,
                              @RequestParam(required = false) String bloodType,
                              @RequestParam(required = false) String allergies,
                              @RequestParam(required = false) String gender,
                              @RequestParam(required = false) String emergencyContact,
                              @RequestParam(required = false) String emergencyPhone,
                              RedirectAttributes ra) {
        if (userService.existsByUsername(user.getUsername())) {
            ra.addFlashAttribute("error", "Логин уже занят");
            return "redirect:/admin/patients";
        }
        user.setRole(User.Role.PATIENT);
        User saved = userService.save(user);

        Patient patient = Patient.builder()
                .user(saved).phone(phone)
                .dateOfBirth(dateOfBirth != null && !dateOfBirth.isBlank()
                        ? java.time.LocalDate.parse(dateOfBirth) : null)
                .address(address).bloodType(bloodType).allergies(allergies)
                .gender(gender != null && !gender.isBlank() ? Patient.Gender.valueOf(gender) : null)
                .emergencyContact(emergencyContact).emergencyPhone(emergencyPhone)
                .build();
        patientService.save(patient);
        ra.addFlashAttribute("success", "Пациент добавлен");
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/delete/{id}")
    public String deletePatient(@PathVariable Long id, RedirectAttributes ra) {
        patientService.deleteById(id);
        ra.addFlashAttribute("success", "Пациент удалён");
        return "redirect:/admin/patients";
    }

    // ── DOCTORS ────────────────────────────────────────────────────────────────
    @GetMapping("/doctors")
    public String doctors(Model model) {
        model.addAttribute("doctors",     doctorService.findAll());
        model.addAttribute("departments", departmentService.findAll());
        model.addAttribute("activePage",  "doctors");
        return "admin/doctors";
    }

    @PostMapping("/doctors/save")
    public String saveDoctor(@ModelAttribute User user,
                             @RequestParam String specialization,
                             @RequestParam(required = false) String licenseNumber,
                             @RequestParam(required = false) String phone,
                             @RequestParam(required = false) String cabinet,
                             @RequestParam(required = false) Integer experienceYears,
                             @RequestParam(required = false) Long departmentId,
                             RedirectAttributes ra) {
        if (userService.existsByUsername(user.getUsername())) {
            ra.addFlashAttribute("error", "Логин уже занят");
            return "redirect:/admin/doctors";
        }
        user.setRole(User.Role.DOCTOR);
        User saved = userService.save(user);

        Doctor doctor = Doctor.builder()
                .user(saved).specialization(specialization)
                .licenseNumber(licenseNumber).phone(phone)
                .cabinet(cabinet).experienceYears(experienceYears)
                .department(departmentId != null
                        ? departmentService.findById(departmentId).orElse(null) : null)
                .build();
        doctorService.save(doctor);
        ra.addFlashAttribute("success", "Врач добавлен");
        return "redirect:/admin/doctors";
    }

    @PostMapping("/doctors/delete/{id}")
    public String deleteDoctor(@PathVariable Long id, RedirectAttributes ra) {
        doctorService.deleteById(id);
        ra.addFlashAttribute("success", "Врач удалён");
        return "redirect:/admin/doctors";
    }

    // ── DEPARTMENTS ────────────────────────────────────────────────────────────
    @GetMapping("/departments")
    public String departments(Model model) {
        var departments = departmentService.findAll();
        model.addAttribute("departments", departments);
        // Count doctors per department safely
        java.util.Map<Long, Integer> counts = new java.util.HashMap<>();
        departments.forEach(d -> counts.put(d.getId(),
                doctorService.findByDepartment(d.getId()).size()));
        model.addAttribute("doctorCounts", counts);
        model.addAttribute("activePage", "departments");
        return "admin/departments";
    }

    @PostMapping("/departments/save")
    public String saveDept(@ModelAttribute Department department, RedirectAttributes ra) {
        departmentService.save(department);
        ra.addFlashAttribute("success", "Отделение сохранено");
        return "redirect:/admin/departments";
    }

    @PostMapping("/departments/delete/{id}")
    public String deleteDept(@PathVariable Long id, RedirectAttributes ra) {
        departmentService.deleteById(id);
        ra.addFlashAttribute("success", "Отделение удалено");
        return "redirect:/admin/departments";
    }

    // ── WARD ASSIGNMENT ────────────────────────────────────────────────────────
    @PostMapping("/patients/{id}/assign-ward")
    public String assignWard(@PathVariable Long id,
                             @RequestParam Long wardId,
                             @RequestParam(required = false) String notes,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes ra) {
        User adminUser = userService.findByUsername(ud.getUsername()).orElse(null);
        patientService.findById(id).ifPresent(patient ->
                wardService.findById(wardId).ifPresent(ward ->
                        wardAssignmentService.assign(patient, ward, adminUser, notes)));
        ra.addFlashAttribute("success", "Пациент помещён в палату");
        return "redirect:/admin/patients";
    }

    @PostMapping("/patients/{patientId}/discharge/{assignmentId}")
    public String dischargePatient(@PathVariable Long patientId,
                                   @PathVariable Long assignmentId,
                                   RedirectAttributes ra) {
        wardAssignmentService.discharge(assignmentId);
        ra.addFlashAttribute("success", "Пациент выписан");
        return "redirect:/admin/patients";
    }

    // ── APPOINTMENTS ───────────────────────────────────────────────────────────
    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments",
                appointmentService.findAll().stream()
                        .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime()))
                        .toList());
        model.addAttribute("activePage", "appointments");
        return "admin/appointments";
    }

    @PostMapping("/appointments/status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               RedirectAttributes ra) {
        appointmentService.findById(id).ifPresent(a -> {
            a.setStatus(Appointment.Status.valueOf(status));
            appointmentService.save(a);
        });
        ra.addFlashAttribute("success", "Статус обновлён");
        return "redirect:/admin/appointments";
    }
}
