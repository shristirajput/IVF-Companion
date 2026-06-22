package com.ivf.companion.config;

import com.ivf.companion.model.*;
import com.ivf.companion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private IvfCycleRepository cycleRepository;
    @Autowired private MedicationRepository medicationRepository;
    @Autowired private HealthLogRepository healthLogRepository;
    @Autowired private ForumPostRepository forumPostRepository;
    @Autowired private ForumCommentRepository forumCommentRepository;
    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            return;
        }

        // ── 1. Admin User ─────────────────────────────────────────────────────
        User adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setEmail("admin@ivfcompanion.com");
        adminUser.setPassword(passwordEncoder.encode("admin123"));
        adminUser.setFullName("Dr. Elizabeth Blackwell");
        adminUser.setRole(Role.ROLE_ADMIN);
        adminUser.setActive(true);
        userRepository.save(adminUser);

        // ── 2. Doctor User ────────────────────────────────────────────────────
        User doctorUser = new User();
        doctorUser.setUsername("doctor");
        doctorUser.setEmail("doctor@ivfcompanion.com");
        doctorUser.setPassword(passwordEncoder.encode("doctor123"));
        doctorUser.setFullName("Dr. Alexander Fleming");
        doctorUser.setRole(Role.ROLE_DOCTOR);
        doctorUser.setActive(true);
        User savedDoctorUser = userRepository.save(doctorUser);

        Doctor doctor = new Doctor();
        doctor.setUser(savedDoctorUser);
        doctor.setSpecialization("Reproductive Endocrinology");
        doctor.setLicenseNumber("RE-987654");
        doctor.setClinicName("Grace IVF Center");
        Doctor savedDoctor = doctorRepository.save(doctor);

        // ── 3. Patient User ───────────────────────────────────────────────────
        User patientUser = new User();
        patientUser.setUsername("patient");
        patientUser.setEmail("patient@ivfcompanion.com");
        patientUser.setPassword(passwordEncoder.encode("patient123"));
        patientUser.setFullName("Sarah Jenkins");
        patientUser.setRole(Role.ROLE_PATIENT);
        patientUser.setActive(true);
        User savedPatientUser = userRepository.save(patientUser);

        Patient patient = new Patient();
        patient.setUser(savedPatientUser);
        patient.setDateOfBirth(LocalDate.of(1994, 4, 12));
        patient.setAge(32);
        patient.setAmhLevel(2.4);
        patient.setFshLevel(6.5);
        patient.setHistory("Trying to conceive for 3 years. Diagnosed with mild endometriosis.");
        patient.setAssignedDoctor(savedDoctor);
        Patient savedPatient = patientRepository.save(patient);

        // ── 4. IVF Cycle ──────────────────────────────────────────────────────
        IvfCycle cycle = new IvfCycle();
        cycle.setPatient(savedPatient);
        cycle.setStatus("STIMULATION");
        cycle.setStartDate(LocalDate.now().minusDays(10));
        cycle.setCurrentDay(11);
        cycle.setNotes("Day 11 of Stimulation. Follicles developing symmetrically.");
        cycleRepository.save(cycle);

        // ── 5. Medications ────────────────────────────────────────────────────
        Medication med1 = new Medication();
        med1.setPatient(savedPatient);
        med1.setName("Gonal-F (Follitropin Alfa)");
        med1.setDosage("225 IU");
        med1.setTimeOfDay("08:00 PM");
        med1.setStartDate(LocalDate.now().minusDays(10));
        med1.setEndDate(LocalDate.now().plusDays(4));
        med1.setInstruction("Subcutaneous injection in abdominal area daily.");
        med1.setCompleted(false);
        medicationRepository.save(med1);

        Medication med2 = new Medication();
        med2.setPatient(savedPatient);
        med2.setName("Cetrotide (Ganirelix)");
        med2.setDosage("0.25 mg");
        med2.setTimeOfDay("08:00 AM");
        med2.setStartDate(LocalDate.now().minusDays(3));
        med2.setEndDate(LocalDate.now().plusDays(4));
        med2.setInstruction("Subcutaneous injection daily to prevent premature ovulation.");
        med2.setCompleted(false);
        medicationRepository.save(med2);

        Medication med3 = new Medication();
        med3.setPatient(savedPatient);
        med3.setName("Menopur");
        med3.setDosage("75 IU");
        med3.setTimeOfDay("08:00 PM");
        med3.setStartDate(LocalDate.now().minusDays(10));
        med3.setEndDate(LocalDate.now().plusDays(4));
        med3.setInstruction("Subcutaneous injection mixed with Gonal-F.");
        med3.setCompleted(false);
        medicationRepository.save(med3);

        // ── 6. Health Logs ────────────────────────────────────────────────────
        HealthLog log1 = new HealthLog();
        log1.setPatient(savedPatient);
        log1.setDate(LocalDate.now().minusDays(2));
        log1.setMood("Hopeful");
        log1.setSymptoms("Mild bloating, slight cramping");
        log1.setHormoneLevel(1200.0);
        log1.setSleepHours(7.5);
        log1.setWeight(62.5);
        log1.setNotes("Feeling okay. Injections getting easier. Drank 3L of water today.");
        healthLogRepository.save(log1);

        HealthLog log2 = new HealthLog();
        log2.setPatient(savedPatient);
        log2.setDate(LocalDate.now().minusDays(1));
        log2.setMood("Anxious");
        log2.setSymptoms("Moderate bloating, breast tenderness");
        log2.setHormoneLevel(1650.0);
        log2.setSleepHours(6.8);
        log2.setWeight(62.8);
        log2.setNotes("A bit bloated. Anxious about tomorrow's scan. Keeping positive.");
        healthLogRepository.save(log2);

        HealthLog log3 = new HealthLog();
        log3.setPatient(savedPatient);
        log3.setDate(LocalDate.now());
        log3.setMood("Calm");
        log3.setSymptoms("Bloating");
        log3.setHormoneLevel(2100.0);
        log3.setSleepHours(8.0);
        log3.setWeight(63.0);
        log3.setNotes("Day 11 monitoring went well. Egg retrieval may be in 3-4 days!");
        healthLogRepository.save(log3);

        // ── 7. Forum Posts & Comments ─────────────────────────────────────────
        ForumPost post1 = new ForumPost();
        post1.setAuthor(savedPatientUser);
        post1.setTitle("Starting my first IVF cycle today! Tips?");
        post1.setContent("Hi everyone, I am Sarah and I am starting stimulation injections today. Any tips for managing side effects?");
        post1.setAnonymous(false);
        ForumPost savedPost1 = forumPostRepository.save(post1);

        ForumComment comment1 = new ForumComment();
        comment1.setPost(savedPost1);
        comment1.setAuthor(savedDoctorUser);
        comment1.setContent("Welcome Sarah! Take injections at the same time each evening. Ice on the injection site reduces discomfort. You've got this!");
        forumCommentRepository.save(comment1);

        ForumComment comment2 = new ForumComment();
        comment2.setPost(savedPost1);
        comment2.setAuthor(savedPatientUser);
        comment2.setContent("Thank you Dr. Fleming! The ice tip worked like a charm!");
        forumCommentRepository.save(comment2);

        ForumPost post2 = new ForumPost();
        post2.setAuthor(savedPatientUser);
        post2.setTitle("Understanding AMH levels and what they mean");
        post2.setContent("I just got AMH results back at 1.2 ng/mL. I am 34 years old. Is this normal or diminished?");
        post2.setAnonymous(true);
        ForumPost savedPost2 = forumPostRepository.save(post2);

        ForumComment comment3 = new ForumComment();
        comment3.setPost(savedPost2);
        comment3.setAuthor(savedDoctorUser);
        comment3.setContent("AMH 1.2 ng/mL for age 34 is within normal range, though on the lower end. You should respond well to standard stimulation protocols.");
        forumCommentRepository.save(comment3);

        // ── 8. Appointments ───────────────────────────────────────────────────
        Appointment app1 = new Appointment();
        app1.setPatient(savedPatient);
        app1.setDoctor(savedDoctor);
        app1.setTitle("Follicular Ultrasound Scan");
        app1.setDateTime(LocalDateTime.now().plusDays(1).withHour(9).withMinute(30).withSecond(0));
        app1.setStatus("SCHEDULED");
        app1.setNotes("Follow-up monitoring scan to measure follicle sizes and count.");
        appointmentRepository.save(app1);

        Appointment app2 = new Appointment();
        app2.setPatient(savedPatient);
        app2.setDoctor(savedDoctor);
        app2.setTitle("Egg Retrieval Procedure");
        app2.setDateTime(LocalDateTime.now().plusDays(4).withHour(8).withMinute(0).withSecond(0));
        app2.setStatus("SCHEDULED");
        app2.setNotes("Tentative egg retrieval procedure. Fasting required from midnight before.");
        appointmentRepository.save(app2);

        System.out.println(">>> Seeded database with users, cycles, logs, posts and appointments successfully.");
    }
}
