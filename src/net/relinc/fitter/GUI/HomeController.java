package net.relinc.fitter.GUI;

import java.util.ArrayList;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import net.relinc.fitter.application.FitableDataset;
import net.relinc.fitter.application.LineChartWithMarkers;

public class HomeController {
	@FXML ListView<FitableDataset> datasetsListView;
	@FXML VBox chartVBox;
	@FXML Label datasetNameLabel;
	@FXML Label pointsToRemoveLabel;
	@FXML Label orderOfPolynomialFitLabel;
	@FXML ScrollBar polynomialOrderScrollBar;
	@FXML ScrollBar pointsToRemoveScrollBar;
	@FXML RadioButton setBeginRadioButton;
	@FXML RadioButton setEndRadioButton;
	int DataPointsToShow = 2000;
	ToggleGroup beginEndGroup = new ToggleGroup();

	public void initialize(){
		setBeginRadioButton.setToggleGroup(beginEndGroup);
		setEndRadioButton.setToggleGroup(beginEndGroup);
		datasetsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<FitableDataset>() {
			@Override
			public void changed(ObservableValue<? extends FitableDataset> observable, FitableDataset oldValue,
					FitableDataset newValue) {
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
				pointsToRemoveLabel.setText("Points To Be Removed: " + num);
			}
		});
		
	}
	
	
	public void renderGUI() {
		ArrayList<Double> testX = new ArrayList<Double>();
		testX.add(new Double(1));
		testX.add(new Double(2));
		testX.add(new Double(3));
		testX.add(new Double(4));
		testX.add(new Double(5));
		testX.add(new Double(6));
		testX.add(new Double(7));
		testX.add(new Double(8));
		testX.add(new Double(9));
		testX.add(new Double(10));
		ArrayList<Double> testY = new ArrayList<Double>();
		testY.add(new Double(1));
		testY.add(new Double(1));
		testY.add(new Double(1));
		testY.add(new Double(1));
		testY.add(new Double(2));
		testY.add(new Double(3));
		testY.add(new Double(4));
		testY.add(new Double(20));
		testY.add(new Double(5));
		testY.add(new Double(6));
		
		FitableDataset test = new FitableDataset(testX, testY, "Testing");
		datasetsListView.getItems().add(test);
		
		renderCharts();
	}
	private void renderCharts() {
		chartVBox.getChildren().clear();
		
		
		FitableDataset theData = datasetsListView.getSelectionModel().getSelectedItem();
		if(theData == null)
			return;
		
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
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				System.out.println("Chart clicked");
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
			if(!theData.omittedIndices.contains(i))
				residual.add(new Data<Number, Number>(theData.origX.get(i), theData.origY.get(i) - theData.fittedY.get(i)));
			//i += totalDataPoints / DataPointsToShow + 1;
		}
		series1.getData().addAll(residual);
		chart.getData().addAll(series1);
		chart.setCreateSymbols(true);
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getBeginFit()), 0));
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getEndFit()), 0));
		return chart;
	}
	private LineChartWithMarkers<Number, Number> getFitChart(FitableDataset theData) {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "X";
		String yLabel = "Y";

		XAxis.setLabel(xlabel);
		YAxis.setLabel(yLabel);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
		chart.setCreateSymbols(false);
		chart.setTitle("X Vs Y");
		
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				System.out.println("Chart clicked");
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
			
			//i += totalDataPoints / DataPointsToShow + 1;
			
		}
		rawDataSeries.getData().addAll(rawDataPoints);
		fittedDataSeries.getData().addAll(fittedDataPoints);
		chart.setCreateSymbols(true);
		chart.getData().addAll(rawDataSeries);
		chart.getData().addAll(fittedDataSeries);
		
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getBeginFit()), 0));
		chart.addVerticalValueMarker(new Data<Number, Number>(getCurrentDataset().origX.get(getCurrentDataset().getEndFit()), 0));
		//chart.getData().addAll(series1);
		return chart;

	}
	
	private FitableDataset getCurrentDataset() {
		return datasetsListView.getSelectionModel().getSelectedItem();
	}
	
}
