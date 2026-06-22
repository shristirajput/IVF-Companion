package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.dto.HealthLogRequest;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.HealthLog;
import com.ivf.companion.model.Patient;
import com.ivf.companion.repository.HealthLogRepository;
import com.ivf.companion.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patient/logs")
public class HealthLogController {

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public ResponseEntity<?> getLogs(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        List<HealthLog> logs = healthLogRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/today")
    public ResponseEntity<?> getTodayLog(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        HealthLog log = healthLogRepository.findByPatientIdAndDate(patient.getId(), LocalDate.now())
                .orElse(null);

        return ResponseEntity.ok(log);
    }

    @PostMapping
    public ResponseEntity<?> submitDailyLog(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody HealthLogRequest request) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        LocalDate today = LocalDate.now();
        Optional<HealthLog> existingLogOpt = healthLogRepository.findByPatientIdAndDate(patient.getId(), today);

        HealthLog log;
        if (existingLogOpt.isPresent()) {
            // Update existing today's log
            log = existingLogOpt.get();
            log.setMood(request.getMood());
            log.setSymptoms(request.getSymptoms());
            log.setHormoneLevel(request.getHormoneLevel());
            log.setSleepHours(request.getSleepHours());
            log.setWeight(request.getWeight());
            log.setNotes(request.getNotes());
        } else {
            // Create new today's log
            log = new HealthLog();
            log.setPatient(patient);
            log.setDate(today);
            log.setMood(request.getMood());
            log.setSymptoms(request.getSymptoms());
            log.setHormoneLevel(request.getHormoneLevel());
            log.setSleepHours(request.getSleepHours());
            log.setWeight(request.getWeight());
            log.setNotes(request.getNotes());
        }

        HealthLog saved = healthLogRepository.save(log);
        return ResponseEntity.ok(saved);
    }
}
