package net.relinc.fitter.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.relinc.datafileparser.application.Home;
import net.relinc.fitter.application.LineChartWithMarkers;
import net.relinc.fitter.staticClasses.SPMath;
import net.relinc.libraries.application.FitableDataset;

public class HomeController {
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
	@FXML CheckBox smoothAllPointsCB;
	int DataPointsToShow = 2000;
	ToggleGroup beginEndGroup = new ToggleGroup();
	public boolean showLoadFileButton;
	public List<List<String>> dataFileLoaderBucket = new ArrayList<List<String>>();

	public void initialize(){
		setBeginRadioButton.setToggleGroup(beginEndGroup);
		setEndRadioButton.setToggleGroup(beginEndGroup);
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
				FitableDataset set = getCurrentDataset();
				if(set == null)
					return;
				set.setPointsToRemove(num);
				set.renderFittedData();
				renderCharts();
				pointsToRemoveLabel.setText("Points Removed: " + num);
			}
		});
		

		
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
	
	public void renderCharts() {
		chartVBox.getChildren().clear();
		
		FitableDataset theData = datasetsListView.getSelectionModel().getSelectedItem();
		if(theData == null)
			return;
		updateLabels();
		LineChartWithMarkers<Number, Number> fitChart = getFitChart(theData);
		LineChartWithMarkers<Number, Number> residualChart = getResidualChart(theData);
		
		fitChart.getStylesheets().add(getClass().getResource("mixedChart.css").toExternalForm());
		
		chartVBox.getChildren().add(fitChart);
		chartVBox.getChildren().add(residualChart);
		VBox.setVgrow(fitChart, Priority.ALWAYS);
		VBox.setVgrow(residualChart, Priority.ALWAYS);
	}
	private LineChartWithMarkers<Number, Number> getResidualChart(FitableDataset theData) {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "X";
		String yLabel = "Residual";

		XAxis.setLabel(xlabel);
		YAxis.setLabel(yLabel);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
	private LineChartWithMarkers<Number, Number> getFitChart(FitableDataset theData) {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();
		YAxis.setForceZeroInRange(false);

		String xlabel = "X";
		String yLabel = "Y";

		XAxis.setLabel(xlabel);
		YAxis.setLabel(yLabel);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
		chart.setCreateSymbols(false);
		chart.setTitle("Data and Fitted Line");
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
		chart.setCreateSymbols(true);
		chart.getData().add(rawDataSeries);
		chart.getData().add(fittedDataSeries);
		
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getBeginFit()), 0));
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getEndFit()), 0));
		return chart;

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
}
