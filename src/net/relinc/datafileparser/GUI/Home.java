package net.relinc.datafileparser.GUI;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.relinc.datafileparser.application.Model;

public class Home {
	
	Stage stage;
	Model model;
	File selectedFile;
	TableView<List<String>> tableView;
	
	// Frame parameter controls
	RadioButton frameNewlineRadioButton = new RadioButton("New Line");
	RadioButton frameCustomRadioButton = new RadioButton("Custom");
	ToggleGroup frameGroup = new ToggleGroup();
	TextField frameCustomTextField = new TextField();
	TextField frameStartOffsetTextField = new TextField();
	TextField frameEndOffsetTextField = new TextField();
	
	public Home(Stage stage)
	{
		this.stage = stage;
		model = new Model("\n", ",");
		try {
			AnchorPane root = new AnchorPane();
			createWidget(root);
			render();
			Scene scene = new Scene(root);//, dims.getWidth(), dims.getHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	} //Home

	private void createWidget(AnchorPane pane) {
		VBox controlsVBox = new VBox();
		controlsVBox.setAlignment(Pos.TOP_CENTER);
		controlsVBox.setSpacing(15);
		controlsVBox.setPadding(new Insets(10, 10, 10, 10));
		
		Button loadButton = new Button("Load File");
		loadButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FileChooser chooser = new FileChooser();
				File f = chooser.showOpenDialog(stage);
				if(f != null){
					selectedFile = f;
					render();
				}
			}
		});
		controlsVBox.getChildren().add(loadButton);
		
		VBox frameControlsVBox = new VBox();
		frameControlsVBox.getStyleClass().add("frameControlsVBox");
		Label frameLabel = new Label("Frame/Row");
		frameLabel.getStyleClass().add("big-label");
		frameControlsVBox.getChildren().add(frameLabel);
		HBox frameDelimiterHBox = new HBox();
		frameDelimiterHBox.getStyleClass().add("frameDelimiterHBox");
		frameDelimiterHBox.getChildren().add(new Label("Delimiter:"));
		frameDelimiterHBox.getChildren().add(frameNewlineRadioButton);
		
		HBox customFrameDelimiter = new HBox();
		customFrameDelimiter.getStyleClass().add("customFrameDelimiter");
		customFrameDelimiter.getChildren().add(frameCustomRadioButton);
		frameCustomTextField.getStyleClass().add("small-textfield");
		customFrameDelimiter.getChildren().add(frameCustomTextField);
		frameNewlineRadioButton.setToggleGroup(frameGroup);
		frameCustomRadioButton.setToggleGroup(frameGroup);
		frameCustomTextField.disableProperty().bind(frameCustomRadioButton.selectedProperty().not());
		frameNewlineRadioButton.setSelected(true);
		frameDelimiterHBox.getChildren().add(customFrameDelimiter);
		frameControlsVBox.getChildren().add(frameDelimiterHBox);
		controlsVBox.getChildren().add(frameControlsVBox);
		HBox frameOffsetHBox = new HBox();
		frameOffsetHBox.getStyleClass().add("frameOffsetHBox");
		HBox frameStartOffset = new HBox();
		frameStartOffset.getStyleClass().add("left-hbox");
		frameStartOffset.getStyleClass().add("label-textbox-hbox");
		frameStartOffset.getChildren().add(new Label("Start Offset: "));
		frameStartOffsetTextField.getStyleClass().add("small-textfield");
		frameStartOffset.getChildren().add(frameStartOffsetTextField);
		frameOffsetHBox.getChildren().add(frameStartOffset);
		HBox frameEndOffset = new HBox();
		frameEndOffset.getStyleClass().add("left-hbox");
		frameEndOffset.getStyleClass().add("label-textbox-hbox");
		frameEndOffset.getChildren().add(new Label("End Offset: "));
		frameEndOffsetTextField.getStyleClass().add("small-textfield");
		frameEndOffset.getChildren().add(frameEndOffsetTextField);
		frameOffsetHBox.getChildren().add(frameEndOffset);
		frameControlsVBox.getChildren().add(frameOffsetHBox);
		
		tableView = new TableView<List<String>>();
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		controlsVBox.getChildren().add(tableView);
		
		Button doneButton = new Button("Accept");
		controlsVBox.getChildren().add(doneButton);
		pane.getChildren().add(controlsVBox);
		AnchorPane.setBottomAnchor(controlsVBox, 0.0);
		AnchorPane.setLeftAnchor(controlsVBox, 0.0);
		AnchorPane.setRightAnchor(controlsVBox, 0.0);
		AnchorPane.setTopAnchor(controlsVBox, 0.0);
	} //addControls
	
	public void render()
	{
		String testFile = "1,1,1\n2,1,1\n3,2,1\n4,2,2\n5,4,4\n";
		
		tableView.getColumns().clear();
		tableView.getItems().clear();
		
		List<String> lines = Arrays.asList(testFile.split(model.getFrameDelimiter()));


		lines.stream().map(line -> line.split(model.getDatapointDelimiter())).forEach(values -> {

			for (int i = tableView.getColumns().size(); i < values.length; i++) {
				TableColumn<List<String>, String> col = new TableColumn<>("Column:" + (i + 1));
				col.setMinWidth(80);
				
				final int colIndex = i;
				col.setCellValueFactory(data -> {
					List<String> rowValues = data.getValue();
					String cellValue;
					if (colIndex < rowValues.size()) {
						cellValue = rowValues.get(colIndex);
					} else {
						cellValue = "";
					}
					return new ReadOnlyStringWrapper(cellValue);
				});

				// this sets the click event
				col.setCellFactory(new Callback<TableColumn<List<String>, String>, TableCell<List<String>, String>>() {
					@Override
					public TableCell<List<String>, String> call(TableColumn<List<String>, String> col) {
						final TableCell<List<String>, String> cell = new TableCell<List<String>, String>() {
							@Override
							public void updateItem(String firstName, boolean empty) {
								super.updateItem(firstName, empty);
								if (empty) {
									setText(null);
								} else {
									setText(firstName);
								}
							}
						};
//						cell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
//							@Override
//							public void handle(MouseEvent event) {
//								if (event.getClickCount() > 1) {
//									categorizeData(
//											Integer.parseInt(cell.getTableColumn().getText().split(":")[1]) - 1 - model.startDataSplitter, cell.getTableColumn());
//									
//								}
//							}
//						});
						return cell;
					}
				});
				// done setting click event
				col.setEditable(false);
				col.setSortable(false);
				tableView.getColumns().add(col);
			}

			// add row:
			tableView.getItems().add(Arrays.asList(values));
		});
		tableView.setSelectionModel(null);
	} //render
}
