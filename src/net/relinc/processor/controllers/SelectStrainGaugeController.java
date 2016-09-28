package net.relinc.processor.controllers;


import java.util.ArrayList;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.SGProp;
import net.relinc.libraries.application.StrainGaugeOnBar;
import net.relinc.libraries.data.RawDataset;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.splibraries.*;
import net.relinc.libraries.staticClasses.SPSettings;

public class SelectStrainGaugeController {
	public Bar bar;
	public Mode mode;
	
	@FXML Label instructionsLabel;
	@FXML TableView<SGProp> tableView;
	public RawDataset rawDataset;
	public Stage stage;
	
	public enum Mode{
		INCIDENT, TRANSMISSION;
	}

	public void initialize(){
		System.out.println("Initialized");
	}
	
	public void doneButtonFired(){
		if(tableView.getSelectionModel().getSelectedIndex() == -1) {
			Dialogs.showInformationDialog("Strain Gauge Not Selected", null, "Please Select a Strain Gauge From The List",stage);
			return;
		}
		rawDataset.interpreter.strainGauge = bar.strainGauges.get(tableView.getSelectionModel().getSelectedIndex());
		
		
		 Stage stage = (Stage) tableView.getScene().getWindow();
		    // do what you have to do
		 stage.close();
	}

	public void updateInterface() {
		if(mode == Mode.INCIDENT)
			instructionsLabel.setText("Select a strain gauge from the incident bar");
		else
			instructionsLabel.setText("Select a strain gauge from the tranmission bar");
		updateTable();
	}

	private void updateTable() {
		ArrayList<SGProp> incidentData = new ArrayList<SGProp>();

		for(StrainGaugeOnBar sg : bar.strainGauges){
			if(SPSettings.metricMode.get())
				incidentData.add(new SGProp(sg.genericName, sg.specificName, Converter.mmFromM(sg.distanceToSample)));
			else
				incidentData.add(new SGProp(sg.genericName, sg.specificName, Converter.InchFromMeter(sg.distanceToSample)));
		}
		
		ObservableList<SGProp> data =
		        FXCollections.observableArrayList(
		        		incidentData);
		
		
		tableView.setEditable(true);
		tableView.getColumns().clear();
		
		 
        TableColumn<SGProp, String> firstNameCol = new TableColumn<SGProp, String>("Strain Gauge");
        //firstNameCol.setMinWidth(200);
        firstNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,String>("genericname"));
        
        TableColumn<SGProp, String> specificNameCol = new TableColumn<SGProp, String>("Specific Name");
        //specificNameCol.setMinWidth(200);
        specificNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp,String>("specificname"));
 
        TableColumn<SGProp, Double> lastNameCol = new TableColumn<SGProp, Double>("Distance To Sample");
       // lastNameCol.setMinWidth(200);
        lastNameCol.setCellValueFactory(
                new PropertyValueFactory<SGProp, Double>("distance"));

        tableView.setItems(data);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.getColumns().addAll(firstNameCol,specificNameCol, lastNameCol);
	}
	
}
