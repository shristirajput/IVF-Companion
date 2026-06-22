package com.ivf.companion.service;

import com.ivf.companion.config.JwtTokenProvider;
import com.ivf.companion.dto.ApiResponse;
import com.ivf.companion.dto.JwtAuthenticationResponse;
import com.ivf.companion.dto.LoginRequest;
import com.ivf.companion.dto.RegisterRequest;
import com.ivf.companion.exception.BadRequestException;
import com.ivf.companion.model.*;
import com.ivf.companion.repository.DoctorRepository;
import com.ivf.companion.repository.PatientRepository;
import com.ivf.companion.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;

@Service
public class AuthService {

    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider tokenProvider;

    public JwtAuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new BadRequestException("User profile error."));

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setAccessToken(jwt);
        response.setTokenType("Bearer");
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFullName(user.getFullName());
        response.setRole(user.getRole().name());
        return response;
    }

    @Transactional
    public ApiResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email Address already in use!");
        }

        Role userRole;
        try {
            userRole = Role.valueOf("ROLE_" + registerRequest.getRole().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid user role. Allowed: PATIENT, DOCTOR, ADMIN");
        }

        // Create User entity
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setFullName(registerRequest.getFullName());
        user.setRole(userRole);
        user.setActive(true);
        User savedUser = userRepository.save(user);

        // Map dependent role-specific records
        if (userRole == Role.ROLE_PATIENT) {
            Integer age = null;
            if (registerRequest.getDateOfBirth() != null) {
                age = Period.between(registerRequest.getDateOfBirth(), LocalDate.now()).getYears();
            }

            Patient patient = new Patient();
            patient.setUser(savedUser);
            patient.setDateOfBirth(registerRequest.getDateOfBirth());
            patient.setAge(age);
            patient.setAmhLevel(registerRequest.getAmhLevel());
            patient.setFshLevel(registerRequest.getFshLevel());
            patient.setHistory(registerRequest.getHistory());
            patientRepository.save(patient);

        } else if (userRole == Role.ROLE_DOCTOR) {
            if (registerRequest.getLicenseNumber() == null || registerRequest.getLicenseNumber().isBlank()) {
                throw new BadRequestException("Medical license number is required for doctor accounts.");
            }

            Doctor doctor = new Doctor();
            doctor.setUser(savedUser);
            doctor.setSpecialization(registerRequest.getSpecialization());
            doctor.setLicenseNumber(registerRequest.getLicenseNumber());
            doctor.setClinicName(registerRequest.getClinicName());
            doctorRepository.save(doctor);
        }

        return new ApiResponse(true, "User registered successfully");
    }
}
