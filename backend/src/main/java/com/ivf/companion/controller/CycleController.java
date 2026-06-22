package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.exception.BadRequestException;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.IvfCycle;
import com.ivf.companion.model.Patient;
import com.ivf.companion.repository.IvfCycleRepository;
import com.ivf.companion.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patient/cycles")
public class CycleController {

    @Autowired
    private IvfCycleRepository ivfCycleRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentCycle(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        IvfCycle cycle = ivfCycleRepository.findTopByPatientIdOrderByIdDesc(patient.getId())
                .orElse(null);

        return ResponseEntity.ok(cycle);
    }

    @PostMapping
    public ResponseEntity<?> startNewCycle(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody IvfCycle cycleRequest) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        IvfCycle cycle = new IvfCycle();
        cycle.setPatient(patient);
        cycle.setStatus(cycleRequest.getStatus() != null ? cycleRequest.getStatus() : "STIMULATION");
        cycle.setStartDate(cycleRequest.getStartDate() != null ? cycleRequest.getStartDate() : LocalDate.now());
        cycle.setCurrentDay(1);
        cycle.setNotes(cycleRequest.getNotes());

        IvfCycle saved = ivfCycleRepository.save(cycle);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}/day")
    public ResponseEntity<?> updateCurrentDay(@PathVariable Long id, @RequestParam Integer day, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        IvfCycle cycle = ivfCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IVF Cycle not found with id: " + id));

        // Security check
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));
        if (!cycle.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        cycle.setCurrentDay(day);
        IvfCycle updated = ivfCycleRepository.save(cycle);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        IvfCycle cycle = ivfCycleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("IVF Cycle not found with id: " + id));

        // Security check
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found."));
        if (!cycle.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        cycle.setStatus(status.toUpperCase());
        IvfCycle updated = ivfCycleRepository.save(cycle);
        return ResponseEntity.ok(updated);
    }
}
