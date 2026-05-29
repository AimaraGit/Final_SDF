package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/receptionist")
@RequiredArgsConstructor
public class ReceptionistController {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final UserService userService;
    private final DepartmentService departmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalPatients", patientService.count());
        model.addAttribute("scheduledToday", appointmentService.findAll().stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED
                        && a.getAppointmentTime().toLocalDate().equals(java.time.LocalDate.now()))
                .count());
        model.addAttribute("upcomingAppointments", appointmentService.findAll().stream()
                .filter(a -> a.getStatus() == Appointment.Status.SCHEDULED)
                .sorted((a, b) -> a.getAppointmentTime().compareTo(b.getAppointmentTime()))
                .limit(8).toList());
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("patients", patientService.findAll());
        return "receptionist/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentService.findAll().stream()
                .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime())).toList());
        model.addAttribute("doctors", doctorService.findAll());
        model.addAttribute("patients", patientService.findAll());
        return "receptionist/appointments";
    }

    @PostMapping("/appointments/book")
    public String bookAppointment(@RequestParam Long patientId,
                                  @RequestParam Long doctorId,
                                  @RequestParam String appointmentTime,
                                  @RequestParam(required = false) String reason,
                                  RedirectAttributes ra) {
        patientService.findById(patientId).ifPresent(patient ->
            doctorService.findById(doctorId).ifPresent(doctor -> {
                Appointment a = Appointment.builder()
                        .patient(patient).doctor(doctor)
                        .appointmentTime(java.time.LocalDateTime.parse(appointmentTime))
                        .reason(reason).status(Appointment.Status.SCHEDULED).build();
                appointmentService.save(a);
            })
        );
        ra.addFlashAttribute("success", "Запись успешно оформлена");
        return "redirect:/receptionist/appointments";
    }

    @PostMapping("/appointments/cancel/{id}")
    public String cancelAppointment(@PathVariable Long id, RedirectAttributes ra) {
        appointmentService.findById(id).ifPresent(a -> {
            a.setStatus(Appointment.Status.CANCELLED);
            appointmentService.save(a);
        });
        ra.addFlashAttribute("success", "Запись отменена");
        return "redirect:/receptionist/appointments";
    }

    @GetMapping("/patients")
    public String patients(Model model) {
        model.addAttribute("patients", patientService.findAll());
        return "receptionist/patients";
    }

    @GetMapping("/patients/{id}")
    public String patientDetail(@PathVariable Long id, Model model) {
        patientService.findById(id).ifPresent(p -> {
            model.addAttribute("patient", p);
            model.addAttribute("appointments", appointmentService.findByPatient(id).stream()
                    .sorted((a, b) -> b.getAppointmentTime().compareTo(a.getAppointmentTime())).toList());
        });
        return "receptionist/patient-detail";
    }
}
