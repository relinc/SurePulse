package net.relinc.processor.controllers;

import java.net.URI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;
import net.relinc.processor.staticClasses.SPTracker;

public class AboutController {
	@FXML VBox aboutVbox;
	@FXML CheckBox enableTrackingCB;
	Stage stage;
	@FXML
	public void initialize(){
		enableTrackingCB.setSelected(SPTracker.initiallyEnabled);
		//String version = SPOperations.readStringFromFile("libs/surepulseversioninfo.txt");
		String versionNum = SPOperations.getDataProcessorVersion();
		
		if (versionNum != null) {
			Label label = new Label("SURE-Pulse Data Processor");
			label.paddingProperty().set(new Insets(10, 0, 0, 0));
			Label label2 = new Label("Version Number: " + versionNum);
			
			Label label2pt1 = SPOperations.getLatestDataProcessorVersionNumber() != null 
					 ? new Label("Latest Version Available: " + SPOperations.getLatestDataProcessorVersionNumber())
					 : null;
			if(!versionNum.equals(SPOperations.getLatestDataProcessorVersionNumber()) && label2pt1 != null)
				label2pt1.setTextFill(Color.RED);
			Label label3 = new Label("\u00a9 2015 REL Inc.");
			Label label4 = new Label("Phone:  1-906-337-3018");
			Label label5 = new Label("Email: rel@relinc.net");
			Hyperlink link = new Hyperlink("http://relinc.net");
			link.setBorder(Border.EMPTY);
			TextFlow flow = new TextFlow(new Text("Website: "), link);
			flow.textAlignmentProperty().set(TextAlignment.CENTER);
			link.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					try {
						openURL("http://relinc.net");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			int i = 1;
			aboutVbox.getChildren().add(i++, label);
			aboutVbox.getChildren().add(i++, label2);
			if(label2pt1 != null)
				aboutVbox.getChildren().add(i++, label2pt1);
			aboutVbox.getChildren().add(i++, label3);
			aboutVbox.getChildren().add(i++, label4);
			aboutVbox.getChildren().add(i++, label5);
			aboutVbox.getChildren().add(i++, flow);
		}
	}
	
	public void openURL(String url) throws Exception{
		 java.awt.Desktop.getDesktop().browse(new URI(url));
	}
	
	public void enableTrackingCBFired(){
		SPTracker.initiallyEnabled = enableTrackingCB.isSelected();
		SPTracker.setEnabled(SPTracker.initiallyEnabled);
		SPSettings.writeSPSettings();
	}
}
