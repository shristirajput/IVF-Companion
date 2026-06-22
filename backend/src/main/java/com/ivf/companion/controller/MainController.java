package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.model.Role;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

@Component
public class MainController {

    @Autowired
    private UserSession userSession;

    @Autowired
    private ApplicationContext applicationContext;

    @FXML private Label userNameLabel;
    @FXML private Label userRoleBadge;

    @FXML private Button btnPatientDash;
    @FXML private Button btnDoctorDash;
    @FXML private Button btnAdminDash;
    @FXML private Button btnRecommendations;
    @FXML private Button btnAppointments;
    @FXML private Button btnCommunity;
    @FXML private Button btnLogout;

    @FXML private StackPane contentArea;

    private List<Button> navButtons;

    @FXML
    public void initialize() {
        navButtons = Arrays.asList(btnPatientDash, btnDoctorDash, btnAdminDash, btnRecommendations, btnAppointments, btnCommunity);

        // Populate User Info
        userNameLabel.setText(userSession.getFullName());
        
        Role role = userSession.getRole();
        if (role == Role.ROLE_PATIENT) {
            userRoleBadge.setText("PATIENT");
            userRoleBadge.getStyleClass().add("badge");
            
            // Hide Doctor and Admin features
            btnDoctorDash.setVisible(false);
            btnDoctorDash.setManaged(false);
            btnAdminDash.setVisible(false);
            btnAdminDash.setManaged(false);
            
            // Default View
            showPatientDashboard(null);

        } else if (role == Role.ROLE_DOCTOR) {
            userRoleBadge.setText("DOCTOR");
            userRoleBadge.getStyleClass().addAll("badge", "badge-rose");
            
            // Hide Patient and Admin features
            btnPatientDash.setVisible(false);
            btnPatientDash.setManaged(false);
            btnAdminDash.setVisible(false);
            btnAdminDash.setManaged(false);
            
            // Default View
            showDoctorDashboard(null);

        } else if (role == Role.ROLE_ADMIN) {
            userRoleBadge.setText("SYSTEM ADMINISTRATOR");
            userRoleBadge.getStyleClass().addAll("badge", "badge-rose");
            
            // Hide Patient features, show both Doc & Admin
            btnPatientDash.setVisible(false);
            btnPatientDash.setManaged(false);
            
            // Default View
            showAdminDashboard(null);
        }
    }

    @FXML
    private void showPatientDashboard(ActionEvent event) {
        loadSubView("/fxml/PatientDashboardView.fxml", btnPatientDash);
    }

    @FXML
    private void showDoctorDashboard(ActionEvent event) {
        loadSubView("/fxml/DoctorDashboardView.fxml", btnDoctorDash);
    }

    @FXML
    private void showAdminDashboard(ActionEvent event) {
        loadSubView("/fxml/AdminDashboardView.fxml", btnAdminDash);
    }

    @FXML
    private void showRecommendations(ActionEvent event) {
        loadSubView("/fxml/AiRecommendationsView.fxml", btnRecommendations);
    }

    @FXML
    private void showAppointments(ActionEvent event) {
        loadSubView("/fxml/AppointmentsView.fxml", btnAppointments);
    }

    @FXML
    private void showCommunity(ActionEvent event) {
        loadSubView("/fxml/CommunityView.fxml", btnCommunity);
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        try {
            // Clean up session context
            userSession.cleanUserSession();
            
            Stage stage = (Stage) btnLogout.getScene().getWindow();
            URL url = getClass().getResource("/fxml/LoginView.fxml");
            if (url == null) {
                throw new RuntimeException("Could not find /fxml/LoginView.fxml");
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent parent = fxmlLoader.load();
            
            Scene scene = new Scene(parent, 1100, 700);
            URL cssUrl = getClass().getResource("/css/styles.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("System Error");
            alert.setHeaderText("Logout Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void loadSubView(String fxmlPath, Button activeButton) {
        try {
            URL url = getClass().getResource(fxmlPath);
            if (url == null) {
                throw new RuntimeException("Could not find view file: " + fxmlPath);
            }
            
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent subView = fxmlLoader.load();
            
            // Set in StackPane
            contentArea.getChildren().setAll(subView);
            
            // Update Navigation Button States
            for (Button btn : navButtons) {
                btn.getStyleClass().remove("nav-btn-active");
            }
            if (activeButton != null) {
                activeButton.getStyleClass().add("nav-btn-active");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Dashboard Error");
            alert.setHeaderText("Could not load view: " + fxmlPath);
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
