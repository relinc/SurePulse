package net.relinc.viewer.GUI;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.sample.HopkinsonBarSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.viewer.application.ScaledResults;


@SuppressWarnings("restriction") //For the chart legend warnings. Uses deprecated code in javafx library...
public class ChartsGUI extends CommonGUI{
	private HomeController homeController;
	
	public ChartsGUI(HomeController hc)
	{
		homeController = hc;
	}
	
	public LineChartWithMarkers<Number, Number> getStressTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Stress";
		String xUnits = "(" + timeUnits.getString() + "s)";

		String yUnits = "(ksi)";


		if(!isEngineering.get())
			yLabel = "True Stress";
		if(!isEnglish.get())
			yUnits = "(MPa)";

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRESS);
		chart.setCreateSymbols(false);
		chart.setTitle("Stress Vs Time");

		if(homeController.zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}


		for(Sample s : getCheckedSamples()){
			System.out.println(s.getResults().size());
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++)
			{
				ScaledResults scaledResults = new ScaledResults(s, resultIdx);
				double[] load = scaledResults.getLoad();
				double[] time = scaledResults.getTime();

				if(load == null) //failed to find the stress data
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName() + String.valueOf(resultIdx));

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = load.length;

				for(int i = 0; i < load.length; i++){
					dataPoints.add(new Data<Number, Number>(time[i], load[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}

		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStrainTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Strain";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/in)";

		if(!isEngineering.get()){
			yLabel = "True Strain";
		}
		if(!isEnglish.get()){
			yUnits = "(mm/mm)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAIN);
		chart.setCreateSymbols(false);
		chart.setTitle("Strain Vs Time");

		if(homeController.zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] strain = results.getDisplacement();
				double[] time = results.getTime();

				if (strain == null)
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = strain.length;

				for (int i = 0; i < strain.length; i++) {
					dataPoints.add(new Data<Number, Number>(time[i], strain[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStrainRateTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Strain Rate";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/in/s)";

		if(!isEngineering.get()){
			yLabel = "True Strain Rate";
		}
		if(!isEnglish.get()){
			yUnits = "(mm/mm/s)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAINRATE);

		chart.setCreateSymbols(false);

		chart.setTitle("Strain Rate Vs Time");

		if(homeController.zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}



		 double maxPlottedVal = Double.MIN_VALUE;

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] time = results.getTime();
				double[] strain = results.getDisplacement();

				if (strain == null)
					continue;

				double[] strainRate = null;
				try {
					strainRate = SPOperations.getDerivative(s.getResults().get(resultIdx).time, strain);
				} catch (Exception e) {
					e.printStackTrace();
				}


				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = strain.length;

				for (int i = 0; i < strain.length; i++) {
					if (strainRate[i] > maxPlottedVal)
						maxPlottedVal = strainRate[i];
					dataPoints.add(new Data<Number, Number>(time[i], strainRate[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);

				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStressStrainChart() {
		// This will not go 
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Engineering Strain";
		String yLabel = "Engineering Stress";
		String xUnits = "(in/in)";
		String yUnits = "(ksi)";


		if(!isEngineering.get()){
			xlabel = "True Strain";
			yLabel = "True Stress";
		}
		if(!isEnglish.get()){
			xUnits = "(mm/mm)";
			yUnits = "(MPa)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.STRAIN, chartDataType.STRESS);
		chart.setCreateSymbols(false);
		chart.setTitle("Stress Vs Strain");

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] load = results.getLoad();
				double[] displacement = results.getDisplacement();

				if (load == null || displacement == null) //failed to find the stress data
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = load.length;

				for (int i = 0; i < load.length; i++) {
					dataPoints.add(new Data<Number, Number>(displacement[i], load[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);

				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getLoadTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Load";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(Lbf)";

		if(!isEnglish.get()){
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Load Vs Time");

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] load = results.getLoad();
				double[] time = results.getTime();

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = load.length;

				for (int i = 0; i < load.length; i++) {
					dataPoints.add(new Data<Number, Number>(time[i], load[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getDisplacementTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Displacement";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in)";

		if(!isEnglish.get()){
			yUnits = "(mm)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENT);
		chart.setCreateSymbols(false);
		chart.setTitle("Displacement Vs Time");



		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] displacement = results.getDisplacement();
				double[] time = results.getTime();
				if (displacement == null)
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = displacement.length;

				for (int i = 0; i < displacement.length; i++) {
					dataPoints.add(new Data<Number, Number>(time[i], displacement[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getDisplacementRateTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Displacement Rate";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/s)";

		if(!isEnglish.get()){
			yUnits = "(mm/s)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<Number, Number>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENTRATE);
		
		chart.setCreateSymbols(false);

		chart.setTitle("Displacement Rate Vs Time");

		double maxPlottedVal = Double.MIN_VALUE;
		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] displacement = results.getDisplacement();
				double[] time = results.getTime();

				if (displacement == null)
					continue;

				double[] strainRate = null;
				try {
					strainRate = SPOperations.getDerivative(s.getResults().get(resultIdx).time, displacement);
				} catch (Exception e) {
					e.printStackTrace();
				}


				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = displacement.length;

				for (int i = 0; i < displacement.length; i++) {
					if (strainRate[i] > maxPlottedVal)
						maxPlottedVal = strainRate[i];
					dataPoints.add(new Data<Number, Number>(time[i], strainRate[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);

				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getLoadDisplacementChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Displacement";
		String yLabel = "Load";
		String xUnits = "(in)";
		String yUnits = "(Lbf)";

		if (!isEnglish.get()) {
			xUnits = "(mm)";
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.DISPLACEMENT, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Load Vs Displacement");

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] load = results.getLoad();
				double[] displacement = results.getDisplacement();

				if (load == null || displacement == null) //failed to find the stress data
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = load.length;

				for (int i = 0; i < load.length; i++) {
					dataPoints.add(new Data<Number, Number>(displacement[i], load[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getFaceForceTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Force";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(Lbf)";


		if(!isEnglish.get()){
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Face Force Vs Time");

		if(homeController.zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample) s; // Only hbar samples are checked if face force is graphable.

				ScaledResults results = new ScaledResults(hopkinsonBarSample, resultIdx);

				double[] frontFaceForce = results.getFrontFaceForce();
				double[] backFaceForce = results.getBackFaceForce();
				double[] time = results.getTime();

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName() + " Front Face Force");
				XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
				series2.setName(s.getName() + " Back Face Force");

				ArrayList<Data<Number, Number>> frontFaceForceDatapoints = new ArrayList<Data<Number, Number>>();
				ArrayList<Data<Number, Number>> backFaceForceDatapoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = frontFaceForce.length;
				for (int i = 0; i < frontFaceForce.length; i++) {
					frontFaceForceDatapoints.add(new Data<Number, Number>(time[i], frontFaceForce[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(frontFaceForceDatapoints);

				totalDataPoints = backFaceForce.length;
				for (int i = 0; i < backFaceForce.length; i++) {
					backFaceForceDatapoints.add(new Data<Number, Number>(time[i], backFaceForce[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series2.getData().addAll(backFaceForceDatapoints);

				chart.getData().add(series1);
				chart.getData().add(series2);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColor(s, resultIdx, s.getResults().size()));
				setSeriesColor(series2, getColor(s, resultIdx, s.getResults().size(), true)); //makes it a bit darker
			}
		}

		createChartLegend(chart, true);

		return chart;
	}

	private String getColor(Sample s, int resultIdx, int resultLength, boolean darker) {
		Color color = seriesColors.get(getSampleIndex(s) % seriesColors.size());
		if(darker)
			color = color.darker();
		int tint = 100 * (resultIdx) / resultLength;
		String rgb = String.format("%d, %d, %d",
				(int) (color.getRed() * 255 - tint),
				(int) (color.getGreen() * 255- tint),
				(int) (color.getBlue() * 255- tint));
		return rgb;
	}

	private String getColor(Sample s, int resultIdx, int resultLength) {
		return getColor(s, resultIdx, resultLength, false);
	}
	
	private void setSeriesColor(Series<Number, Number> series, String rgbString){
		series.nodeProperty().get().setStyle("-fx-stroke: rgba(" + rgbString + ", 1.0);");
	}

	private void createChartLegend(LineChartWithMarkers<Number, Number> chart, boolean addTintedLegends) {
		ArrayList<LegendItem> items = new ArrayList<>();
		for(Sample s : getCheckedSamples()){

			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++)
			{
				if(addTintedLegends){
					Color c = Color.web(String.format("rgb(%s)", getColor(s, resultIdx, s.getResults().size())));
					items.add(new Legend.LegendItem(s.getName() + " Front Face", new javafx.scene.shape.Rectangle(10,4, c)));
					c = Color.web(String.format("rgb(%s)", getColor(s, resultIdx, s.getResults().size(), true)));
					items.add(new Legend.LegendItem(s.getName() + " Back Face", new javafx.scene.shape.Rectangle(10,4, c)));
				}
				else{
					Color c = Color.web(String.format("rgb(%s)", getColor(s, resultIdx, s.getResults().size())));
					items.add(new Legend.LegendItem(s.getName() + s.getResults().get(resultIdx).getChartLegendPostFix(), new javafx.scene.shape.Rectangle(10,4, c)));
				}
			}

		}
		
		Legend legend = (Legend)chart.lookup(".chart-legend");
		legend.getItems().setAll(items);
	}

}
