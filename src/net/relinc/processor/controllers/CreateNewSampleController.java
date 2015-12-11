package net.relinc.processor.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.lingala.zip4j.exception.ZipException;
import net.relinc.processor.application.BarSetup;
import net.relinc.processor.application.FileFX;
import net.relinc.processor.controllers.CalibrationController.BarSetupMode;
import net.relinc.processor.data.DataFileListWrapper;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.Descriptor;
import net.relinc.processor.data.DescriptorDictionary;
import net.relinc.processor.fxControls.NumberTextField;
import net.relinc.processor.sample.CompressionSample;
import net.relinc.processor.sample.LoadDisplacementSample;
import net.relinc.processor.sample.Sample;
import net.relinc.processor.sample.ShearCompressionSample;
import net.relinc.processor.sample.TensionRectangularSample;
import net.relinc.processor.sample.TensionRoundSample;
import net.relinc.processor.staticClasses.Converter;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class CreateNewSampleController {
	@FXML Button backButton;
	@FXML Button nextButton;
	@FXML Button buttonDoneCreatingSample;
	//@FXML Button buttonCreateNewSample;
	@FXML Button buttonAnalyzeResults;
	@FXML ListView<DataSubset> dataListView;
	@FXML HBox sampleSaveParamsHbox;

	@FXML TextField Name;
	@FXML TextField folderNameTF;
	
	@FXML CheckBox metricCB;
	@FXML TableView<Descriptor> dictionaryTableView;
	//@FXML CheckBox loadDisplacementCB;

	//This region has the ingredients to make a sample
	BarSetup barSetup = new BarSetup();
	DataFileListWrapper sampleDataFiles = new DataFileListWrapper();
	private Sample lastSavedSample;
	private DescriptorDictionary descriptorDictionary = new DescriptorDictionary();

	@FXML NumberTextField Length;
	@FXML NumberTextField Diameter;

	@FXML TextField tbName;
	@FXML TextField tbName2;
	@FXML NumberTextField tbLength;
	@FXML NumberTextField tbDiameter;
	@FXML NumberTextField tbWidth;
	@FXML NumberTextField tbHeight;
	@FXML NumberTextField tbGaugeHeight;
	@FXML NumberTextField tbGaugeWidth;
	@FXML NumberTextField tbDensity;
	@FXML NumberTextField tbYoungsMod;
	@FXML NumberTextField tbHeatCapacity;

	@FXML TreeView<FileFX> previousSamplesTreeView;
	@FXML TreeView<FileFX> saveSampleTreeView;
	TreeItem<FileFX> selectedPreviousSamplesTreeItem;
	TreeItem<FileFX> selectedSaveSampleTreeItem;

	@FXML GridPane sampleParameterGrid;
	@FXML ChoiceBox<String> sampleType;
	@FXML TabPane tabPane;

	@FXML Label currentSelectedBarSetupLabel;
	
	//private DataSubset currentSelectedDataSubset;
	
	public Stage stage;

	private String treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";

	@FXML
	public void initialize(){
		initializeDynamicFields();
		
		if(SPSettings.metricMode.getValue())
			metricCB.selectedProperty().set(true);
		
		SPSettings.metricMode.bindBidirectional(metricCB.selectedProperty());
		
		sampleType.getItems().add("Compression");
		sampleType.getItems().add("Shear Compression");
		sampleType.getItems().add("Tension Rectangular");
		sampleType.getItems().add("Tension Round");
		sampleType.getItems().add("Load Displacement");
		sampleType.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
				setVisiblePreferences(sampleType.getItems().get((Integer)newValue));
			}
		});
		sampleType.getSelectionModel().selectFirst();
		
//		loadDisplacementCB.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				setVisiblePreferences(sampleType.getSelectionModel().getSelectedItem());
//			}
//		});
		
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
				TreeItem<FileFX> selectedItem = new_val;
				selectedSaveSampleTreeItem = selectedItem;
			}
		});
		
		buttonAnalyzeResults.managedProperty().bind(buttonAnalyzeResults.visibleProperty());
		//buttonCreateNewSample.managedProperty().bind(buttonCreateNewSample.visibleProperty());
		buttonDoneCreatingSample.managedProperty().bind(buttonDoneCreatingSample.visibleProperty());
		nextButton.managedProperty().bind(nextButton.visibleProperty());

		buttonAnalyzeResults.setVisible(false);
		//buttonCreateNewSample.setVisible(false);
		buttonDoneCreatingSample.setVisible(false);
		
//		dataListView.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
//			@Override
//			public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
//				currentSelectedDataSubset = dataListView.getSelectionModel().getSelectedItem();
//			}
//		});
		
		tabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
		    @Override
		    public void changed(ObservableValue<? extends Number> ov, Number oldValue, Number newValue) {
				if(newValue.intValue() == tabPane.getTabs().size() - 1) {
					nextButton.setVisible(false);
					buttonAnalyzeResults.setVisible(true);
					//buttonCreateNewSample.setVisible(true);
					buttonDoneCreatingSample.setVisible(true);
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
		
		//sex on a screen right here
		tbName.textProperty().bindBidirectional(tbName2.textProperty());
		
		setSelectedBarSetup(barSetup);
		descriptorDictionary.updateDictionary();
		updateDescriptorTable();
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
	
	public void deleteSampleButtonFired(){
		String path = SPOperations.getPathFromFXTreeViewItem(selectedSaveSampleTreeItem);
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
			if(Dialogs.showConfirmationDialog("Deleting Sample", "Confirm", "Are you sure you want to delete " + SPOperations.stripExtension(file.getName()) + "?", stage)){
				//new File(file.getPath() + ".zip").delete();
				file.delete();
			}
		}
		updateTreeViews();
	}
	
	public void updateDescriptorTable(){
		dictionaryTableView.getColumns().clear();
		dictionaryTableView.setEditable(true);

		descriptorDictionary.updateDictionary();
		TableColumn<Descriptor, String> key = new TableColumn<Descriptor, String>("Parameter");
        TableColumn<Descriptor, String> value = new TableColumn<Descriptor, String>("Value");
        
        key.setCellValueFactory(new PropertyValueFactory<Descriptor, String>("key"));
        key.setCellFactory(TextFieldTableCell.forTableColumn());
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
        value.setCellFactory(TextFieldTableCell.forTableColumn());
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
        
        value.setPrefWidth(200);
        key.setMinWidth(200);
        
        dictionaryTableView.getColumns().add(key);
        dictionaryTableView.getColumns().add(value);
		
        dictionaryTableView.setItems(descriptorDictionary.descriptors);

    }
	
	public void updateDataListView(){
		dataListView.getItems().clear();
		//System.out.println("Cleared list");
		for(DataSubset d : sampleDataFiles.getAllDatasets()){
			dataListView.getItems().add(d);
		}
	}

	public void selectedPreviousSampleChanged() {
		String path = getPathFromTreeViewItem(selectedPreviousSamplesTreeItem);
		File file = new File(path);
		//File file = new File(SPSettings.Workspace + "/" + path);
		if(file.isDirectory()){
			System.out.println("Directory cannot be a sample file.");
			return;
		}
	
		//it's not .zip everytime, must change
		//have to find the name in the directory, name must be unique
//		File fullSampleFile = new File("");
//		File parent = file.getParentFile();
//		for(File child : parent.listFiles()){
//			if(!child.isDirectory()){
//				if(SPOperations.stripExtension(child.getName()).equals(SPOperations.stripExtension(file.getName())))
//					fullSampleFile = child;
//			}
//				
//		}
		
		File newDir = file;//new File(file.getPath() + ".zip");
		
		if(!newDir.exists()){
			System.out.println("Sample doesn't exist");
			return;
		}
		
		Sample currentSample = null;
		
		try {
			currentSample = SPOperations.loadSample(newDir.getPath());
		} catch (ZipException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		sampleDataFiles = currentSample.DataFiles;
		
		setSampleParameterTextFieldsFromSample(currentSample);
		
		setSelectedBarSetup(currentSample.barSetup);
		
		descriptorDictionary = currentSample.descriptorDictionary;
		//dictionaryTableView.setItems(descriptorDictionary.descriptors);
		updateDescriptorTable();
		updateDataListView();
	}

	private void setSampleParameterTextFieldsFromSample(Sample currentSample) {
		clearTextFields();
		
		tbName.setText(currentSample.getName());
		
		if(metricCB.isSelected()){
			//metric convert
			if(currentSample.getLength() > 0)
				tbLength.setNumberText(Double.toString(currentSample.getLength() * 1000));
			if(currentSample.getDensity() > 0)
				tbDensity.setNumberText(Double.toString(Converter.gccFromKgm3(currentSample.getDensity())));
			if(currentSample.getYoungsModulus() > 0)
				tbYoungsMod.setNumberText(Double.toString(currentSample.getYoungsModulus() / Math.pow(10, 6)));
			if(currentSample.getHeatCapacity() > 0)
				tbHeatCapacity.setNumberText(Double.toString(currentSample.getHeatCapacity()));
			
			if(currentSample instanceof CompressionSample) {
				CompressionSample comp = (CompressionSample)currentSample;
				if(comp.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString((comp).getDiameter() * 1000));
				sampleType.getSelectionModel().select(0);
			}
			if(currentSample instanceof ShearCompressionSample) {
				ShearCompressionSample smp = (ShearCompressionSample)currentSample;
				if(smp.getGaugeWidth() > 0)
					tbGaugeWidth.setText(Double.toString((smp).getGaugeWidth()   * 1000));
				if(smp.getGaugeHeight() > 0)
					tbGaugeHeight.setText(Double.toString((smp).getGaugeHeight() * 1000));
				sampleType.getSelectionModel().select(1);
			}
			if(currentSample instanceof TensionRectangularSample) {
				TensionRectangularSample ten = (TensionRectangularSample)currentSample;
				if(ten.getWidth() > 0)
					tbWidth.setNumberText(Double.toString((ten).getWidth()  * 1000));
				if(ten.getHeight() > 0)
					tbHeight.setNumberText(Double.toString((ten).getHeight() * 1000));
				sampleType.getSelectionModel().select(2);
			}
			if(currentSample instanceof TensionRoundSample) {
				TensionRoundSample rnd = (TensionRoundSample)currentSample;
				if(rnd.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString((rnd).getDiameter() * 1000));
				sampleType.getSelectionModel().select(3);
			}
		}
		else{
			//english convert
			if(currentSample.getLength() > 0)
				tbLength.setNumberText(Double.toString(Converter.InchFromMeter(currentSample.getLength())));
			if(currentSample.getDensity() > 0)
				tbDensity.setNumberText(Double.toString(Converter.Lbin3FromKgM3(currentSample.getDensity())));
			if(currentSample.getYoungsModulus() > 0)
				tbYoungsMod.setNumberText(Double.toString(Converter.MpsiFromPa(currentSample.getYoungsModulus())));
			if(currentSample.getHeatCapacity() > 0)
				tbHeatCapacity.setNumberText(Double.toString(Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(currentSample.getHeatCapacity())));
			
			if(currentSample instanceof CompressionSample) {
				CompressionSample sam = (CompressionSample)currentSample;
				if(sam.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(Converter.InchFromMeter((sam).getDiameter())));
				sampleType.getSelectionModel().select(0);
			}
			if(currentSample instanceof ShearCompressionSample) {
				ShearCompressionSample shear = (ShearCompressionSample)currentSample;
				if(shear.getGaugeWidth() > 0)
					tbGaugeWidth.setText(Double.toString(Converter.InchFromMeter((shear).getGaugeWidth())));
				if(shear.getGaugeHeight() > 0)
					tbGaugeHeight.setText(Double.toString(Converter.InchFromMeter((shear).getGaugeHeight())));
				sampleType.getSelectionModel().select(1);
			}
			if(currentSample instanceof TensionRectangularSample) {
				TensionRectangularSample tenRect = (TensionRectangularSample)currentSample;
				if(tenRect.getWidth() > 0)
					tbWidth.setNumberText(Double.toString(Converter.InchFromMeter((tenRect).getWidth())));
				if(tenRect.getHeight() > 0)
					tbHeight.setNumberText(Double.toString(Converter.InchFromMeter((tenRect).getHeight())));
				sampleType.getSelectionModel().select(2);
			}
			if(currentSample instanceof TensionRoundSample) {
				TensionRoundSample rnd = (TensionRoundSample)currentSample;
				if(rnd.getDiameter() > 0)
					tbDiameter.setNumberText(Double.toString(Converter.InchFromMeter((rnd).getDiameter())));
				sampleType.getSelectionModel().select(3);
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
//		String path = item.getValue().
//		while(item.getParent() != null){
//			item = item.getParent();
//			path = item.getValue() + "/" + path;
//		}
//		return path;
	}

	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
			{
				updateTreeViews();
				updateDataListView();
			}
		});
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	          public void handle(WindowEvent we) {
	        	  System.out.println("Create new sample page closing.");
	              //TODO: Check if sample is saved, give warning.
	        	  File tempDataFilesFolder = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/TempSampleData");
	        	  final File[] files = tempDataFilesFolder.listFiles();
	        	  for(File f : files){
	        		  if(f.isDirectory()){
	        			  SPOperations.deleteFolder(f);
	        		  }
	        		  else{
	        			  f.delete();
	        		  }
	        	  }
	          }
	      });
	}

	private void updateTreeViews(){
		File home = new File(treeViewHomePath);
		if(home.exists()){
			findFiles(home, null, previousSamplesTreeView);
			findFiles(home, null, saveSampleTreeView);
		}
		
		
	}

	private void findFiles(File dir, TreeItem<FileFX> parent, TreeView<FileFX> tree) { //Warning removed added string parameter
		FileFX filefx = new FileFX(dir);
		TreeItem<FileFX> root = new TreeItem<FileFX>(filefx, getRootIcon());
		//TreeItem<FileFX> roo2 = new TreeItem<FileFX>();
		
		root.setExpanded(true);
		File[] files = dir.listFiles();
		//System.out.println(Arrays.toString(files));
		for (File file : files) {
			if (file.isDirectory()) {
				//System.out.println("directory:" + file.getCanonicalPath());
				findFiles(file,root,tree);
			} else {
				//String withoutExtension = SPOperations.stripExtension(file.getName());
				if(file.getName().endsWith(SPSettings.tensionRectangularExtension)){
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRectImageLocation)));
				}
				else if(file.getName().endsWith(SPSettings.tensionRoundExtension)){
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRoundImageLocation)));
				}
				else if(file.getName().endsWith(SPSettings.shearCompressionExtension)){
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));
				}
				else if(file.getName().endsWith(SPSettings.compressionExtension)){
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));
				}
				else if(file.getName().endsWith(SPSettings.loadDisplacementExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.loadDisplacementImageLocation)));
			}
		}
		if(parent==null){
			tree.setRoot(root);
		} else {

			parent.getChildren().add(root);
		}
	} 

	private Node getRootIcon(){
		ImageView rootIcon = new ImageView(
				new Image(getClass().getResourceAsStream("/net/relinc/processor/images/folderIcon.jpeg"))
				);
		rootIcon.setFitHeight(16);
		rootIcon.setFitWidth(16);
		Node a = rootIcon;
		return a;
	}

	@SuppressWarnings("unused") //Warning removed, think we can suppress this one, or just remove the method if not needed
	private Node getBarSetupIcon(){
		ImageView rootIcon = new ImageView(
				new Image(getClass().getResourceAsStream("/net/relinc/processor/images/barSetup.png"))
				);
		rootIcon.setPreserveRatio(true);
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
	}

	private void setVisiblePreferences(String sampleTypeSelection) {
//		if(loadDisplacementCB.isSelected())
//			sampleType.setDisable(true);
//		else 
//			sampleType.setDisable(false);
		boolean loadDisplacement = sampleTypeSelection.equals("Load Displacement");
		
		
		String required = loadDisplacement ? "" : "";
		
		if(sampleParameterGrid.getChildren().size() > 6) 
			sampleParameterGrid.getChildren().remove(5, sampleParameterGrid.getChildren().size());

		Label densityLabel = new Label("Density");
		Label youngsModulusLabel = new Label("Young's Modulus");
		Label heatCapacityLabel = new Label("Heat Capacity");
		double opacity = .7;
		densityLabel.setOpacity(opacity);
		youngsModulusLabel.setOpacity(opacity);
		heatCapacityLabel.setOpacity(opacity);
		int i = 2, j = 2;
		switch (sampleTypeSelection) {
		case "Compression":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Diameter"), 0, i++);
			sampleParameterGrid.add(densityLabel, 0, i++);
			sampleParameterGrid.add(youngsModulusLabel, 0, i++);
			sampleParameterGrid.add(heatCapacityLabel, 0, i++);

			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDiameter, 1, j++);
			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDensity, 1, j++);
			sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbYoungsMod, 1, j++);
			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeatCapacity, 1, j++);
			sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);

			break;
		case "Shear Compression":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Gauge Height"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Gauge Width"), 0, i++);
			sampleParameterGrid.add(densityLabel, 0, i++);
			sampleParameterGrid.add(youngsModulusLabel, 0, i++);
			sampleParameterGrid.add(heatCapacityLabel, 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbGaugeHeight, 1, j++);
			sampleParameterGrid.add(tbGaugeHeight.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbGaugeWidth, 1, j++);
			sampleParameterGrid.add(tbGaugeWidth.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDensity, 1, j++);
			sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbYoungsMod, 1, j++);
			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeatCapacity, 1, j++);
			sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);
			break;
		case "Tension Rectangular":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Width"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Height"), 0, i++);
			sampleParameterGrid.add(densityLabel, 0, i++);
			sampleParameterGrid.add(youngsModulusLabel, 0, i++);
			sampleParameterGrid.add(heatCapacityLabel, 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbWidth, 1, j++);
			sampleParameterGrid.add(tbWidth.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeight, 1, j++);
			sampleParameterGrid.add(tbHeight.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDensity, 1, j++);
			sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbYoungsMod, 1, j++);
			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeatCapacity, 1, j++);
			sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);
			break;
		case "Tension Round":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
			sampleParameterGrid.add(new Label(required + "Diameter"), 0, i++);
			sampleParameterGrid.add(densityLabel, 0, i++);
			sampleParameterGrid.add(youngsModulusLabel, 0, i++);
			sampleParameterGrid.add(heatCapacityLabel, 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
			sampleParameterGrid.add(tbLength, 1, j++);
			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDiameter, 1, j++);
			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbDensity, 1, j++);
			sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbYoungsMod, 1, j++);
			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
			sampleParameterGrid.add(tbHeatCapacity, 1, j++);
			sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);
			break;
		case "Load Displacement":
			sampleParameterGrid.add(new Label("Name"), 0, i++);
//			sampleParameterGrid.add(new Label(required + "Length"), 0, i++);
//			sampleParameterGrid.add(new Label(required + "Diameter"), 0, i++);
//			sampleParameterGrid.add(new Label("Density"), 0, i++);
//			sampleParameterGrid.add(new Label("Young's Modulus"), 0, i++);
//			sampleParameterGrid.add(new Label("Heat Capacity"), 0, i++);
			sampleParameterGrid.add(tbName, 1, j++);
//			sampleParameterGrid.add(tbLength, 1, j++);
//			sampleParameterGrid.add(tbLength.unitLabel, 1, j-1);
//			sampleParameterGrid.add(tbDiameter, 1, j++);
//			sampleParameterGrid.add(tbDiameter.unitLabel, 1, j-1);
//			sampleParameterGrid.add(tbDensity, 1, j++);
//			sampleParameterGrid.add(tbDensity.unitLabel, 1, j-1);
//			sampleParameterGrid.add(tbYoungsMod, 1, j++);
//			sampleParameterGrid.add(tbYoungsMod.unitLabel, 1, j-1);
//			sampleParameterGrid.add(tbHeatCapacity, 1, j++);
//			sampleParameterGrid.add(tbHeatCapacity.unitLabel, 1, j-1);
		}
		//treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";
		//updateTreeViews();
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
			c.loadDisplacement = sampleType.getSelectionModel().getSelectedItem().equals("Load Displacement");
			anotherStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}

	}
	public void trimSampleDataButtonFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/TrimData.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			TrimDataController c = root1.<TrimDataController>getController();
			
			//c.sample = createSampleFromIngredients();
			c.DataFiles = sampleDataFiles;
			
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
		Sample sample = null;
		switch (sampleType.getValue()) {
		case "Compression":
			sample = new CompressionSample();
			if(!setSampleParameters(sample))
				return null;
			break;
		case "Shear Compression":
			sample = new ShearCompressionSample();
			if(!setSampleParameters(sample))
				return null;
			break;
		case "Tension Rectangular":
			sample = new TensionRectangularSample();
			if(!setSampleParameters(sample))
				return null;
			break;
		case "Tension Round":
			sample = new TensionRoundSample();
			if(!setSampleParameters(sample))
				return null;
			break;
		case "Load Displacement":
			sample = new LoadDisplacementSample();
			if(!setSampleParameters(sample))
				return null;
			break;
		default:
				Dialogs.showAlert("Sample could not be created",stage);
				return null;
		}
		sample.barSetup = barSetup;
		sample.DataFiles = sampleDataFiles;
		sample.descriptorDictionary = descriptorDictionary;
		return sample;

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
		
		String extension = SPSettings.compressionExtension; //compression
		if(sample instanceof TensionRectangularSample)
			extension = SPSettings.tensionRectangularExtension;
		else if(sample instanceof TensionRoundSample)
			extension = SPSettings.tensionRoundExtension;
		else if(sample instanceof ShearCompressionSample)
			extension =  SPSettings.shearCompressionExtension;
		else if(sample instanceof LoadDisplacementSample)
			extension = SPSettings.loadDisplacementExtension;
		File samplePath = new File(file.getPath() + "/" + sample.getName() + extension);
		
		
		
		if (samplePath.exists()) {
			Dialogs.showAlert("This sample already exists. Please rename your sample.",stage);
			return;
		}
		//that only checks for that type of sample
		File parent = samplePath.getParentFile();
		for(File child : parent.listFiles()){
			if(SPOperations.stripExtension(child.getName()).equals(sample.getName())){
				Dialogs.showAlert("This sample already exists, please rename.", stage);
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
//		if(barSetup != null)
//			sample.writeBarSetupToSampleFile(testPath.getPath(), barSetup);
		updateTreeViews();
		
		
		
		
//		File testPath = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Sample Data/" + sampleType.getValue() + "/" + tbName.getText() + ".zip");
//		if(!testPath.exists()) {
//			if(sample.writeSampleToFile(testPath.getPath()))
//				clearTextFields();
//		}
//		else {
//			tbName.getStyleClass().add("textbox-error");
//			showDuplicateNameError();
//			return;
//		}
////		if(barSetup != null)
////			sample.writeBarSetupToSampleFile(testPath.getPath(), barSetup);
//		updateTreeViews();
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
			
		sample.setLength(length);
		sample.setDensity(density);
		sample.setYoungsModulus(youngs);
		sample.setHeatCapacity(heatCapacity);
		//common parameters done
		if(sample instanceof CompressionSample){
			double diameter = Converter.MeterFromInch(tbDiameter.getDouble());
			if(metricCB.isSelected())
				diameter = tbDiameter.getDouble() / Math.pow(10, 3);
			
			((CompressionSample) sample).setDiameter(diameter);
		}
		if(sample instanceof TensionRoundSample){
			double diameter = Converter.MeterFromInch(tbDiameter.getDouble());
			if(metricCB.isSelected())
				diameter = tbDiameter.getDouble() / Math.pow(10, 3);
			
			((TensionRoundSample) sample).setDiameter(diameter);
		}
		if(sample instanceof TensionRectangularSample){
			double height = Converter.MeterFromInch(tbHeight.getDouble());
			double width = Converter.MeterFromInch(tbWidth.getDouble());
			if(metricCB.isSelected()){
				height = tbHeight.getDouble() / Math.pow(10, 3);
				width = tbWidth.getDouble() / Math.pow(10, 3);
			}
			((TensionRectangularSample)sample).setHeight(height);
			((TensionRectangularSample)sample).setWidth(width);
		}
		if(sample instanceof ShearCompressionSample){
			double gHeight = Converter.MeterFromInch(tbGaugeHeight.getDouble());
			double gWidth = Converter.MeterFromInch(tbGaugeWidth.getDouble());
			if(metricCB.isSelected()){
				gHeight = tbGaugeHeight.getDouble() / Math.pow(10, 3);
				gWidth = tbGaugeWidth.getDouble() / Math.pow(10, 3);
			}
			((ShearCompressionSample)sample).setGaugeHeight(gHeight);
			((ShearCompressionSample)sample).setGaugeWidth(gWidth);
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
//		if(!validate(tbDensity)){
//			tbDensity.getStyleClass().add("textbox-error");
//			return false;
//		}
//		if(!validate(tbYoungsMod)){
//			tbYoungsMod.getStyleClass().add("textbox-error");
//			return false;
//		}
//		if(!validate(tbHeatCapacity)){
//			tbHeatCapacity.getStyleClass().add("textbox-error");
//			return false;
//		}
		 //only need name for load displacement
		
		if(sample instanceof CompressionSample){
			if(!validate(tbDiameter)){
				tbDiameter.getStyleClass().add("textbox-error");
				return false;
			}
		}
		if(sample instanceof TensionRoundSample){
			if(!validate(tbDiameter)){
				tbDiameter.getStyleClass().add("textbox-error");
				return false;
			}
		}
		if(sample instanceof TensionRectangularSample){
			if(!validate(tbHeight)){
				tbHeight.getStyleClass().add("textbox-error");
				return false;
			}
			if(!validate(tbWidth)){
				tbWidth.getStyleClass().add("textbox-error");
				return false;
			}
		}
		if(sample instanceof ShearCompressionSample){
			if(!validate(tbGaugeHeight)){
				tbGaugeHeight.getStyleClass().add("textbox-error");
				return false;
			}
			if(!validate(tbGaugeWidth)){
				tbGaugeWidth.getStyleClass().add("textbox-error");
				return false;
			}
		}
		return true;
	}
	
	public boolean loadDisplacementSelected(){
		return sampleType.getSelectionModel().getSelectedItem().equals("Load Displacement");
	}

	public void setSelectedBarSetup(BarSetup barSetup) {
		this.barSetup = barSetup;
		if(barSetup != null && barSetup.name != null){
			currentSelectedBarSetupLabel.setText(barSetup.name);
			currentSelectedBarSetupLabel.setTextFill(Color.BLACK);
		}
		else{
			currentSelectedBarSetupLabel.setText("Not Selected");
			currentSelectedBarSetupLabel.setTextFill(Color.RED);
		}
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
		updateDataListView();
		tabPane.getSelectionModel().select(0);
	}
	
	public void analyzeResultsButtonFired() {
		try {
			if(!SPOperations.launchSureAnalyze(stage))
			{
				Dialogs.showErrorDialog("Error Launching SURE-Pulse Viewer", "SURE-Pulse Viewer has either been moved or does not exist on this machine", "Please install SURE-Pulse Viewer, contact REL Inc if the problem persists",stage);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void metricCBAction() {		
		toggleUnits();
	}
	
	private void toggleUnits() {
		
//		if(metricCB.selectedProperty().getValue())
//			SPSettings.isMetric = true;
//		else
//			SPSettings.isMetric = false;
		
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
	}
	
//	public void deleteSelectedDataButtonFired() {
//		DataSubset currentSelectedDataSubset = dataListView.getSelectionModel().getSelectedItem();
//		if(currentSelectedDataSubset == null) {
//			Dialogs.showInformationDialog("Delete Data Set", "Not able to delete data", "Please choose a data set from the list above",stage);
//			return;
//		}
//		DataSubset dataSubset = currentSelectedDataSubset;
//		
//		if(Dialogs.showConfirmationDialog("Delete Data Set", currentSelectedDataSubset.toString() + " will be deleted", "Are you sure you wish to proceed?",stage)) {
//			for(DataFile data : sampleDataFiles.dataFiles) {
//					for(DataSubset subset : data.dataSubsets.datasets) {
//						if(dataSubset.equals(subset)) {
//							data.dataSubsets.datasets.remove(subset);
//							
//							break;
//						}
//					}
//			}
//		}
//		updateDataListView();
//		
//	}
}
