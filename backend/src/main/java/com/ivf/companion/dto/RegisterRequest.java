package com.ivf.companion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Role is required")
    private String role; // "PATIENT", "DOCTOR", "ADMIN"

    // Doctor specific fields
    private String specialization;
    private String licenseNumber;
    private String clinicName;

    // Patient specific fields
    private LocalDate dateOfBirth;
    private Double amhLevel;
    private Double fshLevel;
    private String history;
}
