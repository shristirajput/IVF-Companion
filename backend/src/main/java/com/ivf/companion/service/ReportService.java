package com.ivf.companion.service;

import com.ivf.companion.exception.ResourceNotFoundException;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import com.ivf.companion.model.*;
import com.ivf.companion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IvfCycleRepository ivfCycleRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Transactional(readOnly = true)
    public byte[] generatePatientReportPdf(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Font Settings
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(0, 153, 115));
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.DARK_GRAY);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
            Font smallFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 8, Color.GRAY);

            // 1. Header Banner using PdfPTable
            PdfPTable headerTable = new PdfPTable(1);
            headerTable.setWidthPercentage(100);
            PdfPCell headerCell = new PdfPCell(new Paragraph("IVF COMPANION - MEDICAL TREATMENT REPORT", titleFont));
            headerCell.setBackgroundColor(new Color(0, 153, 115)); // Brand Medical Teal
            headerCell.setPadding(15);
            headerCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            headerCell.setBorder(Rectangle.NO_BORDER);
            headerTable.addCell(headerCell);
            document.add(headerTable);
            document.add(new Paragraph(" ")); // Spacer

            // 2. Patient & Clinic Profiles Grid
            PdfPTable profileTable = new PdfPTable(2);
            profileTable.setWidthPercentage(100);
            profileTable.setSpacingBefore(10);
            profileTable.setSpacingAfter(15);
            float[] columnWidths = {1f, 1f};
            profileTable.setWidths(columnWidths);

            // Patient Info Cell
            PdfPCell pCell = new PdfPCell();
            pCell.setBorder(Rectangle.BOX);
            pCell.setBorderColor(new Color(230, 230, 230));
            pCell.setPadding(10);
            pCell.setUseAscender(true);
            pCell.setUseDescender(true);
            Paragraph patientHeader = new Paragraph("PATIENT PROFILE", sectionFont);
            patientHeader.setSpacingAfter(5);
            pCell.addElement(patientHeader);
            pCell.addElement(new Paragraph("Name: " + patient.getUser().getFullName(), regularFont));
            pCell.addElement(new Paragraph("Age: " + (patient.getAge() != null ? patient.getAge() : "N/A"), regularFont));
            pCell.addElement(new Paragraph("AMH Level: " + (patient.getAmhLevel() != null ? patient.getAmhLevel() + " ng/mL" : "N/A"), regularFont));
            pCell.addElement(new Paragraph("FSH Level: " + (patient.getFshLevel() != null ? patient.getFshLevel() + " mIU/mL" : "N/A"), regularFont));
            profileTable.addCell(pCell);

            // Clinic Info Cell
            PdfPCell cCell = new PdfPCell();
            cCell.setBorder(Rectangle.BOX);
            cCell.setBorderColor(new Color(230, 230, 230));
            cCell.setPadding(10);
            Paragraph clinicHeader = new Paragraph("CLINIC & DOCTOR INFO", sectionFont);
            clinicHeader.setSpacingAfter(5);
            cCell.addElement(clinicHeader);
            if (patient.getAssignedDoctor() != null) {
                Doctor doc = patient.getAssignedDoctor();
                cCell.addElement(new Paragraph("Physician: " + doc.getUser().getFullName(), regularFont));
                cCell.addElement(new Paragraph("Specialty: " + doc.getSpecialization(), regularFont));
                cCell.addElement(new Paragraph("Clinic: " + doc.getClinicName(), regularFont));
                cCell.addElement(new Paragraph("License: " + doc.getLicenseNumber(), regularFont));
            } else {
                cCell.addElement(new Paragraph("Physician: Not Assigned", regularFont));
                cCell.addElement(new Paragraph("Clinic: IVF General Center", regularFont));
            }
            profileTable.addCell(cCell);
            document.add(profileTable);

            // 3. Treatment Cycle Progress Status
            List<IvfCycle> cycles = ivfCycleRepository.findByPatientId(patientId);
            if (!cycles.isEmpty()) {
                IvfCycle currentCycle = cycles.get(cycles.size() - 1);
                document.add(new Paragraph("ACTIVE IVF CYCLE PROGRESS", sectionFont));
                document.add(new Paragraph("Current Status Phase: " + currentCycle.getStatus() + " (Day " + currentCycle.getCurrentDay() + ")", boldFont));
                document.add(new Paragraph("Cycle Start Date: " + currentCycle.getStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), regularFont));
                if (currentCycle.getNotes() != null) {
                    document.add(new Paragraph("Cycle Notes: " + currentCycle.getNotes(), regularFont));
                }
                document.add(new Paragraph(" ")); // Spacer
            }

            // 4. Medications Table
            List<Medication> medications = medicationRepository.findByPatientId(patientId);
            document.add(new Paragraph("PRESCRIBED HORMONAL SCHEDULE", sectionFont));
            document.add(new Paragraph(" ")); // Tiny spacer
            
            if (!medications.isEmpty()) {
                PdfPTable medTable = new PdfPTable(5);
                medTable.setWidthPercentage(100);
                medTable.setWidths(new float[]{2f, 1f, 1.5f, 2f, 1f});

                String[] headers = {"Medication Name", "Dosage", "Timing", "Special Instructions", "Status"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Paragraph(header, boldFont));
                    cell.setBackgroundColor(new Color(240, 240, 240));
                    cell.setPadding(6);
                    cell.setBorderColor(new Color(220, 220, 220));
                    medTable.addCell(cell);
                }

                for (Medication med : medications) {
                    medTable.addCell(new PdfPCell(new Paragraph(med.getName(), regularFont)));
                    medTable.addCell(new PdfPCell(new Paragraph(med.getDosage(), regularFont)));
                    medTable.addCell(new PdfPCell(new Paragraph(med.getTimeOfDay(), regularFont)));
                    medTable.addCell(new PdfPCell(new Paragraph(med.getInstruction() != null ? med.getInstruction() : "None", regularFont)));
                    
                    String statusText = med.isCompleted() ? "Active Administered" : "Scheduled Pending";
                    PdfPCell statusCell = new PdfPCell(new Paragraph(statusText, regularFont));
                    if (med.isCompleted()) {
                        statusCell.setBackgroundColor(new Color(230, 249, 245));
                    }
                    medTable.addCell(statusCell);
                }
                document.add(medTable);
            } else {
                document.add(new Paragraph("No active medication schedule recorded.", regularFont));
            }
            document.add(new Paragraph(" ")); // Spacer

            // 5. Daily Wellness & Symptom Logs (Past 5 entries)
            List<HealthLog> logs = healthLogRepository.findByPatientId(patientId);
            document.add(new Paragraph("DAILY WELLNESS & HORMONAL LOGS SUMMARY", sectionFont));
            document.add(new Paragraph(" ")); // Tiny spacer

            if (!logs.isEmpty()) {
                PdfPTable logTable = new PdfPTable(5);
                logTable.setWidthPercentage(100);
                logTable.setWidths(new float[]{1.5f, 1f, 2f, 1.5f, 1f});

                String[] headers = {"Date Logged", "Mood Index", "Recorded Symptoms", "Hormones Level", "Sleep (Hrs)"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Paragraph(header, boldFont));
                    cell.setBackgroundColor(new Color(240, 240, 240));
                    cell.setPadding(6);
                    cell.setBorderColor(new Color(220, 220, 220));
                    logTable.addCell(cell);
                }

                int count = 0;
                // Get latest 5 logs
                for (int i = logs.size() - 1; i >= 0 && count < 5; i--) {
                    HealthLog log = logs.get(i);
                    logTable.addCell(new PdfPCell(new Paragraph(log.getDate().toString(), regularFont)));
                    logTable.addCell(new PdfPCell(new Paragraph(log.getMood() != null ? log.getMood() : "N/A", regularFont)));
                    logTable.addCell(new PdfPCell(new Paragraph(log.getSymptoms() != null && !log.getSymptoms().isEmpty() ? log.getSymptoms() : "None", regularFont)));
                    logTable.addCell(new PdfPCell(new Paragraph(log.getHormoneLevel() != null ? log.getHormoneLevel() + " pg/mL" : "Not measured", regularFont)));
                    logTable.addCell(new PdfPCell(new Paragraph(log.getSleepHours() != null ? log.getSleepHours() + " hrs" : "N/A", regularFont)));
                    count++;
                }
                document.add(logTable);
            } else {
                document.add(new Paragraph("No health tracking entries logged.", regularFont));
            }
            document.add(new Paragraph(" ")); // Spacer

            // 6. Medical Report Footer
            Paragraph footer = new Paragraph("Report generated on: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) 
                    + " | Powered by IVF Companion platform. Confidential Medical Record.", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(30);
            document.add(footer);

        } catch (DocumentException ex) {
            ex.printStackTrace();
        } finally {
            document.close();
        }

        return out.toByteArray();
    }
}
