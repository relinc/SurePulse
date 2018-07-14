package net.relinc.processor.controllers;


import java.io.File;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class WorkingDirectoryPromptController {
	@FXML TextField directoryNameTF;
	public SplashPageController parent;
	public File workingDirectory;
	
	@FXML
	public void createNewDirectoryButtonFired(){
		File newDir = new File(workingDirectory.getPath() + "/" + directoryNameTF.getText());
		newDir.mkdir();
		workingDirectory = newDir;
		Stage stage = (Stage) directoryNameTF.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void useSelectedDirectoryFired(){
		Stage stage = (Stage) directoryNameTF.getScene().getWindow();
		stage.close();
	}
	
	@FXML
	public void cancelButtonFired(){
		workingDirectory = null;
		Stage stage = (Stage) directoryNameTF.getScene().getWindow();
		stage.close();
	}
	
}
