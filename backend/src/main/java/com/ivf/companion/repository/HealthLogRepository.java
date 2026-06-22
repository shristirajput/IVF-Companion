package com.ivf.companion.repository;

import com.ivf.companion.model.HealthLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface HealthLogRepository extends JpaRepository<HealthLog, Long> {
    List<HealthLog> findByPatientId(Long patientId);
    Optional<HealthLog> findByPatientIdAndDate(Long patientId, LocalDate date);
    List<HealthLog> findByPatientIdAndDateBetweenOrderByDateAsc(Long patientId, LocalDate startDate, LocalDate endDate);
}
