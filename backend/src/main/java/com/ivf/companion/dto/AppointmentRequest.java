package com.ivf.companion.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AppointmentRequest {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotBlank(message = "Appointment title is required")
    private String title;

    @NotNull(message = "Appointment date/time is required")
    private LocalDateTime dateTime;

    private String notes;
}
