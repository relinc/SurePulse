package net.relinc.processor.controllers;

import java.io.File;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.relinc.processor.application.Bar;
import net.relinc.processor.application.StrainGauge;
import net.relinc.processor.application.StrainGaugeOnBar;
import net.relinc.processor.fxControls.NumberTextField;
import net.relinc.processor.staticClasses.Converter;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class StrainGaugeController {
	@FXML TreeView<String> treeView;
	@FXML TextField folderNameTF;
	@FXML TextField strainGaugeNameTF;
	NumberTextField resistanceTF;
	NumberTextField lengthTF;
	NumberTextField voltageCalibratedTF;
	NumberTextField gaugeFactorTF;
	NumberTextField shuntResistanceTF; 
	NumberTextField distanceToSampleTF;
	@FXML TextField specificNameTF;
	@FXML Button addStrainGaugeButton;
	@FXML Button addFolderButton;
	@FXML Button saveStrainGaugeButton;
	
	@FXML GridPane strainGaugeGrid;
	@FXML VBox rightVBox;
	public Stage stage;
	
//	private final Node rootIcon = getRootIcon();
//	private final Node textFileIcon = getTextFileIcon();
	
	private TreeItem<String> selectedTreeItem;
	public boolean incidentBarMode;
	public boolean transmissionBarMode;
	public Bar bar;
	
	

	@FXML
	public void initialize() {
		updateTreeView();
		treeView.getSelectionModel().selectedItemProperty()
	    .addListener(new ChangeListener<TreeItem<String>>() {

	        @Override
	        public void changed(
	                ObservableValue<? extends TreeItem<String>> observable,
	                TreeItem<String> old_val, TreeItem<String> new_val) {
	            TreeItem<String> selectedItem = new_val;
	            selectedTreeItem = selectedItem;
	            selectedItemChanged();
	        }

			

	    });
		
		initializeDynamicTextFields();
	}
	
	private void initializeDynamicTextFields() {
		resistanceTF = new NumberTextField("Ohm", "Ohm");
		lengthTF = new NumberTextField("in", "mm");
		voltageCalibratedTF = new NumberTextField("v", "v");
		gaugeFactorTF = new NumberTextField("","");
		shuntResistanceTF = new NumberTextField("kOhm", "kOhm");
		distanceToSampleTF = new NumberTextField("in", "mm");
		if(SPSettings.metricMode.get())
			distanceToSampleTF.setPromptText("Distance to Sample (mm)");
		else
			distanceToSampleTF.setPromptText("Distance to Sample (in)");
		
		int j = 1;
		strainGaugeGrid.add(resistanceTF, 1, j++);
		strainGaugeGrid.add(resistanceTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(lengthTF, 1, j++);
		strainGaugeGrid.add(lengthTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(voltageCalibratedTF, 1, j++);
		strainGaugeGrid.add(voltageCalibratedTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(gaugeFactorTF, 1, j++);
		strainGaugeGrid.add(gaugeFactorTF.unitLabel, 1, j-1);
		strainGaugeGrid.add(shuntResistanceTF, 1, j++);
		strainGaugeGrid.add(shuntResistanceTF.unitLabel, 1, j-1);
		
		//rightVBox.getChildren().add(3,distanceToSampleTF.unitLabel);
		rightVBox.getChildren().add(3,distanceToSampleTF);
		//distanceToSampleTF.
	}
	
	public void addStrainGaugeFired(){
		try{
			Double.parseDouble(distanceToSampleTF.getText());
		}
		catch(Exception w){
			Dialogs.showAlert("Please enter the distance to sample",stage);
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(SPSettings.Workspace.getPath() + "/" + path);
		if(file.isDirectory()){
			Dialogs.showAlert("Please select a strain gauge to add to the bar", stage);
			return;
		}
		//TODO: Check name validity. Cannot have repeat names on a bar. Either bar? pry could
		double dist = Converter.MeterFromInch(Double.parseDouble(distanceToSampleTF.getText()));
		if(SPSettings.metricMode.get())
		{
			System.out.println("HREE");
			dist = Converter.mFromMm(Double.parseDouble(distanceToSampleTF.getText()));
		}
		
		bar.strainGauges.add(new StrainGaugeOnBar(file.getPath() + ".txt", dist, specificNameTF.getText()));
		
		Stage stage = (Stage) addStrainGaugeButton.getScene().getWindow();
	    stage.close();

	}

	public void saveStrainGaugeFired(){
		if(!SPOperations.specialCharactersAreNotInTextField(strainGaugeNameTF)) {
			Dialogs.showInformationDialog("Save Strain Gauge","Invalid Character In Strain Gauge Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		if(!checkStrainGaugeParameters()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Invalid parameters");
			alert.setHeaderText("Invalid strain gauge parameters.");
			alert.showAndWait();
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		if(path == ""){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No directory selected");
			alert.setHeaderText("Please select a directory to save into.");
			alert.showAndWait();
			return;
		}
		File file = new File(SPSettings.Workspace.getPath() + "/" + path);
		if(!file.isDirectory()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("No directory selected");
			alert.setHeaderText("Please select a directory to save into.");
			alert.showAndWait();
			return;
		}
		File newFile = new File(file.getPath() + "/" + strainGaugeNameTF.getText() + ".txt");
		if(newFile.exists()){
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Strain Gauge Already Exists");
			alert.setHeaderText("Please rename the strain gauge.");
			alert.showAndWait();
			return;
		}
		String SGName = strainGaugeNameTF.getText();
		double resistance = resistanceTF.getDouble();
		double length = Converter.MeterFromInch(lengthTF.getDouble());
		if(SPSettings.metricMode.getValue())
			length = Converter.mFromMm(lengthTF.getDouble());
		double voltageCalibrated = voltageCalibratedTF.getDouble();
		double gaugeFactor = gaugeFactorTF.getDouble();
		double shuntResistance = shuntResistanceTF.getDouble() * 1000; //kΩ -> Ω
		
		StrainGauge SG = new StrainGauge(SGName, gaugeFactor, resistance, shuntResistance, length, voltageCalibrated);
		SPOperations.writeStringToFile(SG.stringForFile(), newFile.getPath());
		updateTreeView();
	}

	public void newFolderFired(){
		if(!SPOperations.specialCharactersAreNotInTextField(folderNameTF)) {
			Dialogs.showInformationDialog("Add Strain Gauge Folder","Invalid Character In Strain Gauge Folder Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		String path = getPathFromTreeViewItem(selectedTreeItem);
		System.out.println(path);
		if(path == null || path.trim().equals("")) {
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Please select a directory",stage);
		    return;
		}
		if(folderNameTF.getText().trim().equals("")){
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Please enter a folder name",stage);
			return;
		}
		File file = new File(SPSettings.Workspace.getPath() + "/" + path);
		if(!file.isDirectory()){
			System.out.println("Must be in directory");
			Dialogs.showInformationDialog("Failed to Create Folder", null, "You selected a file, please select a directory",stage);
			return;
		}
		File newDir = new File(file.getPath() + "/" + folderNameTF.getText());
		if(newDir.exists()){
			System.out.println("Folder already exists");
			Dialogs.showInformationDialog("Failed to Create Folder", null, "Folder already exists",stage);
			return;
		}
		newDir.mkdir();
		updateTreeView();
	}
	
	private void selectedItemChanged() {
		String path = getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(SPSettings.Workspace + "/" + path);
		if(file.isDirectory()){
			System.out.println("Directory cannot be strain gauge file.");
			return;
		}
		File newDir = new File(file.getPath() + ".txt");
		if(!newDir.exists()){
			System.out.println("Strain Gauge doesn't exist");
			return;
		}
		System.out.println("Loading: " + newDir.getPath());
		StrainGauge SG = new StrainGauge(newDir.getPath());
		
		strainGaugeNameTF.setText(SG.genericName);
		resistanceTF.setNumberText(Double.toString(SG.resistance));
		voltageCalibratedTF.setNumberText(Double.toString(SG.voltageCalibrated));
		lengthTF.setNumberText(Double.toString(Converter.InchFromMeter(SG.length)));
		if(SPSettings.metricMode.getValue())
			lengthTF.setNumberText(Double.toString(Converter.mmFromM(SG.length)));
		gaugeFactorTF.setNumberText(Double.toString(SG.gaugeFactor));
		shuntResistanceTF.setNumberText(Double.toString(SG.shuntResistance / 1000));//convert to kilo
		
	}
	
	public void refresh(){
		updateTreeView();
		if(incidentBarMode || transmissionBarMode){
			folderNameTF.setVisible(false);
			folderNameTF.setManaged(false);
			
//			strainGaugeNameTF.setEditable(false);
//			resistanceTF.setEditable(false);
//			voltageCalibratedTF.setEditable(false);
//			lengthTF.setEditable(false);
//			gaugeFactorTF.setEditable(false);
//			shuntResistanceTF.setEditable(false);
			
			strainGaugeNameTF.setDisable(true);
			resistanceTF.setDisable(true);
			voltageCalibratedTF.setDisable(true);
			lengthTF.setDisable(true);
			gaugeFactorTF.setDisable(true);
			shuntResistanceTF.setDisable(true);
			
			saveStrainGaugeButton.setVisible(false);
			saveStrainGaugeButton.setManaged(false);
			addFolderButton.setVisible(false);
			addFolderButton.setManaged(false);
			
		}
		
		
		if(incidentBarMode){
			addStrainGaugeButton.setText("Add Strain Gauge To Incident Bar");
			specificNameTF.setText("Incident SG #" + (bar.strainGauges.size() + 1));
		}
		else if(transmissionBarMode){
			addStrainGaugeButton.setText("Add Strain Gauge To Transmission Bar");
			specificNameTF.setText("Transmission SG #" + (bar.strainGauges.size() + 1));
		}
		else{
			//edit mode
			addStrainGaugeButton.setVisible(false);
			distanceToSampleTF.setVisible(false);
			specificNameTF.setVisible(false);
		}
	}
	
	private String getPathFromTreeViewItem(TreeItem<String> item) {
		if(item == null)
		{
			System.out.println("cannot get path from null tree object.");
			return "";
		}
		String path = item.getValue();
		while(item.getParent() != null){
			item = item.getParent();
			path = item.getValue() + "/" + path;
		}
		return path;
	}

	private boolean checkStrainGaugeParameters() {
		boolean passes = true;
		try{
		double d = resistanceTF.getDouble();
		d = lengthTF.getDouble();
		d = voltageCalibratedTF.getDouble();
		d = gaugeFactorTF.getDouble();
		d = shuntResistanceTF.getDouble();
		}
		catch(Exception e){
			return false;
		}
		if(strainGaugeNameTF.getText().equals(""))
			return false;
		return true;
	}
	
	private void updateTreeView(){
		File home = new File(SPSettings.Workspace.getPath() + "/Strain Gauges");
		SPOperations.findFiles(home, null, treeView, SPOperations.folderImageLocation, SPOperations.strainGaugeImageLocation);
		//findFiles(home, null);
	}
	
//	private void findFiles(File dir, TreeItem<String> parent) {
//	    TreeItem<String> root = new TreeItem<>(dir.getName(), getRootIcon());
//	    root.setExpanded(true);
//	    try {
//	        File[] files = dir.listFiles();
//	        for (File file : files) {
//	            if (file.isDirectory()) {
//	                System.out.println("directory:" + file.getCanonicalPath());
//	                findFiles(file,root);
//	            } else {
//	                if(file.getName().endsWith(".txt"))
//	                	root.getChildren().add(new TreeItem<>(file.getName().substring(0, file.getName().length() - 4),getTextFileIcon()));
//	            }
//	        }
//	        if(parent==null){
//	            treeView.setRoot(root);
//	        } else {
//	        	
//	            parent.getChildren().add(root);
//	        }
//	    } catch (IOException e) {
//	       e.printStackTrace();
//	    }
//	} 
	
}
