package net.relinc.processor.staticClasses;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;

public final class Dialogs {
	
	public static void showAlert(String message, Stage parentStage){
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle("Alert");
		alert.setHeaderText(message);
		alert.showAndWait();
	}
	
	public static void showInformationDialogNoStage(String title, String header, String content) {
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		//stage.initOwner(stage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	public static void showInformationDialog(String title, String header, String content, Stage parentStage) {
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	
	public static Alert getInformationDialog(String title, String header, String content, Stage parentStage) {
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		return alert;
	}
//	public static void showInformationDialogFullScreen(String title, String header, String content,Window owner) {
//		Alert alert = new Alert(AlertType.INFORMATION);
//		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//		stage.getIcons().add(new Image("/images/rel-logo.png"));
//		stage.initOwner(owner);
//		stage.toFront();
//		alert.setTitle(title);
//		alert.setHeaderText(header);
//		alert.setContentText(content);
//		alert.showAndWait();
//	}
	
	public static void showErrorDialog(String title, String header, String content, Stage parentStage) {
		Alert alert = new Alert(AlertType.ERROR);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
	
	public static boolean showConfirmationDialog(String title, String headerText, String content, Stage parentStage) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(content);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
		    return true;
		} else {
		    return false;
		}
	}
}
