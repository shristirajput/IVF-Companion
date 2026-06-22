package com.ivf.companion.repository;

import com.ivf.companion.model.IvfCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IvfCycleRepository extends JpaRepository<IvfCycle, Long> {
    List<IvfCycle> findByPatientId(Long patientId);
    Optional<IvfCycle> findTopByPatientIdOrderByIdDesc(Long patientId);
}
