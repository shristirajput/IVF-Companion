package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.*;
import com.ivf.companion.repository.*;
import com.ivf.companion.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private IvfCycleRepository ivfCycleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EmailService emailService;

    // Get doctor's assigned patients
    @GetMapping("/patients")
    public ResponseEntity<?> getAssignedPatients(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Doctor doctor = doctorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));

        List<Patient> patients = patientRepository.findByAssignedDoctorId(doctor.getId());
        
        List<Map<String, Object>> response = patients.stream().map(patient -> {
            Map<String, Object> map = new HashMap<>();
            map.put("patientId", patient.getId());
            map.put("fullName", patient.getUser().getFullName());
            map.put("username", patient.getUser().getUsername());
            map.put("email", patient.getUser().getEmail());
            map.put("age", patient.getAge());
            map.put("amhLevel", patient.getAmhLevel());
            map.put("fshLevel", patient.getFshLevel());
            map.put("history", patient.getHistory());
            
            IvfCycle currentCycle = ivfCycleRepository.findTopByPatientIdOrderByIdDesc(patient.getId()).orElse(null);
            if (currentCycle != null) {
                map.put("activeCycleStatus", currentCycle.getStatus());
                map.put("activeCycleDay", currentCycle.getCurrentDay());
            } else {
                map.put("activeCycleStatus", "No Active Cycle");
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Get unassigned patients
    @GetMapping("/unassigned-patients")
    public ResponseEntity<?> getUnassignedPatients() {
        List<Patient> patients = patientRepository.findAll().stream()
            .filter(p -> p.getAssignedDoctor() == null)
            .collect(Collectors.toList());
            
        List<Map<String, Object>> response = patients.stream().map(patient -> {
            Map<String, Object> map = new HashMap<>();
            map.put("patientId", patient.getId());
            map.put("fullName", patient.getUser().getFullName());
            map.put("email", patient.getUser().getEmail());
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Doctor claims a patient
    @PutMapping("/patients/{patientId}/claim")
    public ResponseEntity<?> claimPatient(@PathVariable Long patientId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Doctor doctor = doctorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));
                
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
                
        patient.setAssignedDoctor(doctor);
        patientRepository.save(patient);
        return ResponseEntity.ok().build();
    }

    // View specific patient's health logs
    @GetMapping("/patients/{patientId}/logs")
    public ResponseEntity<?> getPatientLogs(@PathVariable Long patientId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyDoctorAccess(patientId, userPrincipal.getId());
        List<HealthLog> logs = healthLogRepository.findByPatientId(patientId);
        return ResponseEntity.ok(logs);
    }

    // View specific patient's medications
    @GetMapping("/patients/{patientId}/medications")
    public ResponseEntity<?> getPatientMedications(@PathVariable Long patientId, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        verifyDoctorAccess(patientId, userPrincipal.getId());
        List<Medication> meds = medicationRepository.findByPatientId(patientId);
        return ResponseEntity.ok(meds);
    }

    // Add new medication for patient
    @PostMapping("/patients/{patientId}/medications")
    public ResponseEntity<?> prescribeMedication(@PathVariable Long patientId, @RequestBody Medication medRequest, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Doctor doctor = verifyDoctorAccess(patientId, userPrincipal.getId());
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Medication med = new Medication();
        med.setPatient(patient);
        med.setName(medRequest.getName());
        med.setDosage(medRequest.getDosage());
        med.setTimeOfDay(medRequest.getTimeOfDay());
        med.setStartDate(medRequest.getStartDate() != null ? medRequest.getStartDate() : LocalDate.now());
        med.setEndDate(medRequest.getEndDate() != null ? medRequest.getEndDate() : LocalDate.now().plusDays(10));
        med.setInstruction(medRequest.getInstruction());
        med.setCompleted(false);

        Medication saved = medicationRepository.save(med);

        // Send notification to patient
        Notification notification = new Notification();
        notification.setUser(patient.getUser());
        notification.setMessage(String.format("Dr. %s prescribed a new medication: %s (%s, %s).", 
                doctor.getUser().getFullName(), med.getName(), med.getDosage(), med.getTimeOfDay()));
        notification.setType("REMINDER");
        notification.setRead(false);
        notificationRepository.save(notification);

        // Send Real Email
        String subject = "New Medication Prescribed";
        String body = String.format("Dear %s,\n\nDr. %s has prescribed a new medication for you:\n\nMedication: %s\nDosage: %s\nTime: %s\nInstructions: %s\n\nPlease log in to your IVF Companion dashboard to view the full details.\n\nBest,\nIVF Companion Team",
                patient.getUser().getFullName(), doctor.getUser().getFullName(), med.getName(), med.getDosage(), med.getTimeOfDay(), med.getInstruction());
        emailService.sendEmail(patient.getUser().getEmail(), subject, body);

        return ResponseEntity.ok(saved);
    }

    // Add appointment for patient
    @PostMapping("/patients/{patientId}/appointments")
    public ResponseEntity<?> scheduleDoctorAppointment(@PathVariable Long patientId, @RequestBody Appointment apptRequest, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Doctor doctor = verifyDoctorAccess(patientId, userPrincipal.getId());
        Patient patient = patientRepository.findById(patientId).orElseThrow(() -> new ResourceNotFoundException("Patient not found"));

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setTitle(apptRequest.getTitle());
        appt.setDateTime(apptRequest.getDateTime());
        appt.setStatus("SCHEDULED");
        appt.setNotes(apptRequest.getNotes());

        Appointment saved = appointmentRepository.save(appt);

        // Notify patient
        Notification notification = new Notification();
        notification.setUser(patient.getUser());
        notification.setMessage(String.format("New clinical appointment scheduled with Dr. %s: '%s' on %s.", 
                doctor.getUser().getFullName(), appt.getTitle(), appt.getDateTime().toString().replace("T", " ")));
        notification.setType("APPOINTMENT");
        notification.setRead(false);
        notificationRepository.save(notification);

        // Send Real Email
        String subject = "New Appointment Scheduled";
        String body = String.format("Dear %s,\n\nA new clinical appointment has been scheduled with Dr. %s.\n\nTitle: %s\nDate & Time: %s\nNotes: %s\n\nPlease log in to your IVF Companion dashboard to view the full details.\n\nBest,\nIVF Companion Team",
                patient.getUser().getFullName(), doctor.getUser().getFullName(), appt.getTitle(), appt.getDateTime().toString().replace("T", " "), appt.getNotes() != null ? appt.getNotes() : "None");
        emailService.sendEmail(patient.getUser().getEmail(), subject, body);

        return ResponseEntity.ok(saved);
    }

    // Get all appointments booked with this doctor (from patients booking directly)
    @GetMapping("/appointments")
    public ResponseEntity<?> getDoctorAppointments(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Doctor doctor = doctorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));

        List<Appointment> appointments = appointmentRepository.findByDoctorId(doctor.getId());

        List<Map<String, Object>> response = appointments.stream().map(appt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", appt.getId());
            map.put("title", appt.getTitle());
            map.put("dateTime", appt.getDateTime());
            map.put("status", appt.getStatus());
            map.put("notes", appt.getNotes());
            if (appt.getPatient() != null) {
                map.put("patientName", appt.getPatient().getUser().getFullName());
                map.put("patientId", appt.getPatient().getId());
            }
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    // Update appointment status (confirm or cancel)
    @PutMapping("/appointments/{apptId}/status")
    public ResponseEntity<?> updateAppointmentStatus(
            @PathVariable Long apptId,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        Doctor doctor = doctorRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));

        Appointment appt = appointmentRepository.findById(apptId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found."));

        if (!appt.getDoctor().getId().equals(doctor.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        String newStatus = body.get("status");
        appt.setStatus(newStatus);
        Appointment saved = appointmentRepository.save(appt);

        // Notify patient
        Notification notification = new Notification();
        notification.setUser(appt.getPatient().getUser());
        notification.setMessage(String.format("Your appointment '%s' has been %s by Dr. %s.",
                appt.getTitle(), newStatus.toLowerCase(), doctor.getUser().getFullName()));
        notification.setType("APPOINTMENT");
        notification.setRead(false);
        notificationRepository.save(notification);

        return ResponseEntity.ok(saved);
    }

    // Private helper to verify that the doctor is actually assigned to the queried patient

    private Doctor verifyDoctorAccess(Long patientId, Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found."));

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        if (patient.getAssignedDoctor() == null || !patient.getAssignedDoctor().getId().equals(doctor.getId())) {
            throw new ResourceNotFoundException("Access Denied: Patient is not assigned to this physician.");
        }
        return doctor;
    }
}
