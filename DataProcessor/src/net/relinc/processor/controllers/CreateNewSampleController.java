package net.relinc.processor.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.relinc.libraries.sample.*;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.lingala.zip4j.exception.ZipException;
import net.relinc.correlation.controllers.DICSplashpageController;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.application.StrikerBar;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataFileInterpreter;
import net.relinc.libraries.data.DataFileListWrapper;
import net.relinc.libraries.data.DataInterpreter;
import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.splibraries.DICProcessorIntegrator;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.processor.controllers.CalibrationController.BarSetupMode;
import net.relinc.processor.pico.PicoScopeCLI;
import net.relinc.viewer.application.AnalyzeMain;

public class CreateNewSampleController {
	@FXML Button backButton;
	@FXML Button nextButton;
	@FXML Button buttonDoneCreatingSample;
	@FXML Button buttonAnalyzeResults;
	@FXML ListView<DataSubset> dataListView;
	@FXML HBox sampleSaveParamsHbox;
	@FXML VBox barSetupVBox;
	@FXML HBox deleteSelectedDatasetHBox;

	@FXML TextField Name;
	@FXML TextField folderNameTF;

	@FXML CheckBox metricCB;
	@FXML TableView<Descriptor> dictionaryTableView;
	@FXML VBox allSamplesKeyValueTableVBox;
	//This region has the ingredients to make a sample
	BarSetup barSetup = new BarSetup();
	DataFileListWrapper sampleDataFiles = new DataFileListWrapper();
	private Sample lastSavedSample;
	private DescriptorDictionary descriptorDictionary = new DescriptorDictionary();
	private File savedImagesLocation;

	@FXML TextField tbName;
	@FXML TextField tbName2;
	NumberTextField tbLength;
	NumberTextField tbDiameter;
	NumberTextField tbWidth;
	NumberTextField tbHeight;
	NumberTextField tbGaugeHeight;
	NumberTextField tbGaugeWidth;
	NumberTextField tbDensity;
	NumberTextField tbYoungsMod;
	NumberTextField tbHeatCapacity;
	NumberTextField tbInnerDiameter;
	NumberTextField tbOuterDiameter;
	NumberTextField tbStrikerBarDensity;
	NumberTextField tbStrikerBarLength;
	NumberTextField tbStrikerBarDiameter;
	NumberTextField tbStrikerBarSpeed;
	Label dateSavedLabel = new Label();

	@FXML TreeView<FileFX> previousSamplesTreeView;
	@FXML TreeView<FileFX> saveSampleTreeView;
	TreeItem<FileFX> selectedPreviousSamplesTreeItem;
	TreeItem<FileFX> selectedSaveSampleTreeItem;

	@FXML GridPane sampleParameterGrid;
	@FXML ChoiceBox<String> sampleType;
	@FXML TabPane tabPane;

	@FXML Label currentSelectedBarSetupLabel;

	@FXML Button clearSampleDictionaryTableButton;
	@FXML Button deleteDescriptorButton;
	@FXML Button addBarSetupButton;
	@FXML Button removeBarSetupButton;
	@FXML Button createNewSampleButton;
	@FXML Button trimDataButton;
	@FXML Button refreshAllSamplesDescriptorsButton;
	@FXML Button deleteSelectedDatasetButton;

	@FXML Button addDataFileButton;
	@FXML Button deleteSelectedData;

	@FXML Button refreshButton;

	public Stage stage;

	private String treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";

	Image folderIconImage = new Image(getClass().getResourceAsStream("/net/relinc/libraries/images/folderIcon.jpeg"));

	@FXML
	public void initialize(){
		
        
		barSetupVBox.setStyle("-fx-border-color: #bdbdbd;\n"
				+ "-fx-border-insets: 3;\n"
				+ "-fx-border-width: 1;\n"
				+ "-fx-border-style: solid;\n");
		deleteSelectedDatasetButton.setDisable(true);

		initializeDynamicFields();

		if(SPSettings.metricMode.getValue())
			metricCB.selectedProperty().set(true);

		SPSettings.metricMode.bindBidirectional(metricCB.selectedProperty());

		SampleTypes.getSampleConstantsMap().values().forEach(constants -> {
			sampleType.getItems().add(constants.getShortName());
		});
		
		sampleType.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				setVisiblePreferences(newValue);
			}
		});

		sampleType.getSelectionModel().selectFirst();

		previousSamplesTreeView.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<TreeItem<FileFX>>() {

			@Override
			public void changed(
					ObservableValue<? extends TreeItem<FileFX>> observable,
							TreeItem<FileFX> old_val, TreeItem<FileFX> new_val) {
				TreeItem<FileFX> selectedItem = new_val;
				selectedPreviousSamplesTreeItem = selectedItem;
				selectedPreviousSampleChanged();
			}
		});

		saveSampleTreeView.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<TreeItem<FileFX>>() {

			@Override
			public void changed(
					ObservableValue<? extends TreeItem<FileFX>> observable,
							TreeItem<FileFX> old_val, TreeItem<FileFX> new_val) {
				selectedSaveSampleTreeItem = new_val;
			}
		});

		buttonAnalyzeResults.managedProperty().bind(buttonAnalyzeResults.visibleProperty());
		buttonDoneCreatingSample.managedProperty().bind(buttonDoneCreatingSample.visibleProperty());
		nextButton.managedProperty().bind(nextButton.visibleProperty());

		buttonAnalyzeResults.setVisible(false);
		buttonDoneCreatingSample.setVisible(false);

		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				if(newValue.intValue() == tabPane.getTabs().size() - 1) {
					nextButton.setVisible(false);
					buttonAnalyzeResults.setVisible(true);
					//buttonCreateNewSample.setVisible(true);
					buttonDoneCreatingSample.setVisible(true);
					refreshAllSamplesDescriptorsTableButtonFired();
				} 
				if(newValue.intValue() < tabPane.getTabs().size() - 1) {
					nextButton.setVisible(true);
					buttonAnalyzeResults.setVisible(false);
					//buttonCreateNewSample.setVisible(false);
					buttonDoneCreatingSample.setVisible(false);
				}
			}
		});

		descriptorDictionary.descriptors.addListener(new ListChangeListener<Descriptor>() {
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Descriptor> c) {
				descriptorDictionary.updateDictionary();
			}
		});
		
		deleteSelectedDatasetButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				DataSubset dataset = dataListView.getSelectionModel().getSelectedItem();
				if(dataset == null){
					Dialogs.showAlert("Please select a dataset to delete.", stage);
					return;
				}
				DataFile file = null;
				for(DataFile df : sampleDataFiles){
					for(DataSubset set : df.dataSubsets)
					{
						if(set.equals(dataset))
							file = df;
					}
				}
				String message = "Deleting this dataset will also delete all the datasets in this datafile:\n";
				String files = "";
				for(DataSubset sub : file.dataSubsets){
					files += sub.name + "\n";
				}
				boolean delete = true;
				if(file.dataSubsets.size() == 1){
					delete = Dialogs.showConfirmationDialog("Confirm", "Confirm Deletion", "Are you sure you want to delete?", stage);
				}
				else if(file.dataSubsets.size() > 1){
					delete = Dialogs.showConfirmationDialog("Confirm", "All datasets must be deleted.", message + files, stage);
				}
				else{
					delete = false;
					System.err.println("Something went wrong when deleting a datafile");
				}
				if(delete)
					sampleDataFiles.remove(file);
				updateDataListView();
			}
		});
		
		dataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
			@Override
			public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue,
					DataSubset newValue) {
				if(newValue == null)
					deleteSelectedDatasetButton.setDisable(true);
				else
					deleteSelectedDatasetButton.setDisable(false);
			}
		});

		//sex on a screen right here
		tbName.textProperty().bindBidirectional(tbName2.textProperty());

		setSelectedBarSetup(barSetup);
		descriptorDictionary.updateDictionary();
		updateDescriptorTable();

		refreshButton.setGraphic(SPOperations.getIcon("/net/relinc/viewer/images/refreshIcon.png", 25));
		refreshButton.setOnMouseClicked((a) -> {
			refreshSamples();
		});


		//tooltips
		buttonDoneCreatingSample.setTooltip(new Tooltip("Closes the window and returns to the home window"));
		buttonAnalyzeResults.setTooltip(new Tooltip("Launches the viewer where samples can be compared"));
		dataListView.setTooltip(new Tooltip("The currently loaded datasets for this sample"));
		metricCB.setTooltip(new Tooltip("Metric units mode"));
		dictionaryTableView.setTooltip(new Tooltip("A dictionary of user defined parameter-values. Put custom sample information here. You must hit 'Enter' to save text"));
		previousSamplesTreeView.setTooltip(new Tooltip("Samples in this workspace. Click on a sample to load it"));
		sampleType.setTooltip(new Tooltip("Select the geometry of the sample"));
		clearSampleDictionaryTableButton.setTooltip(new Tooltip("Clears the parameter-value table"));
		deleteDescriptorButton.setTooltip(new Tooltip("Deletes the selected descriptor"));
		addBarSetupButton.setTooltip(new Tooltip("Opens a dialog that allows you to choose a bar setup for this sample"));
		removeBarSetupButton.setTooltip(new Tooltip("Removes the currently selected bar setup from this sample"));
		createNewSampleButton.setTooltip(new Tooltip("Clears data and appropriate text boxes to allow you to create another sample"));
		trimDataButton.setTooltip(new Tooltip("Opens the trim data window that allows you to specify the relevant data in your datasets"));
		refreshAllSamplesDescriptorsButton.setTooltip(new Tooltip("Loads each sample in the workspace and puts the parameter-values in a table"));
		addDataFileButton.setTooltip(new Tooltip("Opens the load data window that allows you to load .txt and .csv files and define datasets"));
		deleteSelectedData.setTooltip(new Tooltip("Deletes the selected dataset"));
		Tooltip.install(deleteSelectedDatasetHBox, new Tooltip("Deletes the selected dataset"));
		//deleteSelectedDatasetHBox.install // .setTooltip(new Tooltip("Deletes the selected dataset"));
	}

	public void clearTableButtonFired(){
		descriptorDictionary.descriptors.clear();
		updateDescriptorTable();
	}

	public void deleteDescriptorButtonFired(){
		descriptorDictionary.descriptors.remove(dictionaryTableView.getSelectionModel().getSelectedItem());
	}

	public void addFolderFired() {
		if(!SPOperations.specialCharactersAreNotInTextField(folderNameTF)) {
			Dialogs.showInformationDialog("Add Sample Folder","Invalid Character In Folder Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		String path = SPOperations.getPathFromFXTreeViewItem(selectedSaveSampleTreeItem);
		if(path.equals("")){
			Dialogs.showAlert("Please select a directory to add the folder to.",stage);
			return;
		}
		//File file = new File(SPSettings.Workspace.getPath() + "/" + path);
		File file = new File(path);
		if (!file.isDirectory()) {
			Dialogs.showAlert("Must be in a directory", stage);
			return;
		}
		File newDir = new File(file.getPath() + "/" + folderNameTF.getText());
		if (newDir.exists()) {
			Dialogs.showAlert("Folder already exists", stage);
			return;
		}
		newDir.mkdir();
		updateTreeViews();

	}
	
	private void deleteSample(String path, boolean showWarning)
	{
		if(path.equals("")){
			Dialogs.showInformationDialog("Delete Sample", "There Was A Problem Deleting", "Nothing selected to delete.",stage);
			return;
		}
		File file = new File(path);//new File(SPSettings.Workspace.getPath() + "/" + path);

		if (file.isDirectory()) {
			if(file.getName().equals("Sample Data")){
				Dialogs.showAlert("Cannot delete base directory", stage);
				return;
			}
			int sampleCount = 0;
			int folderCount = 0;
			List<Integer> contents = SPOperations.countContentsInFolder(file);
			sampleCount = contents.get(0);
			folderCount = contents.get(1);
			String message = "";
			if(sampleCount > 0 && folderCount > 0)
				message = "It contains " + folderCount + " folder(s) and " + sampleCount + " sample(s).";
			else if(sampleCount > 0)
				message = "It contains " + sampleCount + " sample(s).";
			else if(folderCount > 0)
				message = "It contains " + folderCount + " folder(s).";

			if(Dialogs.showConfirmationDialog("Deleting Folder", "Confirm", 
					"Are you sure you want to delete this folder?\n" + message, stage)){
				SPOperations.deleteFolder(file);
			}
			else{
				return;
			}

		}
		else{
			if(!showWarning || Dialogs.showConfirmationDialog("Deleting Sample", "Confirm", "Are you sure you want to delete " + SPOperations.stripExtension(file.getName()) + "?", stage)){
				//new File(file.getPath() + ".zip").delete();
				file.delete();
			}
		}
		updateTreeViews();
	}

	public void deleteSampleButtonFired(){
		String path = SPOperations.getPathFromFXTreeViewItem(selectedSaveSampleTreeItem);
		deleteSample(path, true);
	}

	public void refreshAllSamplesDescriptorsTableButtonFired(){
		updateWorkspaceDescriptorTable();
	}

	public void exportAllSampleDescriptionTableToCSVButtonFired(){
		exportGridToCSV();
	}

	@FXML
	private void processImagesButtonFired(){
		Stage primaryStage = new Stage();
		File file = new File(SPSettings.applicationSupportDirectory + "/RELFX/SURE-DIC/");
		if(!file.exists()) {
			file.mkdirs();
		}
		try {
			//prepare app data directory. 

			//BorderPane root = new BorderPane();
			FXMLLoader root = new FXMLLoader((new DICSplashpageController()).getClass().getResource("/net/relinc/correlation/fxml/DICSplashpage.fxml"));

			Scene scene = new Scene(root.load());
			//scene.getStylesheets().add(getClass().getResource("dicapplication.css").toExternalForm());
			primaryStage.setScene(scene);
			DICSplashpageController cont = root.getController();
			cont.stage = primaryStage;
			cont.createRefreshListener();

			DICProcessorIntegrator integrator = cont.dicProcessorIntegrator;

			//Double[] trueStrain = cont.strainToExport;
			primaryStage.showAndWait();
			//double[] testing = {1,2,3,4,5,6,7,9,10,132};
			//wrapper.array = testing;
			//run the strain file through the file creation process.
			//1st, save file to a location.
			//Could get target tracking strain, dic, or both

			String strainFile = "";
			String strainExportLocation = "";
			if(integrator.targetTrackingDisplacement != null){
				//create target tracking file and dataset.
				strainFile = "Target Tracking Displacement" + SPSettings.lineSeperator;
				for(int i = 0; i < integrator.targetTrackingDisplacement.size(); i++)
					strainFile += integrator.targetTrackingDisplacement.get(i) + SPSettings.lineSeperator;
				strainExportLocation = SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/TempDICStrainExport/TargetTrackingDisplacement.txt";

				SPOperations.writeStringToFile(strainFile,strainExportLocation);

				DataModel model = new DataModel();
				model.currentFile = new File(strainExportLocation);
				model.readDataFromFile(new File(strainExportLocation).toPath());

				DataFileInterpreter FileInterpreter = new DataFileInterpreter();
				DataInterpreter dataInterpreter = new DataInterpreter();
				dataInterpreter.DataType = dataType.DISPLACEMENT;
				dataInterpreter.multiplier = 1;
				FileInterpreter.interpreters = new ArrayList<DataInterpreter>();
				FileInterpreter.interpreters.add(dataInterpreter);
				FileInterpreter.setDefaultNames(sampleDataFiles);
				model.applyDataInterpreter(FileInterpreter);
				model.setCollectionRate(integrator.collectionRate);
				sampleDataFiles.add(model.exportToDataFile(true, false));
			}
			else if(integrator.dicLagrangianStrain != null){
				strainFile = "Lagrangian Strain DIC" + SPSettings.lineSeperator;
				for(int i = 0; i < integrator.dicLagrangianStrain.length; i++)
					strainFile += integrator.dicLagrangianStrain[i] + SPSettings.lineSeperator;
				strainExportLocation = SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/TempDICStrainExport/DICLagrangianStrain.txt";
			
				SPOperations.writeStringToFile(strainFile,strainExportLocation);

				DataModel model = new DataModel();
				model.currentFile = new File(strainExportLocation);
				model.readDataFromFile(new File(strainExportLocation).toPath());

				DataFileInterpreter FileInterpreter = new DataFileInterpreter();
				DataInterpreter dataInterpreter = new DataInterpreter();
				dataInterpreter.DataType = dataType.LAGRANGIANSTRAIN;
				FileInterpreter.interpreters = new ArrayList<DataInterpreter>();
				FileInterpreter.interpreters.add(dataInterpreter);
				FileInterpreter.setDefaultNames(sampleDataFiles);
				model.applyDataInterpreter(FileInterpreter);
				model.setCollectionRate(integrator.collectionRate);
				sampleDataFiles.add(model.exportToDataFile(true, false));
			}

			
			

			savedImagesLocation = integrator.imagesLocation;

			updateDataListView();


		} catch(Exception e) {
			e.printStackTrace();
		}
	} 

	public void exportGridToCSV(){
		SpreadsheetView view = (SpreadsheetView)allSamplesKeyValueTableVBox.getChildren().get(1);
		FileChooser chooser = new FileChooser();
		File save = chooser.showSaveDialog(stage);
		if(save != null){
			ArrayList<String> stringRows = new ArrayList<>();
			ObservableList<ObservableList<SpreadsheetCell>> rows = view.getGrid().getRows();
			for(ObservableList<SpreadsheetCell> row : rows){
				String stringRow = "";
				for(SpreadsheetCell cell : row){
					stringRow += cell.getText() + ",";
				}
				stringRow += "\n";
				stringRows.add(stringRow);
			}
			SPOperations.writeListToFile(stringRows, save.getPath() + ".csv");
		}
	}

	public void updateDescriptorTable(){
		dictionaryTableView.getColumns().clear();
		dictionaryTableView.setEditable(true);
		dictionaryTableView.getSelectionModel().cellSelectionEnabledProperty().set(true);

		descriptorDictionary.updateDictionary();
		TableColumn<Descriptor, String> key = new TableColumn<Descriptor, String>("Parameter");
		TableColumn<Descriptor, String> value = new TableColumn<Descriptor, String>("Value");

		key.setCellValueFactory(new PropertyValueFactory<Descriptor, String>("key"));

		key.setCellFactory(EditCell.forTableColumn());
		key.setOnEditCommit(
				new EventHandler<CellEditEvent<Descriptor, String>>() {
					@Override
					public void handle(CellEditEvent<Descriptor, String> t) {
						((Descriptor) t.getTableView().getItems().get(
								t.getTablePosition().getRow())
								).setKey(t.getNewValue());
						descriptorDictionary.updateDictionary();
					}
				}
				);
		value.setCellValueFactory(new PropertyValueFactory<Descriptor, String>("value"));
		value.setCellFactory(EditCell.forTableColumn());
		value.setOnEditCommit(
				new EventHandler<CellEditEvent<Descriptor, String>>() {
					@Override
					public void handle(CellEditEvent<Descriptor, String> t) {
						((Descriptor) t.getTableView().getItems().get(
								t.getTablePosition().getRow())
								).setValue(t.getNewValue());
						descriptorDictionary.updateDictionary();
					}
				}
				);

		//        value.setPrefWidth(200);
		//        key.setMinWidth(200);

		dictionaryTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		dictionaryTableView.getColumns().add(key);
		dictionaryTableView.getColumns().add(value);

		dictionaryTableView.setItems(descriptorDictionary.descriptors);
	}

	public void updateWorkspaceDescriptorTable(){
		SPTracker.track(SPTracker.surepulseProcessorCategory, "AllSamplesDescriptionsTableRefreshed");
		while(allSamplesKeyValueTableVBox.getChildren().size() > 1)
			allSamplesKeyValueTableVBox.getChildren().remove(1);//remove everything (only the table) except the refresh button.

		//updates table with all descriptors in the Workspace.
		ArrayList<DescriptorDictionary> sampleDictionaries = new ArrayList<DescriptorDictionary>();
		recursivelyLoadSampleParametersDictionary(new File(SPSettings.Workspace + "/Sample Data"), sampleDictionaries);
		sampleDictionaries.sort(new Comparator<DescriptorDictionary>() {
			@Override
			public int compare(DescriptorDictionary o1, DescriptorDictionary o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		//going down each list, index gets priority.
		int longestDictionary = 0;

		for(DescriptorDictionary dict : sampleDictionaries){
			if(dict.descriptors.size() > longestDictionary)
				longestDictionary = dict.descriptors.size();
		}

		DescriptorDictionary template = new DescriptorDictionary();

		//minimum longest dictionary columns

		for (DescriptorDictionary dict : sampleDictionaries) {
			for (int i = 0; i < longestDictionary; i++) {
				if (i < dict.descriptors.size()) {
					if (template.getValue(dict.descriptors.get(i).getKey()).equals("")) {
						// not in the template, add.
						template.descriptors.add(new Descriptor(dict.descriptors.get(i).getKey(), "Placeholder"));
					}
				}
			}
		}

		//use template to populate a dictionary for each sample.
		ArrayList<DescriptorDictionary> sampleRows = new ArrayList<DescriptorDictionary>(); //need to fill empty key Values
		for(DescriptorDictionary d : sampleDictionaries){
			DescriptorDictionary sample = new DescriptorDictionary();
			sample.setName(d.getName());
			for(Descriptor descrip : template.descriptors){
				//if the sample has the descriptor, add it, else a key with empty value
				sample.descriptors.add(new Descriptor(descrip.getKey(), d.getValue(descrip.getKey())));
			}
			sampleRows.add(sample);
		}

		int rowCount = sampleDictionaries.size() + 1;
		int columnCount = template.descriptors.size();
		GridBase grid = new GridBase(rowCount, columnCount);

		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
		//row1 = Sample, Length, Density, etc. Template values.
		final ObservableList<SpreadsheetCell> headers = FXCollections.observableArrayList();
		//headers.add(SpreadsheetCellType.STRING.createCell(1, 1, 1, 1, "Sample Name"));
		int index = 0;
		for(Descriptor descrip : template.descriptors){
			SpreadsheetCell h = SpreadsheetCellType.STRING.createCell(0, index, 1, 1, descrip.getKey());
			h.getStyleClass().add("row_header");
			headers.add(h);
			index++;
		}
		rows.add(headers);
		int row = 1;
		for(DescriptorDictionary dict : sampleRows){
			final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
			int column = 0;
			//list.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,d.getValue()));
			for(Descriptor d : dict.descriptors){
				SpreadsheetCell h = SpreadsheetCellType.STRING.createCell(row, column, 1, 1,d.getValue());
				if(row % 2 == 0)
					h.getStyleClass().add("gray_background");
				list.add(h);
				column++;
			}
			rows.add(list);
			row++;
		}

		grid.setRows(rows);
		SpreadsheetView spv = new SpreadsheetView(grid);

		allSamplesKeyValueTableVBox.getChildren().add(spv);
		VBox.setVgrow(spv, Priority.ALWAYS);
	}

	private void recursivelyLoadSampleParametersDictionary(File dir, ArrayList<DescriptorDictionary> list) { //Warning removed added string parameter
		//dir = home directory
		//list = list to fill

		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				recursivelyLoadSampleParametersDictionary(file,list); //recursive call
			} else {
				
				boolean isSampleFile = SampleTypes.getSampleConstantsMap().values().stream()
					.filter(constants -> file.getName().endsWith(constants.getExtension()))
					.findFirst().isPresent();
				
				if(isSampleFile) {
					try {
						Sample sample = SPOperations.loadSampleParametersOnly(file.getPath());
						DescriptorDictionary d = sample.createAllParametersDecriptorDictionary();
						list.add(d);
					} catch (ZipException e) {
						e.printStackTrace();
					}
				} else {
					System.out.println("Failed to load sample for populating the workspace table: " + file.getName());
				}
			}
		}
	} 

	public void updateDataListView(){
		dataListView.getItems().clear();
		for(DataSubset d : sampleDataFiles.getAllDatasets()){
			dataListView.getItems().add(d);
		}
	}

	public void selectedPreviousSampleChanged() {
		String path = getPathFromTreeViewItem(selectedPreviousSamplesTreeItem);
		File file = new File(path);
		if(file.isDirectory()){
			System.out.println("Directory cannot be a sample file.");
			return;
		}

		File newDir = file;

		if(!newDir.exists()){
			System.out.println("Sample doesn't exist");
			return;
		}

		Sample currentSample = null;

		try {
			currentSample = SPOperations.loadSample(newDir.getPath());
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		sampleDataFiles = currentSample.DataFiles;
		
		setSampleParameterTextFieldsFromSample(currentSample);
		
		setSelectedBarSetup(currentSample.barSetup);
		
		descriptorDictionary = currentSample.descriptorDictionary;
		
		updateDescriptorTable();
		updateDataListView();
	}

	private void setSampleParameterTextFieldsFromSample(Sample currentSample) {
		clearTextFields();

		tbName.setText(currentSample.getName());

		if(currentSample.getDateSaved() > 0)
			dateSavedLabel.setText(Converter.getFormattedDate(new Date(currentSample.getDateSaved())));

		if(metricCB.isSelected()){
			//metric convert
			if(currentSample.getDensity() > 0)
				tbDensity.setNumberText(Double.toString(Converter.gccFromKgm3(currentSample.getDensity())));
			if(currentSample.getYoungsModulus() > 0)
				tbYoungsMod.setNumberText(Double.toString(currentSample.getYoungsModulus() / Math.pow(10, 9)));
			if(currentSample.getHeatCapacity() > 0)
				tbHeatCapacity.setNumberText(Double.toString(currentSample.getHeatCapacity()));
			if(currentSample.strikerBar.getDensity()>0.0){
			tbStrikerBarDensity.setNumberText(Double.toString(Converter.gccFromKgm3(currentSample.strikerBar.getDensity())));
			}
			if(currentSample.strikerBar.getLength()>0.0){
			tbStrikerBarLength.setNumberText(Double.toString(Converter.mmFromM(currentSample.strikerBar.getLength())));
			}
			if(currentSample.strikerBar.getDiameter()>0.0){
			tbStrikerBarDiameter.setNumberText(Double.toString(Converter.mmFromM(currentSample.strikerBar.getDiameter())));
			}
			if(currentSample.strikerBar.getSpeed()>0.0){
			tbStrikerBarSpeed.setNumberText(Double.toString(currentSample.strikerBar.getSpeed()));
			}
			
			
			if(currentSample instanceof HopkinsonBarSample){
				tbLength.setNumberText(Double.toString(((HopkinsonBarSample)currentSample).getLength() * 1000));
			}

			if(currentSample instanceof CompressionSample) {
				CompressionSample comp = (CompressionSample)currentSample;
				if(comp.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString((comp).getDiameter() * 1000));
				sampleType.getSelectionModel().select(CompressionSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof ShearCompressionSample) {
				ShearCompressionSample smp = (ShearCompressionSample)currentSample;
				if(smp.getGaugeWidth() > 0)
					tbGaugeWidth.setText(Double.toString((smp).getGaugeWidth()   * 1000));
				if(smp.getGaugeHeight() > 0)
					tbGaugeHeight.setText(Double.toString((smp).getGaugeHeight() * 1000));
				sampleType.getSelectionModel().select(ShearCompressionSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TensionRectangularSample) {
				TensionRectangularSample ten = (TensionRectangularSample)currentSample;
				if(ten.getWidth() > 0)
					tbWidth.setNumberText(Double.toString((ten).getWidth()  * 1000));
				if(ten.getHeight() > 0)
					tbHeight.setNumberText(Double.toString((ten).getHeight() * 1000));
				sampleType.getSelectionModel().select(TensionRectangularSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TensionRoundSample) {
				TensionRoundSample rnd = (TensionRoundSample)currentSample;
				if(rnd.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString((rnd).getDiameter() * 1000));
				sampleType.getSelectionModel().select(TensionRoundSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof LoadDisplacementSample){
				sampleType.getSelectionModel().select(LoadDisplacementSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TorsionSample)
			{
				TorsionSample s = (TorsionSample)currentSample;
				if(s.getInnerDiameter() > 0)
					tbInnerDiameter.setNumberText(Double.toString(s.getInnerDiameter() * 1000));
				if(s.getOuterDiameter() > 0)
					tbOuterDiameter.setNumberText(Double.toString(s.getOuterDiameter() * 1000));
				if(s.getLength() > 0)
					tbLength.setNumberText(Double.toString(s.getLength() * 1000));
				sampleType.getSelectionModel().select(TorsionSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof BrazilianTensileSample)
			{
				BrazilianTensileSample s = (BrazilianTensileSample)currentSample;
				if(s.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(s.getDiameter() * 1000));
				if(s.getLength() > 0)
					tbLength.setNumberText(Double.toString(s.getLength() * 1000));
				sampleType.getSelectionModel().select(BrazilianTensileSample.getSampleConstants().getShortName());
			}
			else{
				System.err.println("This sample type is not implemented (metric): " + currentSample);
			}
		}
		else{
			//english convert
			if(currentSample.getDensity() > 0)
				tbDensity.setNumberText(Double.toString(Converter.Lbin3FromKgM3(currentSample.getDensity())));
			if(currentSample.getYoungsModulus() > 0)
				tbYoungsMod.setNumberText(Double.toString(Converter.MpsiFromPa(currentSample.getYoungsModulus())));
			if(currentSample.getHeatCapacity() > 0)
				tbHeatCapacity.setNumberText(Double.toString(Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(currentSample.getHeatCapacity())));
			if(currentSample.strikerBar.getDensity()>0.0){
				tbStrikerBarDensity.setNumberText(Double.toString(Converter.Lbin3FromKgM3(currentSample.strikerBar.getDensity())));
			}
			if(currentSample.strikerBar.getLength()>0.0){
				tbStrikerBarLength.setNumberText(Double.toString(Converter.InchFromMeter(currentSample.strikerBar.getLength())));
			}
			if(currentSample.strikerBar.getDiameter()>0.0){
				tbStrikerBarDiameter.setNumberText(Double.toString(Converter.InchFromMeter(currentSample.strikerBar.getDiameter())));
			}
			if(currentSample.strikerBar.getSpeed()>0.0){
					tbStrikerBarSpeed.setNumberText(Double.toString(Converter.FootFromMeter(currentSample.strikerBar.getSpeed())));
			}

			if(currentSample instanceof HopkinsonBarSample){
				tbLength.setNumberText(Double.toString(Converter.InchFromMeter(((HopkinsonBarSample)currentSample).getLength())));
			}

			if(currentSample instanceof CompressionSample) {
				CompressionSample sam = (CompressionSample)currentSample;
				if(sam.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(Converter.InchFromMeter((sam).getDiameter())));
				sampleType.getSelectionModel().select(CompressionSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof ShearCompressionSample) {
				ShearCompressionSample shear = (ShearCompressionSample)currentSample;
				if(shear.getGaugeWidth() > 0)
					tbGaugeWidth.setText(Double.toString(Converter.InchFromMeter((shear).getGaugeWidth())));
				if(shear.getGaugeHeight() > 0)
					tbGaugeHeight.setText(Double.toString(Converter.InchFromMeter((shear).getGaugeHeight())));
				sampleType.getSelectionModel().select(ShearCompressionSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TensionRectangularSample) {
				TensionRectangularSample tenRect = (TensionRectangularSample)currentSample;
				if(tenRect.getWidth() > 0)
					tbWidth.setNumberText(Double.toString(Converter.InchFromMeter((tenRect).getWidth())));
				if(tenRect.getHeight() > 0)
					tbHeight.setNumberText(Double.toString(Converter.InchFromMeter((tenRect).getHeight())));
				sampleType.getSelectionModel().select(TensionRectangularSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TensionRoundSample) {
				TensionRoundSample rnd = (TensionRoundSample)currentSample;
				if(rnd.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(Converter.InchFromMeter((rnd).getDiameter())));
				sampleType.getSelectionModel().select(TensionRoundSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof LoadDisplacementSample){
				sampleType.getSelectionModel().select(LoadDisplacementSample.getSampleConstants().getShortName());
			}
			else if(currentSample instanceof TorsionSample) {
				TorsionSample s = (TorsionSample)currentSample;
				if(s.getInnerDiameter() > 0)
					tbInnerDiameter.setNumberText(Double.toString(Converter.InchFromMeter(s.getInnerDiameter())));
				if(s.getOuterDiameter() > 0)
					tbOuterDiameter.setNumberText(Double.toString(Converter.InchFromMeter(s.getOuterDiameter())));
				if(s.getLength() > 0)
					tbLength.setNumberText(Double.toString(Converter.InchFromMeter(s.getLength())));
				sampleType.getSelectionModel().select(TorsionSample.getSampleConstants().getShortName());
				
			}
			else if(currentSample instanceof BrazilianTensileSample)
			{
				BrazilianTensileSample s = (BrazilianTensileSample)currentSample;
				if(s.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(Converter.InchFromMeter(s.getDiameter())));
				if(s.getLength() > 0)
					tbLength.setNumberText(Double.toString(Converter.InchFromMeter(s.getLength())));
				sampleType.getSelectionModel().select(BrazilianTensileSample.getSampleConstants().getShortName());
			}
			else{
				System.err.println("This sample type is not implemented (english): " + currentSample);
			}
		}



	}

	private String getPathFromTreeViewItem(TreeItem<FileFX> item) {
		if(item == null)
		{
			System.out.println("cannot get path from null tree object.");
			return "";
		}
		return item.getValue().file.getPath();
	}

	public void refreshSamples(){
		updateTreeViews();
		updateDataListView();
	}

	private void updateTreeViews(){
		File home = new File(treeViewHomePath);
		if(home.exists()){
			findFiles(home, null, previousSamplesTreeView);
			findFilesWithDragDropCapabilities(home, null, saveSampleTreeView);
		}


	}

	private void findFilesWithDragDropCapabilities(File dir, TreeItem<FileFX> parent, TreeView<FileFX> tree) { //Warning removed added string parameter

		FileFX filefx = new FileFX(dir);
		TreeItem<FileFX> root = new TreeItem<FileFX>(filefx, getRootIcon());

		root.setExpanded(true);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root,tree);
			} else {
				SampleTypes.getSampleConstantsMap().values().forEach(constants -> {
					if(file.getName().endsWith(constants.getExtension())){
						root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(constants.getIconLocation())));
					}
				});
			}
		}
		if(parent==null){
			tree.setRoot(root);
		} else {
			parent.getChildren().add(root);
		}
	} 

	private void findFiles(File dir, TreeItem<FileFX> parent, TreeView<FileFX> tree) { //Warning removed added string parameter
		FileFX filefx = new FileFX(dir);
		TreeItem<FileFX> root = new TreeItem<FileFX>(filefx , getRootIcon());

		root.setExpanded(false);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root,tree);
			} else {
				SampleTypes.getSampleConstantsMap().values().forEach(fileInfo -> {
					if(file.getName().endsWith(fileInfo.getExtension())){
						TreeItem i = new TreeItem<>(new FileFX(file), SPOperations.getIcon(fileInfo.getIconLocation()));
						root.getChildren().add(i);
					}
				});
			}
		}

		if(parent==null){
			tree.setRoot(root);
			root.setExpanded(true);
		} else {

			parent.getChildren().add(root);
		}
	} 

	private Node getRootIcon(){
		// return rootIconNode;
		ImageView rootIcon = new ImageView(
				folderIconImage
				);
		rootIcon.setFitHeight(16);
		rootIcon.setFitWidth(16);
		Node a = rootIcon;
		return a;
	}

	public void initializeDynamicFields() {

		tbName = new TextField();
		tbLength = new NumberTextField("inches", "mm");
		tbDiameter = new NumberTextField("inches", "mm");
		tbWidth = new NumberTextField("inches", "mm"); 
		tbHeight = new NumberTextField("inches", "mm");
		tbGaugeHeight = new NumberTextField("inches", "mm");
		tbGaugeWidth = new NumberTextField("inches", "mm");
		tbDensity = new NumberTextField("Lb/in^3", "g/cc");
		tbYoungsMod = new NumberTextField("psi*10^6", "GPa");
		tbHeatCapacity = new NumberTextField("Btu/Lb/F", "J/K");
		tbInnerDiameter = new NumberTextField("inches", "mm");
		tbOuterDiameter = new NumberTextField("inches", "mm");
		tbStrikerBarDensity = new NumberTextField("Lb/in^3", "g/cc");
		tbStrikerBarLength = new NumberTextField("in", "mm");
		tbStrikerBarDiameter = new NumberTextField("in", "mm");
		tbStrikerBarSpeed = new NumberTextField("ft/s", "m/s");
	}

	private void clearSampleParameterGrid(){
		if(sampleParameterGrid.getChildren().size() > 5) //this is copied below to the load displacement configuration. 
			sampleParameterGrid.getChildren().remove(4, sampleParameterGrid.getChildren().size());
	}

	private void setVisiblePreferences(String sampleTypeSelection) {
		boolean loadDisplacement = sampleTypeSelection.equals("Load Displacement");

		String required = loadDisplacement ? "" : ""; // For now, required feilds are full opacity while non-required are greyish

		clearSampleParameterGrid();

		Label densityLabel = new Label("Density");
		Label youngsModulusLabel = new Label("Young's Modulus");
		Label heatCapacityLabel = new Label("Heat Capacity");
		Label strikerBarDensityLabel = new Label("Striker Bar Density");
		Label strikerBarLengthLabel = new Label("Striker Bar Length");
		Label strikerBarDiameterLabel = new Label("Striker Bar Diameter");
		Label strikerBarSpeedLabel = new Label("Striker Bar Speed");
		double opacity = .7;
		densityLabel.setOpacity(opacity);
		youngsModulusLabel.setOpacity(opacity);
		heatCapacityLabel.setOpacity(opacity);
		strikerBarDensityLabel.setOpacity(opacity);
		strikerBarLengthLabel.setOpacity(opacity);
		strikerBarDiameterLabel.setOpacity(opacity);
		strikerBarSpeedLabel.setOpacity(opacity);
		int i = 2, j = 2;
		switch (sampleTypeSelection) {
		case "Compression":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Diameter"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDiameter, 1, j++);
			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
			break;
		case "Shear Compression":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Gauge Height"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Gauge Width"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbGaugeHeight, 1, j++);
			sampleParameterGrid.add(tbGaugeHeight.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbGaugeWidth, 1, j++);
			sampleParameterGrid.add(tbGaugeWidth.unitLabel, 1, j-1);
			break;
		case "Tension Rectangular":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Width"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Height"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbWidth, 1, j++);
			sampleParameterGrid.add(tbWidth.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeight, 1, j++);
			sampleParameterGrid.add(tbHeight.unitLabel, 1, j-1);
			break;
		case "Tension Round":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Diameter"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDiameter, 1, j++);
			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
			break;
		}

		//default parameters. Cleared if its a load-displacement or Torsion sample.
		sampleParameterGrid.add(densityLabel, 0, i++);
		sampleParameterGrid.add(youngsModulusLabel, 0, i++);
		sampleParameterGrid.add(heatCapacityLabel, 0, i++);
		sampleParameterGrid.add(strikerBarDensityLabel, 0, i++);
		sampleParameterGrid.add(strikerBarLengthLabel, 0, i++);
		sampleParameterGrid.add(strikerBarDiameterLabel, 0, i++);
		sampleParameterGrid.add(strikerBarSpeedLabel, 0, i++);

		sampleParameterGrid.add(tbDensity, 1, j++);
		sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbYoungsMod, 1, j++);
		sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbHeatCapacity, 1, j++);
		sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbStrikerBarDensity, 1, j++);
		sampleParameterGrid.add(tbStrikerBarDensity.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbStrikerBarLength, 1, j++);
		sampleParameterGrid.add(tbStrikerBarLength.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbStrikerBarDiameter, 1, j++);
		sampleParameterGrid.add(tbStrikerBarDiameter.unitLabel, 1, j-1);
		sampleParameterGrid.add(tbStrikerBarSpeed, 1, j++);
		sampleParameterGrid.add(tbStrikerBarSpeed.unitLabel, 1, j-1);


		if(LoadDisplacementSample.getSampleConstants().getShortName().equals(sampleTypeSelection)){
			clearSampleParameterGrid();
			i = 2; j = 2;
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
		} else if (TorsionSample.getSampleConstants().getShortName().equals(sampleTypeSelection)) {
			this.clearSampleParameterGrid();
			i = j = 2;
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(new Label("Length"), 0, i++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(new Label("Young's Modulus"), 0, i++);
			sampleParameterGrid.add(tbYoungsMod, 1, j++);
			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
			sampleParameterGrid.add(new Label("Inner Diameter"), 0, i++);
			sampleParameterGrid.add(tbInnerDiameter, 1, j++);
			sampleParameterGrid.add(tbInnerDiameter.unitLabel, 1, j-1);
			sampleParameterGrid.add(new Label("Outer Diameter"), 0, i++);
			sampleParameterGrid.add(tbOuterDiameter, 1, j++);
			sampleParameterGrid.add(tbOuterDiameter.unitLabel, 1, j-1);
			
		}
		else if (BrazilianTensileSample.getSampleConstants().getShortName().equals(sampleTypeSelection)) {
			this.clearSampleParameterGrid();
			i = j = 2;
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(new Label("Diameter"), 0, i++);
			sampleParameterGrid.add(tbDiameter, 1, j++);
			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
			sampleParameterGrid.add(new Label("Length"), 0, i++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
		}
		sampleParameterGrid.add(new Label("Date Saved"), 0, i++);
		sampleParameterGrid.add(dateSavedLabel, 1, j++);
		
	}
	@FXML
	public void addDataFileFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/NewDataFile.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/table-column-background.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.initOwner(stage);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.initModality(Modality.WINDOW_MODAL);
			NewDataFileController c = root1.<NewDataFileController>getController();
			c.stage = anotherStage;
			c.existingSampleDataFiles = sampleDataFiles;
			c.createRefreshListener();
			c.barSetup = barSetup;
			c.loadDisplacement = sampleType
					.getSelectionModel()
					.getSelectedItem()
					.equals(LoadDisplacementSample.getSampleConstants().getShortName());
			anotherStage.showAndWait();
			updateDataListView();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	public void trimSampleDataButtonFired(){
		Stage anotherStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/TrimData.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			TrimDataController c = root1.<TrimDataController>getController();

			c.DataFiles = sampleDataFiles;
			c.stage = anotherStage;
			c.barSetup = barSetup;
			c.strikerBar = createStrikerBar();
			c.isCompressionSample = sampleType
					.getSelectionModel()
					.getSelectedItem()
					.equals(CompressionSample.getSampleConstants().getShortName()) 
					|| 
					sampleType
					.getSelectionModel()
					.getSelectedItem()
					.equals(ShearCompressionSample.getSampleConstants().getShortName());
			c.isTorsionSample = sampleType
					.getSelectionModel()
					.getSelectedItem()
					.equals(TorsionSample.getSampleConstants().getShortName());
			if(c.DataFiles.size() == 0) {
				Dialogs.showInformationDialog("Trim Data", "No data files found", "You must load your sample data before trimming",stage);
				return;
			}
			c.update();
			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private StrikerBar createStrikerBar() {
		StrikerBar strikerBar = new StrikerBar();
		double strikerDensityEntry = tbStrikerBarDensity.getDouble() != -1 ? tbStrikerBarDensity.getDouble():0;
		double strikerLengthEntry = tbStrikerBarLength.getDouble() != -1 ? tbStrikerBarLength.getDouble():0;
		double strikerDiameterEntry = tbStrikerBarDiameter.getDouble() != -1 ? tbStrikerBarDiameter.getDouble():0;
		double strikerSpeedEntry = tbStrikerBarSpeed.getDouble() != -1 ? tbStrikerBarSpeed.getDouble():0;
		
		double strikerBarDensity = Converter.KgM3FromLbin3(strikerDensityEntry);
		double strikerBarLength = Converter.MeterFromInch(strikerLengthEntry);
		double strikerBarDiameter = Converter.MeterFromInch(strikerDiameterEntry);
		double strikerBarSpeed = Converter.MeterFromFoot(strikerSpeedEntry);
		if (metricCB.isSelected()) {
			strikerBarDensity = Converter.Kgm3FromGcc(strikerDensityEntry);
			strikerBarLength = Converter.mFromMm(strikerLengthEntry);
			strikerBarDiameter = Converter.mFromMm(strikerDiameterEntry);
			strikerBarSpeed = strikerSpeedEntry;
		}

		strikerBar.setDensity(strikerBarDensity);
		strikerBar.setLength(strikerBarLength);
		strikerBar.setDiameter(strikerBarDiameter);
		strikerBar.setSpeed(strikerBarSpeed);
		return strikerBar;
	}

	public void removeBarSetupButtonFired(){
		barSetup = null;
		setSelectedBarSetup(barSetup);
	}

	public void clearAllDataButtonFired(){
		if(!Dialogs.showConfirmationDialog("Confirm", "Deleting All Data", "Are you sure you want"
				+ " to clear all data?", stage))
			return;
		sampleDataFiles = new DataFileListWrapper();
		updateDataListView();
	}

	private Sample createSampleFromIngredients() {
		
		Optional<Sample> sample = SampleTypes.getSampleConstantsMap().entrySet().stream()
			.filter(entry -> entry.getValue().getShortName().equals(sampleType.getValue()))
			.findFirst()
			.map(entry -> {
				try {
					return entry.getKey().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					e.printStackTrace();
				}
				throw new RuntimeException("Failed to create an instance of the sample!");
			});
		
		if(!sample.isPresent())
		{
			System.out.println("Failed to find: " + sampleType.getValue());
			return null; // Not changing the return type for now..
		}
			
		
		if(!setSampleParameters(sample.get()))
			return null;
		
		return sample.map(s -> {
			s.barSetup = barSetup;
			s.DataFiles = sampleDataFiles;
			s.descriptorDictionary = descriptorDictionary;
			s.savedImagesLocation = savedImagesLocation;
			return s;
		}).orElse(null);
	}

	public void saveSampleButtonFired() {
		Sample sample = createSampleFromIngredients();

		if(!SPOperations.specialCharactersAreNotInTextField(tbName)) {
			Dialogs.showInformationDialog("Save Sample","Invalid Character In Sample Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}

		if(sample == null) {
			Dialogs.showErrorDialog("Save Sample", "Error: Cannot Save Sample", "Please check to make sure information you entered is correct",stage);
			return;
		}

		String path = SPOperations.getPathFromFXTreeViewItem(selectedSaveSampleTreeItem);
		if(path.equals("")){
			Dialogs.showInformationDialog("Save Sample", "There Was A Problem Saving Your Sample", "Please select a directory to save sample into.",stage);
			return;
		}
		File file = new File(path);//new File(SPSettings.Workspace.getPath() + "/" + path);

		if (!file.isDirectory()) {
			Dialogs.showInformationDialog("Save Sample", "There Was A Problem Saving Your Sample", "You selected a file, please choose a directory to save the sample into",stage);
			return;
		}

		String extension = sample.getFileExtension();
		File samplePath = new File(file.getPath() + "/" + sample.getName() + extension);

		if (samplePath.exists()) {
			boolean result = Dialogs.showOverwriteDialog(stage, "Warning", "Sample Name Already Exists", "Please rename");
			if(result)
			{
				return;
			}
			else{
				// They want to overwrite
				deleteSample(samplePath.getPath(), false);
			}
		}
		//that only checks for that type of sample
		File parent = samplePath.getParentFile();
		for(File child : parent.listFiles()){
			if(SPOperations.stripExtension(child.getName()).equals(sample.getName())){
				Dialogs.showAlert("This sample already exists as a different sample type, please rename.", stage);
				return;
			}
		}
		
		if(!samplePath.exists()) {
			if(sample.writeSampleToFile(samplePath.getPath())) {
				Dialogs.showInformationDialog("Save Sample", "Success", "Your Sample Has Been Saved",stage);
				lastSavedSample = sample;
			} else {
				Dialogs.showErrorDialog("Save Sample", "Failed To Write Sample", "The Sample Has Failed to Save",stage);
			}
		} else {
			tbName.getStyleClass().add("textbox-error");
			Dialogs.showInformationDialog("Save Sample", "There Was A Problem Saving Your Sample", "Sample already exists, please choose a unique name",stage);
			return;
		}
		updateTreeViews();

	}

	public void addBarSetupButtonFired() {
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/Calibration.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			CalibrationController c = root1.<CalibrationController>getController();
			c.stage = anotherStage;
			c.barSetupMode = BarSetupMode.ADD;
			c.barSetup = barSetup;
			c.newSampleController = this;
			c.refresh();
			c.createRefreshListener();

			anotherStage.show();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public boolean setSampleParameters(Sample sample){

		if(!validateTextFields(sample)){
			return false;
		}

		StrikerBar strikerBar = createStrikerBar();

		sample.setName(tbName.getText()); //always valid
		double length = Converter.MeterFromInch(tbLength.getDouble());
		double density = Converter.KgM3FromLbin3(tbDensity.getDouble());
		double youngs = Converter.paFromMpsi(tbYoungsMod.getDouble());
		double heatCapacity = Converter
				.JoulesPerKilogramKelvinFromButanesPerPoundFarenheit(tbHeatCapacity.getDouble());

		if (metricCB.isSelected()) {
			length = tbLength.getDouble() / Math.pow(10, 3);
			density = Converter.Kgm3FromGcc(tbDensity.getDouble());
			youngs = tbYoungsMod.getDouble() * Math.pow(10, 9);
			heatCapacity = tbHeatCapacity.getDouble();
		}

		sample.setDensity(density);
		sample.setYoungsModulus(youngs);
		sample.setHeatCapacity(heatCapacity);

		sample.strikerBar = strikerBar;
		//common parameters done
		
		if(sample instanceof HopkinsonBarSample){
			((HopkinsonBarSample)sample).setLength(length);
		}
		
		if(sample instanceof CompressionSample){
			double diameter = Converter.MeterFromInch(tbDiameter.getDouble());
			if(metricCB.isSelected())
				diameter = tbDiameter.getDouble() / Math.pow(10, 3);

			((CompressionSample) sample).setDiameter(diameter);
		}
		else if(sample instanceof TensionRoundSample){
			double diameter = Converter.MeterFromInch(tbDiameter.getDouble());
			if(metricCB.isSelected())
				diameter = tbDiameter.getDouble() / Math.pow(10, 3);

			((TensionRoundSample) sample).setDiameter(diameter);
		}
		else if(sample instanceof TensionRectangularSample){
			double height = Converter.MeterFromInch(tbHeight.getDouble());
			double width = Converter.MeterFromInch(tbWidth.getDouble());
			if(metricCB.isSelected()){
				height = tbHeight.getDouble() / Math.pow(10, 3);
				width = tbWidth.getDouble() / Math.pow(10, 3);
			}
			((TensionRectangularSample)sample).setHeight(height);
			((TensionRectangularSample)sample).setWidth(width);
		}
		else if(sample instanceof ShearCompressionSample){
			double gHeight = Converter.MeterFromInch(tbGaugeHeight.getDouble());
			double gWidth = Converter.MeterFromInch(tbGaugeWidth.getDouble());
			if(metricCB.isSelected()){
				gHeight = tbGaugeHeight.getDouble() / Math.pow(10, 3);
				gWidth = tbGaugeWidth.getDouble() / Math.pow(10, 3);
			}
			((ShearCompressionSample)sample).setGaugeHeight(gHeight);
			((ShearCompressionSample)sample).setGaugeWidth(gWidth);
		}
		else if(sample instanceof TorsionSample) {
			
			double innerDiameter = Converter.MeterFromInch(tbInnerDiameter.getDouble());
			double outerDiameter = Converter.MeterFromInch(tbOuterDiameter.getDouble());
			if(metricCB.isSelected()) {
				innerDiameter = tbInnerDiameter.getDouble() / Math.pow(10, 3);
				outerDiameter = tbOuterDiameter.getDouble() / Math.pow(10, 3);
			}
			((TorsionSample)sample).setInnerDiameter(innerDiameter);
			((TorsionSample)sample).setOuterDiameter(outerDiameter);
			((TorsionSample)sample).setLength(length);
		}
		else if(sample instanceof BrazilianTensileSample) {
			double diameter;
			if(metricCB.isSelected()) {
				diameter = tbDiameter.getDouble() / Math.pow(10,3);
			} else {
				diameter = Converter.MeterFromInch(tbDiameter.getDouble());
			}
			((BrazilianTensileSample)sample).setDiameter(diameter);
			((BrazilianTensileSample)sample).setLength(length);
		}

		return true;
	}

	private boolean validateTextFields(Sample sample) {
		tbName.getStyleClass().remove("textbox-error");
		tbName2.getStyleClass().remove("textbox-error");
		tbLength.getStyleClass().remove("textbox-error");
		tbDiameter.getStyleClass().remove("textbox-error");
		tbDensity.getStyleClass().remove("textbox-error");
		tbYoungsMod.getStyleClass().remove("textbox-error");
		tbHeatCapacity.getStyleClass().remove("textbox-error");
		tbInnerDiameter.getStyleClass().remove("textbox-error");
		tbOuterDiameter.getStyleClass().remove("textbox-error");
		tbStrikerBarDensity.getStyleClass().remove("textbox-error");
		tbStrikerBarLength.getStyleClass().remove("textbox-error");
		tbStrikerBarDiameter.getStyleClass().remove("textbox-error");
		tbStrikerBarSpeed.getStyleClass().remove("textbox-error");
		//validate text boxes first, then do set sample params
		if(!validate(tbName)){
			tbName.getStyleClass().add("textbox-error");
			return false;
		}

		if(sample instanceof LoadDisplacementSample)
			return true;
		else{
			if(!validate(tbLength)){
				tbLength.getStyleClass().add("textbox-error");
				return false;
			}
		}

		if(sample instanceof CompressionSample){
			if(!validate(tbDiameter)){
				tbDiameter.getStyleClass().add("textbox-error");
				return false;
			}
		}
		else if(sample instanceof TensionRoundSample){
			if(!validate(tbDiameter)){
				tbDiameter.getStyleClass().add("textbox-error");
				return false;
			}
		}
		else if(sample instanceof TensionRectangularSample){
			if(!validate(tbHeight)){
				tbHeight.getStyleClass().add("textbox-error");
				return false;
			}
			if(!validate(tbWidth)){
				tbWidth.getStyleClass().add("textbox-error");
				return false;
			}
		}
		else if(sample instanceof ShearCompressionSample){
			if(!validate(tbGaugeHeight)){
				tbGaugeHeight.getStyleClass().add("textbox-error");
				return false;
			}
			if(!validate(tbGaugeWidth)){
				tbGaugeWidth.getStyleClass().add("textbox-error");
				return false;
			}
		}
		else if(sample instanceof TorsionSample) {
			List<NumberTextField> invalidTbs = Stream.of(tbInnerDiameter, tbOuterDiameter)
					.filter(tb -> !validate(tb))
					.collect(Collectors.toList());
			invalidTbs.stream().forEach(tb -> tb.getStyleClass().add("textbox-error"));
			if(invalidTbs.size() > 0)
				return false;
		}
		else if(sample instanceof BrazilianTensileSample) {
			List<NumberTextField> invalidTbs = Stream.of(tbLength, tbDiameter)
					.filter(tb -> !validate(tb))
					.collect(Collectors.toList());
			invalidTbs.stream().forEach(tb -> tb.getStyleClass().add("textbox-error"));
			if(invalidTbs.size() > 0)
				return false;
		}
		return true;
	}

	public boolean loadDisplacementSelected(){
		return sampleType
				.getSelectionModel()
				.getSelectedItem()
				.equals(LoadDisplacementSample.getSampleConstants().getShortName());
	}

	public boolean setSelectedBarSetup(BarSetup barSetup) {
		
		// Check if bar setup is valid
		if(barSetup != null && sampleType.getSelectionModel().getSelectedItem().equals(TorsionSample.getSampleConstants().getShortName())) {
			double poissonsRatio = barSetup.IncidentBar.getPoissonsRatio();
			if(poissonsRatio <= 0 || poissonsRatio >= 1) {

				return false;
			}
		}
		
		this.barSetup = barSetup;
		
		if(barSetup != null && barSetup.name != null){
			currentSelectedBarSetupLabel.setText(barSetup.name);
			currentSelectedBarSetupLabel.setTextFill(Color.BLACK);
		}
		else{
			currentSelectedBarSetupLabel.setText("Not Selected");
			currentSelectedBarSetupLabel.setTextFill(Color.RED);
		}
		return true;
	}

	private boolean validate(NumberTextField tb) {
		if(tb.isRequired && tb.getText().length() == 0)
			return false;
		else
			return true;
	}

	private boolean validate(TextField tb) {
		if(tb.getText().length() == 0)
			return false;
		else
			return true;
	}


	private void clearTextFields() {
		tbName.setText("");
		tbLength.setText("");
		tbDiameter.setText("");
		tbWidth.setText(""); 
		tbHeight.setText("");
		tbGaugeHeight.setText("");
		tbGaugeWidth.setText("");
		tbDensity.setText("");
		tbYoungsMod.setText("");
		tbHeatCapacity.setText("");
		tbStrikerBarDensity.setText("");
		tbStrikerBarLength.setText("");
		tbStrikerBarDiameter.setText("");
		tbStrikerBarSpeed.setText("");
		tbInnerDiameter.setText("");
		tbOuterDiameter.setText("");
	}

	public void onNextButtonClicked() {
		tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() + 1);
	}

	public void onBackButtonClicked() {
		if(tabPane.getSelectionModel().getSelectedIndex() == 0) {
			doneCreatingSampleButtonFired();
		}
		tabPane.getSelectionModel().select(tabPane.getSelectionModel().getSelectedIndex() - 1);
	}

	public void doneCreatingSampleButtonFired() {
		if(lastSavedSample == null || !noChangesMadeToSample()) {

			if(Dialogs.showConfirmationDialog("Sample Not Saved", "You have unsaved changes to this sample", "Are you sure you want to proceed? All unsaved changes will be lost.",stage)) {
				stage.close();
			}
		} else {
			stage.close();
		}
	}

	public SplashPageController parent;

	public void clearAllButtonFired() {

		if(Dialogs.showConfirmationDialog("Clear All", "You are about to remove all of your entered data and start over", "Are you sure you want to proceed?",stage)) {
			if(parent!=null) {
				parent.newSampleFired();
				stage.close();
			}
		}
	}

	private boolean noChangesMadeToSample() {
		Sample sample = createSampleFromIngredients();
		DataFileListWrapper data = sample.DataFiles;
		BarSetup barsetup = sample.barSetup;
		if(data.equals(lastSavedSample.DataFiles) && barsetup.equals(lastSavedSample.barSetup)) {
			return true;
		}
		return false;
	}

	public void createNewSampleButtonFired() {
		if(lastSavedSample == null || !noChangesMadeToSample()) {

			if(Dialogs.showConfirmationDialog("Sample Not Saved", "You have unsaved changes to this sample", "Are you sure you want to proceed? All unsaved changes will be lost.",stage)) {
				prepareForNewSample();
			}
		} else {
			prepareForNewSample();
		}
	}

	private void prepareForNewSample(){
		sampleDataFiles = new DataFileListWrapper();
		tbName.setText("");
		tbDiameter.setText("");
		tbLength.setText("");
		tbHeight.setText("");
		tbWidth.setText("");
		tbGaugeHeight.setText("");
		tbGaugeWidth.setText("");
		tbInnerDiameter.setText("");
		tbOuterDiameter.setText("");
		tbStrikerBarSpeed.setText("");
		updateDataListView();
		tabPane.getSelectionModel().select(0);
	}

	public void analyzeResultsButtonFired() {
		//try {
		new AnalyzeMain().start(new Stage());
		//			if(!SPOperations.launchSureAnalyze(stage, new HomeController().getClass().getResource("/net/relinc/viewer/GUI/Home.fxml")));
		//			{
		//				Dialogs.showErrorDialog("Error Launching SURE-Pulse Viewer", "SURE-Pulse Viewer has either been moved or does not exist on this machine", "Please install SURE-Pulse Viewer, contact REL Inc if the problem persists",stage);
		//			}
		//		} catch (IOException e) {
		//			e.printStackTrace();
		//		}
	}

	public void metricCBAction() {		
		toggleUnits();
	}

	private void toggleUnits() {
		convertTextFieldValues();
		updateLabelUnits();
	}

	private void convertTextFieldValues() {
		if(!SPSettings.metricMode.getValue()) {
			Converter.convertTBValueFromMMToInch(tbLength);
			Converter.convertTBValueFromGramsPerCCtoLbsPerCubicInch(tbDensity);
			Converter.convertTBValueFromMMToInch(tbGaugeHeight);
			Converter.convertTBValueFromMMToInch(tbWidth);
			Converter.convertTBValueFromMMToInch(tbHeight);
			Converter.convertTBValueFromMMToInch(tbDiameter);
			Converter.convertTBValueFromMMToInch(tbGaugeWidth);
			Converter.convertTBValueFromGigapascalsPsiTimesTenToTheSixth(tbYoungsMod);
			Converter.convertTBValueFromButanesPerPoundFarenheitFromJoulesPerKilogramKelvin(tbHeatCapacity);
			Converter.convertTBValueFromMMToInch(tbInnerDiameter);
			Converter.convertTBValueFromMMToInch(tbOuterDiameter);
			Converter.convertTBValueFromGramsPerCCtoLbsPerCubicInch(tbStrikerBarDensity);
			Converter.convertTBValueFromMMToInch(tbStrikerBarLength);
			Converter.convertTBValueFromMMToInch(tbStrikerBarDiameter);
			Converter.convertTBValueFromMToFeet(tbStrikerBarSpeed);
		} else {
			Converter.convertTBValueFromInchToMM(tbLength);
			Converter.convertTBValueFromLbsPerCubicInchtoGramsPerCC(tbDensity);
			Converter.convertTBValueFromInchToMM(tbGaugeHeight);
			Converter.convertTBValueFromInchToMM(tbWidth);
			Converter.convertTBValueFromInchToMM(tbHeight);
			Converter.convertTBValueFromInchToMM(tbDiameter);
			Converter.convertTBValueFromInchToMM(tbGaugeWidth);
			Converter.convertTBValueFromPsiTimesTenToTheSixthToGigapascals(tbYoungsMod);
			Converter.convertTBValueFromJoulesPerKilogramKelvinFromButanesPerPoundFarenheit(tbHeatCapacity);
			Converter.convertTBValueFromInchToMM(tbInnerDiameter);
			Converter.convertTBValueFromInchToMM(tbOuterDiameter);
			Converter.convertTBValueFromLbsPerCubicInchtoGramsPerCC(tbStrikerBarDensity);
			Converter.convertTBValueFromInchToMM(tbStrikerBarLength);
			Converter.convertTBValueFromInchToMM(tbStrikerBarDiameter);
			Converter.convertTBValueFromFeetToM(tbStrikerBarSpeed);
		}
	}

	private void updateLabelUnits() {
		tbDensity.updateTextFieldLabelUnits();
		tbWidth.updateTextFieldLabelUnits();
		tbGaugeHeight.updateTextFieldLabelUnits();
		tbGaugeWidth.updateTextFieldLabelUnits();
		tbDiameter.updateTextFieldLabelUnits();
		tbHeatCapacity.updateTextFieldLabelUnits();
		tbYoungsMod.updateTextFieldLabelUnits();
		tbHeight.updateTextFieldLabelUnits();
		tbLength.updateTextFieldLabelUnits();
		tbInnerDiameter.updateTextFieldLabelUnits();
		tbOuterDiameter.updateTextFieldLabelUnits();
		tbStrikerBarDensity.updateTextFieldLabelUnits();
		tbStrikerBarLength.updateTextFieldLabelUnits();
		tbStrikerBarDiameter.updateTextFieldLabelUnits();
		tbStrikerBarSpeed.updateTextFieldLabelUnits();
	}


	public void picoScopeButtonFired() {
		PicoScopeCLI pico = new PicoScopeCLI(PicoScopeCLI.PICO_VERSION_3000);
		pico.startPico();
	}

}
