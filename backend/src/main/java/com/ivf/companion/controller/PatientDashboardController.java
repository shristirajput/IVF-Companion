package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.HealthLog;
import com.ivf.companion.model.IvfCycle;
import com.ivf.companion.model.Medication;
import com.ivf.companion.model.Patient;
import com.ivf.companion.repository.HealthLogRepository;
import com.ivf.companion.repository.IvfCycleRepository;
import com.ivf.companion.repository.MedicationRepository;
import com.ivf.companion.repository.PatientRepository;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class PatientDashboardController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private IvfCycleRepository cycleRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private HealthLogRepository healthLogRepository;

    // FXML Bindings
    @FXML private Label welcomeTitle;
    @FXML private Label cycleStageBadge;
    @FXML private Label cycleDayLabel;
    @FXML private ProgressBar cycleProgressBar;
    @FXML private Label nextActionLabel;
    @FXML private Label amhLabel;
    @FXML private Label fshLabel;
    @FXML private Label assignedDoctorLabel;

    @FXML private VBox medsChecklistContainer;

    @FXML private LineChart<String, Number> hormoneChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<HealthLog> logsTable;
    @FXML private TableColumn<HealthLog, LocalDate> colDate;
    @FXML private TableColumn<HealthLog, String> colMood;
    @FXML private TableColumn<HealthLog, String> colSymptoms;
    @FXML private TableColumn<HealthLog, Double> colEstrogen;
    @FXML private TableColumn<HealthLog, Double> colSleep;
    @FXML private TableColumn<HealthLog, Double> colWeight;
    @FXML private TableColumn<HealthLog, String> colNotes;

    private Patient currentPatient;

    @FXML
    public void initialize() {
        // Setup Table Columns
        logsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colDate.setCellValueFactory(new PropertyValueFactory<>("date"));
        colMood.setCellValueFactory(new PropertyValueFactory<>("mood"));
        colSymptoms.setCellValueFactory(new PropertyValueFactory<>("symptoms"));
        colEstrogen.setCellValueFactory(new PropertyValueFactory<>("hormoneLevel"));
        colSleep.setCellValueFactory(new PropertyValueFactory<>("sleepHours"));
        colWeight.setCellValueFactory(new PropertyValueFactory<>("weight"));
        colNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));

        // Format dates in TableView beautifully
        colDate.setCellFactory(column -> new TableCell<HealthLog, LocalDate>() {
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

        // Load Patient data
        refreshDashboardData();
    }

    private void refreshDashboardData() {
        Optional<Patient> patientOpt = patientRepository.findByUserId(userSession.getId());
        if (patientOpt.isEmpty()) {
            return;
        }

        currentPatient = patientOpt.get();

        // 1. Set demographics
        welcomeTitle.setText(currentPatient.getUser().getFullName() + "'s IVF Hub");
        amhLabel.setText(currentPatient.getAmhLevel() != null ? currentPatient.getAmhLevel() + " ng/mL" : "N/A");
        fshLabel.setText(currentPatient.getFshLevel() != null ? currentPatient.getFshLevel() + " mIU/mL" : "N/A");
        assignedDoctorLabel.setText(currentPatient.getAssignedDoctor() != null ? 
                currentPatient.getAssignedDoctor().getUser().getFullName() : "Not Assigned");

        // 2. Load Cycle
        Optional<IvfCycle> cycleOpt = cycleRepository.findTopByPatientIdOrderByIdDesc(currentPatient.getId());
        if (cycleOpt.isPresent()) {
            IvfCycle cycle = cycleOpt.get();
            cycleStageBadge.setText(cycle.getStatus() + " PHASE");
            int dayNum = cycle.getCurrentDay() != null ? cycle.getCurrentDay() : 1;
            cycleDayLabel.setText("Day " + dayNum);
            
            // Assume 14 stimulation days, update progress accordingly
            double progress = dayNum / 14.0;
            cycleProgressBar.setProgress(Math.min(progress, 1.0));
            
            if (cycle.getNotes() != null && !cycle.getNotes().isEmpty()) {
                nextActionLabel.setText("Protocol Status: " + cycle.getNotes());
            }
        } else {
            cycleStageBadge.setText("NO ACTIVE CYCLE");
            cycleDayLabel.setText("Day -");
            cycleProgressBar.setProgress(0.0);
            nextActionLabel.setText("No active cycle protocol registered. Consult your specialist.");
        }

        // 3. Load Medications Checklist
        loadMedications();

        // 4. Load Table logs and Chart
        loadLogsAndChart();
    }

    private void loadMedications() {
        medsChecklistContainer.getChildren().clear();
        List<Medication> meds = medicationRepository.findByPatientId(currentPatient.getId());

        if (meds.isEmpty()) {
            Label noMeds = new Label("No active medications prescribed for today.");
            noMeds.setStyle("-fx-text-fill: -text-muted; -fx-font-style: italic;");
            medsChecklistContainer.getChildren().add(noMeds);
            return;
        }

        for (Medication med : meds) {
            HBox row = new HBox(10);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setStyle("-fx-background-color: rgba(255,255,255,0.02); -fx-background-radius: 6px; -fx-padding: 10px;");

            VBox info = new VBox(2);
            Label nameLbl = new Label(med.getName());
            nameLbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
            Label dosageLbl = new Label(med.getDosage() + " • " + med.getTimeOfDay() + " • " + med.getInstruction());
            dosageLbl.setStyle("-fx-text-fill: -text-secondary; -fx-font-size: 11px;");
            info.getChildren().addAll(nameLbl, dosageLbl);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            CheckBox checkBox = new CheckBox("Taken");
            checkBox.setSelected(med.isCompleted());
            checkBox.setStyle("-fx-text-fill: -text-secondary; -fx-cursor: hand;");

            checkBox.setOnAction(e -> {
                med.setCompleted(checkBox.isSelected());
                new Thread(() -> {
                    medicationRepository.save(med);
                    Platform.runLater(() -> {
                        if (checkBox.isSelected()) {
                            checkBox.setStyle("-fx-text-fill: -primary-teal; -fx-font-weight: bold;");
                        } else {
                            checkBox.setStyle("-fx-text-fill: -text-secondary;");
                        }
                    });
                }).start();
            });

            if (med.isCompleted()) {
                checkBox.setStyle("-fx-text-fill: -primary-teal; -fx-font-weight: bold;");
            }

            row.getChildren().addAll(info, spacer, checkBox);
            medsChecklistContainer.getChildren().add(row);
        }
    }

    private void loadLogsAndChart() {
        List<HealthLog> logs = healthLogRepository.findByPatientId(currentPatient.getId());
        
        // Sort chronologically for table & chart
        logs.sort(Comparator.comparing(HealthLog::getDate));
        
        logsTable.setItems(FXCollections.observableArrayList(logs));

        // Render Chart
        hormoneChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        
        for (HealthLog log : logs) {
            if (log.getHormoneLevel() != null) {
                String formattedDate = log.getDate().format(DateTimeFormatter.ofPattern("MM/dd"));
                series.getData().add(new XYChart.Data<>(formattedDate, log.getHormoneLevel()));
            }
        }
        
        hormoneChart.getData().add(series);
    }

    @FXML
    private void showAddLogDialog(ActionEvent event) {
        // Create dynamic modal dialog
        Dialog<HealthLog> dialog = new Dialog<>();
        dialog.setTitle("Daily Physiological Log");
        dialog.setHeaderText("Record your daily symptoms, mood, and vital measurements.");

        // Custom styled buttons
        ButtonType saveButtonType = new ButtonType("Save Log", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Form Fields
        ComboBox<String> moodBox = new ComboBox<>();
        moodBox.getItems().addAll("Happy", "Anxious", "Sad", "Neutral", "Stressed", "Hopeful");
        moodBox.setValue("Hopeful");

        TextField symptomsField = new TextField();
        symptomsField.setPromptText("e.g. Bloating, Cramping");

        TextField hormoneField = new TextField();
        hormoneField.setPromptText("Estrogen level in pg/mL");

        TextField sleepField = new TextField();
        sleepField.setPromptText("Hours slept last night");

        TextField weightField = new TextField();
        weightField.setPromptText("Weight in kg");

        TextArea notesArea = new TextArea();
        notesArea.setPrefHeight(80);
        notesArea.setWrapText(true);
        notesArea.setPromptText("Add daily observations, hydration status, etc.");

        grid.add(new Label("Mood:"), 0, 0);
        grid.add(moodBox, 1, 0);
        grid.add(new Label("Symptoms:"), 0, 1);
        grid.add(symptomsField, 1, 1);
        grid.add(new Label("Estrogen (pg/mL):"), 0, 2);
        grid.add(hormoneField, 1, 2);
        grid.add(new Label("Sleep (hours):"), 0, 3);
        grid.add(sleepField, 1, 3);
        grid.add(new Label("Weight (kg):"), 0, 4);
        grid.add(weightField, 1, 4);
        grid.add(new Label("Notes:"), 0, 5);
        grid.add(notesArea, 1, 5);

        // Apply styles to inputs in dialog
        for (javafx.scene.Node n : grid.getChildren()) {
            if (n instanceof Label) {
                n.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
            }
        }
        moodBox.setStyle("-fx-pref-width: 250px;");
        symptomsField.setStyle("-fx-pref-width: 250px;");
        hormoneField.setStyle("-fx-pref-width: 250px;");
        sleepField.setStyle("-fx-pref-width: 250px;");
        weightField.setStyle("-fx-pref-width: 250px;");
        notesArea.setStyle("-fx-pref-width: 250px;");

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setStyle("-fx-background-color: #0f172a; -fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1px;");

        // Load existing today's log if present
        LocalDate today = LocalDate.now();
        Optional<HealthLog> existingLogOpt = healthLogRepository.findByPatientIdAndDate(currentPatient.getId(), today);
        if (existingLogOpt.isPresent()) {
            HealthLog el = existingLogOpt.get();
            moodBox.setValue(el.getMood());
            symptomsField.setText(el.getSymptoms());
            hormoneField.setText(el.getHormoneLevel() != null ? String.valueOf(el.getHormoneLevel()) : "");
            sleepField.setText(el.getSleepHours() != null ? String.valueOf(el.getSleepHours()) : "");
            weightField.setText(el.getWeight() != null ? String.valueOf(el.getWeight()) : "");
            notesArea.setText(el.getNotes());
        }

        // Convert the result to a health log when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                HealthLog log;
                if (existingLogOpt.isPresent()) {
                    log = existingLogOpt.get();
                } else {
                    log = new HealthLog();
                    log.setPatient(currentPatient);
                    log.setDate(today);
                }
                
                log.setMood(moodBox.getValue());
                log.setSymptoms(symptomsField.getText().trim());
                
                try {
                    if (!hormoneField.getText().isEmpty()) log.setHormoneLevel(Double.parseDouble(hormoneField.getText()));
                    if (!sleepField.getText().isEmpty()) log.setSleepHours(Double.parseDouble(sleepField.getText()));
                    if (!weightField.getText().isEmpty()) log.setWeight(Double.parseDouble(weightField.getText()));
                } catch (NumberFormatException nfe) {
                    // Ignore or show notification
                }
                
                log.setNotes(notesArea.getText().trim());
                return log;
            }
            return null;
        });

        Optional<HealthLog> result = dialog.showAndWait();

        result.ifPresent(log -> {
            new Thread(() -> {
                healthLogRepository.save(log);
                Platform.runLater(() -> {
                    refreshDashboardData();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Success");
                    alert.setHeaderText(null);
                    alert.setContentText("Daily physiological log saved successfully!");
                    alert.showAndWait();
                });
            }).start();
        });
    }
}
