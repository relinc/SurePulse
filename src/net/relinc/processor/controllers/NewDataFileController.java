package net.relinc.processor.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.relinc.libraries.data.DataFileInterpreter;
import net.relinc.libraries.data.DataFileListWrapper;
import net.relinc.libraries.data.DataInterpreter;
import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.processor.controllers.BarCalibratorController.CalibrationMode;

public class NewDataFileController implements Initializable{
	@FXML TableView<List<String>> tableView;
	@FXML ListView<String> selectInterpreterListView;
	@FXML ListView<String> saveInterpreterListView;
	@FXML TextField interpreterNameTF;
	@FXML Label selectedDataFileLabel;
	@FXML Label collectionRateLabel;
	NumberTextField collectionRateTF = new NumberTextField("","");
	@FXML TabPane tabPane;
	@FXML Button nextButton;
	@FXML Button doneButton;
	@FXML VBox vBoxCollectionRate;
	
	Stage stage;
	//File currentFile;
	DataModel model = new DataModel();
	public DataFileListWrapper existingSampleDataFiles;
	//public List<DataSubset> sampleDataSets;
	public net.relinc.libraries.application.BarSetup barSetup;
	String dataFileInterpretersPath = SPSettings.Workspace.getPath() + "/Data File Interpreters";
	public boolean loadDisplacement;
	public CalibrationMode calibrationMode;

	public void collectionRateButtonFired(){
		if(collectionRateTF.getText().equals("") || collectionRateTF.getDouble() <= 0){
			Dialogs.showErrorDialog("Invalid", "Collection Rate Not Valid", "Please enter a collection "
					+ "rate greater than 0.", stage);
					return;
		}
		try {
			model.setCollectionRate(collectionRateTF.getDouble());
			updateCollectionRateLabel();
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void backButtonFired(){
		if(tabPane.getSelectionModel().getSelectedIndex() == 0) {
				if(
			Dialogs.showConfirmationDialog("Load data files", "You will lose any unsaved changes", "Are you sure you want to proceed?",stage)) {
					stage.close();
				}
		}
		tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() - 1);
	}
	
	public void nextButtonFired(){
		
			//if the next pane is interpreter, only go to it if there are interpreters..
			if(tabPane.getTabs().get(tabPane.getSelectionModel().getSelectedIndex() + 1).getText().equals("Select Interpreter") &&
					selectInterpreterListView.getItems().size() == 0){
				tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() + 2);
			}
			else{
				tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() + 1);
			}
		
	}
	
	public void doneButtonFired(){
		exportDataAndExit();
	}
	
	public void loadDataFileFired(){
		FileChooser fileChooser = new FileChooser();
		fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("txt", "*.txt" , "*.csv")
            );
		if(SPSettings.lastUploadDirectory != null && SPSettings.lastUploadDirectory.exists())
			fileChooser.setInitialDirectory(SPSettings.lastUploadDirectory);
		File file = fileChooser.showOpenDialog(stage);
		if(file == null)
			return;
		SPSettings.lastUploadDirectory = file.getParentFile();
		SPSettings.writeSPSettings();
		if(existingSampleDataFiles.dataFileExists(file.getName()))
		{
			Dialogs.showAlert("Cannot have multiple data files with the same name.", stage);
			return;
		}
		model = new DataModel();
        model.currentFile = file;
        
        try {
			if(!model.readDataFromFile(file.toPath())){
				Dialogs.showErrorDialog("Error", "Unable to parse", "Sure-Pulse was unable to parse your data. Please send "
						+ "the file to softwaresupport@relinc.net and we'd be glad to adjust our algorithm so it can parse your data.", stage);
				model = new DataModel();
				return;
			}
			refreshTable();
		} catch (IOException e) {
			e.printStackTrace();
		}
        //displayMessage("Double-Click each column to configure data types or select a data interpreter.");
        selectedDataFileLabel.setText("Selected Data File: " + model.currentFile.getName());
        updateColumnBackColors();
        updateListView();
	}

	
	public void saveInterpreterFired(){
		if(interpreterNameTF.getText().equals("")){
			
			Dialogs.showAlert("Please name your interpreter",stage);
			return;
		}
		
		if(!SPOperations.specialCharactersAreNotInTextField(interpreterNameTF)) {
			Dialogs.showInformationDialog("Save Interpreter","Invalid Character In Interpreter Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
			
		String path = dataFileInterpretersPath + "/" 
		+ interpreterNameTF.getText() + ".txt";
		
		if(new File(path).exists()){
			
			Dialogs.showAlert("Interpreter name is already taken. Please rename.",stage);
			return;
		}
		
		model.writeToPath(path);
		updateListView();
	}

	public void setDatasetDelimeterButtonFired(){
		updateDatasetDelimeterLabel();
	}
	
	public void clearCategorizationsFired(){
		model.rawDataSets.stream().forEach(data -> data.interpreter.DataType = null);
		model.setCollectionRate(-1);
		updateColumnBackColors();
		updateCollectionRateTBAvailability();
		updateCollectionRateLabel();
	}
	
	public void updateCollectionRateLabel(){
		if(model.getCollectionRate() > 0){
			collectionRateLabel.setText("Collection Rate: " + model.getCollectionRate() + SPSettings.lineSeperator + 
					"(Leave blank if time data exists)");
		}
		else{
			collectionRateLabel.setText("Collection Rate: " + "N/A" + SPSettings.lineSeperator + 
					"(Leave blank if time data exists)");
		}
	}
	
	public void updateDatasetDelimeterLabel(){
		//datasetDelimeterLabel.setText("Dataset Delimeter: " + model.dataTypeDelimiter);
	}
	
	public void setFrameDelimeterButtonFired(){
		//model
		//frameDelimeterLabel.setText(frameDelimeterTF.getText());
		System.out.println("Not implemented");
	}
	

	private void updateListView() {
		selectInterpreterListView.getSelectionModel().clearSelection();
		selectInterpreterListView.getItems().clear();
		saveInterpreterListView.getItems().clear();
		
		
		ObservableList<String> items =FXCollections.observableArrayList();
		File dir = new File(dataFileInterpretersPath);
		for(File f : dir.listFiles()){
			if(f == null)
				continue;
			DataFileInterpreter d = new DataFileInterpreter(f.getPath(), null);
			if(model.interpreterIsCompatible(d, barSetup)){
				items.add(f.getName());
			}
			//items.add(d.getName());
		}
		selectInterpreterListView.setItems(items);
		saveInterpreterListView.setItems(items);
	}



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		vBoxCollectionRate.getChildren().add(2, collectionRateTF);
		updateDatasetDelimeterLabel();
		updateWizardButtons();
		
		selectInterpreterListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

		    @Override
		    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
		        //load the interpreter, apply it, and refresh the friggin page son
		    	if(newValue == null || newValue == "")
		    		return;
		    	DataFileInterpreter d = new DataFileInterpreter(dataFileInterpretersPath + "/" + newValue, null);
		    	d.setDefaultNames(existingSampleDataFiles);
		    	for(int i = 0; i < d.interpreters.size(); i++){
		    		DataInterpreter interpreter = d.interpreters.get(i);
		    		if(interpreter.DataType == dataType.INCIDENTSG){
		    			//System.out.println("WORKING");
		    			if(barSetup.IncidentBar.strainGauges.size() > 0){
		    				interpreter.strainGauge = barSetup.IncidentBar.strainGauges.get(0);
		    				//TODO: Multiple strain gauges not implemented. It always just gets the first one
		    			}
		    			else{
		    				
		    				Dialogs.showInformationDialog("Interperter incompatible", "No incident bar strain gauge", "Use a bar setup with a transmission strain gauge",stage);
		    				return;
		    			}
		    		}
		    		else if(interpreter.DataType == dataType.TRANSMISSIONSG){
		    			if(barSetup.TransmissionBar.strainGauges.size() > 0){
		    				interpreter.strainGauge = barSetup.TransmissionBar.strainGauges.get(0);
		    				//TODO: Multiple strain gauges not implemented
		    			}
		    			else{
		    				Dialogs.showInformationDialog("Interperter incompatible", "No transmission bar strain gauge", "Use a bar setup with a transmission strain gauge",stage);
		    				return;
		    			}
		    		}
		    		else if(interpreter.DataType == dataType.INCIDENTBARSTRAIN){
		    			if(barSetup.IncidentBar.strainGauges.size() > 0){
		    				interpreter.strainGauge = barSetup.IncidentBar.strainGauges.get(0);
		    			}
		    			else{
		    				Dialogs.showInformationDialog("Interperter incompatible", "No incident bar strain gauge", "Use a bar setup with a incident strain gauge",stage);
		    				return;
		    			}
		    		}
		    		else if(interpreter.DataType == dataType.TRANSMISSIONBARSTRAIN){
		    			if(barSetup.TransmissionBar.strainGauges.size() > 0){
		    				interpreter.strainGauge = barSetup.TransmissionBar.strainGauges.get(0);
		    			}
		    			else{
		    				Dialogs.showInformationDialog("Interperter incompatible", "No transmission bar strain gauge", "Use a bar setup with a transmission strain gauge",stage);
		    				return;
		    			}
		    		}
		    	}
		    	model.applyDataInterpreter(d);
		    	updateColumnBackColors();
		    	updateCollectionRateLabel();
		    	updateCollectionRateTBAvailability();
		    	updateListView();
		    	updateWizardButtons();
		    }
		});
		
		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				updateColumnBackColors();
				updateWizardButtons();
				
				if((int)newValue == tabPane.getTabs().size() - 1){
					
					if(model.countDataType(dataType.INCIDENTSG) > 1 || model.countDataType(dataType.TRANSMISSIONSG) > 1 || model.countDataType(dataType.INCIDENTBARSTRAIN) > 1 
							|| model.countDataType(dataType.TRANSMISSIONBARSTRAIN) > 1)
					{
						//disallow saving interpreters with multiple strain gauges
						saveInterpreterListView.setDisable(true);
						interpreterNameTF.setDisable(true);
					}
					else{
						saveInterpreterListView.setDisable(false);
						interpreterNameTF.setDisable(false);
					}
				
					if(model.getCollectionRate() > 0){
						//saveInterpreterListView.setDisable(true);
						//interpreterNameTF.setDisable(true);
						
					}
				}
				else{
					
				}
			}
		});
		
	}
	
	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		  @Override
		  public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
		  {
			  updateColumnBackColors();
			  updateCollectionRateTBAvailability();
		  }
		});
		 
	}

	private void updateCollectionRateTBAvailability(){
		if(model.hasTimeData() && model.getCollectionRate() <= 0)
			collectionRateTF.setDisable(true);
		else
			collectionRateTF.setDisable(false);
	}
	
	private void updateColumnBackColors() {
		if(model == null || model.rawDataSets.size() == 0)
			return;
		tableView.getColumns().get(0).getStyleClass();

		for(int i = model.startDataSplitter; i < tableView.getColumns().size(); i++){
			int dataIndex = i - model.startDataSplitter;
			if(model.rawDataSets.size() > dataIndex && model.rawDataSets.get(dataIndex).interpreter.DataType == null){
				tableView.getColumns().get(i).getStyleClass().remove("column-red");
				tableView.getColumns().get(i).getStyleClass().remove("column-green");
				if(tabPane.getSelectionModel().getSelectedIndex() != 0)
					tableView.getColumns().get(i).getStyleClass().add("column-red");
			}
			else{
				tableView.getColumns().get(i).getStyleClass().remove("column-red");
				tableView.getColumns().get(i).getStyleClass().remove("column-green");
				if(tabPane.getSelectionModel().getSelectedIndex() != 0 && model.rawDataSets.size() > dataIndex)
					tableView.getColumns().get(i).getStyleClass().add("column-green");
			}
		}

	}


	private void refreshTable() {
		try {
			fillTableFromModel(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void fillTableFromModel(DataModel model) throws IOException {
		tableView.getColumns().clear();
		tableView.getItems().clear();
		
		List<String> lines = new ArrayList<String>();
		
		for(int i = 0; i < model.lines.size(); i++){
			if(i >= model.startFrameSplitter)
				lines.add(model.lines.get(i));
		}
		String find = lines.get(0);
		int deletedIndex = model.origLines.indexOf(find);
		for(int i = deletedIndex - 1; i >= 0; i--){
			lines.add(0,model.origLines.get(i));
		}

		lines.stream().map(line -> line.split(model.dataTypeDelimiter)).forEach(values -> {

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
						cell.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								if (event.getClickCount() > 1) {
									categorizeData(
											Integer.parseInt(cell.getTableColumn().getText().split(":")[1]) - 1 - model.startDataSplitter, cell.getTableColumn());
									
								}
							}
						});
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
	}

	public void categorizeData(int index, TableColumn<List<String>, String> column){
		
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/CategorizeData.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			//scene.getStylesheets().add(getClass().getResource("/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			//CategorizeDataController c = root1.<CategorizeDataController>getController();
			CategorizeDataController c = root1.<CategorizeDataController>getController();
			//c.stage = anotherStage;
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			
			c.rawDataSet = model.rawDataSets.get(index);
			c.model = model;
			c.existingSampleDataFiles = existingSampleDataFiles;
			c.barSetup = barSetup;
			c.tableColumn = column;
			c.tableView.getItems().addAll(tableView.getItems());
			c.loadDisplacement = loadDisplacement;
			c.calibrationMode = calibrationMode;
			c.updateControls();
			c.renderGraph();
			
			
			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exportDataAndExit(){
		//done
		//At this point, the rawDatasets should be converted to Dataset objects.
		//Here you need to check if a time array was set. If not, ask for collection rate.
		if(!model.hasTimeData()){
			Dialogs.showAlert("No Time Data, cannot export", stage);
			return;
		}
		
		try {
			existingSampleDataFiles.add(model.exportToDataFile(true, calibrationMode != null));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Stage stage = (Stage) nextButton.getScene().getWindow();
	    stage.close();
	}

	public void updateWizardButtons(){
		if(tabPane.getSelectionModel().getSelectedIndex() == tabPane.getTabs().size() - 1){
			//on the last page
			nextButton.setVisible(false);
			nextButton.setManaged(false);
			doneButton.setVisible(true);
			doneButton.setManaged(true);
		}
		else{
			if(tabPane.getSelectionModel().getSelectedIndex() == 1){
				//on interpreter page
				if(selectInterpreterListView.getSelectionModel().getSelectedIndex() != -1){
					//an interpreter is selected
					nextButton.setVisible(true);
					nextButton.setManaged(true);
					doneButton.setVisible(true);
					doneButton.setManaged(true);
				}
				else{
					nextButton.setVisible(true);
					nextButton.setManaged(true);
					doneButton.setVisible(false);
					doneButton.setManaged(false);
				}
			}
			else{
				nextButton.setVisible(true);
				nextButton.setManaged(true);
				doneButton.setVisible(false);
				doneButton.setManaged(false);
			}
		}
	}

}
