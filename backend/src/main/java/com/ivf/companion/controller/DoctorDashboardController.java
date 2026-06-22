package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.*;
import com.ivf.companion.repository.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class DoctorDashboardController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private IvfCycleRepository cycleRepository;

    // FXML Bindings - Sidebar Selection
    @FXML private TextField searchField;
    @FXML private ListView<Patient> patientListView;

    // FXML Bindings - Workspace States
    @FXML private VBox placeholderView;
    @FXML private VBox clinicalWorkspace;

    // FXML Bindings - Selected Patient Info
    @FXML private Label patientNameLabel;
    @FXML private Label patientBioLabel;
    @FXML private Label patientCycleBadge;
    @FXML private Label patientAmhLabel;
    @FXML private Label patientFshLabel;
    @FXML private Label patientCycleDayLabel;

    // FXML Bindings - Logs Tab
    @FXML private TableView<HealthLog> patientLogsTable;
    @FXML private TableColumn<HealthLog, LocalDate> colLogDate;
    @FXML private TableColumn<HealthLog, String> colLogMood;
    @FXML private TableColumn<HealthLog, String> colLogSymptoms;
    @FXML private TableColumn<HealthLog, Double> colLogEstrogen;
    @FXML private TableColumn<HealthLog, Double> colLogSleep;
    @FXML private TableColumn<HealthLog, String> colLogNotes;

    // FXML Bindings - Meds Tab
    @FXML private TableView<Medication> patientMedsTable;
    @FXML private TableColumn<Medication, String> colMedName;
    @FXML private TableColumn<Medication, String> colMedDosage;
    @FXML private TableColumn<Medication, String> colMedTime;
    @FXML private TableColumn<Medication, LocalDate> colMedStart;
    @FXML private TableColumn<Medication, LocalDate> colMedEnd;
    @FXML private TableColumn<Medication, String> colMedAdherence;

    // FXML Bindings - Prescribe Form
    @FXML private TextField prescNameField;
    @FXML private TextField prescDosageField;
    @FXML private TextField prescTimeField;
    @FXML private DatePicker prescStartDatePicker;
    @FXML private DatePicker prescEndDatePicker;
    @FXML private TextField prescInstructionField;
    @FXML private Label prescErrorLabel;

    private Doctor currentDoctor;
    private Patient selectedPatient;
    private ObservableList<Patient> patientList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup Table Columns - Logs
        patientLogsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        patientMedsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colLogDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colLogMood.setCellValueFactory(new PropertyValueFactory<>("mood"));
        colLogSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        colLogEstrogen.setCellValueFactory(new PropertyValueFactory<>("hormoneLevel"));
        colLogSleep.setCellValueFactory(new PropertyValueFactory<>("sleepHours"));
        colLogNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Setup Date Cell Formatter for Log Dates
        colLogDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
                }
            }
        });

        // Setup Table Columns - Meds
        colMedName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMedDosage.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        colMedTime.setCellValueFactory(new PropertyValueFactory<>("timeOfDay"));
        colMedStart.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        colMedEnd.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        colMedAdherence.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isCompleted() ? "Taken" : "Pending"));

        // Format dates for medication start/end
        colMedStart.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
        });
        colMedEnd.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.format(DateTimeFormatter.ofPattern("MM/dd/yyyy")));
            }
        });

        // Load Doctor Profile and Patients in background
        new Thread(this::loadDoctorPortalData).start();

        // Listen for list selections
        patientListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal;
                showPatientClinicalProfile(newVal);
            }
        });
    }

    private void loadDoctorPortalData() {
        Optional<Doctor> docOpt;
        if (userSession.getRole() == Role.ROLE_ADMIN) {
            // Admin role can oversee all patients, let's grab the first doctor's ID as anchor or see all
            List<Doctor> docs = doctorRepository.findAll();
            if (!docs.isEmpty()) {
                currentDoctor = docs.get(0);
            }
        } else {
            Optional<Doctor> dbDoc = doctorRepository.findByUserId(userSession.getId());
            if (dbDoc.isPresent()) {
                currentDoctor = dbDoc.get();
            }
        }

        if (currentDoctor == null) {
            return;
        }

        // Fetch patients
        List<Patient> dbPatients = patientRepository.findByAssignedDoctorId(currentDoctor.getId());

        Platform.runLater(() -> {
            patientList.addAll(dbPatients);
            
            // Filter configuration
            FilteredList<Patient> filteredPatients = new FilteredList<>(patientList, p -> true);
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filteredPatients.setPredicate(patient -> {
                    if (newVal == null || newVal.isEmpty()) {
                        return true;
                    }
                    String lower = newVal.toLowerCase();
                    return patient.getUser().getFullName().toLowerCase().contains(lower) ||
                           patient.getUser().getUsername().toLowerCase().contains(lower);
                });
            });

            patientListView.setItems(filteredPatients);
            
            // Beautiful cell rendering showing full names
            patientListView.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Patient item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText("👤 " + item.getUser().getFullName() + " (Age " + item.getAge() + ")");
                        setStyle("-fx-text-fill: white; -fx-padding: 10px; -fx-cursor: hand;");
                    }
                }
            });
        });
    }

    private void showPatientClinicalProfile(Patient p) {
        // Hide placeholder and show portal
        placeholderView.setVisible(false);
        clinicalWorkspace.setVisible(true);

        // Header info
        patientNameLabel.setText(p.getUser().getFullName());
        patientBioLabel.setText("Age: " + p.getAge() + " • DOB: " + 
                p.getDateOfBirth().format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")) + 
                "\nClinical History: " + p.getHistory());
        patientAmhLabel.setText(p.getAmhLevel() != null ? p.getAmhLevel() + " ng/mL" : "N/A");
        patientFshLabel.setText(p.getFshLevel() != null ? p.getFshLevel() + " mIU/mL" : "N/A");

        // Load active cycle information
        Optional<IvfCycle> cycleOpt = cycleRepository.findTopByPatientIdOrderByIdDesc(p.getId());
        if (cycleOpt.isPresent()) {
            IvfCycle cycle = cycleOpt.get();
            patientCycleBadge.setText(cycle.getStatus());
            patientCycleDayLabel.setText("Day " + cycle.getCurrentDay() + " (" + cycle.getStatus() + ")");
        } else {
            patientCycleBadge.setText("NO CYCLE");
            patientCycleDayLabel.setText("No active cycle registered");
        }

        // Fetch logs and prescriptions in background
        new Thread(() -> {
            List<HealthLog> logs = healthLogRepository.findByPatientId(p.getId());
            logs.sort(Comparator.comparing(HealthLog::getDate).reversed()); // Most recent first

            List<Medication> meds = medicationRepository.findByPatientId(p.getId());

            Platform.runLater(() -> {
                patientLogsTable.setItems(FXCollections.observableArrayList(logs));
                patientMedsTable.setItems(FXCollections.observableArrayList(meds));
            });
        }).start();
    }

    @FXML
    private void handlePrescribe(ActionEvent event) {
        if (selectedPatient == null) return;

        String name = prescNameField.getText().trim();
        String dosage = prescDosageField.getText().trim();
        String time = prescTimeField.getText().trim();
        LocalDate start = prescStartDatePicker.getValue();
        LocalDate end = prescEndDatePicker.getValue();
        String instruction = prescInstructionField.getText().trim();

        if (name.isEmpty() || dosage.isEmpty() || time.isEmpty() || start == null || end == null) {
            showPrescError("Please fill out all prescription fields.");
            return;
        }

        if (end.isBefore(start)) {
            showPrescError("End Date cannot be before Start Date.");
            return;
        }

        prescErrorLabel.setVisible(false);

        Medication med = new Medication();
        med.setPatient(selectedPatient);
        med.setName(name);
        med.setDosage(dosage);
        med.setTimeOfDay(time);
        med.setStartDate(start);
        med.setEndDate(end);
        med.setInstruction(instruction);
        med.setCompleted(false);

        new Thread(() -> {
            medicationRepository.save(med);
            Platform.runLater(() -> {
                // Refresh med table
                showPatientClinicalProfile(selectedPatient);

                // Alert success
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Issued prescription for " + name + " successfully.");
                alert.showAndWait();

                // Clear fields
                prescNameField.clear();
                prescDosageField.clear();
                prescTimeField.clear();
                prescStartDatePicker.setValue(null);
                prescEndDatePicker.setValue(null);
                prescInstructionField.clear();
            });
        }).start();
    }

    private void showPrescError(String msg) {
        prescErrorLabel.setText(msg);
        prescErrorLabel.setVisible(true);
    }
}
