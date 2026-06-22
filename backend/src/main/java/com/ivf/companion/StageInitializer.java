package com.ivf.companion;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.net.URL;

@Component
public class StageInitializer implements ApplicationListener<IvfCompanionApp.StageReadyEvent> {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(IvfCompanionApp.StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            URL url = getClass().getResource("/fxml/LoginView.fxml");
            if (url == null) {
                throw new RuntimeException("Could not find /fxml/LoginView.fxml. Ensure it is placed in src/main/resources/fxml/LoginView.fxml");
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
            stage.setTitle("IVF Companion Desktop");
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load Login FXML", e);
        }
    }
}
