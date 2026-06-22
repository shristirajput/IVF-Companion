package com.ivf.companion.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "health_logs",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"patient_id", "date"})}
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false)
    private LocalDate date;

    @Column(length = 50)
    private String mood; // 'Happy', 'Anxious', 'Sad', 'Neutral', 'Stressed', 'Hopeful'

    @Column(length = 255)
    private String symptoms; // e.g., 'Cramping, Bloating, Fatigue'

    @Column(name = "hormone_level")
    private Double hormoneLevel; // Estrogen or Progesterone in pg/mL or ng/mL

    @Column(name = "sleep_hours")
    private Double sleepHours;

    private Double weight; // in kg

    @Column(columnDefinition = "TEXT")
    private String notes;
}
