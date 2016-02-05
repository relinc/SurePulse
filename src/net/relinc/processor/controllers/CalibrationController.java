package net.relinc.processor.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.controlsfx.control.SegmentedButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.SGProp;
import net.relinc.libraries.application.StrainGaugeOnBar;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.processor.controllers.BarCalibratorController.CalibrationMode;

public class CalibrationController {
		
	@FXML CheckBox metricCB;
	@FXML TextField incidentBarNameTB;
	NumberTextField incidentBarDiameterTB;
	NumberTextField incidentBarDensityTB;
	NumberTextField incidentBarYeildTB;
	NumberTextField incidentBarYoungsModulusTB;
	NumberTextField incidentBarSpeedLimitTB;
	NumberTextField incidentBarLengthTB;
	
	@FXML TextField transmissionBarNameTB;
	NumberTextField transmissionBarDiameterTB;
	NumberTextField transmissionBarDensityTB;
	NumberTextField transmissionBarYeildTB;
	NumberTextField transmissionBarYoungsModulusTB;
	NumberTextField transmissionBarSpeedLimitTB;
	NumberTextField transmissionBarLengthTB;
	@FXML SplitPane rootSplitPane;
	@FXML TableView<SGProp> incidentStrainGaugeTable;
	@FXML TableView<SGProp> transmissionStrainGaugeTable;
	@FXML TreeView<String> treeView;
	@FXML TextField folderNameTF;
	@FXML TextField barSetupNameTF;
	
	@FXML Label incidentBarDensityLabel;
	@FXML Label incidentBarDiameterLabel;
	@FXML Label incidentBarYieldLabel;
	@FXML Label incidentBarYoungsModLabel;
	@FXML Label incidentBarSpeedLabel;
	@FXML Label incidentBarLengthLabel;
	@FXML Label transmissionBarDensityLabel;
	@FXML Label transmissionBarDiameterLabel;
	@FXML Label transmissionBarYieldLabel;
	@FXML Label transmissionBarYoungsModLabel;
	@FXML Label transmissionBarSpeedLimitLabel;
	@FXML Label transmissionBarLengthLabel;
	@FXML Label labelBarSetups;
	@FXML GridPane incidentBarGrid;
	@FXML GridPane transmissionBarGrid;
	//@FXML Button addStrainGaugeIncidentButton;
	//@FXML Button addStrainGaugeTransmissionButton;
	@FXML Button addStrainGaugeButton;
	@FXML Button deleteStrainGaugeButton;
	@FXML Button manageStrainGaugesButton;
	@FXML Button addFolderButton;
	@FXML Button saveBarSetup;
	@FXML Button addBarSetupToSample;
	@FXML Button deleteBarSetupButton;
	@FXML Button doneButton;
	@FXML TabPane barTabPane;
	@FXML VBox saveBarSetupVBox;
	
	@FXML Button calibrateIncidentBarButton;
	@FXML Button calibrateTransmissionBarButton;
	@FXML Button copyFromTransmissionBarButton;
	@FXML Button copyFromIncidentBarButton;
	
	private File currentWorkingDirectory = SPSettings.Workspace;
	
	CreateNewSampleController newSampleController;
	final ContextMenu deleteBarSetupContextMenu = new ContextMenu();
	MenuItem deleteMenuItem = new MenuItem("Delete");

	BarSetup currentBarSetup;
	public Stage stage;
	TreeItem<String> selectedTreeItem;
	public BarSetup barSetup;
	
	public enum BarSetupMode{
		EDIT, ADD;
	}
	
	public BarSetupMode barSetupMode = BarSetupMode.EDIT;
	
	private Node getRootIcon(){
		ImageView rootIcon = new ImageView(
		        new Image(getClass().getResourceAsStream(SPOperations.folderImageLocation))
		    );
		rootIcon.setFitHeight(16);
		rootIcon.setFitWidth(16);
		Node a = rootIcon;
		return a;
	}
	private Node getBarSetupIcon(){
		ImageView rootIcon = new ImageView(
		        new Image(getClass().getResourceAsStream("/net/relinc/libraries/images/barSetup.png"))
		    );
		rootIcon.setPreserveRatio(true);
		rootIcon.setFitWidth(16);
		Node a = rootIcon;
		return a;
	}
	
	@FXML
	public void initialize(){
		
		if(SPSettings.metricMode.getValue())
			metricCB.selectedProperty().set(true);
		
		SegmentedButton b = new SegmentedButton();
		ToggleButton b1 = new ToggleButton("Workspace");
		ToggleButton b2 = new ToggleButton("Global");
		b.getButtons().addAll(b1,b2);
		b.getButtons().get(0).setSelected(true);
		saveBarSetupVBox.getChildren().add(2, b);
		
		b1.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if(b1.isSelected()) {
					currentWorkingDirectory = SPSettings.Workspace;
					updateTreeView();
				}
			}
		});
		
		b2.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				if(b2.isSelected()) {
					currentWorkingDirectory = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse");
					updateTreeView();
				}
			}
		});
		
		
		SPSettings.metricMode.bindBidirectional(metricCB.selectedProperty());
		
		currentBarSetup = new BarSetup(new Bar(), new Bar());
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
		
		treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.SECONDARY) {
					deleteBarSetupContextMenu.show(treeView, Side.LEFT, 0, 0);
                    System.out.println("consuming right release button in cm filter");
                    event.consume();
                }
				
			}
		});
		
		doneButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Stage s = (Stage) doneButton.getScene().getWindow();
			    s.close();
			}
		});
		
		incidentBarDensityTB = new NumberTextField("lb/in^3", "g/cc");
		incidentBarDiameterTB = new NumberTextField("inches", "mm");
		incidentBarYeildTB = new NumberTextField("ksi", "MPa");
		incidentBarYoungsModulusTB = new NumberTextField("psi * 10^6", "GPa");
		incidentBarSpeedLimitTB = new NumberTextField("ft/s", "m/s");
		incidentBarLengthTB = new NumberTextField("inches", "mm");
		
		transmissionBarDensityTB = new NumberTextField("lb/in^3", "g/cc");
		transmissionBarDiameterTB = new NumberTextField("inches", "mm");
		transmissionBarYeildTB = new NumberTextField("ksi", "MPa");
		transmissionBarYoungsModulusTB = new NumberTextField("psi * 10^6", "GPa");
		transmissionBarSpeedLimitTB = new NumberTextField("ft/s", "m/s");
		transmissionBarLengthTB = new NumberTextField("inches", "mm");
		
		int i = 1;
		incidentBarGrid.add(incidentBarDensityTB, 1, i++);
		incidentBarGrid.add(incidentBarDensityTB.unitLabel, 1, i-1);		
		incidentBarGrid.add(incidentBarDiameterTB, 1, i++);
		incidentBarGrid.add(incidentBarDiameterTB.unitLabel, 1, i-1);
		incidentBarGrid.add(incidentBarYeildTB, 1, i++);
		incidentBarGrid.add(incidentBarYeildTB.unitLabel, 1, i-1);
		incidentBarGrid.add(incidentBarYoungsModulusTB, 1, i++);
		incidentBarGrid.add(incidentBarYoungsModulusTB.unitLabel, 1, i-1);
		incidentBarGrid.add(incidentBarSpeedLimitTB, 1, i++);
		incidentBarGrid.add(incidentBarSpeedLimitTB.unitLabel, 1, i-1);
		incidentBarGrid.add(incidentBarLengthTB, 1, i++);
		incidentBarGrid.add(incidentBarLengthTB.unitLabel, 1, i-1);
		
		i = 1;
		transmissionBarGrid.add(transmissionBarDensityTB, 1, i++);
		transmissionBarGrid.add(transmissionBarDensityTB.unitLabel, 1, i-1);		
		transmissionBarGrid.add(transmissionBarDiameterTB, 1, i++);
		transmissionBarGrid.add(transmissionBarDiameterTB.unitLabel, 1, i-1);
		transmissionBarGrid.add(transmissionBarYeildTB, 1, i++);
		transmissionBarGrid.add(transmissionBarYeildTB.unitLabel, 1, i-1);
		transmissionBarGrid.add(transmissionBarYoungsModulusTB, 1, i++);
		transmissionBarGrid.add(transmissionBarYoungsModulusTB.unitLabel, 1, i-1);
		transmissionBarGrid.add(transmissionBarSpeedLimitTB, 1, i++);
		transmissionBarGrid.add(transmissionBarSpeedLimitTB.unitLabel, 1, i-1);
		transmissionBarGrid.add(transmissionBarLengthTB, 1, i++);
		transmissionBarGrid.add(transmissionBarLengthTB.unitLabel, 1, i-1);
		
		deleteMenuItem.setOnAction(new EventHandler<ActionEvent>() {
		    public void handle(ActionEvent e) {
		        System.out.println("Delete");
		    }
		});
		deleteBarSetupContextMenu.getItems().addAll(deleteMenuItem);
		
		//speed limit listeners
		incidentBarDensityTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateIncidentSpeedLimit();
			}
			
		});
		incidentBarYoungsModulusTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateIncidentSpeedLimit();
			}
		});
		incidentBarYeildTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateIncidentSpeedLimit();
			}
		});
		transmissionBarDensityTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateTransmissionSpeedLimit();
			}
			
		});
		transmissionBarYoungsModulusTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateTransmissionSpeedLimit();
			}
		});
		transmissionBarYeildTB.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				calculateTransmissionSpeedLimit();
			}
		});
		barSetupNameTF.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				barSetupNameTF.setStyle("-fx-text-inner-color: black;");
			}
		});
		
		//tooltips
		metricCB.setTooltip(new Tooltip("Switches to metric units"));
		incidentBarNameTB.setTooltip(new Tooltip("Name of the indcident bar"));
		incidentBarSpeedLimitTB.setTooltip(new Tooltip("Speed limit of the incident bar (automatically calculated)"));
		transmissionBarNameTB.setTooltip(new Tooltip("Name of the transmission bar"));
		transmissionBarSpeedLimitTB.setTooltip(new Tooltip("Speed limit of the transmission bar (automatically calculated)"));
		incidentStrainGaugeTable.setTooltip(new Tooltip("The strain gauges currently on this bar"));
		transmissionStrainGaugeTable.setTooltip(new Tooltip("The strain gauges currently on this bar"));
		addStrainGaugeButton.setTooltip(new Tooltip("Opens a dialog that allows you to add a strain gauge to this bar"));
		deleteStrainGaugeButton.setTooltip(new Tooltip("Removes the selected strain gauge from this bar"));
		manageStrainGaugesButton.setTooltip(new Tooltip("Opens a dialog that allows you to create strain gauges"));
		saveBarSetup.setTooltip(new Tooltip("Saves the bar setup into the selected folder"));
		addBarSetupToSample.setTooltip(new Tooltip("Sets the selected bar setup as sample's bar setup"));
		deleteBarSetupButton.setTooltip(new Tooltip("Deletes the selected bar setup"));
		calibrateIncidentBarButton.setTooltip(new Tooltip("Opens a dialog that allows you to load calibration data to find the Young's Modulus"));
		calibrateTransmissionBarButton.setTooltip(new Tooltip("Opens a dialog that allows you to load calibration data to find the Young's Modulus"));
		copyFromTransmissionBarButton.setTooltip(new Tooltip("Copies the bar parameters from the transmission bar"));
		copyFromIncidentBarButton.setTooltip(new Tooltip("Copies the bar parameters from the incident bar"));
	}
	
	@FXML
	public void deleteBarSetupButtonFired(){
		String path = SPOperations.getPathFromTreeViewItem(selectedTreeItem);
		if(path == ""){
			Dialogs.showAlert("Cannot delete base directory", stage);
			return;
		}
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		
		if(file.getName().equals("Bar Setups")){
			Dialogs.showAlert("Cannot delete base directory", stage);
			return;
		}
		
		if(file.isDirectory()){
			List<Integer> contents = SPOperations.countContentsInFolder(file);
			String message = "It contains " + contents.get(0) + " setup(s) and "
					+ contents.get(1) + " folder(s)";
			if(Dialogs.showConfirmationDialog("Deleting Folder", "Please Confirm", 
						"Are you sure you want to delete this folder?\n" + message, stage)){
				SPOperations.deleteFolder(file);
			}
				
		}
		else{
			if(Dialogs.showConfirmationDialog("Deleting Setup", "Please Confirm",
					"Are you sure you want to delete " + file.getName() + "?", stage)){
				new File(file.getPath() + ".zip").delete();
			}
				
		}
	
		updateTreeView();
	}
	
	public void calibrateIncidentBarFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/BarCalibrator.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			BarCalibratorController c = root1.<BarCalibratorController>getController();
			//c.parent = this;
			if(incidentBarDensityTB.getText().equals("")){
				Dialogs.showAlert("Please enter a valid density.", stage);
				return;
			}
			if(currentBarSetup.IncidentBar.strainGauges.size() == 0){
				Dialogs.showAlert("Please add at least one strain gauge to the incident bar.", stage);
				return;
			}
			currentBarSetup.IncidentBar.density = Converter.KgM3FromLbin3(incidentBarDensityTB.getDouble());
			c.barSetup = currentBarSetup;
			c.stage = anotherStage;
			c.calibrationMode = CalibrationMode.INCIDENT;
			c.createRefreshListener();

			anotherStage.showAndWait();
			if(metricCB.isSelected())
				incidentBarYoungsModulusTB.setNumberText(Double.toString(Converter.GpaFromPa(currentBarSetup.IncidentBar.youngsModulus)));
			else
				incidentBarYoungsModulusTB.setNumberText(Double.toString(Converter.MpsiFromPa(currentBarSetup.IncidentBar.youngsModulus)));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void calibrateTransmissionBarFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/BarCalibrator.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			anotherStage.setScene(scene);
			BarCalibratorController c = root1.<BarCalibratorController>getController();
			//c.parent = this;
			c.stage = anotherStage;
			if(transmissionBarDensityTB.getText().equals("")){
				Dialogs.showAlert("Please enter a valid density.", stage);
				return;
			}
			if(currentBarSetup.TransmissionBar.strainGauges.size() == 0){
				Dialogs.showAlert("Please add at least one strain gauge to the transmission bar.", stage);
				return;
			}
			
			currentBarSetup.TransmissionBar.density = Converter.KgM3FromLbin3(Double.parseDouble(transmissionBarDensityTB.getText()));
			c.barSetup = currentBarSetup;
			c.calibrationMode = CalibrationMode.TRANSMISSION;
			c.createRefreshListener();

			anotherStage.showAndWait();
			if(metricCB.isSelected())
				transmissionBarYoungsModulusTB.setNumberText(Double.toString(Converter.GpaFromPa(currentBarSetup.TransmissionBar.youngsModulus)));
			else
				transmissionBarYoungsModulusTB.setNumberText(Double.toString(Converter.MpsiFromPa(currentBarSetup.TransmissionBar.youngsModulus)));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void refresh() {
		switch(barSetupMode) {
			case ADD:
				for(Node n : incidentBarGrid.getChildren()) {
					if(n instanceof TextField) {
						TextField t = (TextField) n;
						t.setEditable(false);
						t.setDisable(true);
					}
				}
				for(Node n : transmissionBarGrid.getChildren()) {
					if(n instanceof TextField) {
						TextField t = (TextField) n;
						t.setEditable(false);
						t.setDisable(true);
					}
				}
				addStrainGaugeButton.setVisible(false);
				addStrainGaugeButton.setManaged(false);
				deleteStrainGaugeButton.setVisible(false);
				deleteStrainGaugeButton.setManaged(false);
				manageStrainGaugesButton.setVisible(false);
				manageStrainGaugesButton.setManaged(false);
				addFolderButton.setVisible(false);
				addFolderButton.setManaged(false);
				folderNameTF.setVisible(false);
				folderNameTF.setManaged(false);
				barSetupNameTF.setVisible(false);
				barSetupNameTF.setManaged(false);
				saveBarSetup.setVisible(false);
				saveBarSetup.setManaged(false);
				deleteBarSetupButton.setVisible(false);
				deleteBarSetupButton.setManaged(false);
				doneButton.setVisible(false);
				doneButton.setManaged(false);
				
				addBarSetupToSample.setVisible(true);
				
				treeView.setLayoutY(63.0);
				treeView.setPrefHeight(403.0);
				labelBarSetups.setText("Select Bar Setup");
				break;
			case EDIT:
				addBarSetupToSample.setVisible(false);
				addBarSetupToSample.setManaged(false);
				System.out.println("Edit Bar Setup");
				break;
		}
	}
	
	public void metricCBAction() {
		toggleUnits();
		updateBarStrainGauges();
	}
	
	public void addBarSetupToSampleFired() {
		if (currentBarSetup.name == null) {
			Dialogs.showAlert("No bar setup selected",stage);
		} else {
			if (newSampleController != null)
				newSampleController.setSelectedBarSetup(currentBarSetup);
			// get a handle to the stage
			Stage stage = (Stage) addBarSetupToSample.getScene().getWindow();
			// do what you have to do
			stage.close();
		}
	}
	public void saveBarSetupFired(){
		if(!checkBarSetupParameters()){
			Dialogs.showAlert("Please enter valid bar setup parameters.",stage);
			return;
		}
		
		if(barSetupNameTF.getText().equals(""))
		{
			Dialogs.showAlert("Please name the bar setup.",stage);
			return;
		}
		
		if(!SPOperations.specialCharactersAreNotInTextField(incidentBarNameTB)) {
			Dialogs.showInformationDialog("Save Bar Setup","Invalid Character In Incident Bar Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		if(!SPOperations.specialCharactersAreNotInTextField(transmissionBarNameTB)) {
			Dialogs.showInformationDialog("Save Bar Setup","Invalid Character In Transmission Bar Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		if(!SPOperations.specialCharactersAreNotInTextField(barSetupNameTF)) {
			Dialogs.showInformationDialog("Save Bar Setup","Invalid Character In Bar Setup Name", "Only 0-9, a-z, A-Z, dash, space, and parenthesis are allowed",stage);
			return;
		}
		
		if(metricCB.isSelected()){
			currentBarSetup.IncidentBar.name = incidentBarNameTB.getText();
			currentBarSetup.IncidentBar.density = Converter.Kgm3FromGcc(incidentBarDensityTB.getDouble());
			currentBarSetup.IncidentBar.diameter = Converter.MeterFromMm(incidentBarDiameterTB.getDouble());
			currentBarSetup.IncidentBar.length = Converter.MeterFromMm(incidentBarLengthTB.getDouble());
			currentBarSetup.IncidentBar.speedLimit = incidentBarSpeedLimitTB.getDouble();
			currentBarSetup.IncidentBar.yield = Converter.paFromMpa(incidentBarYeildTB.getDouble());
			currentBarSetup.IncidentBar.youngsModulus = Converter.paFromGpa(incidentBarYoungsModulusTB.getDouble());
			
			currentBarSetup.TransmissionBar.name = transmissionBarNameTB.getText();
			currentBarSetup.TransmissionBar.density = Converter.Kgm3FromGcc(transmissionBarDensityTB.getDouble());
			currentBarSetup.TransmissionBar.diameter = Converter.MeterFromMm(transmissionBarDiameterTB.getDouble());
			currentBarSetup.TransmissionBar.length = Converter.MeterFromMm(transmissionBarLengthTB.getDouble());
			currentBarSetup.TransmissionBar.speedLimit = transmissionBarSpeedLimitTB.getDouble();
			currentBarSetup.TransmissionBar.yield = Converter.paFromMpa(transmissionBarYeildTB.getDouble());
			currentBarSetup.TransmissionBar.youngsModulus = Converter.paFromGpa(transmissionBarYoungsModulusTB.getDouble());
		}
		else{
			currentBarSetup.IncidentBar.name = incidentBarNameTB.getText();
			currentBarSetup.IncidentBar.density = Converter.KgM3FromLbin3(incidentBarDensityTB.getDouble());
			currentBarSetup.IncidentBar.diameter = Converter.MeterFromInch(incidentBarDiameterTB.getDouble());
			currentBarSetup.IncidentBar.length = Converter.MeterFromInch(incidentBarLengthTB.getDouble());
			currentBarSetup.IncidentBar.speedLimit = Converter.MeterFromFoot(incidentBarSpeedLimitTB.getDouble());
			currentBarSetup.IncidentBar.yield = Converter.paFromKsi(incidentBarYeildTB.getDouble());
			currentBarSetup.IncidentBar.youngsModulus = Converter.paFromMpsi(incidentBarYoungsModulusTB.getDouble());
			
			currentBarSetup.TransmissionBar.name = transmissionBarNameTB.getText();
			currentBarSetup.TransmissionBar.density = Converter.KgM3FromLbin3(transmissionBarDensityTB.getDouble());
			currentBarSetup.TransmissionBar.diameter = Converter.MeterFromInch(transmissionBarDiameterTB.getDouble());
			currentBarSetup.TransmissionBar.length = Converter.MeterFromInch(transmissionBarLengthTB.getDouble());
			currentBarSetup.TransmissionBar.speedLimit = Converter.MeterFromFoot(transmissionBarSpeedLimitTB.getDouble());
			currentBarSetup.TransmissionBar.yield = Converter.paFromKsi(transmissionBarYeildTB.getDouble());
			currentBarSetup.TransmissionBar.youngsModulus = Converter.paFromMpsi(transmissionBarYoungsModulusTB.getDouble());
		}
		
		currentBarSetup.name = barSetupNameTF.getText();
		String path = SPOperations.getPathFromTreeViewItem(selectedTreeItem);
		if(path.equals("")){
			Dialogs.showAlert("Please select a folder to save the bar setup into.",stage);
			return;
		}
		String fullPath = currentWorkingDirectory.getPath() + "/" + path;
		if(!(new File(fullPath).isDirectory())){
			Dialogs.showAlert("Please select a directory to save the bar setup into.",stage);
			return;
		}
		if(new File(fullPath + "/" + barSetupNameTF.getText() + ".zip").exists()){
			if(!Dialogs.showConfirmationDialog("Bar Setup Already Exists.", "Setup Exists", "Do you want to overwrite?", stage))
				return;
		}
		currentBarSetup.writeToFile(fullPath + "/" + barSetupNameTF.getText());//creates a zip file in the given directory. Name of zip file is name of barsetup.
		
		updateTreeView();
		
		
	}

	public void newFolderFired(){
		String path = SPOperations.getPathFromTreeViewItem(selectedTreeItem);
		if(path.equals(""))
		{
			Dialogs.showAlert("Please select a parent directory",stage);
			return;
		}
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(!file.isDirectory()){
			Dialogs.showAlert("Please select a parent directory.",stage);
			return;
		}
		if(folderNameTF.getText().equals("")){
			Dialogs.showAlert("Please name your new directory.",stage);
			return;
		}
		
		File newDir = new File(file.getPath() + "/" + folderNameTF.getText());
		if(newDir.exists()){
			Dialogs.showAlert("This directory already exists. Please rename your directory.",stage);
			return;
		}
		newDir.mkdir();
		updateTreeView();
	}
	public void manageStrainGaugesFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/StrainGauge.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/StrainGauge.fxml"));
			
			
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			StrainGaugeController controller = root1.<StrainGaugeController>getController();
			
			//controller.incidentBarMode = true;
			controller.refresh();
			controller.stage = anotherStage;
			anotherStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	@FXML
	public void addStrainGaugeFired(){
		File strainGaugeFolder = new File(currentWorkingDirectory + "/Strain Gauges");
		File globalStrainGaugeFolder = new File(SPSettings.applicationSupportDirectory + SPSettings.surePulseLocation + SPSettings.globalStrainGaugeLocation);
		if(strainGaugeFolder.listFiles().length == 0 && globalStrainGaugeFolder.listFiles().length == 0){
			Dialogs.showAlert("No strain gauges exist. Please create a strain gauge first.", stage);
			return;
		}
		
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/StrainGauge.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/StrainGauge.fxml"));
			
			
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("SURE-Pulse Data Processor");
			StrainGaugeController controller = root1.<StrainGaugeController>getController();
			controller.stage = anotherStage;
			if(incidentBarTabSelected()){
				controller.incidentBarMode = true;
				controller.bar = currentBarSetup.IncidentBar;
			}
			else{
				controller.transmissionBarMode = true;
				controller.bar = currentBarSetup.TransmissionBar;
			}
			controller.refresh();
			
			anotherStage.show();
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML 
	public void deleteStrainGaugeButtonFired(){
		if(incidentBarTabSelected()){
			SGProp selectedSG = incidentStrainGaugeTable.getSelectionModel().getSelectedItem();
			if(selectedSG != null)
				currentBarSetup.IncidentBar.removeStrainGauge(selectedSG.getSpecificname());
			else
				Dialogs.showAlert("Please select a strain gauge to delete", stage);
		}
		else{
			SGProp selectedSG = transmissionStrainGaugeTable.getSelectionModel().getSelectedItem();
			if(selectedSG != null)
				currentBarSetup.TransmissionBar.removeStrainGauge(selectedSG.getSpecificname());
			else
				Dialogs.showAlert("Please select a strain gauge to delete", stage);
		}
		updateIncidentBarStrainGauges();
		updateTransmissionBarStrainGauges();
	}
	
	public void copyFromIncidentBarButtonFired(){
		transmissionBarDensityTB.setNumberText(incidentBarDensityTB.getText());
		transmissionBarDiameterTB.setNumberText(incidentBarDiameterTB.getText());
		transmissionBarYeildTB.setNumberText(incidentBarYeildTB.getText());
		transmissionBarYoungsModulusTB.setNumberText(incidentBarYoungsModulusTB.getText());
		transmissionBarSpeedLimitTB.setNumberText(incidentBarSpeedLimitTB.getText());
		transmissionBarLengthTB.setNumberText(incidentBarLengthTB.getText());
	}
	
	public void copyFromTransmissionBarButtonFired(){
		incidentBarDensityTB.setNumberText(transmissionBarDensityTB.getText());
		incidentBarDiameterTB.setNumberText(transmissionBarDiameterTB.getText());
		incidentBarYeildTB.setNumberText(transmissionBarYeildTB.getText());
		incidentBarYoungsModulusTB.setNumberText(transmissionBarYoungsModulusTB.getText());
		incidentBarSpeedLimitTB.setNumberText(transmissionBarSpeedLimitTB.getText());
		incidentBarLengthTB.setNumberText(transmissionBarLengthTB.getText());
	}
	
	public boolean incidentBarTabSelected(){
		return barTabPane.getSelectionModel().getSelectedItem().getText().equals("Incident Bar");
	}
	
	
	private void selectedItemChanged() {
		String path = SPOperations.getPathFromTreeViewItem(selectedTreeItem);
		File file = new File(currentWorkingDirectory.getPath() + "/" + path);
		if(file.isDirectory()){
			System.out.println("Directory cannot be a bar setup file.");
			return;
		}
		File newDir = new File(file.getPath() + ".zip");
		if(!newDir.exists()){
			System.out.println("Bar setup doesn't exist");
			return;
		}
		
		currentBarSetup = new BarSetup(newDir.getPath());
		currentBarSetup.name = selectedTreeItem.getValue().toString();
		barSetupNameTF.setText(currentBarSetup.name);
		//barSetupNameTF.getStyleClass().add("textbox-error");
		barSetupNameTF.setStyle("-fx-text-inner-color: red;");
		updateBarStrainGauges();
		setUIParametersFromCurrentBar();
		
	}
	
	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		  @Override
		  public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
		  {
			  updateBarStrainGauges();
			  updateTreeView();
		  }
		});
	}
	
	
	
	
	private void setUIParametersFromCurrentBar(){
		if(!metricCB.isSelected()) {
			//english
			incidentBarNameTB.setText(currentBarSetup.IncidentBar.name);
			incidentBarDensityTB.setNumberText(Double.toString(Converter.Lbin3FromKgM3(currentBarSetup.IncidentBar.density)));
			incidentBarDiameterTB.setNumberText(Double.toString(Converter.InchFromMeter(currentBarSetup.IncidentBar.diameter)));
			incidentBarLengthTB.setNumberText(Double.toString(Converter.InchFromMeter(currentBarSetup.IncidentBar.length)));
			incidentBarSpeedLimitTB.setNumberText(Double.toString(Converter.FootFromMeter(currentBarSetup.IncidentBar.speedLimit)));
			incidentBarYeildTB.setNumberText(Double.toString(Converter.ksiFromPa(currentBarSetup.IncidentBar.yield)));
			incidentBarYoungsModulusTB.setNumberText(Double.toString(Converter.MpsiFromPa(currentBarSetup.IncidentBar.youngsModulus)));
			
			transmissionBarNameTB.setText(currentBarSetup.TransmissionBar.name);
			transmissionBarDensityTB.setNumberText(Double.toString(Converter.Lbin3FromKgM3(currentBarSetup.TransmissionBar.density)));
			transmissionBarDiameterTB.setNumberText(Double.toString(Converter.InchFromMeter(currentBarSetup.TransmissionBar.diameter)));
			transmissionBarLengthTB.setNumberText(Double.toString(Converter.InchFromMeter(currentBarSetup.TransmissionBar.length)));
			transmissionBarSpeedLimitTB.setNumberText(Double.toString(Converter.FootFromMeter(currentBarSetup.TransmissionBar.speedLimit)));
			transmissionBarYeildTB.setNumberText(Double.toString(Converter.ksiFromPa(currentBarSetup.TransmissionBar.yield)));
			transmissionBarYoungsModulusTB.setNumberText(Double.toString(Converter.MpsiFromPa(currentBarSetup.TransmissionBar.youngsModulus)));
		} else {
			//metric
			incidentBarNameTB.setText(currentBarSetup.IncidentBar.name);
			incidentBarDensityTB.setNumberText(Double.toString(Converter.gccFromKgm3(currentBarSetup.IncidentBar.density)));
			incidentBarDiameterTB.setNumberText(Double.toString(Converter.mmFromM(currentBarSetup.IncidentBar.diameter)));
			incidentBarLengthTB.setNumberText(Double.toString(Converter.mmFromM(currentBarSetup.IncidentBar.length)));
			incidentBarSpeedLimitTB.setNumberText(Double.toString(currentBarSetup.IncidentBar.speedLimit));
			incidentBarYeildTB.setNumberText(Double.toString(Converter.MpaFromPa(currentBarSetup.IncidentBar.yield)));
			incidentBarYoungsModulusTB.setNumberText(Double.toString(Converter.GpaFromPa(currentBarSetup.IncidentBar.youngsModulus)));
			
			transmissionBarNameTB.setText(currentBarSetup.TransmissionBar.name);
			transmissionBarDensityTB.setNumberText(Double.toString(Converter.gccFromKgm3(currentBarSetup.TransmissionBar.density)));
			transmissionBarDiameterTB.setNumberText(Double.toString(Converter.mmFromM(currentBarSetup.TransmissionBar.diameter)));
			transmissionBarLengthTB.setNumberText(Double.toString(Converter.mmFromM(currentBarSetup.TransmissionBar.length)));
			transmissionBarSpeedLimitTB.setNumberText(Double.toString(currentBarSetup.TransmissionBar.speedLimit));
			transmissionBarYeildTB.setNumberText(Double.toString(Converter.MpaFromPa(currentBarSetup.TransmissionBar.yield)));
			transmissionBarYoungsModulusTB.setNumberText(Double.toString(Converter.GpaFromPa(currentBarSetup.TransmissionBar.youngsModulus)));
		}
		
		updateIncidentBarStrainGauges();
		updateTransmissionBarStrainGauges();
	}
	private boolean checkBarSetupParameters() {
		try{
			@SuppressWarnings("unused") //Warning removed, think this one can be suppressed
			double d = incidentBarDensityTB.getDouble();
			d = incidentBarDiameterTB.getDouble();
			d = incidentBarYeildTB.getDouble();
			d = incidentBarYoungsModulusTB.getDouble();
			d = incidentBarSpeedLimitTB.getDouble();
			d = incidentBarLengthTB.getDouble();
			
			d = transmissionBarDensityTB.getDouble();
			d = transmissionBarDiameterTB.getDouble();
			d = transmissionBarYeildTB.getDouble();
			d = transmissionBarYoungsModulusTB.getDouble();
			d = transmissionBarSpeedLimitTB.getDouble();
			d = transmissionBarLengthTB.getDouble();
		}
		catch(Exception e)
		{
			return false;
		}
		
		return true;
	}
	
	
	private void updateTreeView(){
		File home = new File(currentWorkingDirectory.getPath() + "/Bar Setups");
		findFiles(home, null);
	}
	private void findFiles(File dir, TreeItem<String> parent) {
	    TreeItem<String> root = new TreeItem<>(dir.getName(), getRootIcon());
	    root.setExpanded(true);
	    File[] files = dir.listFiles();
		for (File file : files) {
		    if (file.isDirectory()) {
		        findFiles(file,root);
		    } else {
		        if(file.getName().endsWith(".zip")){
		        	root.getChildren().add(new TreeItem<>(file.getName().substring(0, file.getName().length() - 4),getBarSetupIcon()));
		        }
		    }
		}
		if(parent==null){
		    treeView.setRoot(root);
		} else {
			
		    parent.getChildren().add(root);
		}
	} 
	
	private void updateBarStrainGauges() {
		updateIncidentBarStrainGauges();
		updateTransmissionBarStrainGauges();
	}

	private void updateIncidentBarStrainGauges() {
		
		ArrayList<SGProp> incidentData = new ArrayList<SGProp>();
		for(StrainGaugeOnBar sg : currentBarSetup.IncidentBar.strainGauges){
			if(SPSettings.metricMode.getValue())
				incidentData.add(new SGProp(sg.genericName, sg.specificName, SPOperations.round(Converter.mmFromM(sg.distanceToSample), 4)));
			else 
				incidentData.add(new SGProp(sg.genericName, sg.specificName, SPOperations.round(Converter.InchFromMeter(sg.distanceToSample),4)));
		}
		
		ObservableList<SGProp> data =
		        FXCollections.observableArrayList(
		        		incidentData);
		
		
		incidentStrainGaugeTable.setEditable(true);
		incidentStrainGaugeTable.getColumns().clear();
		
		
		 
        TableColumn<SGProp, String> firstNameCol = new TableColumn<SGProp, String>("Strain Gauge");
        firstNameCol.setMinWidth(200);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,String>("genericname"));
        
        TableColumn<SGProp, String> specificNameCol = new TableColumn<SGProp, String>("Specific Name");
        specificNameCol.setMinWidth(200);
        specificNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,String>("specificname"));
        
        
 
        TableColumn<SGProp, Double> lastNameCol = new TableColumn<SGProp, Double>("Distance To Sample");
        lastNameCol.setMinWidth(200);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp, Double>("distance"));

        incidentStrainGaugeTable.setItems(data);
        
        incidentStrainGaugeTable.getColumns().addAll(firstNameCol,specificNameCol, lastNameCol);
	}
	
	private void updateTransmissionBarStrainGauges(){
		ArrayList<SGProp> transmissionData = new ArrayList<SGProp>();
		for(StrainGaugeOnBar sg : currentBarSetup.TransmissionBar.strainGauges){
			if(SPSettings.metricMode.getValue())
				transmissionData.add(new SGProp(sg.genericName, sg.specificName, SPOperations.round(Converter.mmFromM(sg.distanceToSample),4)));
			else 
				transmissionData.add(new SGProp(sg.genericName, sg.specificName, SPOperations.round(Converter.InchFromMeter(sg.distanceToSample),4)));
		}
		
		ObservableList<SGProp> data =
		        FXCollections.observableArrayList(
		        		transmissionData);
		
		
		transmissionStrainGaugeTable.setEditable(true);
		transmissionStrainGaugeTable.getColumns().clear();
		
		
		 
        TableColumn<SGProp, String> firstNameCol = new TableColumn<SGProp, String>("Strain Gauge");
        firstNameCol.setMinWidth(200);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp, String>("genericname")); //Warning removed Edited parameters
        
        TableColumn<SGProp, String> specificNameCol = new TableColumn<SGProp, String>("Specific Name");
        specificNameCol.setMinWidth(200);
        specificNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,String>("specificname"));
 
        TableColumn<SGProp, Double> lastNameCol = new TableColumn<SGProp, Double>("Distance To Sample");
        lastNameCol.setMinWidth(200);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,Double>("distance")); //Warning removed Edited parameters

        transmissionStrainGaugeTable.setItems(data);
        
        transmissionStrainGaugeTable.getColumns().addAll(firstNameCol,specificNameCol, lastNameCol);
	}
	
	private void toggleUnits() {
		convertTextFieldValues();
		updateLabelUnits();
	}

	private void convertTextFieldValues() {
		if(metricCB.isSelected()){
			Converter.convertTBValueFromLbsPerCubicInchtoGramsPerCC(incidentBarDensityTB);
			Converter.convertTBValueFromInchToMM(incidentBarDiameterTB);
			Converter.convertTBValueFromInchToMM(incidentBarLengthTB);
			Converter.convertTBValueFromFpStoMpS(incidentBarSpeedLimitTB);
			Converter.convertTBValueFromKSItoMPa(incidentBarYeildTB);
			Converter.convertTBValueFromPsiTimesTenToTheSixthToGigapascals(incidentBarYoungsModulusTB);
			Converter.convertTBValueFromLbsPerCubicInchtoGramsPerCC(transmissionBarDensityTB);
			Converter.convertTBValueFromInchToMM(transmissionBarDiameterTB);
			Converter.convertTBValueFromInchToMM(transmissionBarLengthTB);
			Converter.convertTBValueFromFpStoMpS(transmissionBarSpeedLimitTB);		
			Converter.convertTBValueFromKSItoMPa(transmissionBarYeildTB);
			Converter.convertTBValueFromPsiTimesTenToTheSixthToGigapascals(transmissionBarYoungsModulusTB);
			//SPSettings.isMetric = true;
		}
		else{
			Converter.convertTBValueFromGramsPerCCtoLbsPerCubicInch(incidentBarDensityTB);
			Converter.convertTBValueFromMMToInch(incidentBarDiameterTB);
			Converter.convertTBValueFromMMToInch(incidentBarLengthTB);
			Converter.convertTBValueFromMpStoFpS(incidentBarSpeedLimitTB);
			Converter.convertTBValueFromMPatoKSI(incidentBarYeildTB);
			Converter.convertTBValueFromGigapascalsPsiTimesTenToTheSixth(incidentBarYoungsModulusTB);
			Converter.convertTBValueFromGramsPerCCtoLbsPerCubicInch(transmissionBarDensityTB);
			Converter.convertTBValueFromMMToInch(transmissionBarDiameterTB);
			Converter.convertTBValueFromMMToInch(transmissionBarLengthTB);
			Converter.convertTBValueFromMpStoFpS(transmissionBarSpeedLimitTB);	
			Converter.convertTBValueFromMPatoKSI(transmissionBarYeildTB);
			Converter.convertTBValueFromGigapascalsPsiTimesTenToTheSixth(transmissionBarYoungsModulusTB);
			//SPSettings.isMetric = false;
		}
	}

	private void updateLabelUnits() {
		incidentBarDensityTB.updateTextFieldLabelUnits();
		incidentBarDiameterTB.updateTextFieldLabelUnits();
		incidentBarYeildTB.updateTextFieldLabelUnits();
		incidentBarYoungsModulusTB.updateTextFieldLabelUnits();
		incidentBarSpeedLimitTB.updateTextFieldLabelUnits();
		incidentBarLengthTB.updateTextFieldLabelUnits();
		transmissionBarDensityTB.updateTextFieldLabelUnits();
		transmissionBarDiameterTB.updateTextFieldLabelUnits();
		transmissionBarYeildTB.updateTextFieldLabelUnits(); 
		transmissionBarYoungsModulusTB.updateTextFieldLabelUnits();
		transmissionBarSpeedLimitTB.updateTextFieldLabelUnits(); 
		transmissionBarLengthTB.updateTextFieldLabelUnits();
	}
	
	private void calculateIncidentSpeedLimit(){
		if(incidentBarDensityTB.getText().equals("")){
			incidentBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		if(incidentBarDiameterTB.getText().equals("")){
			incidentBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		if(incidentBarYeildTB.getText().equals("")){
			incidentBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		if(incidentBarYoungsModulusTB.getText().equals("")){
			incidentBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		
		if(metricCB.isSelected()){
			currentBarSetup.IncidentBar.density = Converter.Kgm3FromGcc(incidentBarDensityTB.getDouble());
			currentBarSetup.IncidentBar.diameter = Converter.MeterFromMm(incidentBarDiameterTB.getDouble());
			currentBarSetup.IncidentBar.yield = Converter.paFromMpa(incidentBarYeildTB.getDouble());
			currentBarSetup.IncidentBar.youngsModulus = Converter.paFromGpa(incidentBarYoungsModulusTB.getDouble());
			try{
				incidentBarSpeedLimitTB.setNumberText(Double.toString(SPOperations.round(currentBarSetup.IncidentBar.calculateSpeedLimit(), 3)));
			}
			catch(Exception e){
				incidentBarSpeedLimitTB.setNumberText("0.0");
			}
		}
		else{
			currentBarSetup.IncidentBar.density = Converter.KgM3FromLbin3(incidentBarDensityTB.getDouble());
			currentBarSetup.IncidentBar.diameter = Converter.MeterFromInch(incidentBarDiameterTB.getDouble());
			currentBarSetup.IncidentBar.yield = Converter.paFromKsi(incidentBarYeildTB.getDouble());
			currentBarSetup.IncidentBar.youngsModulus = Converter.paFromMpsi(incidentBarYoungsModulusTB.getDouble());
			try{
				incidentBarSpeedLimitTB.setNumberText(Double.toString(SPOperations.round(Converter.FootFromMeter(currentBarSetup.IncidentBar.calculateSpeedLimit()), 3)));
			}
			catch(Exception e){
				incidentBarSpeedLimitTB.setNumberText("0.0");
			}
		}
	}
	
	private void calculateTransmissionSpeedLimit(){
		if(transmissionBarDensityTB.getText().equals("")){
			transmissionBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
			
		if(transmissionBarDiameterTB.getText().equals("")){
			transmissionBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		if(transmissionBarYeildTB.getText().equals("")){
			transmissionBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		if(transmissionBarYoungsModulusTB.getText().equals("")){
			transmissionBarSpeedLimitTB.setNumberText("0.0");
			return;
		}
		
		if(metricCB.isSelected()){
			currentBarSetup.TransmissionBar.density = Converter.Kgm3FromGcc(transmissionBarDensityTB.getDouble());
			currentBarSetup.TransmissionBar.diameter = Converter.MeterFromMm(transmissionBarDiameterTB.getDouble());
			currentBarSetup.TransmissionBar.yield = Converter.paFromMpa(transmissionBarYeildTB.getDouble());
			currentBarSetup.TransmissionBar.youngsModulus = Converter.paFromGpa(transmissionBarYoungsModulusTB.getDouble());
			try{
				transmissionBarSpeedLimitTB.setNumberText(Double.toString(SPOperations.round(currentBarSetup.TransmissionBar.calculateSpeedLimit(), 3)));
			}
			catch(Exception e){
				transmissionBarSpeedLimitTB.setNumberText("0.0");
			}
		}
		else{
			currentBarSetup.TransmissionBar.density = Converter.KgM3FromLbin3(transmissionBarDensityTB.getDouble());
			currentBarSetup.TransmissionBar.diameter = Converter.MeterFromInch(transmissionBarDiameterTB.getDouble());
			currentBarSetup.TransmissionBar.yield = Converter.paFromKsi(transmissionBarYeildTB.getDouble());
			currentBarSetup.TransmissionBar.youngsModulus = Converter.paFromMpsi(transmissionBarYoungsModulusTB.getDouble());
			try{
				transmissionBarSpeedLimitTB.setNumberText(Double.toString(SPOperations.round(Converter.FootFromMeter(currentBarSetup.TransmissionBar.calculateSpeedLimit()), 3)));
			}
			catch(Exception e){
				transmissionBarSpeedLimitTB.setNumberText("0.0");
			}
		}
	}

}
