package com.ivf.companion.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecommendationRequest {

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 60, message = "Age must be at most 60")
    private Integer age;

    @NotNull(message = "AMH level is required")
    @Min(value = 0, message = "AMH level must be positive")
    private Double amhLevel; // Anti-Müllerian Hormone (ng/mL)

    @NotNull(message = "FSH level is required")
    @Min(value = 0, message = "FSH level must be positive")
    private Double fshLevel; // Follicle-Stimulating Hormone (mIU/mL)

    @NotNull(message = "Years of infertility is required")
    @Min(value = 0)
    private Integer yearsInfertility;

    @NotNull(message = "Number of previous IVF cycles is required")
    @Min(value = 0)
    private Integer prevCycles;

    @NotBlank(message = "Primary diagnosis is required")
    private String diagnosis; // "PCOS", "ENDOMETRIOSIS", "MALE_FACTOR", "TUBAL_FACTOR", "UNEXPLAINED"
}
