package net.relinc.processor.controllers;


import java.io.File;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import net.relinc.viewer.application.AnalyzeMain;
import net.relinc.libraries.sample.TensionRectangularSample;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.processor.controllers.CalibrationController.BarSetupMode;
import net.relinc.shotcaller.application.ShotCallerMain;

public class SplashPageController {
	@FXML Label workspaceLabel;
	@FXML ImageView surePulseLogoImageView;
	@FXML Button barSetupButton;
	@FXML Button newSampleButton;
	@FXML Button analyzeResultsButton;
	public Stage stage;

	@FXML
	public void initialize(){
		surePulseLogoImageView.setImage(SPSettings.getSurePulseLogo());
		barSetupButton.setGraphic(SPOperations.getIcon("/net/relinc/processor/images/barSetup.png", 25));
		newSampleButton.setGraphic(SPOperations.getIcon(TensionRectangularSample.getSampleConstants().getIconLocation(), 25));
		analyzeResultsButton.setGraphic(SPOperations.getIcon("/net/relinc/processor/images/viewerIcon.png", 25));
	}

	@FXML
	public void howToSHPBButtonClicked() {
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/controllers/HowToSHPB.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			HowToSHPBController c = root1.<HowToSHPBController>getController();
			c.stage = anotherStage;
			c.addListeners();

			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showFirstMessageDialog(Stage stage){
		if(SPSettings.Workspace == null){
			Dialogs.showAlertNoWait("By default, SURE-Pulse sends some simple application usage \n" + 
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

			anotherStage.initModality(Modality.APPLICATION_MODAL);


			anotherStage.showAndWait();
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
			c.refreshSamples();
			

			Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
			if(primaryScreenBounds.getHeight() < anotherStage.getHeight()){
				anotherStage.setY(primaryScreenBounds.getMinY());
				anotherStage.setHeight(primaryScreenBounds.getHeight());
				anotherStage.setX((primaryScreenBounds.getWidth() - anotherStage.getWidth())/2.0);
			}

			anotherStage.initModality(Modality.APPLICATION_MODAL);
			anotherStage.showAndWait();


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
				Scene scene = new Scene(root1.load());
				scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
				anotherStage.getIcons().add(SPSettings.getRELLogo());
				anotherStage.setTitle("SURE-Pulse Data Processor");
				anotherStage.setScene(scene);
				WorkingDirectoryPromptController c = root1.<WorkingDirectoryPromptController> getController();
				c.parent = this;
				c.workingDirectory = selectedDirectory;

				anotherStage.showAndWait();
				selectedDirectory = c.workingDirectory;
			} catch (Exception e) {

				}
			}
			if(selectedDirectory != null)
			{
				SPSettings.Workspace = new File(selectedDirectory.getPath());
				SPOperations.prepareWorkingDirectory();
				SPSettings.writeSPSettings();
			}
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
		// call that does maximise, stolen from DataProcessor/src/net/relinc/processor/controllers/CreateNewSampleController.java
		new AnalyzeMain().start(new Stage());
		//call that doesnt maximize
		//if(SPSettings.Workspace == null || !SPSettings.Workspace.exists()){
		//	Dialogs.showAlert("Please select working directory",stage);
		//	return;
		//}
		//Stage anotherStage = new Stage();
		//anotherStage.initModality(Modality.APPLICATION_MODAL);

		//new AnalyzeMain().start(anotherStage);
	}
	
	@FXML
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
	
	@FXML
	public void shotCallerButtonFired(){
		new ShotCallerMain().start(new Stage());
	}
	
	@FXML
	public void citeButtonFired(){
		Dialogs.showCitationDialog();
	}
	
	@FXML
	public void tutorialsButtonFired()
	{
		Dialogs.showTutorialDialog();
	}
}
