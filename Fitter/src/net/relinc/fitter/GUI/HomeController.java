package net.relinc.fitter.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.relinc.datafileparser.application.Home;
import net.relinc.fitter.application.LineChartWithMarkers;
import net.relinc.fitter.staticClasses.SPMath;
import net.relinc.libraries.application.FitableDataset;

public class HomeController  {
	@FXML VBox leftVBox;
	@FXML public ListView<FitableDataset> datasetsListView;//fill this with Fitable datasets to use.
	@FXML VBox chartVBox;
	@FXML Label datasetNameLabel;
	@FXML Label pointsToRemoveLabel;
	@FXML Label orderOfPolynomialFitLabel;
	@FXML ScrollBar polynomialOrderScrollBar;
	@FXML ScrollBar pointsToRemoveScrollBar;
	@FXML RadioButton setBeginRadioButton;
	@FXML RadioButton setEndRadioButton;
	@FXML RadioButton resetZoomRadioButton;
	@FXML CheckBox smoothAllPointsCB;
	@FXML AnchorPane fitChartAnchorPane;
	@FXML AnchorPane residualChartAnchorPane;

	public Stage stage;
	int DataPointsToShow = 2000;
	ToggleGroup chartClickRadioButtonGroup = new ToggleGroup();
	public boolean showLoadFileButton;
	public List<List<String>> dataFileLoaderBucket = new ArrayList<List<String>>();
	Point2D beginRectangle = new Point2D(0, 0);
	Point2D endRectangle = new Point2D(0, 0);
	Point2D beginDrawnRectangle = new Point2D(0, 0);
	Point2D endDrawnRectangle = new Point2D(0, 0);
	Rectangle DrawnRectangle = new Rectangle(0,0,Color.RED);
	
	NumberAxis fitChartXAxis = new NumberAxis();
	NumberAxis fitChartYAxis = new NumberAxis();
	LineChartWithMarkers<Number, Number> fitChart = new LineChartWithMarkers<>(fitChartXAxis, fitChartYAxis);
	
	NumberAxis residualChartXAxis = new NumberAxis();
	NumberAxis residualChartYAxis = new NumberAxis();

	public void initialize(){
		setBeginRadioButton.setToggleGroup(chartClickRadioButtonGroup);
		setEndRadioButton.setToggleGroup(chartClickRadioButtonGroup);
		resetZoomRadioButton.setToggleGroup(chartClickRadioButtonGroup);
		DrawnRectangle.setFill(null);
		DrawnRectangle.setStroke(Color.RED);
		
		fitChartXAxis.setForceZeroInRange(false);
		fitChartXAxis.setLabel("X");
		fitChartYAxis.setLabel("Y");
		fitChart.setCreateSymbols(false);
		fitChart.setTitle("Data and Fitted Line");
		fitChart.setAnimated(false);
		fitChartAnchorPane.getChildren().add(fitChart);
		fitChartAnchorPane.getChildren().add(DrawnRectangle);
		AnchorPane.setTopAnchor(fitChart, 0.0);
		AnchorPane.setBottomAnchor(fitChart, 0.0);
		AnchorPane.setLeftAnchor(fitChart, 0.0);
		AnchorPane.setRightAnchor(fitChart, 0.0);
		fitChart.getStylesheets().add(getClass().getResource("mixedChart.css").toExternalForm());
		
		datasetsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FitableDataset>() {
			@Override
			public void changed(ObservableValue<? extends FitableDataset> observable, FitableDataset oldValue,
					FitableDataset newValue) {
				pointsToRemoveScrollBar.setValue(getCurrentDataset().getPointsToRemove());
				polynomialOrderScrollBar.setValue(getCurrentDataset().getPolynomialFit());
				smoothAllPointsCB.setSelected(getCurrentDataset().getSmoothAllPointsMode());
				renderCharts();
				pointsToRemoveScrollBar.setMax(getCurrentDataset().origX.size() - 1);
			}
			
		});
		
		polynomialOrderScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				FitableDataset f = getCurrentDataset();
				if(f == null)
					return;
				int fit = (int)polynomialOrderScrollBar.getValue();
				f.setPolynomialFit(fit);
				f.renderFittedData();
				renderCharts();
				orderOfPolynomialFitLabel.setText("Order Of Polynomial Fit: " + fit);
			}

			
		});
		
		pointsToRemoveScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				int num = (int)pointsToRemoveScrollBar.getValue();
				FitableDataset f = getCurrentDataset();
				if(f == null)
					return;
				int fit = (int)polynomialOrderScrollBar.getValue();
				f.setPolynomialFit(fit);
				f.renderFittedData();
				pointsToRemoveLabel.setText("Points Removed: " + num);
			}
		});
		
		fitChart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double timeValue = (double) fitChart.getXAxis().getValueForDisplay(mouseEvent.getX());
				if(setBeginRadioButton.isSelected()){
					getCurrentDataset().setBeginFromXValue(timeValue);
				}
				else if(setEndRadioButton.isSelected()){
					getCurrentDataset().setEndFromXValue(timeValue);
				}
				else if(resetZoomRadioButton.isSelected()){
					beginRectangle = new Point2D(timeValue, (double)fitChart.getYAxis().getValueForDisplay(mouseEvent.getY()));
				}
				getCurrentDataset().renderFittedData();
				renderCharts();
			}
		});
		
		fitChart.lookup(".chart-plot-background").setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				double xVal = (double)fitChart.getXAxis().getValueForDisplay(mouseEvent.getX());
				double yVal = (double)fitChart.getYAxis().getValueForDisplay(mouseEvent.getY());
				endRectangle = new Point2D(xVal, yVal);
			}
		});
		
		fitChart.lookup(".chart-plot-background").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(resetZoomRadioButton.isSelected()){
					setChartBounds();
					updateFitChart();
				}
			}
		});
		
		fitChartAnchorPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				if(resetZoomRadioButton.isSelected()){
					beginDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
				}
			}
		});
		
		fitChartAnchorPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(resetZoomRadioButton.isSelected()){
					endDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
					Rectangle r = getRectangleFromPoints(beginDrawnRectangle, endDrawnRectangle);
					DrawnRectangle.relocate(r.getX(), r.getY());
					DrawnRectangle.setWidth(r.getWidth());
					DrawnRectangle.setHeight(r.getHeight());
				}
			}
		});
		
		fitChartAnchorPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(resetZoomRadioButton.isSelected()){
					fitChart.getScene().setCursor(Cursor.CROSSHAIR);
				}
				else{
					fitChart.getScene().setCursor(Cursor.DEFAULT);
				}
			}
		});
		
		fitChartAnchorPane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				fitChart.getScene().setCursor(Cursor.DEFAULT);
			}
		});
		
		
		
	}

	public void setFitterFromTrimSelection(Double xAxisBegin, Double xAxisEnd){
		FitableDataset theData = datasetsListView.getSelectionModel().getSelectedItem();
		theData.setBeginFromXValue(xAxisBegin);
		theData.setEndFromXValue(xAxisEnd);

		FitableDataset f = getCurrentDataset();
		if(f == null)
			return;
		int fit = (int)polynomialOrderScrollBar.getValue();
		f.setPolynomialFit(fit);
		f.renderFittedData();

		double maxY = -Double.MAX_VALUE;
		double minY = Double.MAX_VALUE;
		for(int i = 0; i < theData.origX.size(); i++){
			if( theData.origX.get(i) >= xAxisBegin && theData.origX.get(i) <= xAxisEnd) {
				if( theData.origY.get(i) > maxY ) { maxY = theData.origY.get(i); }
				if( theData.origY.get(i) < minY ) { minY = theData.origY.get(i); }
			}
		}

		beginRectangle = new Point2D(xAxisBegin - (xAxisEnd-xAxisBegin)*(0.05), maxY + (maxY-minY)*(0.1));
		endRectangle = new Point2D(xAxisEnd + (xAxisEnd-xAxisBegin)*(0.05), minY-(maxY-minY)*(0.1));
		setChartBounds();
		fitChartYAxis.setTickUnit((maxY-minY)*0.25);
		renderCharts();
	}
	
	public void updateLabels(){
		String coefficients = "";
		for(int i = 0; i < getCurrentDataset().coefficients.length; i++)
			coefficients += SPMath.round(getCurrentDataset().coefficients[i], 2) + ", ";
		datasetNameLabel.setText("Dataset: " + getCurrentDataset().getName() + "\n" + coefficients);
	}
	
	@FXML
	private void leftArrowButtonFired(){
		FitableDataset d = getCurrentDataset();
		if(d == null)
			return;
		if(setBeginRadioButton.isSelected())
			d.setBeginFit(d.getBeginFit() - 1);
		else if(setEndRadioButton.isSelected())
			d.setEndFit(d.getEndFit() - 1);
		d.renderFittedData();
		renderCharts();
	}
	@FXML
	private void rightArrowButtonFired(){
		FitableDataset d = getCurrentDataset();
		if(d == null)
			return;
		if(setBeginRadioButton.isSelected())
			d.setBeginFit(d.getBeginFit() + 1);
		else if(setEndRadioButton.isSelected())
			d.setEndFit(d.getEndFit() + 1);
		d.renderFittedData();
		renderCharts();
	}
	@FXML
	private void resetBeginAndEndButtonFired(){
		FitableDataset d = getCurrentDataset();
		if(d == null)
			return;
		d.resetBeginAndEnd();
		d.renderFittedData();
		renderCharts();
	}
	@FXML
	private void smoothAllPointsCBFired(){
		FitableDataset d = getCurrentDataset();
		d.setSmoothAllPointsMode(smoothAllPointsCB.isSelected());
		d.renderFittedData();
		renderCharts();
	}
	
	@FXML
	private void resetZoomButtonFired(){
		fitChartXAxis.setAutoRanging(true);
		fitChartYAxis.setAutoRanging(true);
		residualChartXAxis.setAutoRanging(true);
		residualChartYAxis.setAutoRanging(true);
		updateFitChart();
	}
	
	public void renderCharts() {
		residualChartAnchorPane.getChildren().clear();
		
		FitableDataset theData = datasetsListView.getSelectionModel().getSelectedItem();
		if(theData == null)
			return;
		updateLabels();
		updateFitChart();
		LineChartWithMarkers<Number, Number> residualChart = getResidualChart(theData);
		residualChartAnchorPane.getChildren().add(residualChart);
		AnchorPane.setTopAnchor(residualChart, 0.0);
		AnchorPane.setBottomAnchor(residualChart, 0.0);
		AnchorPane.setLeftAnchor(residualChart, 0.0);
		AnchorPane.setRightAnchor(residualChart, 0.0);
	}
	
	private LineChartWithMarkers<Number, Number> getResidualChart(FitableDataset theData) {

		String xlabel = "X";
		String yLabel = "Residual";

		residualChartXAxis.setLabel(xlabel);
		residualChartYAxis.setLabel(yLabel);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(residualChartXAxis, residualChartYAxis);
		chart.setCreateSymbols(false);
		chart.setTitle("Residual");
		chart.setAnimated(false);
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
				if(setBeginRadioButton.isSelected()){
					getCurrentDataset().setBeginFromXValue(timeValue);
				}
				else if(setEndRadioButton.isSelected()){
					getCurrentDataset().setEndFromXValue(timeValue);
				}
				getCurrentDataset().renderFittedData();
				renderCharts();
			}
		});
		
		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
		series1.setName(theData.getName());
		
		

		ArrayList<Data<Number, Number>> residual = new ArrayList<Data<Number, Number>>();
		//ArrayList<Data<Number, Number>> fittedData = new ArrayList<Data<Number, Number>>();
		
		int totalDataPoints = theData.origX.size();
		for(int i = 0; i < theData.origX.size(); i++){
			//if(!theData.omittedIndices.contains(i))
			residual.add(new Data<Number, Number>(theData.origX.get(i), theData.origY.get(i) - theData.fittedY.get(i)));
			i += totalDataPoints / DataPointsToShow;// == 0 ? 1 : totalDataPoints / DataPointsToShow;
		}
		series1.getData().addAll(residual);
		chart.getData().add(series1);
		chart.setCreateSymbols(true);
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getBeginFit()), 0));
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getEndFit()), 0));
		return chart;
	}
	
	private void updateFitChart() {
		FitableDataset theData = datasetsListView.getSelectionModel().getSelectedItem();
		if(theData == null)
			return;
		XYChart.Series<Number, Number> rawDataSeries = new XYChart.Series<Number, Number>();
		rawDataSeries.setName(theData.getName());
		
		XYChart.Series<Number, Number> fittedDataSeries = new XYChart.Series<Number, Number>();
		fittedDataSeries.setName("Fitted Line");

		ArrayList<Data<Number, Number>> rawDataPoints = new ArrayList<Data<Number, Number>>();
		ArrayList<Data<Number, Number>> fittedDataPoints = new ArrayList<Data<Number, Number>>();
		
		int totalDataPoints = theData.origX.size();
		for(int i = 0; i < theData.origX.size(); i++){
			rawDataPoints.add(new Data<Number, Number>(theData.origX.get(i), theData.origY.get(i)));
			fittedDataPoints.add(new Data<Number, Number>(theData.fittedX.get(i), theData.fittedY.get(i)));
			
			i += totalDataPoints / DataPointsToShow;// == 0 ? 1 : totalDataPoints / DataPointsToShow;
			
		}
		rawDataSeries.getData().addAll(rawDataPoints);
		fittedDataSeries.getData().addAll(fittedDataPoints);
		fitChart.setCreateSymbols(true);
		fitChart.getData().clear();
		fitChart.getData().add(rawDataSeries);
		fitChart.getData().add(fittedDataSeries);
		
		fitChart.clearVerticalMarkers();
		fitChart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getBeginFit()), 0));
		fitChart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getEndFit()), 0));
	}
	
	private FitableDataset getCurrentDataset() {
		return datasetsListView.getSelectionModel().getSelectedItem();
	}

	public void createWidget() {
		if(showLoadFileButton){
			Button loadFileButton = new Button("Load File");
			loadFileButton.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					Stage primaryStage = new Stage();
					try {
						new Home(primaryStage, dataFileLoaderBucket);
						primaryStage.showAndWait();
						datasetsListView.getItems().clear();
						List<Double> time = IntStream.range(0, dataFileLoaderBucket.get(0).size()).boxed().map(i -> new Double(i)).collect(Collectors.toList());
						for(int i = 0; i < dataFileLoaderBucket.size(); i++)
						{
							List<Double> y = dataFileLoaderBucket.get(i).stream().map(v -> Double.parseDouble(v)).collect(Collectors.toList());
							datasetsListView.getItems().add(new FitableDataset(time, y, Integer.toString(i)));
						}
						datasetsListView.getSelectionModel().select(0);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
				
			});
			leftVBox.getChildren().add(0, loadFileButton);
		}
	}
	
	private void setChartBounds() {
		fitChartXAxis.setLowerBound(Math.min(beginRectangle.getX(), endRectangle.getX()));
		fitChartXAxis.setUpperBound(Math.max(beginRectangle.getX(), endRectangle.getX()));
		
		
		fitChartYAxis.setLowerBound(Math.min(beginRectangle.getY(), endRectangle.getY()));
		fitChartYAxis.setUpperBound(Math.max(beginRectangle.getY(), endRectangle.getY()));
		
		fitChartXAxis.setAutoRanging(false);
		fitChartYAxis.setAutoRanging(false);
		
		residualChartXAxis.setLowerBound(Math.min(beginRectangle.getX(), endRectangle.getX()));
		residualChartXAxis.setUpperBound(Math.max(beginRectangle.getX(), endRectangle.getX()));
		
		residualChartXAxis.setAutoRanging(false);
		
		DrawnRectangle.setWidth(0);
		DrawnRectangle.setHeight(0);
	}
	
	private Rectangle getRectangleFromPoints(Point2D p1, Point2D p2) {
		double beginX = Math.min(p1.getX(), p2.getX());
		double beginY = Math.min(p1.getY(), p2.getY());
		double width = Math.abs(p1.getX() - p2.getX());
		double height = Math.abs(p1.getY() - p2.getY());
		return new Rectangle(beginX, beginY, width, height);		
	}

	@FXML
	public void doneButtonClicked() {
		stage.close();
	}
}
