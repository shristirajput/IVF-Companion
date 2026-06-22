package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.Doctor;
import com.ivf.companion.model.Patient;
import com.ivf.companion.model.Role;
import com.ivf.companion.model.User;
import com.ivf.companion.repository.DoctorRepository;
import com.ivf.companion.repository.IvfCycleRepository;
import com.ivf.companion.repository.PatientRepository;
import com.ivf.companion.repository.UserRepository;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Component
public class AdminDashboardController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private IvfCycleRepository cycleRepository;

    @Autowired
    private UserSession userSession;

    // FXML Bindings - Stats Cards
    @FXML private Label statTotalUsers;
    @FXML private Label statTotalPatients;
    @FXML private Label statTotalDoctors;
    @FXML private Label statTotalCycles;

    // FXML Bindings - Filter controls
    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> adminRoleFilterComboBox;

    // FXML Bindings - User Table
    @FXML private TableView<User> adminUsersTable;
    @FXML private TableColumn<User, Long> colAdminId;
    @FXML private TableColumn<User, String> colAdminUsername;
    @FXML private TableColumn<User, String> colAdminFullName;
    @FXML private TableColumn<User, String> colAdminEmail;
    @FXML private TableColumn<User, String> colAdminRole;
    @FXML private TableColumn<User, String> colAdminStatus;
    @FXML private TableColumn<User, LocalDateTime> colAdminCreated;

    @FXML private Label adminStatusLabel;
    @FXML private Button btnToggleActive;
    @FXML private Button btnDeleteUser;

    private ObservableList<User> userList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Setup Table columns
        adminUsersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colAdminId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAdminUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAdminFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colAdminEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        
        colAdminRole.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().getRole().name().replace("ROLE_", "")));
        
        colAdminStatus.setCellValueFactory(cellData -> 
                new SimpleStringProperty(cellData.getValue().isActive() ? "ACTIVE" : "BLOCKED"));
        
        colAdminCreated.setCellValueFactory(new PropertyValueFactory<>("createdAt"));

        // Format dates in TableView beautifully
        colAdminCreated.setCellFactory(column -> new TableCell<User, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
                }
            }
        });

        // Initialize Combobox roles filter
        adminRoleFilterComboBox.getItems().addAll("All Roles", "PATIENT", "DOCTOR", "ADMIN");
        adminRoleFilterComboBox.setValue("All Roles");

        // Load stats and users
        refreshAdminDashboard();
    }

    private void refreshAdminDashboard() {
        new Thread(() -> {
            long usersCount = userRepository.count();
            long patientsCount = patientRepository.count();
            long doctorsCount = doctorRepository.count();
            long cyclesCount = cycleRepository.count();

            List<User> dbUsers = userRepository.findAll();

            Platform.runLater(() -> {
                // Update stats labels
                statTotalUsers.setText(String.valueOf(usersCount));
                statTotalPatients.setText(String.valueOf(patientsCount));
                statTotalDoctors.setText(String.valueOf(doctorsCount));
                statTotalCycles.setText(String.valueOf(cyclesCount));

                // Populate user table list
                userList.clear();
                userList.addAll(dbUsers);

                // Configure dynamic filtering based on search text and role combobox filter
                FilteredList<User> filteredUsers = new FilteredList<>(userList, u -> true);
                
                // Helper selection listener update
                Runnable filterTrigger = () -> {
                    String searchText = adminSearchField.getText().trim().toLowerCase();
                    String selectedRole = adminRoleFilterComboBox.getValue();

                    filteredUsers.setPredicate(user -> {
                        // 1. Filter by Search Text
                        boolean matchesText = searchText.isEmpty() ||
                                user.getUsername().toLowerCase().contains(searchText) ||
                                user.getFullName().toLowerCase().contains(searchText) ||
                                user.getEmail().toLowerCase().contains(searchText);

                        // 2. Filter by Role Selection
                        boolean matchesRole = selectedRole.equals("All Roles") ||
                                user.getRole().name().contains(selectedRole);

                        return matchesText && matchesRole;
                    });
                };

                adminSearchField.textProperty().addListener((obs, oldVal, newVal) -> filterTrigger.run());
                adminRoleFilterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTrigger.run());

                adminUsersTable.setItems(filteredUsers);
            });
        }).start();
    }

    @FXML
    private void handleToggleActive(ActionEvent event) {
        User selectedUser = adminUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "Please select a user account to toggle active status.");
            return;
        }

        if ("admin".equals(selectedUser.getUsername())) {
            showAlert("Security Alert", "Cannot deactivate the root system administrator account.");
            return;
        }

        selectedUser.setActive(!selectedUser.isActive());

        new Thread(() -> {
            userRepository.save(selectedUser);
            Platform.runLater(() -> {
                refreshAdminDashboard();
                showStatusMessage("Deactivated user access for: " + selectedUser.getUsername());
            });
        }).start();
    }

    @FXML
    private void handleDeleteUser(ActionEvent event) {
        User selectedUser = adminUsersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("No Selection", "Please select a user account to delete.");
            return;
        }

        if ("admin".equals(selectedUser.getUsername())) {
            showAlert("Security Alert", "Cannot delete the root system administrator account.");
            return;
        }

        // Custom confirm dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Delete User Account: " + selectedUser.getUsername());
        confirmAlert.setContentText("Warning: This action will permanently remove the user and all associated records. This cannot be undone. Do you wish to continue?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            new Thread(() -> {
                // Delete dependent entity records safely
                if (selectedUser.getRole() == Role.ROLE_PATIENT) {
                    Optional<Patient> pOpt = patientRepository.findByUserId(selectedUser.getId());
                    pOpt.ifPresent(patient -> {
                        // Safe cascade details if required, otherwise JPA takes care or we delete manually
                        patientRepository.delete(patient);
                    });
                } else if (selectedUser.getRole() == Role.ROLE_DOCTOR) {
                    Optional<Doctor> dOpt = doctorRepository.findByUserId(selectedUser.getId());
                    dOpt.ifPresent(doctorRepository::delete);
                }

                // Delete User Entity
                userRepository.delete(selectedUser);

                Platform.runLater(() -> {
                    refreshAdminDashboard();
                    showAlert("Deleted Successfully", "The user profile has been removed from the platform.");
                });
            }).start();
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showStatusMessage(String msg) {
        adminStatusLabel.setText(msg);
        adminStatusLabel.setVisible(true);

        // Hide after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                // Ignore
            }
            Platform.runLater(() -> adminStatusLabel.setVisible(false));
        }).start();
    }
}
