-- IVF Companion Database Seed Script
-- Default password for all users is "password" (BCrypt hashed)
-- Hashed value: $2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u


SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE chat_messages;
TRUNCATE TABLE notifications;
TRUNCATE TABLE forum_comments;
TRUNCATE TABLE forum_posts;
TRUNCATE TABLE health_logs;
TRUNCATE TABLE medications;
TRUNCATE TABLE appointments;
TRUNCATE TABLE ivf_cycles;
TRUNCATE TABLE patients;
TRUNCATE TABLE doctors;
TRUNCATE TABLE users;
SET FOREIGN_KEY_CHECKS = 1;

-- 1. Insert Users (Admin, Doctors, Patients)
INSERT INTO users (id, username, password, email, full_name, role, active) VALUES
(1, 'admin', '$2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u', 'admin@ivfcompanion.com', 'System Administrator', 'ADMIN', 1),
(2, 'dr_smith', '$2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u', 'sarah.smith@ivfcompanion.com', 'Dr. Sarah Smith', 'DOCTOR', 1),
(3, 'dr_jones', '$2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u', 'michael.jones@ivfcompanion.com', 'Dr. Michael Jones', 'DOCTOR', 1),
(4, 'jane_doe', '$2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u', 'jane.doe@gmail.com', 'Jane Doe', 'PATIENT', 1),
(5, 'alice_wonder', '$2a$10$R9h/cIPz0gi.UQrx1ryFhuuafQJKR1TfhYJ78h7257F5tO50aC/5u', 'alice.w@outlook.com', 'Alice Wonderland', 'PATIENT', 1);

-- 2. Insert Doctors
INSERT INTO doctors (id, user_id, specialization, license_number, clinic_name) VALUES
(1, 2, 'Reproductive Endocrinology & Infertility', 'LIC-99382', 'Hope Fertility Clinic'),
(2, 3, 'Obstetrics & Gynecology (IVF Specialist)', 'LIC-88271', 'Miracle Conception Center');

-- 3. Insert Patients
INSERT INTO patients (id, user_id, date_of_birth, age, amh_level, fsh_level, history, assigned_doctor_id) VALUES
(1, 4, '1992-06-15', 33, 1.8, 6.5, 'Trying to conceive for 4 years. Diagnosed with mild endometriosis. One unsuccessful IUI cycle in 2024.', 1),
(2, 5, '1989-11-20', 36, 0.9, 11.2, 'Trying to conceive for 3 years. Low ovarian reserve (AMH 0.9 ng/mL). Seeking rule recommendations.', 2);

-- 4. Insert IVF Cycles
INSERT INTO ivf_cycles (id, patient_id, status, start_date, current_day, notes) VALUES
(1, 1, 'STIMULATION', '2025-05-15', 6, 'Stimulation phase started successfully. Ovarian response looks promising.'),
(2, 2, 'TWO_WEEK_WAIT', '2025-05-08', 13, 'Embryo transfer completed on day 5 blastocyst. Currently on progesterone support.');

-- 5. Insert Appointments
INSERT INTO appointments (id, patient_id, doctor_id, title, date_time, status, notes) VALUES
(1, 1, 1, 'Follicle Ultrasound Scan', '2025-05-22 09:30:00', 'SCHEDULED', 'Monitoring follicle growth via transvaginal ultrasound. Fasting not required.'),
(2, 1, 1, 'Initial Consultation', '2025-05-10 11:00:00', 'COMPLETED', 'Discussed IVF protocol, signed consent forms, and set up medication schedule.'),
(3, 2, 2, 'Beta HCG Blood Test', '2025-05-22 08:00:00', 'SCHEDULED', 'Blood draw to verify pregnancy status following embryo transfer.');

-- 6. Insert Medications
INSERT INTO medications (id, patient_id, name, dosage, time_of_day, start_date, end_date, instruction, completed) VALUES
(1, 1, 'Gonal-F (Follitropin Alfa)', '150 IU', '09:00 PM', '2025-05-15', '2025-05-25', 'Subcutaneous injection in abdominal area daily.', 1),
(2, 1, 'Menopur', '75 IU', '09:00 PM', '2025-05-15', '2025-05-25', 'Subcutaneous injection, mix powder with diluent.', 1),
(3, 1, 'Cetrotide (Ganirelix)', '0.25 mg', '08:00 AM', '2025-05-19', '2025-05-24', 'To prevent premature ovulation.', 0),
(4, 2, 'Endometrin (Progesterone)', '100 mg', 'Three times daily', '2025-05-08', '2025-05-23', 'Vaginal insert for luteal phase support.', 1);

-- 7. Insert Health Logs
INSERT INTO health_logs (id, patient_id, date, mood, symptoms, hormone_level, sleep_hours, weight, notes) VALUES
(1, 1, '2025-05-17', 'Anxious', 'Bloating, Fatigue', 120.5, 7.0, 62.4, 'Feeling bloated after starting Gonal-F. Mild abdominal discomfort.'),
(2, 1, '2025-05-18', 'Hopeful', 'Bloating', 145.2, 7.5, 62.5, 'Feeling better today. Drank plenty of fluids and electrolytes.'),
(3, 1, '2025-05-19', 'Neutral', 'Fatigue', 180.0, 8.0, 62.3, 'Follicles are growing. Feeling tired but resting well.'),
(4, 1, '2025-05-20', 'Hopeful', 'Mild Cramping', 220.0, 7.8, 62.6, 'Slight cramping in lower abdomen, overall feeling positive.'),
(5, 2, '2025-05-19', 'Anxious', 'Fatigue, Nausea', 28.4, 6.5, 58.2, 'Extremely anxious about the upcoming Beta test. Struggling to sleep.'),
(6, 2, '2025-05-20', 'Stressed', 'Fatigue, Nausea', 30.1, 6.0, 58.0, 'Symptoms persist. Trying to stay calm and practice meditation.');

-- 8. Insert Forum Posts
INSERT INTO forum_posts (id, author_id, title, content, is_anonymous, created_at) VALUES
(1, 4, 'Looking for encouragement on Day 6 of Stimulation!', 'Hi everyone, today is Day 6 of my first IVF stimulation. My ovaries feel very heavy and I am starting to get bloated. Is this normal? Any tips to handle the injections?', 0, '2025-05-19 10:00:00'),
(2, 5, 'How do you handle the Two-Week Wait (TWW) anxiety?', 'I am on day 10 of my TWW after a blastocyst transfer. Every little pinch makes me think it either worked or failed. I am going crazy! How do you stay sane during this wait?', 1, '2025-05-18 15:30:00');

-- 9. Insert Forum Comments
INSERT INTO forum_comments (id, post_id, author_id, content, created_at) VALUES
(1, 1, 5, 'Completely normal! The heavy feeling is just your follicles growing. Make sure you stay hydrated with electrolytes and avoid high impact movements. You got this!', '2025-05-19 11:30:00'),
(2, 2, 4, 'Sending you lots of baby dust! The TWW is definitely the hardest part. I tried reading novels and binge-watching a new series to keep my mind off it. Try to stay positive!', '2025-05-19 09:00:00');

-- 10. Insert Notifications
INSERT INTO notifications (id, user_id, message, is_read, type, created_at) VALUES
(1, 4, 'Your follicle scan appointment with Dr. Sarah Smith is scheduled for 2026-05-23 at 09:30 AM.', 0, 'APPOINTMENT', '2025-05-20 08:00:00'),
(2, 4, 'Reminder: Time for your Gonal-F injection (150 IU) at 09:00 PM.', 0, 'REMINDER', '2025-05-20 08:15:00'),
(3, 5, 'Your pregnancy Beta test is scheduled for 2026-05-23 at 08:00 AM.', 0, 'APPOINTMENT', '2025-05-19 20:00:00');

-- 11. Insert Chat Messages (Jane Doe to Dr. Sarah Smith)
INSERT INTO chat_messages (id, sender_id, recipient_id, content, timestamp) VALUES
(1, 4, 2, 'Hi Dr. Smith, I just started my Gonal-F injections today.', '2025-05-15 10:00:00'),
(2, 2, 4, 'Great to hear, Jane! Make sure you stay hydrated. Let me know if you feel overly bloated.', '2025-05-15 10:30:00');
