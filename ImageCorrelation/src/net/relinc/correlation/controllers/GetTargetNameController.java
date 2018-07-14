package net.relinc.correlation.controllers;


import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import net.relinc.correlation.application.Target;

public class GetTargetNameController {

	Stage stage;
	@FXML public TextField targetNameTextField;
	public Target target;
	
	public void initialize(){
	}
	
	@FXML
	public void cancelButtonFired(){
		Stage stage = (Stage) targetNameTextField.getScene().getWindow();
	    // do what you have to do
	    stage.close();
	}
	
	@FXML
	public void acceptButtonFired(){
		if(targetNameTextField.getText().equals(""))
			return;
		target.setName(targetNameTextField.getText());
		// get a handle to the stage
	    Stage stage = (Stage) targetNameTextField.getScene().getWindow();
	    // do what you have to do
	    stage.close();
	}
}
