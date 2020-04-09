package net.relinc.datafileparser.application;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

public class Home {
	
	Stage stage;
	Model model;
	File selectedFile;
	TableView<List<ParseCandidate>> tableView;
	boolean listenerEnabled = true;
	ArrayList<RadioButtonWithValue<String>> dataDelimiterRadioButtons = new ArrayList<>();
	
	// Frame parsing parameter controls
	RadioButtonWithValue<String> frameNewlineRadioButton = new RadioButtonWithValue<String>("New Line", "\n");
	RadioButton frameCustomRadioButton = new RadioButton("Custom");
	ToggleGroup frameGroup = new ToggleGroup();
	TextField frameCustomTextField = new TextField();
	TextField frameStartOffsetTextField = new TextField();
	TextField frameEndOffsetTextField = new TextField();
	
	// Data parsing parameter controls
	RadioButtonWithValue<String> dataCommaRadioButton = new RadioButtonWithValue<String>(",", ",");
	RadioButtonWithValue<String> dataSpaceRadioButton = new RadioButtonWithValue<String>("space", " ");
	RadioButtonWithValue<String> dataTabRadioButton = new RadioButtonWithValue<String>("tab", "\t");
	RadioButtonWithValue<String> dataPipeRadioButton = new RadioButtonWithValue<String>("|", "\\|");
	RadioButton dataCustomRadioButton = new RadioButton("Custom");
	ToggleGroup dataGroup = new ToggleGroup();
	TextField dataCustomTextField = new TextField();
	TextField dataStartOffsetTextField = new TextField();
	TextField dataEndOffsetTextField = new TextField();
	AnchorPane root = new AnchorPane();
	List<List<String>> output;
	
	public Home(Stage stage, List<List<String>> output)
	{
		this.stage = stage;
		model = new Model("\n", ",");
		
		try {
			
			createWidget(root);
			Scene scene = new Scene(root);//, dims.getWidth(), dims.getHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			stage.setScene(scene);
		} catch(Exception e) {
			e.printStackTrace();
		}
		this.output = output;
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
				//String testFile = "1,1,1\n2,1,1\n3,2,1\n4,2,2\n5,4,4\n";
//				String testFile = "pad,pad,pad,pad\npad,1,2,pad\npad,1,2,pad\npad,1,2,pad\npad,pad,pad,pad,\n";
//				model.setDataFile(testFile);
				
				
				FileChooser chooser = new FileChooser();
				File f = chooser.showOpenDialog(stage);
				if(f != null){
					selectedFile = f;
					try{
					model.setDataFile(new String(Files.readAllBytes(f.toPath())));
					}
					catch(Exception e){
						
					}
				}
				
				model.setParsingParametersAutomatically();
				loadParametersFromModel();
				render();
			}

			
		});
		
		controlsVBox.getChildren().add(loadButton);
		
		addRadioButtonsToLists();
		
		addParsingParameterListeners();
		
		// Frame Controls
		VBox frameControlsVBox = new VBox();
		frameControlsVBox.getStyleClass().add("parseInfoVBox");
		Label frameLabel = new Label("Frame/Row");
		frameLabel.getStyleClass().add("big-label");
		frameControlsVBox.getChildren().add(frameLabel);
		HBox frameDelimiterHBox = new HBox();
		frameDelimiterHBox.getStyleClass().add("delimiter-hbox");
		frameDelimiterHBox.getChildren().add(new Label("Delimiter:"));
		frameDelimiterHBox.getChildren().add(frameNewlineRadioButton);
		
		HBox customFrameDelimiter = new HBox();
		customFrameDelimiter.getStyleClass().add("custom-delimeter-hbox");
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
		frameOffsetHBox.getStyleClass().add("offset-hbox");
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
		
		
		// Data controls
		VBox dataControlsVBox = new VBox();
		dataControlsVBox.getStyleClass().add("parseInfoVBox");
		Label dateLabel = new Label("Data/Column");
		dateLabel.getStyleClass().add("big-label");
		dataControlsVBox.getChildren().add(dateLabel);
		HBox dataDelimiterHBox = new HBox();
		dataDelimiterHBox.getStyleClass().add("delimiter-hbox");
		dataDelimiterHBox.getChildren().add(new Label("Delimiter:"));
//		dataDelimiterHBox.getChildren().add(dataCommaRadioButton);
//		dataDelimiterHBox.getChildren().add(dataSpaceRadioButton);
//		dataDelimiterHBox.getChildren().add(dataTabRadioButton);
//		dataDelimiterHBox.getChildren().add(dataPipeRadioButton);
		dataDelimiterRadioButtons.stream().forEach(rb -> dataDelimiterHBox.getChildren().add(rb));
		
		HBox customDataDelimiter = new HBox();
		customDataDelimiter.getStyleClass().add("custom-delimeter-hbox");
		customDataDelimiter.getChildren().add(dataCustomRadioButton);
		dataCustomTextField.getStyleClass().add("small-textfield");
		customDataDelimiter.getChildren().add(dataCustomTextField);
		dataCommaRadioButton.setToggleGroup(dataGroup);
		dataSpaceRadioButton.setToggleGroup(dataGroup);
		dataTabRadioButton.setToggleGroup(dataGroup);
		dataPipeRadioButton.setToggleGroup(dataGroup);
		dataCustomRadioButton.setToggleGroup(dataGroup);
		dataCustomTextField.disableProperty().bind(dataCustomRadioButton.selectedProperty().not());
		dataCommaRadioButton.setSelected(true);
		dataDelimiterHBox.getChildren().add(customDataDelimiter);
		dataControlsVBox.getChildren().add(dataDelimiterHBox);
		HBox dataOffsetHBox = new HBox();
		dataOffsetHBox.getStyleClass().add("offset-hbox");
		HBox dataStartOffset = new HBox();
		dataStartOffset.getStyleClass().add("left-hbox");
		dataStartOffset.getStyleClass().add("label-textbox-hbox");
		dataStartOffset.getChildren().add(new Label("Start Offset: "));
		dataStartOffsetTextField.getStyleClass().add("small-textfield");
		dataStartOffset.getChildren().add(dataStartOffsetTextField);
		dataOffsetHBox.getChildren().add(dataStartOffset);
		HBox dataEndOffset = new HBox();
		dataEndOffset.getStyleClass().add("left-hbox");
		dataEndOffset.getStyleClass().add("label-textbox-hbox");
		dataEndOffset.getChildren().add(new Label("End Offset: "));
		dataEndOffsetTextField.getStyleClass().add("small-textfield");
		dataEndOffset.getChildren().add(dataEndOffsetTextField);
		dataOffsetHBox.getChildren().add(dataEndOffset);
		dataControlsVBox.getChildren().add(dataOffsetHBox);
		controlsVBox.getChildren().add(dataControlsVBox);
		
		tableView = new TableView<List<ParseCandidate>>();
		tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		controlsVBox.getChildren().add(tableView);
		
		Button doneButton = new Button("Accept");
		doneButton.setOnAction((a) -> {
			output.clear();
			output.addAll(model.parse());
			((Stage)root.getScene().getWindow()).close();
		});
		controlsVBox.getChildren().add(doneButton);
		pane.getChildren().add(controlsVBox);
		AnchorPane.setBottomAnchor(controlsVBox, 0.0);
		AnchorPane.setLeftAnchor(controlsVBox, 0.0);
		AnchorPane.setRightAnchor(controlsVBox, 0.0);
		AnchorPane.setTopAnchor(controlsVBox, 0.0);
	} //addControls
	
	private void addRadioButtonsToLists() {
		addDataDelimiterRadioButtonsToList();
	}

	private void addDataDelimiterRadioButtonsToList() {
		dataDelimiterRadioButtons.add(dataCommaRadioButton);
		dataDelimiterRadioButtons.add(dataPipeRadioButton);
		dataDelimiterRadioButtons.add(dataSpaceRadioButton);
		dataDelimiterRadioButtons.add(dataTabRadioButton);
	}

	private void addParsingParameterListeners() {
		
		ArrayList<RadioButton> radioButtons = new ArrayList<>();
		radioButtons.add(frameNewlineRadioButton);
		radioButtons.add(frameCustomRadioButton);
		radioButtons.add(dataCommaRadioButton);
		radioButtons.add(dataSpaceRadioButton);
		radioButtons.add(dataTabRadioButton);
		radioButtons.add(dataPipeRadioButton);
		radioButtons.add(dataCustomRadioButton);
		
		radioButtons.stream().forEach(rb -> rb.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parsingParametersChanged();
			}
		}));
		
		ArrayList<TextField> textFields = new ArrayList<TextField>();
		textFields.add(frameCustomTextField);
		textFields.add(frameEndOffsetTextField);
		textFields.add(frameStartOffsetTextField);
		textFields.add(dataCustomTextField);
		textFields.add(dataStartOffsetTextField);
		textFields.add(dataEndOffsetTextField);
		
		textFields.stream().forEach(tf -> tf.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				parsingParametersChanged();
			}
		}));
		
	} //addParsingParameterListeners

	public void parsingParametersChanged(){
		if(listenerEnabled){
			if(parsingParametersAreValid()){
				model.setFrameDelimiter(getFrameDelimiter());
				model.setStartFrameDelimiter(Integer.parseInt(frameStartOffsetTextField.getText()));
				model.setEndFrameDelimiter(model.getNumFramesFromSplit() - Integer.parseInt(frameEndOffsetTextField.getText()) - 1);
				
				model.setDatapointDelimiter(getDatapointDelimiter());
				model.setStartDatapointDelimiter(Integer.parseInt(dataStartOffsetTextField.getText()));
				model.setEndDatapointDelimiter(model.getNumDatapointsFromSplit() - Integer.parseInt(dataEndOffsetTextField.getText()) - 1); 
				
				model.setDatapointDelimiter(getDatapointDelimiter());
				render();
			}
		}
	} //parsingParametersChanged
	
	private void loadParametersFromModel() {
		listenerEnabled = false;
		setFrameDelimiter(model.getFrameDelimiter());
		frameStartOffsetTextField.setText(Integer.toString(model.getStartFrameDelimiter()));
		frameEndOffsetTextField.setText(Integer.toString(model.getNumFramesFromSplit() - model.getEndFrameDelimiter() - 1));
		
		setDataDelimiter(model.getDatapointDelimiter());
		dataStartOffsetTextField.setText(Integer.toString(model.getStartDatapointDelimiter()));
		dataEndOffsetTextField.setText(Integer.toString(model.getNumDatapointsFromSplit() - model.getEndDatapointDelimiter() - 1));
		
		listenerEnabled = true;
	} //loadParametersFromModel



	private boolean parsingParametersAreValid() {
		if(frameCustomRadioButton.isSelected() && frameCustomTextField.getText().isEmpty())
			return false;
		if(dataCustomRadioButton.isSelected() && dataCustomTextField.getText().isEmpty())
			return false;
		if(frameStartOffsetTextField.getText().isEmpty() || frameEndOffsetTextField.getText().isEmpty() || 
				dataStartOffsetTextField.getText().isEmpty() || dataEndOffsetTextField.getText().isEmpty()){
			return false;
		}
		return true;
	} //parsingParametersAreValid
	
	private String getDatapointDelimiter() {
		
		if(dataDelimiterRadioButtons.stream().filter(rb -> rb.isSelected()).findFirst().isPresent())
			return dataDelimiterRadioButtons.stream().filter(rb -> rb.isSelected()).findFirst().get().getValue();
		
		if(dataCustomRadioButton.isSelected())
			return dataCustomTextField.getText();
		else 
			System.err.println("getDatapointDelimiter Failed");
		return "";
	} //getDatapointDelimiter
	
	private String getFrameDelimiter(){
		if(frameCustomRadioButton.isSelected())
			return frameCustomTextField.getText();
		else if(frameNewlineRadioButton.isSelected())
			return "\n";
		else
			System.err.println("getFrameDelimiter Failed");
		return "";
	}
	
	private void setFrameDelimiter(String frameDelimiter) {
		if(frameDelimiter.equals("\n")){
			frameNewlineRadioButton.setSelected(true);
		}
		else{
			frameCustomTextField.setText(frameDelimiter);
		}
	}
	
	private void setDataDelimiter(String datapointDelimiter) {
		if(dataDelimiterRadioButtons.stream().filter(rb -> rb.getValue().equals(datapointDelimiter)).findFirst().isPresent())
			dataDelimiterRadioButtons.stream().filter(rb -> rb.getValue().equals(datapointDelimiter)).findFirst().get().setSelected(true);
		else
			dataCustomTextField.setText(datapointDelimiter);
	}

	public void render()
	{
		tableView.getColumns().clear();
		tableView.getItems().clear();
		
		List<List<ParseCandidate>> candidates = model.getParseCandidates();
		for(int rowIdx = 0; rowIdx < candidates.size(); rowIdx++)
		{
			List<ParseCandidate> values = candidates.get(rowIdx);
			for (int i = tableView.getColumns().size(); i < values.size(); i++) {
				TableColumn<List<ParseCandidate>, ParseCandidate> col = new TableColumn<>("Column:" + (i + 1));
				col.setMinWidth(80);
				
				final int colIndex = i;
				col.setCellValueFactory(data -> {
					List<ParseCandidate> rowValues = data.getValue();
					ParseCandidate p;
					if (colIndex < rowValues.size()) {
						p = rowValues.get(colIndex);
					} else {
						p = null;
					}
					return p;
				});
				
				// this sets the click event
				col.setCellFactory(new Callback<TableColumn<List<ParseCandidate>, ParseCandidate>, TableCell<List<ParseCandidate>, ParseCandidate>>() {
					@Override
					public TableCell<List<ParseCandidate>, ParseCandidate> call(TableColumn<List<ParseCandidate>, ParseCandidate> col) {
						final TableCell<List<ParseCandidate>, ParseCandidate> cell = new TableCell<List<ParseCandidate>, ParseCandidate>() {
							@Override
							public void updateItem(ParseCandidate firstName, boolean empty) {
								super.updateItem(firstName, empty);
								if (empty || firstName == null) {
									setText(null);
								} else {
									setText(firstName.getText());
								}
								if(firstName != null && firstName.isParsable())
									setStyle("-fx-background-color:#d8ffd8 !important");
							}
						};
						
						return cell;
					}
				});
				// done setting click event
				col.setEditable(false);
				col.setSortable(false);
				tableView.getColumns().add(col);
			}

			// add row:
			tableView.getItems().add(values);
		}

		tableView.setSelectionModel(null);
		
	} //renderXY

}
