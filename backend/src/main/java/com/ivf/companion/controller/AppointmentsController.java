package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.*;
import com.ivf.companion.repository.AppointmentRepository;
import com.ivf.companion.repository.DoctorRepository;
import com.ivf.companion.repository.PatientRepository;
import com.ivf.companion.service.ReportService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class AppointmentsController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ReportService reportService;

    // FXML Bindings - Table list
    @FXML private TableView<Appointment> appointmentsTable;
    @FXML private TableColumn<Appointment, LocalDateTime> colAppDate;
    @FXML private TableColumn<Appointment, String> colAppTitle;
    @FXML private TableColumn<Appointment, String> colAppUser;
    @FXML private TableColumn<Appointment, String> colAppStatus;
    @FXML private TableColumn<Appointment, String> colAppNotes;

    // FXML Bindings - Export
    @FXML private ComboBox<Patient> pdfPatientSelector;
    @FXML private Button btnDownloadPdf;

    // FXML Bindings - Booking form
    @FXML private TextField appTitleField;
    @FXML private DatePicker appDatePicker;
    @FXML private TextField appTimeField;
    @FXML private Label appUserSelectorLabel;
    @FXML private ComboBox<Object> appUserComboBox; // Holds Doctor or Patient depending on role
    @FXML private TextArea appNotesArea;
    @FXML private Label appErrorLabel;

    private Patient currentPatient;
    private Doctor currentDoctor;

    @FXML
    public void initialize() {
        // Setup Table Columns cell value factories
        appointmentsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAppTitle.setCellValueFactory(new PropertyValueFactory<>("title"));
        colAppStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colAppNotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colAppDate.setCellValueFactory(new PropertyValueFactory<>("dateTime"));

        // Format dates in TableView beautifully
        colAppDate.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a")));
                }
            }
        });

        // Set up colAppUser depending on who is logged in
        colAppUser.setCellValueFactory(cellData -> {
            Appointment app = cellData.getValue();
            if (userSession.getRole() == Role.ROLE_PATIENT) {
                return new SimpleStringProperty(app.getDoctor().getUser().getFullName());
            } else {
                return new SimpleStringProperty(app.getPatient().getUser().getFullName());
            }
        });

        // Load data depending on user role
        refreshAppointmentsData();
    }

    private void refreshAppointmentsData() {
        new Thread(() -> {
            Role role = userSession.getRole();
            List<Appointment> dbAppointments = null;

            if (role == Role.ROLE_PATIENT) {
                Optional<Patient> pOpt = patientRepository.findByUserId(userSession.getId());
                if (pOpt.isPresent()) {
                    currentPatient = pOpt.get();
                    dbAppointments = appointmentRepository.findByPatientId(currentPatient.getId());

                    // UI Updates
                    Platform.runLater(() -> {
                        appUserSelectorLabel.setText("Assigned Specialist");
                        appUserComboBox.getItems().clear();
                        
                        if (currentPatient.getAssignedDoctor() != null) {
                            appUserComboBox.getItems().add(currentPatient.getAssignedDoctor());
                            appUserComboBox.setValue(currentPatient.getAssignedDoctor());
                        }
                        appUserComboBox.setDisable(true); // Patients only book with assigned doctor
                    });
                }
            } else if (role == Role.ROLE_DOCTOR) {
                Optional<Doctor> dOpt = doctorRepository.findByUserId(userSession.getId());
                if (dOpt.isPresent()) {
                    currentDoctor = dOpt.get();
                    dbAppointments = appointmentRepository.findByDoctorId(currentDoctor.getId());

                    List<Patient> myPatients = patientRepository.findByAssignedDoctorId(currentDoctor.getId());

                    // UI Updates
                    Platform.runLater(() -> {
                        appUserSelectorLabel.setText("Patient Recipient");
                        appUserComboBox.getItems().clear();
                        appUserComboBox.getItems().addAll(myPatients);
                        appUserComboBox.setDisable(false);

                        // Configure PDF exporter patient selector
                        pdfPatientSelector.setVisible(true);
                        pdfPatientSelector.setManaged(true);
                        pdfPatientSelector.getItems().clear();
                        pdfPatientSelector.getItems().addAll(myPatients);
                        if (!myPatients.isEmpty()) {
                            pdfPatientSelector.setValue(myPatients.get(0));
                        }
                    });
                }
            } else if (role == Role.ROLE_ADMIN) {
                dbAppointments = appointmentRepository.findAll();
                List<Patient> allPatients = patientRepository.findAll();

                Platform.runLater(() -> {
                    appUserSelectorLabel.setText("Patient Recipient");
                    appUserComboBox.getItems().clear();
                    appUserComboBox.getItems().addAll(allPatients);
                    appUserComboBox.setDisable(false);

                    pdfPatientSelector.setVisible(true);
                    pdfPatientSelector.setManaged(true);
                    pdfPatientSelector.getItems().addAll(allPatients);
                });
            }

            if (dbAppointments != null) {
                // Sort chronologically
                dbAppointments.sort(Comparator.comparing(Appointment::getDateTime));
                final List<Appointment> sortedApps = dbAppointments;
                Platform.runLater(() -> appointmentsTable.setItems(FXCollections.observableArrayList(sortedApps)));
            }
        }).start();

        // Custom Cell Formatting for Doctor/Patient comboboxes
        appUserComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Doctor) {
                    setText(((Doctor) item).getUser().getFullName());
                } else if (item instanceof Patient) {
                    setText(((Patient) item).getUser().getFullName());
                }
            }
        });
        
        appUserComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else if (item instanceof Doctor) {
                    setText(((Doctor) item).getUser().getFullName());
                } else if (item instanceof Patient) {
                    setText(((Patient) item).getUser().getFullName());
                }
            }
        });

        pdfPatientSelector.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getUser().getFullName());
            }
        });
        pdfPatientSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Patient item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : item.getUser().getFullName());
            }
        });
    }

    @FXML
    private void handleBookAppointment(ActionEvent event) {
        String title = appTitleField.getText().trim();
        LocalDate date = appDatePicker.getValue();
        String timeStr = appTimeField.getText().trim();
        Object selectedUser = appUserComboBox.getValue();

        if (title.isEmpty() || date == null || timeStr.isEmpty() || selectedUser == null) {
            showError("All fields must be filled to confirm reservation.");
            return;
        }

        // Parse Time: e.g. "09:30 AM" or "14:00"
        LocalTime parsedTime = LocalTime.of(9, 0); // Default to 9:00 AM
        try {
            if (timeStr.toUpperCase().contains("AM") || timeStr.toUpperCase().contains("PM")) {
                parsedTime = LocalTime.parse(timeStr.toUpperCase(), DateTimeFormatter.ofPattern("hh:mm a"));
            } else {
                parsedTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"));
            }
        } catch (Exception e) {
            // Log warning, proceed with default
        }

        LocalDateTime appDateTime = LocalDateTime.of(date, parsedTime);
        appErrorLabel.setVisible(false);

        Appointment app = new Appointment();
        app.setTitle(title);
        app.setDateTime(appDateTime);
        app.setNotes(appNotesArea.getText().trim());
        app.setStatus("SCHEDULED");

        if (userSession.getRole() == Role.ROLE_PATIENT) {
            app.setPatient(currentPatient);
            app.setDoctor((Doctor) selectedUser);
        } else {
            app.setPatient((Patient) selectedUser);
            app.setDoctor(currentDoctor);
        }

        new Thread(() -> {
            appointmentRepository.save(app);
            Platform.runLater(() -> {
                refreshAppointmentsData();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Appointment reserved for: " + appDateTime.format(DateTimeFormatter.ofPattern("MMMM dd 'at' hh:mm a")));
                alert.showAndWait();

                // Clear fields
                appTitleField.clear();
                appDatePicker.setValue(null);
                appTimeField.clear();
                appNotesArea.clear();
            });
        }).start();
    }

    @FXML
    private void handleGeneratePdf(ActionEvent event) {
        Patient targetPatient = null;
        if (userSession.getRole() == Role.ROLE_PATIENT) {
            targetPatient = currentPatient;
        } else {
            targetPatient = pdfPatientSelector.getValue();
            if (targetPatient == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Patient Selected");
                alert.setHeaderText(null);
                alert.setContentText("Please select a patient from the list to export their clinical report.");
                alert.showAndWait();
                return;
            }
        }

        final Patient finalTarget = targetPatient;

        // Open Save File Dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Clinical PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Document (*.pdf)", "*.pdf"));
        
        String cleanName = finalTarget.getUser().getFullName().toLowerCase().replace(" ", "_");
        fileChooser.setInitialFileName(cleanName + "_ivf_report.pdf");

        Stage stage = (Stage) btnDownloadPdf.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            btnDownloadPdf.setDisable(true);
            btnDownloadPdf.setText("Generating...");

            new Thread(() -> {
                try {
                    byte[] pdfBytes = reportService.generatePatientReportPdf(finalTarget.getId());
                    
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(pdfBytes);
                    }

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("PDF Report Exported");
                        alert.setHeaderText("Generation Successful");
                        alert.setContentText("Clinical treatment PDF report saved to:\n" + file.getAbsolutePath());
                        alert.showAndWait();

                        btnDownloadPdf.setDisable(false);
                        btnDownloadPdf.setText("📥 Export PDF Report");
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Export Failed");
                        alert.setHeaderText("PDF Engine Error");
                        alert.setContentText(ex.getMessage());
                        alert.showAndWait();

                        btnDownloadPdf.setDisable(false);
                        btnDownloadPdf.setText("📥 Export PDF Report");
                    });
                }
            }).start();
        }
    }

    private void showError(String msg) {
        appErrorLabel.setText(msg);
        appErrorLabel.setVisible(true);
    }
}
