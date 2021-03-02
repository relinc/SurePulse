package net.relinc.viewer.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sun.javafx.charts.Legend;
import com.sun.javafx.charts.Legend.LegendItem;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.paint.Color;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.referencesample.ReferenceSample;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.referencesample.StressUnit;
import net.relinc.libraries.sample.HopkinsonBarSample;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.viewer.application.ScaledResults;


@SuppressWarnings("restriction") //For the xyChart legend warnings. Uses deprecated code in javafx library...
public class ChartsGUI extends CommonGUI{
	private HomeController homeController;

	public ChartsGUI(HomeController hc)
	{
		homeController = hc;
	}

	public List<Series<Number, Number>> getStressStrainSerie(Optional<LineChart> chartOptional) {
		List<Series<Number, Number>> res = new ArrayList<>();
		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				final int resultIdxFinal = resultIdx;
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] load = results.getLoad();
				double[] displacement = results.getDisplacement();

				if (load == null || displacement == null) //failed to find the rawStressData data
					continue;

				XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
				series1.setName(s.getName());

				ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

				int totalDataPoints = load.length;

				for (int i = 0; i < load.length; i++) {
					dataPoints.add(new Data(displacement[i], load[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(dataPoints);

				res.add(series1);

				chartOptional.ifPresent(chart -> {
					chart.getData().add(series1);
					series1.nodeProperty().get().setMouseTransparent(true);
					setSeriesColor(series1, getColorAsString(getColor(s, resultIdxFinal, s.getResults().size())));
				});


			}
		}
		return res;
	}

	public List<Series<Number, Number>> getReferenceSampleStressStrainSerie(Optional<LineChart> chartOptional) {
		List<Series<Number, Number>> res = new ArrayList<>();
		for(ReferenceSample s : getCheckedReferenceSamples()) {
			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			List<Double> strainData = s.getStrain(isEngineering.get() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE);
			List<Double> stressData = s.getStress(isEngineering.get() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE, isEnglish.get() ? StressUnit.KSI : StressUnit.MPA);

			for(int i = 0; i < strainData.size(); i++) {
				series1.getData().add(new Data<Number, Number>(strainData.get(i), stressData.get(i)));
			}
			res.add(series1);

			chartOptional.ifPresent(chart -> {
				chart.getData().add(series1);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColorAsString(getColor(realCurrentSamplesListView.getItems().size() + getReferenceSampleIndex(s), 0, 1, false)));
			});

		}
		return res;
	}
	
	public LineChartWithMarkers<Number, Number> getStressTimeChart() {
		ChartingPreferences preference = homeController.preferences.stressTimePreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRESS);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

		if(homeController.zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}


		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++)
			{
				ScaledResults scaledResults = new ScaledResults(s, resultIdx);
				double[] load = scaledResults.getLoad();
				double[] time = scaledResults.getTime();

				if(load == null) //failed to find the rawStressData data
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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}

		}

		createChartLegend(chart, false);
		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStrainTimeChart() {
		ChartingPreferences preference = homeController.preferences.strainTimePrefrences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAIN);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStrainRateTimeChart() {
		ChartingPreferences preference = homeController.preferences.strainRateTimePrefrences;
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

//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAINRATE);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getStressStrainChart() {
		ChartingPreferences preference = homeController.preferences.stressStrainPreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.STRAIN, chartDataType.STRESS);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

		getStressStrainSerie(Optional.of(chart));

		getReferenceSampleStressStrainSerie(Optional.of(chart));

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getLoadTimeChart() {
		ChartingPreferences preference = homeController.preferences.loadTimePreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.LOAD);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getDisplacementTimeChart() {
		ChartingPreferences preference = homeController.preferences.displacementTimePreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENT);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getDisplacementRateTimeChart() {
		ChartingPreferences preference = homeController.preferences.displacementRateTimePreferences;
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

//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<Number, Number>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENTRATE);
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);

		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getLoadDisplacementChart() {
		ChartingPreferences preference = homeController.preferences.loadDisplacementPreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.DISPLACEMENT, chartDataType.LOAD);
		chart.setTitle("Load Vs Displacement");
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

		for(Sample s : getCheckedSamples()){
			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++) {
				ScaledResults results = new ScaledResults(s, resultIdx);
				double[] load = results.getLoad();
				double[] displacement = results.getDisplacement();

				if (load == null || displacement == null) //failed to find the rawStressData data
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
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
			}
		}

		createChartLegend(chart, false);



		return chart;
	}
	
	public LineChartWithMarkers<Number, Number> getFaceForceTimeChart() {
		ChartingPreferences preference = homeController.preferences.faceForceTimePreferences;
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
//
		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.FACEFORCE);
		chart.setTitle("Face Force vs Time");
		chart.applyPreferences(preference);
		chart.setCreateSymbols(false);

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

				int totalDataPoints = Math.min(frontFaceForce.length, time.length);
				for (int i = 0; i < totalDataPoints; i++) {
					frontFaceForceDatapoints.add(new Data<Number, Number>(time[i], frontFaceForce[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series1.getData().addAll(frontFaceForceDatapoints);

				totalDataPoints = Math.min(backFaceForce.length, time.length);
				for (int i = 0; i < totalDataPoints; i++) {
					backFaceForceDatapoints.add(new Data<Number, Number>(time[i], backFaceForce[i]));
					i += totalDataPoints / DataPointsToShow;
				}
				series2.getData().addAll(backFaceForceDatapoints);

				chart.getData().add(series1);
				chart.getData().add(series2);
				series1.nodeProperty().get().setMouseTransparent(true);
				setSeriesColor(series1, getColorAsString(getColor(s, resultIdx, s.getResults().size())));
				setSeriesColor(series2, getColorAsString(getColor(s, resultIdx, s.getResults().size(), true))); //makes it a bit darker
			}
		}

		createChartLegend(chart, true);

		return chart;
	}

	public static Color getColor(int sampleIndex, int resultIdx, int resultLength, boolean darker) {
		return getColor(sampleIndex, resultIdx, resultLength, darker, 1);
	}

	public static Color getColor(int sampleIndex, int resultIdx, int resultLength, boolean darker, double opacity) {
		Color color = seriesColors.get(sampleIndex % seriesColors.size());
		if(darker)
			color = color.darker();
		int tint = 100 * (resultIdx) / resultLength;

		double r = color.getRed() * 255 - tint;
		double g = color.getGreen() * 255 - tint;
		double b = color.getBlue() * 255 - tint;
		return new Color(r / 255., g / 255., b / 255., opacity);
	}

	private Color getColor(Sample s, int resultIdx, int resultLength, boolean darker) {
		Sample selectedTrimSample = this.homeController.trimSampleComboBox.getSelectionModel().getSelectedItem();
		if(getSampleIndex(selectedTrimSample) != -1) {
			if(selectedTrimSample == s) {
				return Color.BLACK;
			} else {
				return getColor(getSampleIndex(s), resultIdx, resultLength, darker, .5);
			}
		} else {
			Optional<SampleGroup> g = homeController.getCheckedSampleGroups().stream().filter(group -> group.groupSamples.contains(s)).findFirst();
			if(g.isPresent()) {
				return g.get().color;
			}
			return getColor(getSampleIndex(s), resultIdx, resultLength, darker);
		}
	}

	public static String getColorAsString(Color color) {
		String r = Integer.toHexString((int) Math.round(color.getRed()*255));
		String g = Integer.toHexString((int) Math.round(color.getGreen()*255));
		String b = Integer.toHexString((int) Math.round(color.getBlue()*255));
		return "#" + r + g + b;
	}

	private Color getColor(Sample s, int resultIdx, int resultLength) {
		return getColor(s, resultIdx, resultLength, false);
	}
	
	private void setSeriesColor(Series<Number, Number> series, String hexString){
		series.nodeProperty().get().setStyle("-fx-stroke: " + hexString + ";");
	}

	private void createChartLegend(LineChartWithMarkers<Number, Number> chart, boolean addTintedLegends) {
		ArrayList<LegendItem> items = new ArrayList<>();

		ArrayList<SampleGroup> doneGroups = new ArrayList<>();

		for(Sample s : getCheckedSamples()){

			for(int resultIdx = 0; resultIdx < s.getResults().size(); resultIdx++)
			{
				Optional<SampleGroup> group = homeController.getCheckedSampleGroups().stream().filter(item -> item.groupSamples.contains(s)).findFirst();

				if(group.isPresent()) {
					if(doneGroups.contains(group.get()))
						continue;
				}

				if(addTintedLegends){
					Color c = getColor(s, resultIdx, s.getResults().size());
					items.add(new Legend.LegendItem((group.isPresent() ? group.get().groupName : s.getName()) + " Front Face", new javafx.scene.shape.Rectangle(10,4, c)));
					c = getColor(s, resultIdx, s.getResults().size(), true);
					items.add(new Legend.LegendItem((group.isPresent() ? group.get().groupName : s.getName()) + " Back Face", new javafx.scene.shape.Rectangle(10,4, c)));
				}
				else{
					String title = (group.isPresent() ? group.get().groupName : s.getName()) + (s.getResults().size() > 1 ? s.getResults().get(resultIdx).getChartLegendPostFix() : "");
					Color c = getColor(s, resultIdx, s.getResults().size());

					items.add(new Legend.LegendItem(title, new javafx.scene.shape.Rectangle(10,4, c)));
				}
				group.ifPresent(g -> doneGroups.add(g));
			}
		}

		for(int i = 0; i < getCheckedReferenceSamples().size(); i++) {
			ReferenceSample s = getCheckedReferenceSamples().get(i);
			String title = s.getName();
			Color c = getColor(realCurrentSamplesListView.getItems().size() + getReferenceSampleIndex(s), 0, 1, false);

			items.add(new Legend.LegendItem(title, new javafx.scene.shape.Rectangle(10,4, c)));
		}
		
		Legend legend = (Legend)chart.lookup(".chart-legend");
		if(legend != null) {
			legend.getItems().setAll(items);

		}
	}

}
