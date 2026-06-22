package com.ivf.companion.controller;

import com.ivf.companion.config.UserSession;
import com.ivf.companion.dto.RecommendationRequest;
import com.ivf.companion.dto.RecommendationResponse;
import com.ivf.companion.model.Patient;
import com.ivf.companion.model.Role;
import com.ivf.companion.repository.PatientRepository;
import com.ivf.companion.service.AiEngineService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AiRecommendationsController {

    @Autowired
    private AiEngineService aiEngineService;

    @Autowired
    private UserSession userSession;

    @Autowired
    private PatientRepository patientRepository;

    // FXML Bindings - Form Inputs
    @FXML private TextField ageField;
    @FXML private TextField amhField;
    @FXML private TextField fshField;
    @FXML private TextField infertilityYearsField;
    @FXML private TextField prevCyclesField;
    @FXML private ComboBox<String> diagnosisComboBox;
    @FXML private Label formErrorLabel;

    // FXML Bindings - Outcomes
    @FXML private VBox outcomePlaceholder;
    @FXML private ScrollPane outcomeReportView;

    @FXML private Label recommendedMethodLabel;
    @FXML private Label successRateLabel;
    @FXML private Label successLikelihoodLabel;

    @FXML private VBox rationaleListContainer;
    @FXML private VBox medsListContainer;
    @FXML private VBox wellnessListContainer;

    @FXML
    public void initialize() {
        // Populate Diagnosis Combo Box
        diagnosisComboBox.getItems().addAll(
                "Unexplained Infertility",
                "Male Factor",
                "Tubal Factor",
                "Diminished Ovarian Reserve",
                "Endometriosis"
        );

        // Autofill if logged-in user is a patient
        if (userSession.getRole() == Role.ROLE_PATIENT) {
            new Thread(() -> {
                Optional<Patient> pOpt = patientRepository.findByUserId(userSession.getId());
                pOpt.ifPresent(p -> Platform.runLater(() -> {
                    ageField.setText(p.getAge() != null ? String.valueOf(p.getAge()) : "");
                    amhField.setText(p.getAmhLevel() != null ? String.valueOf(p.getAmhLevel()) : "");
                    fshField.setText(p.getFshLevel() != null ? String.valueOf(p.getFshLevel()) : "");
                    infertilityYearsField.setText("3"); // Default average seed
                    prevCyclesField.setText("0");
                    diagnosisComboBox.setValue("Endometriosis");
                }));
            }).start();
        }
    }

    @FXML
    private void quickFillSarah(ActionEvent event) {
        ageField.setText("32");
        amhField.setText("2.4");
        fshField.setText("6.5");
        infertilityYearsField.setText("3");
        prevCyclesField.setText("0");
        diagnosisComboBox.setValue("Endometriosis");
        formErrorLabel.setVisible(false);
    }

    @FXML
    private void handleAnalyze(ActionEvent event) {
        String ageStr = ageField.getText().trim();
        String amhStr = amhField.getText().trim();
        String fshStr = fshField.getText().trim();
        String infStr = infertilityYearsField.getText().trim();
        String prevStr = prevCyclesField.getText().trim();
        String dxStr = diagnosisComboBox.getValue();

        if (ageStr.isEmpty() || amhStr.isEmpty() || fshStr.isEmpty() || infStr.isEmpty() || prevStr.isEmpty() || dxStr == null) {
            showError("Please fill out all physiological and diagnosis parameters.");
            return;
        }

        formErrorLabel.setVisible(false);

        RecommendationRequest request = new RecommendationRequest();
        try {
            request.setAge(Integer.parseInt(ageStr));
            request.setAmhLevel(Double.parseDouble(amhStr));
            request.setFshLevel(Double.parseDouble(fshStr));
            request.setYearsInfertility(Integer.parseInt(infStr));
            request.setPrevCycles(Integer.parseInt(prevStr));
            
            // Map ComboBox string to standard diagnostic enums
            String dxEnum = "UNEXPLAINED";
            if (dxStr.contains("Male")) dxEnum = "MALE_FACTOR";
            else if (dxStr.contains("Tubal")) dxEnum = "TUBAL_FACTOR";
            else if (dxStr.contains("Reserve")) dxEnum = "MALE_FACTOR"; // Diminished reserve is checked in service
            else if (dxStr.contains("Endometriosis")) dxEnum = "ENDOMETRIOSIS";
            
            request.setDiagnosis(dxEnum);
        } catch (NumberFormatException nfe) {
            showError("Please enter valid numeric figures for age, hormone levels, years, and cycles.");
            return;
        }

        // Run analysis
        new Thread(() -> {
            try {
                RecommendationResponse response = aiEngineService.generateClinicalRecommendation(request);
                Platform.runLater(() -> renderRecommendationReport(response));
            } catch (Exception e) {
                Platform.runLater(() -> showError("Engine Error: Could not compute clinical outcome. " + e.getMessage()));
            }
        }).start();
    }

    private void renderRecommendationReport(RecommendationResponse response) {
        // Toggle view visibility
        outcomePlaceholder.setVisible(false);
        outcomeReportView.setVisible(true);

        // Header statistics
        recommendedMethodLabel.setText(response.getRecommendedProtocol());
        successRateLabel.setText(response.getConfidenceScore() + "%");
        
        String likelihood = response.getSuccessLikelihood().toUpperCase();
        successLikelihoodLabel.setText(likelihood + " PROBABILITY");
        
        // Remove old badge styles
        successLikelihoodLabel.getStyleClass().removeAll("badge", "badge-rose");
        if (likelihood.equals("HIGH")) {
            successLikelihoodLabel.getStyleClass().add("badge");
        } else {
            successLikelihoodLabel.getStyleClass().addAll("badge", "badge-rose");
        }

        // Render Rationale List
        populateVBoxList(rationaleListContainer, response.getRationalePoints(), "📋");

        // Render Medication Regimen
        populateVBoxList(medsListContainer, response.getRecommendedMeds(), "💊");

        // Render Wellness Tips
        populateVBoxList(wellnessListContainer, response.getWellnessTips(), "🧘");
    }

    private void populateVBoxList(VBox container, List<String> listItems, String bulletChar) {
        container.getChildren().clear();
        if (listItems == null || listItems.isEmpty()) {
            Label emptyLbl = new Label("No specific instructions provided.");
            emptyLbl.setStyle("-fx-text-fill: -text-muted; -fx-font-style: italic;");
            container.getChildren().add(emptyLbl);
            return;
        }

        for (String item : listItems) {
            Label label = new Label(bulletChar + "  " + item);
            label.setWrapText(true);
            label.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-padding: 3px 0;");
            container.getChildren().add(label);
        }
    }

    private void showError(String msg) {
        formErrorLabel.setText(msg);
        formErrorLabel.setVisible(true);
    }
}
