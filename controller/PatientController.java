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

import java.util.Set;

@Controller
@RequestMapping("/patient")
@RequiredArgsConstructor
public class PatientController {

    private static final Set<Appointment.Status> ACTIVE_STATUSES = Set.of(
            Appointment.Status.SCHEDULED,
            Appointment.Status.ACCEPTED,
            Appointment.Status.IN_PROGRESS
    );

    private final UserService userService;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final MedicalRecordService medicalRecordService;

    private Patient getCurrentPatient(UserDetails ud) {
        return userService.findByUsername(ud.getUsername())
                .flatMap(patientService::findByUser).orElse(null);
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails ud, Model model) {
        Patient patient = getCurrentPatient(ud);
        if (patient == null) return "redirect:/login";
        model.addAttribute("patient", patient);
        model.addAttribute("upcomingAppointments",
                appointmentService.findByPatient(patient.getId()).stream()
                        .filter(a -> ACTIVE_STATUSES.contains(a.getStatus()))
                        .sorted((a, b) -> a.getAppointmentTime().compareTo(b.getAppointmentTime()))
                        .limit(3).toList());
        model.addAttribute("totalAppointments",
                appointmentService.findByPatient(patient.getId()).size());
        model.addAttribute("totalRecords",
                medicalRecordService.findByPatient(patient.getId()).size());
        return "patient/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(@AuthenticationPrincipal UserDetails ud, Model model) {
        Patient patient = getCurrentPatient(ud);
        if (patient == null) return "redirect:/login";
        model.addAttribute("appointments",
                appointmentService.findByPatient(patient.getId()).stream()
                        .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime()))
                        .toList());
        model.addAttribute("doctors", doctorService.findAll());
        return "patient/appointments";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@AuthenticationPrincipal UserDetails ud,
                                  @RequestParam Long doctorId,
                                  @RequestParam String appointmentTime,
                                  @RequestParam(required = false) String reason,
                                  RedirectAttributes ra) {
        Patient patient = getCurrentPatient(ud);
        if (patient == null) return "redirect:/login";
        doctorService.findById(doctorId).ifPresent(doctor -> {
            Appointment a = Appointment.builder()
                    .patient(patient).doctor(doctor)
                    .appointmentTime(java.time.LocalDateTime.parse(appointmentTime))
                    .reason(reason)
                    .status(Appointment.Status.SCHEDULED)
                    .build();
            appointmentService.save(a);
        });
        ra.addFlashAttribute("success", "Запись оформлена успешно");
        return "redirect:/patient/appointments";
    }

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails ud,
                                    RedirectAttributes ra) {
        Patient patient = getCurrentPatient(ud);
        appointmentService.findById(id).ifPresent(a -> {
            // Only cancel own appointment
            if (patient != null && a.getPatient().getId().equals(patient.getId())
                    && a.getStatus() == Appointment.Status.SCHEDULED) {
                a.setStatus(Appointment.Status.CANCELLED);
                appointmentService.save(a);
            }
        });
        ra.addFlashAttribute("success", "Запись отменена");
        return "redirect:/patient/appointments";
    }

    @GetMapping("/records")
    public String records(@AuthenticationPrincipal UserDetails ud, Model model) {
        Patient patient = getCurrentPatient(ud);
        if (patient == null) return "redirect:/login";
        model.addAttribute("records",
                medicalRecordService.findByPatient(patient.getId()).stream()
                        .sorted((a, b) -> b.getRecordDate().compareTo(a.getRecordDate()))
                        .toList());
        return "patient/records";
    }
}
