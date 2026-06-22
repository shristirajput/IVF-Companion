package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.Medication;
import com.ivf.companion.model.Patient;
import com.ivf.companion.repository.MedicationRepository;
import com.ivf.companion.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient/medications")
public class MedicationController {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PatientRepository patientRepository;

    @GetMapping
    public ResponseEntity<?> getMedications(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));

        List<Medication> meds = medicationRepository.findByPatientId(patient.getId());
        return ResponseEntity.ok(meds);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<?> toggleMedicationAdherence(@PathVariable Long id, @RequestParam boolean completed, @AuthenticationPrincipal UserPrincipal userPrincipal) {
        Medication med = medicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication not found with id: " + id));

        // Security check
        Patient patient = patientRepository.findByUserId(userPrincipal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found."));
        if (!med.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).body("Access Denied");
        }

        med.setCompleted(completed);
        Medication updated = medicationRepository.save(med);
        return ResponseEntity.ok(updated);
    }
}
