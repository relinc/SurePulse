package net.relinc.processor.controllers;


import java.awt.Desktop.Action;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.MathArrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.fitter.application.FitableDataset;
import net.relinc.processor.application.BarSetup;
import net.relinc.processor.application.LineChartWithMarkers;
import net.relinc.processor.data.DataFile;
import net.relinc.processor.data.DataFileListWrapper;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.HopkinsonBarPulse;
import net.relinc.processor.data.IncidentPulse;
import net.relinc.processor.data.ReflectedPulse;
import net.relinc.processor.data.ModifierFolder.Modifier;
import net.relinc.processor.data.ModifierFolder.ZeroOffset;
import net.relinc.processor.data.ModifierFolder.Modifier.ModifierEnum;
import net.relinc.processor.fxControls.NumberTextField;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.processor.staticClasses.PochammerChreeDispersion;
import net.relinc.processor.staticClasses.SPMath;
import net.relinc.processor.staticClasses.SPOperations;


public class TrimDataController {

	//@FXML LineChart<Number, Number> chart;
	@FXML AnchorPane chartAnchorPane;
	@FXML RadioButton beginRadio;
	@FXML RadioButton endRadio;
	@FXML ListView<DataSubset> listView;
	@FXML RadioButton drawZoomRadio;
	@FXML CheckBox logCB;
	//@FXML CheckBox applyFilterCB;
	@FXML HBox bottomHBox;
	@FXML HBox filterHBox;
	@FXML HBox beginEndHBox;
	@FXML HBox modifierControlsHBox;
	@FXML ChoiceBox<Modifier> modifierChoiceBox;
	AnchorPane tfHolder = new AnchorPane();
	
	int dataPointsToShow = 1000;
	int autoselectLocation;
	
	NumberAxis xAxis = new NumberAxis();
	NumberAxis yAxis = new NumberAxis();
	LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<Number, Number>(xAxis, yAxis);
	final ToggleGroup group = new ToggleGroup();
	Button getReflectedBeginFromIncidentButton = new Button("Set Begin From Incident and Bar Setup");
	
	Point2D beginRectangle = new Point2D(0, 0);
	Point2D endRectangle = new Point2D(0, 0);
	Point2D beginDrawnRectangle = new Point2D(0, 0);
	Point2D endDrawnRectangle = new Point2D(0, 0);
	Rectangle DrawnRectangle = new Rectangle(0,0,Color.RED);
	double greyLineVal = 0.0;
	public DataFileListWrapper DataFiles;
	public Stage stage;
	public BarSetup barSetup;
	VBox holdGrid = new VBox();
	
	
	public void initialize(){
		chartAnchorPane.getChildren().add(chart);
		chartAnchorPane.getChildren().add(DrawnRectangle);
		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setBottomAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		
		
		DrawnRectangle.setFill(null);
		DrawnRectangle.setStroke(Color.RED);
		
		
//		GridPane grid = new GridPane();
//		
//
////		filterTF = new NumberTextField("KHz", "KHz");
////		filterTF.setText("1000");
////		filterTF.updateLabelPosition();
//		grid.add(filterTF, 0, 0);
//		grid.add(filterTF.unitLabel, 0, 0);
//		
//		
//		holdGrid.getChildren().add(grid);
//		holdGrid.setAlignment(Pos.CENTER);

		//filterHBox.getChildren().add(1, holdGrid);
		//bottomHBox.getChildren().add(0,holdGrid);

		
		beginRadio.setToggleGroup(group);
		endRadio.setToggleGroup(group);
		drawZoomRadio.setToggleGroup(group);
		
		logCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
	        public void changed(ObservableValue<? extends Boolean> ov,
	                Boolean old_val, Boolean new_val) {
	        	
	                    updateChart();
	                    
	            }
	        });
		
		listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
		    @Override
		    public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue, DataSubset newValue) {
		        xAxis.setAutoRanging(true); //zooms out
		        yAxis.setAutoRanging(true); //zooms out
		        updateControls();
		        updateChart();
		    }
		});
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
				if(beginRadio.isSelected()){
					getActivatedData().setBeginFromTimeValue(timeValue);
				}
				else if(endRadio.isSelected()){
					getActivatedData().setEndFromTimeValue(timeValue);
				}
				else if(drawZoomRadio.isSelected()){
					beginRectangle = new Point2D((double)chart.getXAxis().getValueForDisplay(mouseEvent.getX()), (double)chart.getYAxis().getValueForDisplay(mouseEvent.getY()));
				}
				updateAnnotations();
			}
		});
		
		chart.lookup(".chart-plot-background").setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				endRectangle = new Point2D((double)chart.getXAxis().getValueForDisplay(mouseEvent.getX()), (double)chart.getYAxis().getValueForDisplay(mouseEvent.getY()));
			}
		
		
		});
		chart.lookup(".chart-plot-background").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(drawZoomRadio.isSelected()){
				
				xAxis.setLowerBound(Math.min(beginRectangle.getX(), endRectangle.getX()));
				xAxis.setUpperBound(Math.max(beginRectangle.getX(), endRectangle.getX()));
				
				
				
				yAxis.setLowerBound(Math.min(beginRectangle.getY(), endRectangle.getY()));
				yAxis.setUpperBound(Math.max(beginRectangle.getY(), endRectangle.getY()));
				
				xAxis.setAutoRanging(false);
				yAxis.setAutoRanging(false);
				
				DrawnRectangle.setWidth(0);
				DrawnRectangle.setHeight(0);
				
				updateChart();
				}
			}
		
		
		});
		
		chart.lookup(".chart-plot-background").setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(beginRadio.isSelected()){
					if(getActivatedData().getIndexFromTimeValue((double) xAxis.getValueForDisplay(mouseEvent.getX()))
							< getActivatedData().getEnd()){
						greyLineVal = (double) xAxis.getValueForDisplay(mouseEvent.getX());
					}
					else{
						greyLineVal = Double.MAX_VALUE;
					}
					updateAnnotations();
					
				}
				else if(endRadio.isSelected()){
					if(getActivatedData().getIndexFromTimeValue((double) xAxis.getValueForDisplay(mouseEvent.getX()))
							> getActivatedData().getBegin()){
						greyLineVal = (double) xAxis.getValueForDisplay(mouseEvent.getX());
					}
					else{
						greyLineVal = Double.MAX_VALUE;
					}
					updateAnnotations();
					
				}
			}
		
		
		});
		
		chart.lookup(".chart-plot-background").setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				greyLineVal = Double.MAX_VALUE;
				updateAnnotations();
			}
		});
		
		
		
		chartAnchorPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				
				if(drawZoomRadio.isSelected()){
					beginDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
				}
				updateAnnotations();
			}
		});
		
		chartAnchorPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(drawZoomRadio.isSelected()){
					endDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
					Rectangle r = getRectangleFromPoints(beginDrawnRectangle, endDrawnRectangle);
					DrawnRectangle.relocate(r.getX(), r.getY());
					DrawnRectangle.setWidth(r.getWidth());
					DrawnRectangle.setHeight(r.getHeight());
				}
			}

			
		
		
		});
		

		
		chartAnchorPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(group.getSelectedToggle() == null)
					chart.getScene().setCursor(Cursor.DEFAULT);
				else if(group.getSelectedToggle() == drawZoomRadio)
					chart.getScene().setCursor(Cursor.CROSSHAIR);
			}
		});
		chartAnchorPane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				chart.getScene().setCursor(Cursor.DEFAULT);
				
			}
		});
		
		getReflectedBeginFromIncidentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setReflectedBeginFromIncidentAndBarSetup();
			}
		});
		
//		for(ModifierEnum en : ModifierEnum.values())
//			modifierChoiceBox.getItems().add(Modifier.getNewModifier(en));
		
		modifierChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Modifier>() {
			@Override
			public void changed(ObservableValue<? extends Modifier> observable, Modifier oldValue, Modifier newValue) {
				updateModifierControls();
				updateChart();
			}
		});
		
		
	}
	
	@FXML
	public void runAutoselectButtonFired(){
		runAutoselect();
		int previousEnd = getActivatedData().getEnd();
		Dialog<autoselectDialogResult> dialog = new Dialog<>();
		dialog.setTitle("Configure autoselect");
		dialog.setHeaderText("Autoselect has run. Please select an option below. \nSelecting 'begin' places the begin index at the autoselected location.");
		
		HBox leftRightButtonsHBox = new HBox();
		Button leftButton = new Button("Left");
		leftButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				getActivatedData().setEnd(autoselectLocation);
				runAutoselect();
			}
		});
		Button rightButton = new Button("Right");
		rightButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				getActivatedData().setBegin(autoselectLocation);
				runAutoselect();
			}
		});
		leftRightButtonsHBox.getChildren().add(leftButton);
		leftRightButtonsHBox.getChildren().add(rightButton);
		leftRightButtonsHBox.setAlignment(Pos.CENTER);
		leftRightButtonsHBox.setSpacing(10);
		dialog.getDialogPane().setContent(leftRightButtonsHBox);
//		ButtonType leftButton = new ButtonType("Left", ButtonData.OK_DONE);
//		ButtonType rightButton = new ButtonType("Right", ButtonData.OK_DONE);
		ButtonType cancelButton = new ButtonType("Accept", ButtonData.OK_DONE);
//		dialog.getDialogPane().getButtonTypes().add(leftButton);
//		dialog.getDialogPane().getButtonTypes().add(rightButton);
		dialog.getDialogPane().getButtonTypes().add(cancelButton);
				
		dialog.setResultConverter(new Callback<ButtonType, autoselectDialogResult>() {
		    @Override
		    public autoselectDialogResult call(ButtonType b) {
//		        if (b == rightButton) {
//		            return autoselectDialogResult.BEGIN;
//		        }
//		        else if(b == leftButton)
//		        	return autoselectDialogResult.END;
//		        else 
		        return autoselectDialogResult.CANCEL;
		    }
		});
				
		Optional<autoselectDialogResult> result = dialog.showAndWait();
				
		if (result.isPresent()) {
			getActivatedData().setBegin(autoselectLocation);
			getActivatedData().setEnd(previousEnd);
		}
		autoselectLocation = 0;
		updateChart();
	}

	private void runAutoselect() {
		int theta = 2;
		double[] testX = getActivatedData().getTrimmedData();
		ArrayList<double[]> diluted = SPMath.diluteData(testX, 1000);
		testX = diluted.get(0);
		double[] oldIndices = diluted.get(1);

		double[] differences = getConsectiveDifferences(testX);
		
		
		double[] scan1 = new double[testX.length - 3];
		double[] indexes = new double[scan1.length];
		for(int i = 0; i < scan1.length; i++){
			//System.out.println("First Scan. At: " + i / (double)scan1.length);
			scan1[i] = runQDiff(i + 1, differences.length - 1, differences, theta);
			indexes[i] = i;
		}
		MathArrays.sortInPlace(scan1, indexes);
		
		int winner = (int)indexes[indexes.length - 1];
		
		autoselectLocation = getActivatedData().getBegin() + (int)oldIndices[winner];
		updateAnnotations();
	}
	
	private enum autoselectDialogResult{
		BEGIN, END, CANCEL;
	}
	
	public double empiricalDiv(double[] x, double[] y, double theta){
		int m = x.length;
		int n = y.length;
		double term1 = 0;
		for(int i = 0; i < m; i++){
			for(int j = 0; j < n; j++){
				term1 += Math.pow(Math.abs(x[i] - y[j]), theta);
			}
		}
		term1 = term1 * 2 / m / n;
		
		double term2 = 0;
		for(int k = 0; k < m; k++){
			for(int i = 0; i <= k; i++){
				term2 += Math.pow(Math.abs(x[i] - x[k]), theta);
			}
		}
		term2 = term2 * Math.pow(CombinatoricsUtils.binomialCoefficientDouble(m, 2), -1);
		
		
		double term3 = 0; 
		for(int k = 0; k < n; k++){
			for(int i = 0; i <= k; i++){
				term3 += Math.pow(Math.abs(y[i] - y[k]), theta);
			}
		}
		term3 = term3 * Math.pow(CombinatoricsUtils.binomialCoefficientDouble(n, 2), -1);
		
		return term1 - term2 - term3;
	}
	
	public double Q(double[] x, double[] y, double theta){
		double m = x.length;
		double n = y.length;
		return m*n / (m + n) * empiricalDiv(x, y, theta);
	}
	
	public double[] getConsectiveDifferences(double[] x){
		double[] diff = new double[x.length -1];
		for(int i = 0; i < diff.length; i++){
			diff[i] = x[i + 1] - x[i];
		}
		return diff;
	}
	
	public double runQDiff(int t, int k, double[] diff, double theta){
		if(k > t + 1){
			double[] group1 = new double[t + 1];
			for(int i = 0; i < group1.length; i++){
				group1[i] = diff[i];
			}
			double[] group2 = new double[k - t];
			for(int i = 0; i < group2.length; i++){
				group2[i] = diff[i + t + 1];
			}
			return Q(group1, group2, theta);
		}
		else{
			return -4;
		}
	}
	
	@FXML
	public void lauchFitterButtonFired(){
		Stage primaryStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/fitter/GUI/Home.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Splashpage.fxml"));
			//Scene scene = new Scene(root);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        //primaryStage.getIcons().add(SPSettings.getRELLogo());
	        primaryStage.setTitle("SURE-Pulse Fitter");
			primaryStage.setScene(scene);
			HomeController c = root1.<HomeController>getController();
			c.renderGUI();
			if(getActivatedData().fitableDataset == null)
				getActivatedData().fitableDataset = convertToFitableDataset(getActivatedData());
			c.datasetsListView.getItems().add(getActivatedData().fitableDataset);
			c.datasetsListView.getSelectionModel().select(0);
			c.renderGUI();
			
			//c.stage = primaryStage;
			primaryStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void removeModifierButtonFired(){
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		m.enabled.set(false);
		m.removeModifier();
		updateChart();
	}
	
	@FXML
	public void applyModifierButtonFired(){
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		
		m.configureModifier(getActivatedData());
		m.enabled.set(true);
		m.activateModifier();
		
		updateChart();
	}
	
	@FXML
	public void doneTrimmingDataFired(){
		boolean allAreTrimmed = true;
		for(DataFile d : DataFiles){
			for(DataSubset subset : d.dataSubsets){
				if(subset.getBegin() == 0 && subset.getEnd() == subset.Data.data.length - 1)
					allAreTrimmed = false;
			}
		}
		if(!allAreTrimmed){
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("Not all datasets were trimmed.");
			alert.setContentText("Would you like to continue?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
			    //close
			    Stage stage = (Stage) chart.getScene().getWindow();
			    stage.close();
			} else {
			    //do nothing
			}
		}
		else{
			//close
		    Stage stage = (Stage) chart.getScene().getWindow();
		    stage.close();
		}
	}

	private FitableDataset convertToFitableDataset(DataSubset activatedData) {
		if(activatedData == null)
			return null;
		ArrayList<Double> xValues = new ArrayList<>(activatedData.Data.timeData.length);
		ArrayList<Double> yValues = new ArrayList<>(activatedData.Data.timeData.length);

		for(int i = 0; i < activatedData.Data.timeData.length; i++){
			xValues.add(activatedData.Data.timeData[i]);
			yValues.add(activatedData.Data.data[i]);
		}
		FitableDataset d = new FitableDataset(xValues, yValues, activatedData.name);
		return d;
	}
	
	private Rectangle getRectangleFromPoints(Point2D p1, Point2D p2) {
		double beginX = Math.min(p1.getX(), p2.getX());
		double beginY = Math.min(p1.getY(), p2.getY());
		double width = Math.abs(p1.getX() - p2.getX());
		double height = Math.abs(p1.getY() - p2.getY());
		return new Rectangle(beginX, beginY, width, height);		
	}
	
	public void leftArrowButtonFired(){
		if(beginRadio.isSelected())
			getActivatedData().setBegin(getActivatedData().getBegin() - 1);
		else if(endRadio.isSelected())
			getActivatedData().setEnd(getActivatedData().getEnd() - 1);
		updateAnnotations();
	}
	
	public void rightArrowButtonFired(){
		if(beginRadio.isSelected())
			getActivatedData().setBegin(getActivatedData().getBegin() + 1);
		else if(endRadio.isSelected())
			getActivatedData().setEnd(getActivatedData().getEnd() + 1);
		updateAnnotations();
	}
	
	public void resetZoomFired(){
		xAxis.setAutoRanging(true);
		yAxis.setAutoRanging(true);
		updateChart();
	}
	
	public double[] polynomialSmooth(double[] inputXData, double[] inputYData, int range, int degree){
		double[] smoothedData = new double[inputYData.length];
		for(int i = 0; i < smoothedData.length; i++){
			//System.out.println(i);
			int begin = i - range / 2;
			int end = i + range / 2;
			if(begin < 0)
				begin = 0;
			if(end >= inputYData.length)
				end = inputYData.length - 1;
			
			//this is somewhat slow, could be improved by only sendind data in range.
			//System.out.println("array Length: " + Arrays.copyOfRange(inputXData, begin, end+1).length);
			//System.out.println("Ending at: " + (end - begin));
			
			
			double[] fittedData = SPOperations.getFittedData(Arrays.copyOfRange(inputXData, begin, end + 1), Arrays.copyOfRange(inputYData, begin, end + 1), 0, end - begin, degree);
			
			smoothedData[i] = fittedData[i - begin];
			
		}
		return smoothedData;
	}
	
	public double[] smoothArray( double[] values, double smoothing ){
		  double value = values[0]; // start with the first input
		  for (int i=1; i < values.length; ++i){
		    double currentValue = values[i];
		    value += (currentValue - value) / smoothing;
		    values[i] = value;
		  }
		  return values;
		}
	
//	public double[] filterData(double[] data, int range){
//		double[] output = new double[data.length];
//		for(int i = 0; i < data.length - range; i++){
//			int begin = i - range / 2;
//			int end = i + range / 2;
//			if(begin < 0)
//				begin = 0;
//			if(end >= data.length)
//				end = data.length - 1;
//			output[i] = average(data, begin, end);
//		}
//		return output;
//	}
	
	public double average(double[] a, int beginInclusive, int endInclusive){
		double sum = 0;
		for(int i = beginInclusive; i <= endInclusive; i++){
			sum += a[i];
		}
		return sum / (endInclusive - beginInclusive + 1);
	}
	
	public double[] lowPassFilter(double[] data, double d){
		double[] signal = data;
		
		double[] filter = new double[(int)(d)]; // box-car filter
		for(int i = 0; i < filter.length; i++){
			filter[i] = 1 / (double)filter.length;
		}
		double[] result = new double[signal.length + filter.length + 1];

		// Set result to zero:
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}

		// Do convolution:
		for (int i=0; i < signal.length; i++) 
		  for (int j=0; j < filter.length; j++)
		    result[i+j] = result[i+j] + signal[i] * filter[j];
		
		return result;
	}
	

//	function smoothArray( values, smoothing ){
//		  var value = values[0]; // start with the first input
//		  for (var i=1, len=values.length; i<len; ++i){
//		    var currentValue = values[i];
//		    value += (currentValue - value) / smoothing;
//		    values[i] = value;
//		  }
//		}

	public void update(){
		updateListView();
		updateChart();
		if(listView.getSelectionModel().selectedIndexProperty().getValue() == -1){
		listView.getSelectionModel().select(0);
	
	}
		
	}

	private void updateChart() {
		if(listView.getSelectionModel().getSelectedIndex() == -1)
			return;
		double[] xData = getActivatedData().Data.timeData;
		double[] yData = getActivatedData().Data.data;
		double[] filteredYData = yData.clone();
		double[] pochammerAdjustedData = new double[getActivatedData().getEnd() - getActivatedData().getBegin() + 1];
		double[] zeroedData = yData.clone();
		
		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
			filteredYData = SPMath.fourierLowPassFilter(filteredYData, getActivatedData().modifiers.getLowPassModifier().getLowPassValue(), 1.0 / (xData[1] - xData[0]));
		}
		if(getActivatedData().modifiers.getZeroModifier().activated.get()){
			zeroedData = SPMath.subtractFrom(zeroedData, ((ZeroOffset)getActivatedData().modifiers.getModifier(ModifierEnum.ZERO)).getZero());
		}
		
		if(getActivatedData().modifiers.getPochammerModifier().activated.get()){
			if(getActivatedData() instanceof HopkinsonBarPulse){
				HopkinsonBarPulse pulse = (HopkinsonBarPulse)getActivatedData();
				pochammerAdjustedData = pulse.getPochammerAdjustedArray(barSetup);
			}
		}
		
		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> series3 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> zeroedSeries = new XYChart.Series<Number, Number>();
        series1.setName("Raw Data");
        series2.setName("Filtered");
        series3.setName("Pochammer-Chree Dispersion");
        zeroedSeries.setName("Zeroed");
        chart.setCreateSymbols(false);
        
        ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> filteredDataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> pochammerDataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> zeroedDataPoints = new ArrayList<Data<Number, Number>>();
        
        int beginIndex = getActivatedData().getIndexFromTimeValue(xAxis.getLowerBound());
        int endIndex = getActivatedData().getIndexFromTimeValue(xAxis.getUpperBound());
        if(xAxis.isAutoRanging()){
        	//it is zoomed out
        	beginIndex = 0;
        	endIndex = xData.length - 1;
        }
        int totalDataPoints = endIndex - beginIndex;
        
        int previousPochammerIndex = beginIndex;
        for(int i = beginIndex; i <= endIndex; i++){
        	if(logCB.isSelected()){
        		if(yData[i] == 0 || Math.log(Math.abs(yData[i])) > 50){
        			dataPoints.add(new Data<Number, Number>(xData[i], 0));
        		}
        		else{
        			dataPoints.add(new Data<Number, Number>(xData[i], Math.log(Math.abs(yData[i]))));
        		}
        		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
        			if(filteredYData[i] == 0 || Math.log(Math.abs(filteredYData[i])) > 50){
        				filteredDataPoints.add(new Data<Number, Number>(xData[i], 0));
            		}
            		else{
            			filteredDataPoints.add(new Data<Number, Number>(xData[i], Math.log(Math.abs(filteredYData[i]))));
            		}
            	}
        	}
        	else{
        		dataPoints.add(new Data<Number, Number>(xData[i], yData[i]));
        		if(getActivatedData().modifiers.getModifier(ModifierEnum.ZERO).activated.get())
        			zeroedDataPoints.add(new Data<Number, Number>(xData[i], zeroedData[i]));
        		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
            		filteredDataPoints.add(new Data<Number, Number>(xData[i], filteredYData[i]));
            	}
        		if(getActivatedData().modifiers.getPochammerModifier().activated.get()){
        			if(i >= getActivatedData().getBegin() && i <= getActivatedData().getEnd()){
        				int pochammerIndex = (int)((i - getActivatedData().getBegin()) / PochammerChreeDispersion.skip);
    					if (pochammerIndex != previousPochammerIndex) {
    						pochammerDataPoints.add(new Data<Number, Number>(xData[i],
    								pochammerAdjustedData[pochammerIndex]));
    						previousPochammerIndex = pochammerIndex;
    					}
        			}
        			
        		}
        	}
        	
        	i += totalDataPoints / dataPointsToShow;
        }
        
        series1.getData().addAll(dataPoints);
        series2.getData().addAll(filteredDataPoints);
        series3.getData().addAll(pochammerDataPoints);
        zeroedSeries.getData().addAll(zeroedDataPoints);
//        for(int i = 0; i < xData.length; i++){
//            series1.getData().add(new Data<Number, Number>(xData[i], yData[i]));
//            i += xData.length / dataPointsToShow;
//        }
        
        
        chart.getData().clear();
        chart.getData().addAll(series1);
        chart.getData().addAll(series2);
        chart.getData().addAll(series3);
        chart.getData().addAll(zeroedSeries);
        
        updateAnnotations();
	}
	
	public void updateAnnotations(){
		chart.clearVerticalMarkers();
        chart.addVerticalValueMarker(new Data<Number, Number>(greyLineVal, 0));
        if(autoselectLocation != 0)
        	chart.addVerticalValueMarker(new Data<Number, Number>(getActivatedData().Data.timeData[autoselectLocation],0), Color.RED);
        chart.addVerticalRangeMarker(new Data<Number, Number>(getActivatedData().Data.timeData[getActivatedData().getBegin()], 
        		getActivatedData().Data.timeData[getActivatedData().getEnd()]), Color.BLUE);
	}
	
	public DataSubset getActivatedData() {
		int selectedIndex = listView.getSelectionModel().getSelectedIndex();
		return DataFiles.getAllDatasets().get(selectedIndex);
		
	}
	
	private void updateListView() {
		
//		ObservableList<String> items = FXCollections.observableArrayList (
//			    "Single", "Double", "Suite", "Family App");
//		ArrayList<String> dataDescriptors = new ArrayList<String>();
//		for(DataSubset d : DataFiles.getAllDatasets()){
//			dataDescriptors.add(d.name);
//		}
//		ObservableList<String> items = FXCollections.observableArrayList (dataDescriptors);
		ObservableList<DataSubset> subsets = FXCollections.observableArrayList (DataFiles.getAllDatasets());
		listView.setItems(subsets);
	}
	
	private void updateControls(){
		modifierChoiceBox.getItems().clear();
		modifierChoiceBox.getItems().addAll(getActivatedData().modifiers);
		modifierChoiceBox.getSelectionModel().select(0);
		
		while(beginEndHBox.getChildren().size() > 3)
			beginEndHBox.getChildren().remove(beginEndHBox.getChildren().size() - 1);
		if(getActivatedData() instanceof ReflectedPulse){
			beginEndHBox.getChildren().add(getReflectedBeginFromIncidentButton);
		}
		
		
	}
	
	private void updateModifierControls(){
		modifierControlsHBox.getChildren().clear();
		if(modifierChoiceBox.getSelectionModel().getSelectedItem() == null)
			modifierChoiceBox.getSelectionModel().select(0);
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		if(m == null)
			return;
		for(Node node : m.getTrimDataHBoxControls())
			modifierControlsHBox.getChildren().add(node);
	}
	
	private void setReflectedBeginFromIncidentAndBarSetup(){
		int incidentCount = 0;
		IncidentPulse incidentPulse = null;
		for(DataSubset sub : DataFiles.getAllDatasets()){
			if(sub instanceof IncidentPulse){
				incidentCount++;
				incidentPulse = (IncidentPulse)sub;
			}
		}
		if(incidentCount != 1){
			Dialogs.showAlert("There must be 1 incident pulse.", stage);
			return;
		}
		ReflectedPulse reflectedPulse = (ReflectedPulse)getActivatedData();
		
		
		double beginIncidentTime = incidentPulse.Data.timeData[incidentPulse.getBegin()];
		double IncidWaveSpeed = barSetup.IncidentBar.getWaveSpeed();
		double timeToTravel = incidentPulse.strainGauge.distanceToSample / IncidWaveSpeed + 
				reflectedPulse.strainGauge.distanceToSample / IncidWaveSpeed; //distances to sample are the same. Same SG
		reflectedPulse.setBeginFromTimeValue(beginIncidentTime + timeToTravel);
		updateChart();
	}
	
}
