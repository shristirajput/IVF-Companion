package com.ivf.companion.repository;

import com.ivf.companion.model.Patient;
import com.ivf.companion.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUser(User user);
    Optional<Patient> findByUserId(Long userId);
    List<Patient> findByAssignedDoctorId(Long doctorId);
}
