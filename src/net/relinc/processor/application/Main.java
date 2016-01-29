package net.relinc.processor.application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.processor.controllers.SplashPageController;
import net.relinc.processor.pico.Pico5000;
import javafx.scene.Scene;

public class Main extends Application {

	
	@Override
	public void start(Stage primaryStage) {
		// String latestVersion =
		// SPOperations.getLatestDataProcessorVersionAvailable();
		// String currentVersion = "SUREPulse-" +
		// SPOperations.getDataProcessorVersion() + ".exe";
		// System.out.println("Latest Version: " + latestVersion);
		// System.out.println("Current Version: " + currentVersion);
		// System.out.println(currentVersion.equals(latestVersion));

		SPTracker.setEnabled(SPTracker.initiallyEnabled);
		SPTracker.track(SPTracker.surepulseProcessorCategory, "Launch");
		SPSettings.metricMode.set(false);
		// SPSettings.isMetric = false;
		if (!SPSettings.readSPSettings()) {
			// git
			// no setting, set to appdata.
			// SPSettings.Workspace = new
			// File(SPSettings.applicationSupportDirectory +
			// "/RELFX/SUREPulse");

		}

		SPOperations.prepareAppDataDirectory();
		if (SPSettings.Workspace != null) {
			if (!SPSettings.Workspace.exists()) {// they deleted their workspace
				SPSettings.Workspace = null;
			} else {
				SPOperations.prepareWorkingDirectory();
			}
		}

		try {
			// BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/Splashpage.fxml"));
			// Parent root =
			// FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());

			// Parent root =
			// FXMLLoader.load(getClass().getResource("/fxml/Splashpage.fxml"));
			// Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.getIcons().add(SPSettings.getRELLogo());
			primaryStage.setTitle("SURE-Pulse Data Processor");
			primaryStage.setScene(scene);
			SplashPageController c = root1.<SplashPageController> getController();
			c.renderGUI();
			c.stage = primaryStage;
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	

}
