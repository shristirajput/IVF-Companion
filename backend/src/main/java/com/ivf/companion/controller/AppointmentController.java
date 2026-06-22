package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.dto.AppointmentRequest;
import com.ivf.companion.exception.BadRequestException;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.Appointment;
import com.ivf.companion.model.Doctor;
import com.ivf.companion.model.Patient;
import com.ivf.companion.repository.AppointmentRepository;
import com.ivf.companion.repository.DoctorRepository;
import com.ivf.companion.repository.PatientRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/patient/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @GetMapping
    public ResponseEntity<?> getAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        List<Appointment> appts = appointmentRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(appts);
    }

    @PostMapping
    public ResponseEntity<?> bookAppointment(@AuthenticationPrincipal UserPrincipal userPrincipal, @Valid @RequestBody AppointmentRequest request) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setTitle(request.getTitle());
        appointment.setDateTime(request.getDateTime());
        appointment.setStatus("SCHEDULED");
        appointment.setNotes(request.getNotes());

        Appointment saved = appointmentRepository.save(appointment);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelAppointment(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        // Security check
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));
        if (!appointment.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        appointment.setStatus("CANCELLED");
        Appointment updated = appointmentRepository.save(appointment);
        return ResponseEntity.ok(updated);
    }

    // List all doctors so patients can book appointments with them
    @GetMapping("/doctors")
    public ResponseEntity<?> getAvailableDoctors() {
        List<Doctor> doctors = doctorRepository.findAll();
        
        List<Map<String, Object>> response = doctors.stream().map(doc -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("fullName", doc.getUser().getFullName());
            map.put("specialization", doc.getSpecialization());
            map.put("clinicName", doc.getClinicName());
            return map;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }
}
