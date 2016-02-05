package net.relinc.processor.controllers;


import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.relinc.viewer.GUI.HomeController;
import net.relinc.viewer.application.AnalyzeMain;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.processor.controllers.CalibrationController.BarSetupMode;

public class SplashPageController {
	@FXML Label workspaceLabel;
	@FXML ImageView surePulseLogoImageView;
	public Stage stage;

	@FXML
	public void initialize(){
//		if(keyLockEnabled){
//			try {
//				if(!KeyLock.checkForKeyLock()) {
//					Dialogs.showErrorDialog("Cannot read KeyLock usb dongle","Please insert Keylok usb", "If this problem persists contact REL Inc.",stage);
//					Platform.exit();
//					return;
//				} 
//			} catch(Error e) {
//				Dialogs.showErrorDialog("Error Reading Keylok", "Could not find native Keylok library", "Please contact REL Inc.",stage);
//			}
//			ExpirationResult expirationResult = KeyLock.checkLeaseExpiration();
//
//			if(!expirationResult.result) {
//				Dialogs.showErrorDialog("SUREPulse License",null, expirationResult.message,stage);
//				Platform.exit();
//				return;
//			}	
//			Dialogs.showInformationDialog("SUREPulse License",null, expirationResult.message,stage);
//		}
		surePulseLogoImageView.setImage(SPSettings.getSurePulseLogo());
		
		if(SPSettings.Workspace == null){
			Dialogs.showAlert("By default, SURE-Pulse sends some simple application usage \n" + 
				"statistics. You can turn this off by clicking the \"About Program\" button on the home screen." , stage);
		}
	}

	public void buttonPressed(){
		if(SPSettings.Workspace == null || !SPSettings.Workspace.exists()){
			Dialogs.showAlert("Please select working directory",stage);
			return;
		}

		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/Calibration.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			CalibrationController c = root1.<CalibrationController>getController();
			c.stage = anotherStage;
			c.barSetupMode = BarSetupMode.EDIT;
			c.refresh();
			c.createRefreshListener();

			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@FXML
	public void newSampleFired(){
		if(SPSettings.Workspace == null || !SPSettings.Workspace.exists()){
			Dialogs.showAlert("Please select working directory",stage);
			return;
		}
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/CreateNewSample.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			CreateNewSampleController c = root1.<CreateNewSampleController>getController();
			c.parent = this;
			c.stage = anotherStage;
			c.createRefreshListener();

			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@FXML
	public void changeWorkspaceButtonFired(){
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if(SPSettings.Workspace != null && SPSettings.Workspace.exists())
			directoryChooser.setInitialDirectory(SPSettings.Workspace);
		File selectedDirectory = 
				directoryChooser.showDialog((Stage) workspaceLabel.getScene().getWindow());

		if (selectedDirectory == null) {

		} else {
			boolean existingDirectory = false;
			for(File f : selectedDirectory.listFiles())
				if(f.getName().equals("Sample Data"))
					existingDirectory = true;
			if(selectedDirectory.listFiles().length > 0 && !existingDirectory){
			Stage anotherStage = new Stage();
			try {
				FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/WorkingDirectoryPrompt.fxml"));
				// Parent root =
				// FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
				Scene scene = new Scene(root1.load());
				scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
				anotherStage.getIcons().add(SPSettings.getRELLogo());
				anotherStage.setTitle("SURE-Pulse Data Processor");
				anotherStage.setScene(scene);
				WorkingDirectoryPromptController c = root1.<WorkingDirectoryPromptController> getController();
				c.parent = this;
				c.workingDirectory = selectedDirectory;
				// c.stage = anotherStage;
				// c.createRefreshListener();

				anotherStage.showAndWait();
				selectedDirectory = c.workingDirectory;
			} catch (Exception e) {

			}
			}
			
			
			
			
			SPSettings.Workspace = new File(selectedDirectory.getPath());
			SPOperations.prepareWorkingDirectory();
			SPSettings.writeSPSettings();
			renderGUI();
			//SPTracker.track(new FocusPoint("Working Directory Created"));
		}
	}
	public void renderGUI(){
		if(SPSettings.Workspace == null || !SPSettings.Workspace.exists()){
			workspaceLabel.setText("No Workspace Selected");
		}
		else{
			workspaceLabel.setText("Workspace: " + SPSettings.Workspace.getPath());
		}
	}
	@FXML
	public void analyzeResultsButtonFired(){
		if(SPSettings.Workspace == null || !SPSettings.Workspace.exists()){
			Dialogs.showAlert("Please select working directory",stage);
			return;
		}
		//try {
			new AnalyzeMain().start(new Stage());
//			Stage primaryStage = new Stage();
//			FXMLLoader root = new FXMLLoader(new HomeController().getClass().getResource("/net/relinc/viewer/GUI/Home.fxml"));
//			Scene scene = new Scene(root.load());
//			//scene.getStylesheets().add(getClass().getResource("dicapplication.css").toExternalForm());
//			primaryStage.setScene(scene);
//			primaryStage.showAndWait();
			
//			if(!SPOperations.launchSureAnalyze(stage,new HomeController().getClass().getResource("/net/relinc/viewer/GUI/Home.fxml")))
//				Dialogs.showErrorDialog("Error Launching SURE-Pulse Viewer", "SURE-Pulse Viewer has either been moved or does not exist on this machine", "Please install SURE-Pulse Viewer, contact REL Inc if the problem persists",stage);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	public void aboutProgramButtonFired() {
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/About.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			AboutController c = root1.<AboutController>getController();
			c.stage = anotherStage;
			anotherStage.initModality(Modality.WINDOW_MODAL);
		    anotherStage.initOwner(
		        (stage.getScene().getWindow()));

			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
