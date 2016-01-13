package net.relinc.correlation.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.libraries.splibraries.Settings;
import net.relinc.libraries.splibraries.Operations;
import net.relinc.libraries.splibraries.Dialogs;

public class ExportStrainController {
	@FXML private RadioButton engineeringRadioButton;
	@FXML private RadioButton trueRadioButton;
	@FXML public ListView<Target> targetsListView;
	@FXML private LineChart<Number, Number> chart;
	private ToggleGroup toggleGroup = new ToggleGroup();
	public double inchToPixelRatio;
	public boolean useSmoothedPoints;
	public double lengthOfSample;
	public List<File> imagePaths;
	public int beginIndex;
	public Stage stage;
	
	public void initialize(){
		engineeringRadioButton.setToggleGroup(toggleGroup);
		trueRadioButton.setToggleGroup(toggleGroup);
		
		targetsListView.setCellFactory(new Callback<ListView<Target>, ListCell<Target>>() {
		      @Override
		      public CheckBoxListCell<Target> call(ListView<Target> listView) {
		        final CheckBoxListCell<Target> listCell = new CheckBoxListCell<Target>()
		        {
		          @Override
		          public void updateItem(Target item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		              setText(null);
		            } else {
		              setText(item.getName());
		            }
		          }
		        };
		        listCell.setSelectedStateCallback(new Callback<Target, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(Target param) {
						return param.selectedProperty();
					}
				});
		        return listCell;
		      }
		    });
		engineeringRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderGUI();
			}
		});
	}
	
	public void fillTargetsListView(List<Target> targets){
		for(Target t : targets){
			t.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					renderGUI();
				}
			});
			targetsListView.getItems().add(t);
		}
		
		targetsListView.getItems().get(0).selectedProperty().set(true);
		targetsListView.getItems().get(1).selectedProperty().set(true);
	}
	
	public void exportStrainButtonFired(){
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Strain CSV");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
        	double[] strain = getStrain();
        	if(strain == null){
        		Dialogs.showAlert("Must select two targets", stage);
        		return;
        	}
    		String csv = "Image,";
    		csv += engineeringRadioButton.isSelected() ? "Engineering Strain" : "True Strain";
    		csv += "\n";
    		for(int i = 0; i < strain.length; i++){
    			csv += imagePaths.get(i + beginIndex).getName() + "," + strain[i] + "\n";
    		}
    		Operations.writeStringToFile(csv, file.getPath() + ".csv");
        }
	}

	public void renderGUI() {
		chart.getData().clear();
		double[] strain = getStrain();
		if(strain == null)
			return;
		if(engineeringRadioButton.isSelected()){
			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
	        series1.setName("Engineering Strain");
	        chart.setCreateSymbols(false);
	        chart.setTitle("Engineering Strain");
	        
	        ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

	        for(int i = 0; i < strain.length; i++)
	        	dataPoints.add(new Data<Number, Number>(i,strain[i]));
	        
	        
	        
	        series1.getData().addAll(dataPoints);
	        
	        chart.getData().addAll(series1);
	        
		}
		else{
			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
	        series1.setName("True Strain");
	        chart.setCreateSymbols(false);
	        chart.setTitle("True Strain");
	        
	        ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

	        for(int i = 0; i < strain.length; i++)
	        	dataPoints.add(new Data<Number, Number>(i,strain[i]));
	        
	        series1.getData().addAll(dataPoints);
	        
	        chart.getData().addAll(series1);
		}
	}
	
	public double[] getStrain(){
		List<Target> selected = targetsListView.getItems().stream().filter(t -> t.selectedProperty().get()).collect(Collectors.toList());
		if(selected.size() != 2)
		{
			return null;
		}
		Target t1 = selected.get(0);
		Target t2 = selected.get(1);
		if(engineeringRadioButton.isSelected())
			return SPTargetTracker.calculateEngineeringStrain(t1, t2, inchToPixelRatio, useSmoothedPoints, lengthOfSample);
		else 
			return SPTargetTracker.calculateTrueStrain(t1, t2, inchToPixelRatio, useSmoothedPoints, lengthOfSample);
	}
}
