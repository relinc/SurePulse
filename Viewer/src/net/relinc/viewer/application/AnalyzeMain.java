package net.relinc.viewer.application;
//here is a change
import java.io.File;

import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.viewer.GUI.CommonGUI;
import net.relinc.viewer.GUI.HomeController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;


public class AnalyzeMain extends Application {
	@Override
	public void start(Stage primaryStage) {
		try {
			SPTracker.setEnabled(SPTracker.initiallyEnabled);
			SPTracker.track(SPTracker.surepulseViewerCategory, "Launch");
			if(!SPSettings.readSPSettings()){
				//no setting, set to appdata.
				SPSettings.Workspace = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse");
			}

			SPOperations.prepareAppDataDirectory();
			SPOperations.prepareWorkingDirectory();
			
			CommonGUI.initCommon();

			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/viewer/GUI/Home.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.getIcons().add(SPSettings.getRELLogo());
			primaryStage.setScene(scene);
			primaryStage.setTitle("Sure-Pulse Viewer");
			HomeController c = root1.<HomeController>getController();
			CommonGUI.stage = primaryStage;
			
			c.parameters = this.getParameters() == null ? null : this.getParameters().getRaw();

			primaryStage.show();


			c.createRefreshListener();
			c.createArrowKeyListener();

			c.showInitialOptions();


			SPSettings.globalDisplacementDataLowpassFilter = null;
			SPSettings.globalLoadDataLowpassFilter = null;

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
