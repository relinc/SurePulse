package net.relinc.libraries.staticClasses;

import java.awt.Desktop;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

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
	
	public static void showAlertNoWait(String message, Stage parentStage){
		Alert alert = new Alert(AlertType.INFORMATION);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle("Alert");
		alert.setHeaderText(message);
		alert.show();
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
	
	private static TextField getValueFromUser(String prompt, Optional<String> units)
	{
		Stage anotherStage = new Stage();
		Label label = new Label(prompt);
		TextField tf = new TextField();
		if(units.isPresent())
			tf.setPromptText(units.get()); //TODO: Moving Label
		Button button = new Button("Done");
		button.setDefaultButton(true);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				anotherStage.close();
			}
		});
		VBox box = new VBox();
		box.getChildren().add(label);
		box.getChildren().add(tf);
		box.getChildren().add(button);
		box.setSpacing(15);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(10.0));
		AnchorPane anchor = new AnchorPane();
		AnchorPane.setBottomAnchor(box, 0.0);
		AnchorPane.setTopAnchor(box, 0.0);
		AnchorPane.setLeftAnchor(box, 0.0);
		AnchorPane.setRightAnchor(box, 0.0);
		anchor.getChildren().add(box);
		Scene scene = new Scene(anchor, 400, 220);
		
		anotherStage.setScene(scene);
		anotherStage.showAndWait();
		return tf;
	}
	
	public static double getDoubleValueFromUser(String prompt, String units){
		TextField tf = getValueFromUser(prompt, Optional.of(units));
		return Double.parseDouble(tf.getText().replaceAll(",", ""));
	}
	
	public static int getIntValueFromUser(String prompt, String units) {
		TextField tf = getValueFromUser(prompt, Optional.of(units));
		return Integer.parseInt(tf.getText().replaceAll(",", ""));
	}
	
	public static String getStringValueFromUser(String prompt){
		TextField tf = getValueFromUser(prompt, Optional.empty());
		return tf.getText();
	}
	
	public static boolean showOverwriteDialog(Stage parentStage, String title, String headerText, String content)
	{
		ButtonType okButton = new ButtonType("Ok", ButtonData.OK_DONE);
		ButtonType overwriteButton = new ButtonType("Overwrite", ButtonData.CANCEL_CLOSE);
		Alert alert = new Alert(AlertType.WARNING,content, okButton, overwriteButton);
		Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
		stage.getIcons().add(SPSettings.getRELLogo());
		stage.initOwner(parentStage);
		stage.initModality(Modality.WINDOW_MODAL);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(content);

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == okButton){
		    return true;
		} else {
		    return false;
		}
  }
  public static void showCitationDialog()
	{
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Cite SURE-Pulse");
		ButtonType loginButtonType = new ButtonType("Done", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(loginButtonType);
		VBox box = new VBox();
		box.setPadding(new Insets(10, 10, 10, 10));
		TextArea tf = new TextArea();
		String citation = String.join("\n", "@misc{SUREPulse,",
				"  Author = {{REL Inc.}},",
				"  title = {{SURE-Pulse}},",
				"  year = {2015},,",
				"  publisher = {GitHub},",
				"  journal = {GitHub repository},",
				"  howpublished = {\\url{https://github.com/relinc/SurePulseDataProcessor}},",
				"  commit = {4f57d6a0e4c030202a07a60bc1bb1ed1544bf679} ",
				"} ");
		tf.setText(citation);
		tf.setPadding(new Insets(2, 2, 2, 2));
		TextField tf2 = new TextField();
		tf2.setText("REL Inc. SURE-Pulse. https://github.com/relinc/SurePulseDataProcessor, 2015.");
		box.getChildren().add(new Label("BibTex:"));
		box.getChildren().add(tf);
		box.getChildren().add(new Label("Plain Text:"));
		box.getChildren().add(tf2);
		box.setSpacing(5);
		dialog.getDialogPane().setContent(box);
		Platform.runLater(() -> tf.requestFocus());
		Platform.runLater(() -> tf.selectAll());
		dialog.showAndWait();
	}
	
	public static void showTutorialDialog()
	{
		Dialog<String> dialog = new Dialog<>();
		dialog.setTitle("Tutorials");
		ButtonType loginButtonType = new ButtonType("Done", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().add(loginButtonType);
		VBox box = new VBox();
		box.setPadding(new Insets(10, 10, 10, 10));
		Hyperlink link = new Hyperlink("All Tutorials"); 
		link.setOnAction((a) -> {
			try {
			    Desktop.getDesktop().browse(new URL("https://www.youtube.com/playlist?list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn").toURI());
			} catch (Exception e) {}
		});
		box.getChildren().add(link);
		
		List<Pair<String, String>> tutorials = Stream.of(
				new Pair<String, String>("Creating A Bar Setup", "https://www.youtube.com/watch?v=-MRIdux2lwg&index=2&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn"),
				new Pair<String, String>("Create A Sample", "https://www.youtube.com/watch?v=7iM5QEZqimI&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn&index=3"),
				new Pair<String, String>("Create More Samples", "https://www.youtube.com/watch?v=8mvToXCw0-A&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn&index=4"),
				new Pair<String, String>("Analyze Sample", "https://www.youtube.com/watch?v=FATjMCsCoqA&index=5&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn"),
				new Pair<String, String>("Analyze Multiple Samples", "https://www.youtube.com/watch?v=fcu42G_mzwg&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn&index=6"),
				new Pair<String, String>("Creating A Session", "https://www.youtube.com/watch?v=iYujffKy7HY&index=7&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn"),
				new Pair<String, String>("Saving A Workspace", "https://www.youtube.com/watch?v=oZr071pilps&index=8&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn"),
				new Pair<String, String>("Run DIC (Digital Image Correlation)", "https://www.youtube.com/watch?v=kNDvzftBetY&index=9&list=PLeFdq4ZC_eAttrgIYoYX5FIPbQCaKxPBn")
				).collect(Collectors.toList());
		
		box.getChildren().add(getTutorialList(tutorials));
		
		dialog.getDialogPane().setContent(box);
		dialog.showAndWait();
	}
	
	private static GridPane getTutorialList(List<Pair<String, String>> tutorials)
	{
		GridPane gridPane = new GridPane();
		
		for(int i = 0; i < tutorials.size(); i++)
		{
			final int ii = i;
			Hyperlink link1 = new Hyperlink(tutorials.get(i).getKey()); 
			link1.setOnAction((a) -> {
				try {
				    Desktop.getDesktop().browse(new URL(tutorials.get(ii).getValue()).toURI());
				} catch (Exception e) {}
			});
			
			gridPane.add(new Label((i + 1) + ". "), 0, i + 1);
			gridPane.add(link1, 1, i + 1);
		}
		
		return gridPane;
	}
}

