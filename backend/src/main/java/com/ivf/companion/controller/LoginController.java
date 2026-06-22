package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.dto.JwtAuthenticationResponse;
import com.ivf.companion.dto.LoginRequest;
import com.ivf.companion.dto.RegisterRequest;
import com.ivf.companion.model.Role;
import com.ivf.companion.service.AuthService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.time.LocalDate;

@Component
public class LoginController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private ApplicationContext applicationContext;

    // Login Elements
    @FXML private VBox loginPane;
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginErrorLabel;
    @FXML private Button loginBtn;

    // Registration Elements
    @FXML private ScrollPane registerPane;
    @FXML private TextField regUsernameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regFullNameField;
    @FXML private PasswordField regPasswordField;
    @FXML private ComboBox<String> regRoleComboBox;
    @FXML private Label regErrorLabel;
    @FXML private Button registerBtn;

    // Dynamic fields containers
    @FXML private VBox patientFieldsContainer;
    @FXML private VBox doctorFieldsContainer;

    // Patient Fields
    @FXML private DatePicker patientDobPicker;
    @FXML private TextField patientAmhField;
    @FXML private TextField patientFshField;
    @FXML private TextArea patientHistoryArea;

    // Doctor Fields
    @FXML private TextField docLicenseField;
    @FXML private TextField docSpecializationField;
    @FXML private TextField docClinicField;

    @FXML
    public void initialize() {
        // Setup registration role selector
        regRoleComboBox.getItems().addAll("Patient", "Doctor", "Admin");

        // Bind child form visibilities responsively
        patientFieldsContainer.visibleProperty().bind(regRoleComboBox.valueProperty().isEqualTo("Patient"));
        patientFieldsContainer.managedProperty().bind(regRoleComboBox.valueProperty().isEqualTo("Patient"));

        doctorFieldsContainer.visibleProperty().bind(regRoleComboBox.valueProperty().isEqualTo("Doctor"));
        doctorFieldsContainer.managedProperty().bind(regRoleComboBox.valueProperty().isEqualTo("Doctor"));
    }

    @FXML
    private void toggleViewMode(ActionEvent event) {
        boolean isLoginShowing = loginPane.isVisible();
        loginPane.setVisible(!isLoginShowing);
        registerPane.setVisible(isLoginShowing);
        
        // Clear errors
        loginErrorLabel.setVisible(false);
        regErrorLabel.setVisible(false);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = loginUsernameField.getText().trim();
        String password = loginPasswordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginError("Username and password are required.");
            return;
        }

        loginBtn.setDisable(true);
        loginBtn.setText("Signing In...");
        loginErrorLabel.setVisible(false);

        // Run authentication in background thread to keep UI interactive
        new Thread(() -> {
            try {
                JwtAuthenticationResponse response = authService.login(new LoginRequest(username, password));
                
                // Store in UserSession
                userSession.setId(response.getId());
                userSession.setUsername(response.getUsername());
                userSession.setFullName(response.getFullName());
                userSession.setRole(Role.valueOf(response.getRole()));
                userSession.setToken(response.getAccessToken());

                // Transition screen in FX Application thread
                Platform.runLater(this::navigateToMainDashboard);
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showLoginError("Invalid username or password. Please try again.");
                    loginBtn.setDisable(false);
                    loginBtn.setText("Sign In");
                });
            }
        }).start();
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String fullName = regFullNameField.getText().trim();
        String password = regPasswordField.getText();
        String roleStr = regRoleComboBox.getValue();

        if (username.isEmpty() || email.isEmpty() || fullName.isEmpty() || password.isEmpty() || roleStr == null) {
            showRegError("Please fill out all basic registration fields.");
            return;
        }

        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setEmail(email);
        request.setFullName(fullName);
        request.setPassword(password);
        request.setRole(roleStr.toLowerCase());

        // Fill specific role fields
        if ("Patient".equals(roleStr)) {
            LocalDate dob = patientDobPicker.getValue();
            if (dob == null) {
                showRegError("Date of Birth is required for patient profiles.");
                return;
            }
            request.setDateOfBirth(dob);

            try {
                if (!patientAmhField.getText().isEmpty()) {
                    request.setAmhLevel(Double.parseDouble(patientAmhField.getText().trim()));
                }
                if (!patientFshField.getText().isEmpty()) {
                    request.setFshLevel(Double.parseDouble(patientFshField.getText().trim()));
                }
            } catch (NumberFormatException e) {
                showRegError("AMH and FSH levels must be valid decimals.");
                return;
            }
            request.setHistory(patientHistoryArea.getText().trim());

        } else if ("Doctor".equals(roleStr)) {
            String license = docLicenseField.getText().trim();
            if (license.isEmpty()) {
                showRegError("Medical license number is required for doctor accounts.");
                return;
            }
            request.setLicenseNumber(license);
            request.setSpecialization(docSpecializationField.getText().trim());
            request.setClinicName(docClinicField.getText().trim());
        }

        registerBtn.setDisable(true);
        registerBtn.setText("Registering...");
        regErrorLabel.setVisible(false);

        new Thread(() -> {
            try {
                authService.register(request);
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registration Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Your account has been registered successfully! You can now log in.");
                    alert.showAndWait();
                    
                    // Switch to login view
                    toggleViewMode(null);
                    loginUsernameField.setText(username);
                    loginPasswordField.clear();
                    
                    // Restore button state
                    registerBtn.setDisable(false);
                    registerBtn.setText("Register Account");
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showRegError(ex.getMessage() != null ? ex.getMessage() : "Registration failed. Try a different username/email.");
                    registerBtn.setDisable(false);
                    registerBtn.setText("Register Account");
                });
            }
        }).start();
    }

    @FXML
    private void quickLoginPatient(ActionEvent event) {
        loginUsernameField.setText("patient");
        loginPasswordField.setText("patient123");
        handleLogin(null);
    }

    @FXML
    private void quickLoginDoctor(ActionEvent event) {
        loginUsernameField.setText("doctor");
        loginPasswordField.setText("doctor123");
        handleLogin(null);
    }

    @FXML
    private void quickLoginAdmin(ActionEvent event) {
        loginUsernameField.setText("admin");
        loginPasswordField.setText("admin123");
        handleLogin(null);
    }

    private void showLoginError(String message) {
        loginErrorLabel.setText(message);
        loginErrorLabel.setVisible(true);
    }

    private void showRegError(String message) {
        regErrorLabel.setText(message);
        regErrorLabel.setVisible(true);
    }

    private void navigateToMainDashboard() {
        try {
            Stage stage = (Stage) loginBtn.getScene().getWindow();
            
            URL url = getClass().getResource("/fxml/MainView.fxml");
            if (url == null) {
                throw new RuntimeException("Could not find /fxml/MainView.fxml");
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            
            Scene scene = new Scene(parent, 1200, 780);
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setResizable(true);
            stage.setMinWidth(1100);
            stage.setMinHeight(700);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Could not load Main View");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            loginBtn.setDisable(false);
            loginBtn.setText("Sign In");
        }
    }
}
