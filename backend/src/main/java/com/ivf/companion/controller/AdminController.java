package com.ivf.companion.controller;

import com.ivf.companion.exception.ResourceNotFoundException;
import com.ivf.companion.model.User;
import com.ivf.companion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private IvfCycleRepository ivfCycleRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private ForumPostRepository forumPostRepository;

    // View all platform users
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        
        List<Map<String, Object>> response = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("email", u.getEmail());
            map.put("fullName", u.getFullName());
            map.put("role", u.getRole());
            map.put("active", u.isActive());
            map.put("createdAt", u.getCreatedAt());
            
            if ("PATIENT".equals(u.getRole())) {
                patientRepository.findByUserId(u.getId()).ifPresent(p -> {
                    if (p.getAssignedDoctor() != null) {
                        map.put("assignedDoctorId", p.getAssignedDoctor().getId());
                    }
                });
            }
            return map;
        }).collect(java.util.stream.Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    // Toggle active status (block / unblock user)
    @PutMapping("/users/{id}/toggle-active")
    public ResponseEntity<?> toggleUserActiveStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // System safety block: do not allow deactivating the main admin user
        if (user.getUsername().equals("admin")) {
            return ResponseEntity.status(400).body("Cannot block root system administrator.");
        }

        user.setActive(!user.isActive());
        User updated = userRepository.save(user);
        updated.setPassword(null);
        return ResponseEntity.ok(updated);
    }

    // Platform statistics dashboard summary
    @GetMapping("/stats")
    public ResponseEntity<?> getPlatformStatistics() {
        long totalUsers = userRepository.count();
        long totalPatients = patientRepository.count();
        long totalDoctors = doctorRepository.count();
        long totalCycles = ivfCycleRepository.count();
        long totalAppointments = appointmentRepository.count();
        long totalForumPosts = forumPostRepository.count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalPatients", totalPatients);
        stats.put("totalDoctors", totalDoctors);
        stats.put("totalCycles", totalCycles);
        stats.put("totalAppointments", totalAppointments);
        stats.put("totalForumPosts", totalForumPosts);

        return ResponseEntity.ok(stats);
    }

    // Get all doctors for assignment dropdown
    @GetMapping("/doctors")
    public ResponseEntity<?> getAllDoctors() {
        List<Map<String, Object>> response = doctorRepository.findAll().stream().map(d -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", d.getId());
            
            Map<String, Object> userMap = new HashMap<>();
            if (d.getUser() != null) {
                userMap.put("fullName", d.getUser().getFullName());
            }
            map.put("user", userMap);
            
            return map;
        }).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Assign a doctor to a patient
    @PutMapping("/users/{userId}/assign-doctor/{doctorId}")
    public ResponseEntity<?> assignDoctorToPatient(@PathVariable Long userId, @PathVariable Long doctorId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found for this user"));
        
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));

        patient.setAssignedDoctor(doctor);
        patientRepository.save(patient);
        
        return ResponseEntity.ok().build();
    }
}
