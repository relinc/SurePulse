package net.relinc.viewer.GUI;

import java.util.HashMap;
import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.relinc.libraries.fxControls.NumberTextField;

public class DataReducerDialog {
	static Map<String, Number> showDataReducerDialog() {
		Stage stage = new Stage();
		Label label = new Label("Reduce Data");
		NumberTextField tf = new NumberTextField("points", "hertz");
		Button button = new Button("Done");
		button.setDefaultButton(true);
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				stage.close();
			}
		});
		VBox box = new VBox();
		
		box.getChildren().add(label);
		RadioButton pointsToKeepMode = new RadioButton("Points To Keep");
		pointsToKeepMode.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if(newValue) {
					tf.unitLabel.setText("points");
				} else {
					tf.unitLabel.setText("hertz");
				}
			}
		});
		pointsToKeepMode.setSelected(true);
		RadioButton frequencyMode = new RadioButton("Frequency");
		ToggleGroup group = new ToggleGroup();
		pointsToKeepMode.setToggleGroup(group);
		frequencyMode.setToggleGroup(group);
		
		HBox hBox = new HBox();
		hBox.getChildren().add(pointsToKeepMode);
		hBox.getChildren().add(frequencyMode);
		hBox.setAlignment(Pos.CENTER);
		hBox.setSpacing(5);
		box.getChildren().add(hBox);
		GridPane view = new GridPane();
		view.add(tf, 0, 0);
		view.add(tf.unitLabel, 0, 0);
		view.setAlignment(Pos.CENTER);
		box.getChildren().add(view);
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
		
		stage.setScene(scene);
		stage.showAndWait();
		Map<String, Number> ret = new HashMap<String, Number>();
		if(pointsToKeepMode.isSelected()) {
			ret.put("pointsToKeep", Integer.parseInt(tf.getText().replaceAll(",", "")));
		} else {
			ret.put("frequency", Double.parseDouble(tf.getText().replaceAll(",", "")));
		}
		return ret;
	}
}
