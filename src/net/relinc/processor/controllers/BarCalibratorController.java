package net.relinc.processor.controllers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.ServiceConfigurationError;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataFileListWrapper;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.LowPassFilter;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.ModifierFolder.LowPass;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPMath;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class BarCalibratorController {
	Stage stage;
	public CalibrationController parent;
	DataFileListWrapper dataFiles = new DataFileListWrapper();
	BarSetup barSetup;
	CalibrationMode calibrationMode;
	int dataPointsToShow = 2000;
	LineChartWithMarkers<Number, Number> chart;// = new LineChartWithMarkers<>();
	double youngsModulus;
	
	@FXML ScrollBar noiseLevelScrollBar;
	@FXML TextField distanceTF;
	@FXML Label youngsModulusLabel;
	@FXML AnchorPane chartAnchorPane;
	@FXML Button calculateEnergyRatioButton;
	private double energyRatio;
	
	public void initialize(){
		noiseLevelScrollBar.valueProperty().addListener(new ChangeListener<Number>() {

			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				updateAnnotations();
			}
			
		});
		calculateEnergyRatioButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Dialogs.showInformationDialog("Energy Ratio", "", "The energy ratio (second pulse / first pulse) : " + SPOperations.round(energyRatio, 3) + " %", stage);
			}
		});
		
		noiseLevelScrollBar.setTooltip(new Tooltip("Adjust the noise level to be half of the magnitude of the pulse."));
		calculateEnergyRatioButton.setTooltip(new Tooltip("Calculates the energy ratio of the second pulse to the first pulse. Useful information for calculating the efficiency"
				+ " of energy transfer between separated bars."));
		
	}
	
	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
			{
				renderChartData();
			}
		});
	}
	
	@FXML
	public void loadDataButtonFired(){
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
			c.existingSampleDataFiles = dataFiles;
			c.createRefreshListener();
			c.barSetup = barSetup;
			c.calibrationMode = calibrationMode;
			//c.loadDisplacement = sampleType.getSelectionModel().getSelectedItem().equals("Load Displacement");
			anotherStage.showAndWait();
			
			//add a 1000 KHz lowpass filter by default.
			//dataFiles.get(0).dataSubsets.get(0).modifiers.add(new LowPassFilter())
			for(DataFile file : dataFiles){
				for(DataSubset d : file.dataSubsets){
					for(Modifier mod : d.modifiers){
						if(mod instanceof LowPass){
							LowPass pass = (LowPass)mod;
							pass.setLowPassValue(1000000);
							pass.enabled.set(true);
							pass.activateModifier();
						}
					}
				}
			}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void acceptButtonFired(){
		getCurrentBar().youngsModulus = youngsModulus;
		stage.close();
	}
	
	@FXML
	private void modifyDataButtonFired(){
		Stage anotherStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/processor/fxml/TrimData.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			scene.getStylesheets().add(getClass().getResource("/net/relinc/processor/application/application.css").toExternalForm());
			anotherStage.setScene(scene);
			//anotherStage.initModality(Modality.WINDOW_MODAL);
//			anotherStage.initOwner(
//		        stage.getScene().getWindow());
			TrimDataController c = root1.<TrimDataController>getController();
			
			//c.sample = createSampleFromIngredients();
			c.DataFiles = dataFiles;
			c.stage = anotherStage;
			c.barSetup = barSetup;
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
	
	public ArrayList<DataSubset> getDataSubsets(){
		return (ArrayList<DataSubset>) dataFiles.get(0)
				.dataSubsets.stream().filter(data -> !(data instanceof ReflectedPulse))
				.collect(Collectors.toList());
	}
	
	private void renderChartData() {
		NumberAxis xAxis = new NumberAxis();
		NumberAxis yAxis = new NumberAxis();
		chart = new LineChartWithMarkers<>(xAxis, yAxis, LineChartWithMarkers.chartDataType.TIME, null);
		
		chartAnchorPane.getChildren().clear();
		chartAnchorPane.getChildren().add(chart);
		AnchorPane.setBottomAnchor(chart, 0.0);
		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		if(dataFiles.size() == 0)
			return;
		ArrayList<DataSubset> datasets = getDataSubsets();
		double[] time = datasets.get(0).Data.timeData;
		double[] SG1 = new double[time.length];
		double[] SG2 = new double[time.length];
		SG1 = datasets.get(0).getModifiedData();
		//SG1 = Arrays.stream(SG1).map(n -> Math.abs(n)).toArray();
		SG2 = datasets.size() > 1 ? datasets.get(1).getModifiedData() : null;
		double maxValGraphed = -Double.MAX_VALUE;
		
		if(SG2 != null){
			System.out.println("2 Strain Gauge Mode");
			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
	        series1.setName("SG1 Raw Data");
	        XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
	        series2.setName("SG2 Raw Data");
	        chart.setCreateSymbols(false);
	        
	        ArrayList<Data<Number, Number>> dataPoints1 = new ArrayList<Data<Number, Number>>();
	        ArrayList<Data<Number, Number>> dataPoints2 = new ArrayList<Data<Number, Number>>();
	        
	        for(int i = 0; i <= SG1.length; i++){
	        	dataPoints1.add(new Data<Number, Number>(time[i], SG1[i]));
	        	dataPoints2.add(new Data<Number, Number>(time[i], SG2[i]));
	        	
	        	if(SG1[i] > maxValGraphed || SG2[i] > maxValGraphed)
	        		maxValGraphed = Math.max(SG1[i], SG2[i]);
	        	i += SG1.length / dataPointsToShow;
	        }
	        
	        noiseLevelScrollBar.setMin(0.0);
	        noiseLevelScrollBar.setMax(maxValGraphed * 3 / 4);
	        series1.getData().addAll(dataPoints1);
	        series2.getData().addAll(dataPoints2);
	        
	        chart.getData().clear();
	        chart.getData().addAll(series1);
	        chart.getData().addAll(series2);
	        
		}
		else{
			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
	        chart.setCreateSymbols(false);
	        
	        ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();
	        
	        for(int i = 0; i <= SG1.length; i++){
	        	dataPoints.add(new Data<Number, Number>(time[i], SG1[i]));
	        	if(SG1[i] > maxValGraphed)
	        		maxValGraphed = SG1[i];
	        	i += SG1.length / dataPointsToShow;
	        }
	        
	        noiseLevelScrollBar.setMin(0.0);
	        noiseLevelScrollBar.setMax(maxValGraphed / 3);
	        series1.getData().addAll(dataPoints);
	        
	        chart.getData().clear();
	        chart.getData().addAll(series1);
		}
		
		updateAnnotations();
	}
	
	
	
	public void updateAnnotations(){
		//chart.clearVerticalMarkers();
		chart.clearHorizontalMarkers();
		chart.clearVerticalMarkers();
        chart.addHorizontalValueMarker(new Data<Number, Number>(0, noiseLevelScrollBar.getValue()));
        chart.addHorizontalValueMarker(new Data<Number, Number>(0,-noiseLevelScrollBar.getValue()));
//        chart.addVerticalRangeMarker(new Data<Number, Number>(getActivatedData().Data.timeData[getActivatedData().getBegin()], 
//        		getActivatedData().Data.timeData[getActivatedData().getEnd()]), Color.BLUE);
        double[] data = getDataSubsets().get(0).getModifiedData();
        double[] data2 = getDataSubsets().size() > 1 ? getDataSubsets().get(1).getModifiedData() : null;
        double[] timeData = getDataSubsets().get(0).Data.timeData;
        
        //data = SPMath.fourierLowPassFilter(data, 2000*Math.pow(10, 3), 1 / (timeData[1] - timeData[0]));
        data = Arrays.stream(data).map(n -> Math.abs(n)).toArray();
        int smooth = data.length / 1000 + 1;
        
        ArrayList<Integer> risingEdgeVals = new ArrayList<>();
        ArrayList<Integer> fallingEdgeVals = new ArrayList<>();
        double noiseLevel = noiseLevelScrollBar.getValue();
        
        if(data2 != null){
        	//data2 = SPMath.fourierLowPassFilter(data2, 2000*Math.pow(10, 3), 1 / (timeData[1] - timeData[0]));
            data2 = Arrays.stream(data2).map(n -> Math.abs(n)).toArray();
        	
        	
        	for(int i = 0; i < data.length - smooth; i++){
            	if(data[i] <= noiseLevel && data[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(i);
            		i += smooth;
            	}
            	if(data[i] >= noiseLevel && data[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(i);
            		i += smooth;
            	}
            	
            	
            	if(data2[i] <= noiseLevel && data2[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(i);
            		i += smooth;
            	}
            	if(data2[i] >= noiseLevel && data2[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(i);
            		i += smooth;
            	}
            	
            	
            }
        }
        else{
        	for(int i = 0; i < data.length - smooth; i++){
            	if(data[i] <= noiseLevel && data[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(i);
            		i += smooth;
            	}
            	if(data[i] >= noiseLevel && data[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(i);
            		i += smooth;
            	}
            }
        }
        
        //calculate energy ratios
		double firstPulseEnergy = 0.0;
		double secondPulseEnergy = 0.0;

		if (risingEdgeVals.size() >= 2 && fallingEdgeVals.size() >= 2) {
			for (int i = risingEdgeVals.get(0); i < fallingEdgeVals.get(0); i++) {
				firstPulseEnergy += Math.pow(data[i], 2);
			}
			if (data2 != null) {
				for (int i = risingEdgeVals.get(1); i < fallingEdgeVals.get(1); i++) {
					secondPulseEnergy += Math.pow(data2[i], 2);
				}
			} else {
				for(int i = risingEdgeVals.get(1); i < fallingEdgeVals.get(1); i++){
					secondPulseEnergy += Math.pow(data[i], 2);
				}
			}
		}
		energyRatio = secondPulseEnergy / firstPulseEnergy * 100.0;

		if (risingEdgeVals.size() < 2){
        	youngsModulus = 0;
        	youngsModulusLabel.setText("Young's Modulus: " + SPOperations.round(youngsModulus, 4));
        	return;
        }
        
        for(int i = 0; i < 2; i++){
        	if(i < risingEdgeVals.size())
        		chart.addVerticalValueMarker(new Data<Number, Number>(timeData[risingEdgeVals.get(i)], 0), Color.RED);
        	if(i < fallingEdgeVals.size())
        		chart.addVerticalValueMarker(new Data<Number, Number>(timeData[fallingEdgeVals.get(i)], 0), Color.GREEN);
        }
        
        if(data2 == null){
        	youngsModulus = Math.pow(getCurrentBar().strainGauges.get(0).distanceToSample * 2 / (timeData[risingEdgeVals.get(1)] - timeData[risingEdgeVals.get(0)]), 2) * getCurrentBar().density;
        }
        else{
        	double distance = Math.abs(getCurrentBar().strainGauges.get(0).distanceToSample - getCurrentBar().strainGauges.get(1).distanceToSample);
        	youngsModulus = Math.pow(distance * 2 / (timeData[risingEdgeVals.get(1)] - timeData[risingEdgeVals.get(0)]), 2) * getCurrentBar().density;
        }
        
        if(SPSettings.metricMode.get())
        	youngsModulusLabel.setText("Young's Modulus: " + SPOperations.round(Converter.GpaFromPa(youngsModulus), 4));
        else 
        	youngsModulusLabel.setText("Young's Modulus: " + SPOperations.round(Converter.MpsiFromPa(youngsModulus), 4));
	}
	
	public Bar getCurrentBar(){
		if(calibrationMode == CalibrationMode.INCIDENT)
			return barSetup.IncidentBar;
		else if(calibrationMode == CalibrationMode.TRANSMISSION)
			return barSetup.TransmissionBar;
		else
			Dialogs.showAlert("Calibration Mode not supported.", stage);
		return null;
	}
	

	
	public enum CalibrationMode{
		INCIDENT, TRANSMISSION, ALIGNMENT;
	}
	
}
