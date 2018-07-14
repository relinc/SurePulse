package net.relinc.processor.controllers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.controlsfx.control.SegmentedButton;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataFileListWrapper;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.ModifierFolder.LowPass;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
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
	CalculationMode mode = CalculationMode.AUTO;
	
	@FXML ScrollBar noiseLevelScrollBar;
	@FXML TextField distanceTF;
	@FXML Label youngsModulusLabel;
	@FXML AnchorPane chartAnchorPane;
	@FXML Button calculateEnergyRatioButton;
	@FXML VBox manualControlsVBox;
	ToggleGroup modeGroup = new ToggleGroup();
	ToggleGroup beginEndGroup = new ToggleGroup();
	
	RadioButton drawFirstZoneRadioButton = new RadioButton("Draw First Zone");
	RadioButton drawSecondZoneRadioButton = new RadioButton("Draw Second Zone");
	RadioButton beginRadioButton = new RadioButton("Begin");
	RadioButton endRadioButton = new RadioButton("End");
	NumberTextField distanceBetweenPulses = new NumberTextField("in", "mm");
	double firstZoneStartTime;
	double firstZoneEndTime;
	double secondZoneStartTime;
	double secondZoneEndTime;
	
	private double energyRatio;
	
	private enum CalculationMode{
		AUTO, MANUAL;
	}
	
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
		
		distanceBetweenPulses.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				updateAnnotations();
			}
		});
		
		beginRadioButton.setToggleGroup(beginEndGroup);
		endRadioButton.setToggleGroup(beginEndGroup);
		beginRadioButton.setSelected(true);
		
		SegmentedButton b = new SegmentedButton();
		ToggleButton b1 = new ToggleButton("Auto");
		ToggleButton b2 = new ToggleButton("Manual");
		b.getButtons().addAll(b1, b2);
		b.getButtons().get(0).setSelected(true);
		manualControlsVBox.getChildren().add(b);
		manualControlsVBox.setAlignment(Pos.TOP_LEFT);
		
		b1.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mode = CalculationMode.AUTO;
				renderControls();
			}
		});
		
		b2.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				mode = CalculationMode.MANUAL;
				renderControls();
			}
		});
		
		drawFirstZoneRadioButton.setSelected(true);
		drawFirstZoneRadioButton.setToggleGroup(modeGroup);
		drawSecondZoneRadioButton.setToggleGroup(modeGroup);
		
		noiseLevelScrollBar.setTooltip(new Tooltip("Adjust the noise level to be half of the magnitude of the pulse."));
		calculateEnergyRatioButton.setTooltip(new Tooltip("Calculates the energy ratio of the second pulse to the first pulse. Useful information for calculating the efficiency"
				+ " of energy transfer between separated bars."));
		
	}
	
	public void renderControls(){
		//renders the auto or manual controls. Keep the segmented button in the first position of the VBox.
		while(manualControlsVBox.getChildren().size() > 1)
			manualControlsVBox.getChildren().remove(manualControlsVBox.getChildren().size() -1);
		if(mode == CalculationMode.AUTO){
			//don't add any controls.
		}
		else if(mode == CalculationMode.MANUAL){
			//add manual controls.
			manualControlsVBox.getChildren().add(drawFirstZoneRadioButton);
			manualControlsVBox.getChildren().add(drawSecondZoneRadioButton);
			HBox box = new HBox();
			box.getChildren().add(beginRadioButton);
			box.getChildren().add(endRadioButton);
			box.setAlignment(Pos.CENTER_LEFT);
			box.setSpacing(10);
			box.setPadding(new Insets(5,5,5,5));
			box.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID,new CornerRadii(3), new BorderWidths(1))));
			manualControlsVBox.getChildren().add(box);
			HBox distHbox = new HBox();
			distHbox.getChildren().add(new Label("Distance Between Pulses: "));
			distanceBetweenPulses.setPrefWidth(70);
			GridPane grid = new GridPane();
			grid.add(distanceBetweenPulses, 0, 0);
			grid.add(distanceBetweenPulses.unitLabel, 0, 0);
			distHbox.getChildren().add(grid);
			distHbox.setAlignment(Pos.CENTER_LEFT);
			distHbox.setSpacing(10);
			distHbox.setPadding(new Insets(5,5,5,5));
			manualControlsVBox.getChildren().add(distHbox);
		}
		renderChartData();
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
			int idx = 0;
			for(DataFile file : dataFiles){
				for(DataSubset d : file.dataSubsets){
					if(idx == 0){
						firstZoneStartTime = d.Data.timeData[d.Data.timeData.length / 8];
						firstZoneEndTime = d.Data.timeData[d.Data.timeData.length / 5];
						secondZoneStartTime = d.Data.timeData[d.Data.timeData.length / 2];
						secondZoneEndTime = d.Data.timeData[d.Data.timeData.length * 2 / 3];
					}
					idx++;
					
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
	        chart.getData().add(series1);
	        chart.getData().add(series2);
	        
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
	        noiseLevelScrollBar.setMax(maxValGraphed);
	        series1.getData().addAll(dataPoints);
	        
	        chart.getData().clear();
	        chart.getData().add(series1);
		}
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
				
				if(mode == CalculationMode.MANUAL){
					if(drawFirstZoneRadioButton.isSelected()){
						if(beginRadioButton.isSelected()){
							firstZoneStartTime = timeValue;
						}
						else if(endRadioButton.isSelected()){
							firstZoneEndTime = timeValue;
						}
					}
					else if(drawSecondZoneRadioButton.isSelected()){
						if(beginRadioButton.isSelected()){
							secondZoneStartTime = timeValue;
						}
						else if(endRadioButton.isSelected()){
							secondZoneEndTime = timeValue;
						}
					}
					updateAnnotations();
				}
			}
		});
		
		updateAnnotations();
	}
	
	
	
	public void updateAnnotations(){
		chart.clearHorizontalMarkers();
		chart.clearVerticalMarkers();
        chart.addHorizontalValueMarker(new Data<Number, Number>(0, noiseLevelScrollBar.getValue()));
        chart.addHorizontalValueMarker(new Data<Number, Number>(0,-noiseLevelScrollBar.getValue()));
        
        if(mode == CalculationMode.MANUAL){
        	chart.addVerticalRangeMarker(new Data<Number, Number>(firstZoneStartTime, firstZoneEndTime), Color.BLUE);
        	chart.addVerticalRangeMarker(new Data<Number, Number>(secondZoneStartTime, secondZoneEndTime), Color.ORANGE);
        }
        
        double[] data = getDataSubsets().get(0).getModifiedData();
        double[] data2 = getDataSubsets().size() > 1 ? getDataSubsets().get(1).getModifiedData() : null;
        double[] timeData = getDataSubsets().get(0).Data.timeData;
        
        data = Arrays.stream(data).map(n -> Math.abs(n)).toArray();
        int smooth = data.length / 1000 + 1;
        
        List<DataPoint> risingEdgeVals = new ArrayList<>();
        List<DataPoint> fallingEdgeVals = new ArrayList<>();
        double noiseLevel = noiseLevelScrollBar.getValue();
        
        if(data2 != null){
            data2 = Arrays.stream(data2).map(n -> Math.abs(n)).toArray();
            
        	for(int i = 0; i < data.length - smooth - 1; i++){
            	if(data[i] <= noiseLevel && data[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(new DataPoint(i, timeData[i], data[i]));
            		i += smooth;
            	}
            	if(data[i] >= noiseLevel && data[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(new DataPoint(i, timeData[i], data[i]));
            		i += smooth;
            	}
            	
            	if(data2[i] <= noiseLevel && data2[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(new DataPoint(i, timeData[i], data2[i]));
            		i += smooth;
            	}
            	if(data2[i] >= noiseLevel && data2[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(new DataPoint(i, timeData[i], data2[i]));
            		i += smooth;
            	}
            }
        }
        else{
        	for(int i = 0; i < data.length - smooth - 1; i++){
            	if(data[i] <= noiseLevel && data[i + smooth] >= noiseLevel)
            	{
            		risingEdgeVals.add(new DataPoint(i, timeData[i], data[i]));
            		i += smooth;
            	}
            	if(data[i] >= noiseLevel && data[i + smooth] <= noiseLevel)
            	{
            		fallingEdgeVals.add(new DataPoint(i, timeData[i], data[i]));
            		i += smooth;
            	}
            }
        }
        
        if(mode == CalculationMode.MANUAL){
        	//filter out rising and falling edges that don't fall within the user defined ranges.
        	risingEdgeVals = risingEdgeVals.stream().filter(p -> timeIsWithinAZone(p.time)).collect(Collectors.toList());
        	//don't know if I want this one.
        	fallingEdgeVals = fallingEdgeVals.stream().filter(p -> timeIsWithinAZone(p.time)).collect(Collectors.toList());
        }
        
        //calculate energy ratios
		double firstPulseEnergy = 0.0;
		double secondPulseEnergy = 0.0;

		if (risingEdgeVals.size() >= 2 && fallingEdgeVals.size() >= 2) {
			for (int i = risingEdgeVals.get(0).index; i < fallingEdgeVals.get(0).index; i++) {
				firstPulseEnergy += Math.pow(data[i], 2);
			}
			if (data2 != null) {
				for (int i = risingEdgeVals.get(1).index; i < fallingEdgeVals.get(1).index; i++) {
					secondPulseEnergy += Math.pow(data2[i], 2);
				}
			} else {
				for(int i = risingEdgeVals.get(1).index; i < fallingEdgeVals.get(1).index; i++){
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
        		chart.addVerticalValueMarker(new Data<Number, Number>(risingEdgeVals.get(i).time, 0), Color.RED);
        	if(i < fallingEdgeVals.size())
        		chart.addVerticalValueMarker(new Data<Number, Number>(fallingEdgeVals.get(i).time, 0), Color.GREEN);
        }
        
        if(data2 == null){
        	double distance = getCurrentBar().strainGauges.get(0).distanceToSample * 2;
        	if(mode == CalculationMode.MANUAL){
        		if(SPSettings.metricMode.get()){
        			distance = Converter.mFromMm(distanceBetweenPulses.getDouble());
        		}
        		else{
        			distance = Converter.MeterFromInch(distanceBetweenPulses.getDouble());
        		}
        	}
        	youngsModulus =  Math.pow(distance / (risingEdgeVals.get(1).time - risingEdgeVals.get(0).time), 2) * getCurrentBar().density;
        }
        else{
        	double distance = Math.abs(getCurrentBar().strainGauges.get(0).distanceToSample - getCurrentBar().strainGauges.get(1).distanceToSample);
        	if(mode == CalculationMode.MANUAL){
        		if(SPSettings.metricMode.get()){
        			distance = Converter.mmFromM(distanceBetweenPulses.getDouble());
        		}
        		else{
        			distance = Converter.MeterFromInch(distanceBetweenPulses.getDouble());
        		}
        		
        	}
        	//youngsModulus = Math.pow(distance * 2 / (risingEdgeVals.get(1).time - risingEdgeVals.get(0).time), 2) * getCurrentBar().density; //fixed 4-11-2016
        	youngsModulus = Math.pow(distance / (risingEdgeVals.get(1).time - risingEdgeVals.get(0).time), 2) * getCurrentBar().density;
        }
        
        if(SPSettings.metricMode.get())
        	youngsModulusLabel.setText("Young's Modulus: " + SPOperations.round(Converter.GpaFromPa(youngsModulus), 4));
        else 
        	youngsModulusLabel.setText("Young's Modulus: " + SPOperations.round(Converter.MpsiFromPa(youngsModulus), 4));
	}
	
	private boolean timeIsWithinAZone(double t){
		return (t >= firstZoneStartTime && t <= firstZoneEndTime) || (t >= secondZoneStartTime && t <= secondZoneEndTime);
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
	
	private class DataPoint{
		public int index;
		public double time;
		public DataPoint(int i, double t, double v){
			index = i;
			time = t;
		}
	}

	
	public enum CalibrationMode{
		INCIDENT, TRANSMISSION, ALIGNMENT;
	}
	
}
