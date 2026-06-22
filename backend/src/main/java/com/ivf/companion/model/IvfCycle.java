package com.ivf.companion.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "ivf_cycles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IvfCycle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(nullable = false, length = 50)
    private String status; // 'STIMULATION', 'EGG_RETRIEVAL', 'EMBRYO_DEVELOPMENT', 'EMBRYO_TRANSFER', 'TWO_WEEK_WAIT', 'COMPLETED_SUCCESS', 'COMPLETED_FAILED'

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "current_day")
    private Integer currentDay = 1;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
