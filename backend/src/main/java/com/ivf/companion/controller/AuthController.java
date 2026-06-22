package com.ivf.companion.controller;

import com.ivf.companion.config.UserPrincipal;
import com.ivf.companion.dto.ApiResponse;
import com.ivf.companion.dto.JwtAuthenticationResponse;
import com.ivf.companion.dto.LoginRequest;
import com.ivf.companion.dto.RegisterRequest;
import com.ivf.companion.model.Doctor;
import com.ivf.companion.model.Patient;
import com.ivf.companion.model.User;
import com.ivf.companion.repository.DoctorRepository;
import com.ivf.companion.repository.PatientRepository;
import com.ivf.companion.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        JwtAuthenticationResponse jwtAuthenticationResponse = authService.login(loginRequest);
        return ResponseEntity.ok(jwtAuthenticationResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        ApiResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    // Get current authenticated user session details
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (userPrincipal == null) {
            return ResponseEntity.status(401).body(new ApiResponse(false, "Unauthorized"));
        }

        User user = userPrincipal.getUser();
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("fullName", user.getFullName());
        profile.put("role", user.getRole().name());

        if (user.getRole().name().equals("ROLE_PATIENT")) {
            Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);
            if (patient != null) {
                profile.put("patientId", patient.getId());
                profile.put("age", patient.getAge());
                profile.put("dateOfBirth", patient.getDateOfBirth());
                profile.put("amhLevel", patient.getAmhLevel());
                profile.put("fshLevel", patient.getFshLevel());
                profile.put("history", patient.getHistory());
                if (patient.getAssignedDoctor() != null) {
                    profile.put("assignedDoctorId", patient.getAssignedDoctor().getId());
                    profile.put("assignedDoctorName", patient.getAssignedDoctor().getUser().getFullName());
                    profile.put("assignedDoctorUserId", patient.getAssignedDoctor().getUser().getId());
                }
            }
        } else if (user.getRole().name().equals("ROLE_DOCTOR")) {
            Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
            if (doctor != null) {
                profile.put("doctorId", doctor.getId());
                profile.put("specialization", doctor.getSpecialization());
                profile.put("licenseNumber", doctor.getLicenseNumber());
                profile.put("clinicName", doctor.getClinicName());
            }
        }

        return ResponseEntity.ok(profile);
    }
}
