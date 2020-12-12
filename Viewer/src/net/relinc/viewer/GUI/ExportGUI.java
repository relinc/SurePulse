package net.relinc.viewer.GUI;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import net.relinc.libraries.data.ModifierFolder.Resampler;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.viewer.application.ScaledResults;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

public class ExportGUI extends CommonGUI {
	private HomeController homeController;
	
	public ExportGUI(HomeController hc)
	{
		homeController = hc;
		init();
	}
	
	private void init()
	{
		buttonAddSampleToGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				addSampleToGroupButtonFired();
			}
		});

		buttonDeleteSelectedGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				int selected = treeViewSampleGroups.getSelectionModel().getSelectedIndex();
				if(selected > 0) {
					sampleGroupRoot.getChildren().remove(selected - 1);
					sampleGroupsForExport.remove(selected - 1);
				}
			}
		});

		buttonDeleteSelectedSample.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				TreeItem selected = treeViewSampleGroups.getSelectionModel().getSelectedItem();
				TreeItem<String> parent = null;
				TreeItem<String> child = null;
				for(TreeItem<String> i1 : sampleGroupRoot.getChildren()) {
					for(TreeItem i2: i1.getChildren()) {
						if(i2 == selected) {
							parent = i1;
							child = i2;
						}
					}
				}
				if(parent != null && child != null) {
					final TreeItem<String> parentFinal = parent;
					final TreeItem<String> childFinal = child;

					Optional<SampleGroup> g = sampleGroupsForExport.stream().filter(v -> v.groupName.equals(parentFinal.getValue())).findAny();
					g.ifPresent(v -> {
						Optional<Sample> s = v.groupSamples.stream().filter(v2 -> v2.getName().equals(childFinal.getValue())).findAny();
						s.ifPresent(s1 -> v.groupSamples.remove(s1));
					});
					treeViewSampleGroups.getSelectionModel().clearSelection();
				}
				refreshSampleGroupTreeView();
			}
		});

		buttonCreateSampleGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				addGroupButtonClicked();
			}
		});

		buttonExportData.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				try {
					onExportDataButtonClicked();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		buttonExportCSV.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				exportCSVButtonFired();
			}
		});

		treeViewSampleGroups.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<TreeItem<String>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable,
					TreeItem<String> old_val, TreeItem<String> new_val) {
				onTreeViewItemClicked();
			}
		});
		
		sampleGroupRoot = new TreeItem<>("Sample Groups");
		treeViewSampleGroups.setRoot(sampleGroupRoot);
		
		
	}
	
	public void addSampleToGroupButtonFired() {
		if(currentSelectedSampleGroup != null) {
			for(Sample s : getCheckedSamples()) {
				if(currentSelectedSampleGroup.groupSamples.indexOf(s) < 0) {
					currentSelectedSampleGroup.groupSamples.add(s);
				}
			}
		} else {
			Dialogs.showInformationDialog("Error Adding Sample to Group", null, "Please select a group!",stage);
		}
		refreshSampleGroupTreeView();
	}
	
	public void addGroupButtonClicked() {

		if(tbSampleGroup.getText().trim().equals("")) {
			Dialogs.showInformationDialog("Error Creating Group", null, "Group Must Have a Name",stage);
			return;
		}

		if(!SPOperations.specialCharactersAreNotInTextField(tbSampleGroup)) {
			Dialogs.showInformationDialog("Add Sample Group","Invalid Character In Group Name", "Only a-z, A-Z, 0-9, dash, space, and parenthesis are allowed",stage);
			return;
		}
		
		if(tbSampleGroup.getText().contains("-")){
			Dialogs.showAlert("Dashes (-) are not allowed in group names", stage);
			return;
		}

		if(findStringInSampleGroups(tbSampleGroup.getText()) > -1) {
			Dialogs.showInformationDialog("Error Creating Group", null, "Group Name Already Exists!",stage);
			return;
		}

		SampleGroup sampleGroup = new SampleGroup(tbSampleGroup.getText());
		sampleGroupsForExport.add(sampleGroup);
		sampleGroupRoot.setExpanded(true);
		refreshSampleGroupTreeView();
	}
	
	private void refreshSampleGroupTreeView() {
		sampleGroupRoot.getChildren().clear();
		for(SampleGroup sampleGroup : sampleGroupsForExport) {
			TreeItem<String> treeItemSampleGroup = new TreeItem<>(sampleGroup.groupName);
			sampleGroupRoot.getChildren().add(treeItemSampleGroup);
			for(Sample sample : sampleGroup.groupSamples) {
				TreeItem<String> treeItemSample = new TreeItem<>(sample.getName());
				treeItemSampleGroup.getChildren().add(treeItemSample);
			}
		}

	}
	
	private int findStringInSampleGroups(String find) {

		if(sampleGroupsForExport == null || sampleGroupsForExport.size() == 0)
			return -1;

		int i = 0;

		for(SampleGroup group : sampleGroupsForExport) {
			if (group.groupName.equals(find)) {
				return i;
			}
			i++;
		}

		return -1;
	}
	
	public void onTreeViewItemClicked() {
		TreeItem<String> selectedSampleGroup = treeViewSampleGroups.getSelectionModel().getSelectedItem();
		//github
		if(selectedSampleGroup == sampleGroupRoot) {
			currentSelectedSampleGroup = null;
			return;
		}

		if(selectedSampleGroup != null && findStringInSampleGroups(selectedSampleGroup.getValue()) != -1) {
			currentSelectedSampleGroup = sampleGroupsForExport.get(findStringInSampleGroups(selectedSampleGroup.getValue()));
			return;
		}

		if(currentSelectedSampleGroup == null) {
			currentSelectedSampleGroup = new SampleGroup(selectedSampleGroup.getValue());
		}
	}
	
	public void onExportDataButtonClicked() throws Exception {

		if(sampleGroupsForExport == null || sampleGroupsForExport.size() == 0) {

			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroupsForExport) {
			if(group.groupSamples == null || group.groupSamples.size() == 0)
				noData = true;
			else {
				noData = false;
				break;
			}
		}
		if(noData) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add at least one sample to a group",stage);
			return;
		}
		
		Optional<Integer> pointsToKeep = getPointsToKeepForExcelFileFromUser();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Export Location");
		ExtensionFilter extensionFilter = new ExtensionFilter("Microsoft Excel Worksheet (*.xlsx)","*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		File file = fileChooser.showSaveDialog(stage);

		if (file != null) {
			File jobFile = writeConsoleExcelFileMakerJobFile(file.getPath(), pointsToKeep);

			if(jobFile.exists()) {
				if(SPOperations.writeExcelFileUsingEpPlus(jobFile.getPath())) {
					Dialogs.showInformationDialog("Excel Export", "Job File Created", "EPPlus is creating an excel file at "+file.getAbsolutePath(), stage);
				} else {
					Dialogs.showErrorDialog("Excel Export", "Excel Export Failed", "There was an error exporting your excel file, this usually means the installation of SURE-Pulse Viewer is broken or some files have been removed", stage);
				}
			}
		}
	}
	
	private Optional<Integer> getPointsToKeepForExcelFileFromUser() {
		Stage anotherStage = new Stage();
		Label promptLabel = new Label("If you'd like to reduce the data quantity,\nplease enter the number of points you'd "
				+ "like to keep below.");
		CheckBox reduceDataCheckBox = new CheckBox("Reduce Data Quantity");
		
		Label pointsToKeepLabel = new Label("Points To Keep:");
		NumberTextField userInputTF = new NumberTextField("", "");
		userInputTF.disableProperty().bind(reduceDataCheckBox.selectedProperty().not());
		pointsToKeepLabel.disableProperty().bind(reduceDataCheckBox.selectedProperty().not());
		
		
		Button doneButton = new Button("Done");
		doneButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				anotherStage.close();
			}
		});
		
		AnchorPane anchor = new AnchorPane();
		VBox topVbox = new VBox();
		topVbox.getChildren().add(promptLabel);
		topVbox.getChildren().add(reduceDataCheckBox);
		HBox inputHBox = new HBox();
		inputHBox.getChildren().add(pointsToKeepLabel);
		inputHBox.getChildren().add(userInputTF);
		inputHBox.setAlignment(Pos.CENTER);
		inputHBox.setSpacing(5);
		topVbox.getChildren().add(inputHBox);
		topVbox.getChildren().add(doneButton);
		topVbox.setAlignment(Pos.CENTER);
		topVbox.setSpacing(15);
		AnchorPane.setBottomAnchor(topVbox, 0.0);
		AnchorPane.setLeftAnchor(topVbox, 0.0);
		AnchorPane.setRightAnchor(topVbox, 0.0);
		AnchorPane.setTopAnchor(topVbox, 0.0);
		
		anchor.getChildren().add(topVbox);
		
		Scene scene = new Scene(anchor, 400, 220);
		anotherStage.setScene(scene);
		anotherStage.initModality(Modality.WINDOW_MODAL);
		
		anotherStage.showAndWait();

		Optional<Integer> retVal = Optional.empty();
		if(reduceDataCheckBox.isSelected()) {
			retVal = Optional.of(userInputTF.getDouble().intValue());
		}

		return retVal;
	}

	public void exportCSVButtonFired() {
		if(sampleGroupsForExport == null || sampleGroupsForExport.size() == 0) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroupsForExport) {
			if(group.groupSamples == null || group.groupSamples.size() == 0)
				noData = true;
			else {
				noData = false;
				break;
			}
		}
		if(noData) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add at least one sample to a group",stage);
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Export Location");
		File file = fileChooser.showSaveDialog(stage);

		if (file != null) {
			file.mkdir();
			writeCSVFile(file);
		}
	}
	
	private void writeCSVFile(File file){
		//file is a directory to store all the csvs. A csv for each group.

		String timeUnit = homeController.getDisplayedTimeUnit();
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		String faceForceUnit = getDisplayedFaceForceUnit();

		String timeName = "Time";//always
		String stressName = isLoadDisplacement.get() ? "Load" : "Stress";
		String strainName = isLoadDisplacement.get() ? "Displacement" : "Strain";
		String dataset1Name = timeName + " (" + timeUnit + ")";
		String dataset2Name = stressName + " (" + stressUnit + ")";
		String dataset3Name = strainName + " (" + strainUnit + ")";
		String dataset4Name = strainName + " Rate (" + strainRateUnit + ")";
		String dataset5Name = "Front Face Force (" + faceForceUnit + ")";
		String dataset6Name = "Back Face Force (" + faceForceUnit + ")";

		// Check if face force is in all of the samples.
		boolean faceForcePresent = isFaceForcePresent();

		for(SampleGroup group : sampleGroupsForExport){
			String csv = "";
			int longestData = 0;
			for(Sample s : group.groupSamples){
				for(LoadDisplacementSampleResults results: s.getResults()) {
					String sampleName = s.getName() + (s.getResults().size() > 1 ? results.getChartLegendPostFix() : "");
					if(faceForcePresent)
						csv += sampleName + ",,,,,,,";
					else
						csv += sampleName + ",,,,,";
					if(results.time.length > longestData)
						longestData = results.time.length;
				}

			}
			csv += "\n";
			for(int i = 0; i < group.groupSamples.size(); i++)
			{
				if(faceForcePresent){
					csv += dataset1Name + "," + dataset2Name + "," + dataset3Name + "," + dataset4Name + "," +
							dataset5Name + "," + dataset6Name + ",,";
				}
				else
				{
					csv += dataset1Name + "," + dataset2Name + "," + dataset3Name + "," + dataset4Name + ",,";
				}
			}
			
			csv += "\n";
			//now do data.
			ArrayList<double[]> timeDataList = new ArrayList<double[]>(); //double[] for each sample
			ArrayList<double[]> stressDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainRateDataList = new ArrayList<double[]>();
			ArrayList<double[]> frontFaceForceDataList = new ArrayList<double[]>();
			ArrayList<double[]> backFaceForceDataList = new ArrayList<double[]>();
 
			for(Sample sample : group.groupSamples){
				for(int resultIdx = 0; resultIdx < sample.getResults().size(); resultIdx++) {
					ScaledResults results = new ScaledResults(sample, resultIdx);

					double[] timeData = results.getTime();
					double[] stressData = results.getLoad();
					double[] strainData = results.getDisplacement();
					double[] strainRateData = results.getStrainRate();


					timeDataList.add(timeData);
					stressDataList.add(stressData);
					strainDataList.add(strainData);
					strainRateDataList.add(strainRateData);

					if(faceForcePresent)
					{
						double[] frontFaceForceData = results.getFrontFaceForce();
						double[] backFaceForceData = results.getBackFaceForce();
						frontFaceForceDataList.add(frontFaceForceData);
						backFaceForceDataList.add(backFaceForceData);
					}
				}

			}
			ArrayList<String> lines = new ArrayList<String>();
			//write each line

			for(int i = 0; i < longestData; i++){
				String dataLine = "";
				for(int j = 0; j < timeDataList.size(); j++){
					if(timeDataList.get(j).length > i){
						
						if(faceForcePresent){
							dataLine += timeDataList.get(j)[i] + "," + stressDataList.get(j)[i] + "," + 
									strainDataList.get(j)[i] + "," + strainRateDataList.get(j)[i] + "," +
									+ frontFaceForceDataList.get(j)[i] + "," + backFaceForceDataList.get(j)[i] + ",,";
						}
						else{
							dataLine += timeDataList.get(j)[i] + "," + stressDataList.get(j)[i] + "," + 
									strainDataList.get(j)[i] + "," + strainRateDataList.get(j)[i] + ",,";
						}
						
					}
					else{
						//data isn't long enough, add space
						if(faceForcePresent)
						{
							dataLine += ",,,,,,,"; // This is insanely hard coded.
						}
						else{
							dataLine += ",,,,,";
						}
						
					}
				}
				lines.add(dataLine + "\n");
			}

			SPOperations.writeStringToFile(csv, file.getPath() + "/" + group.groupName + ".csv"); //Header
			SPOperations.writeListToFile(lines, file.getPath() + "/" + group.groupName + ".csv"); //Data
		}

		// Write a csv file for each of the references samples.
		String trueEng = (isEngineering.get() ? "Engineering" : "True");
		if(getCheckedReferenceSamples().size() > 0) {

			for(XYChart.Series<Number, Number> series: homeController.chartsGUI.getReferenceSampleStressStrainSerie(Optional.empty())) {
				ArrayList<String> lines = new ArrayList<String>();

				lines.add(trueEng + " Stress (" + stressUnit + "), " + trueEng +  " Strain (" + strainUnit + ")\n");

				series.getData().stream().forEach(d -> {
					lines.add(d.getXValue().toString() + "," + d.getYValue() + "\n");
				});
				SPOperations.writeListToFile(lines, file.getPath() + "/" + series.getName() + ".csv"); //Data

			}
		}

	}

	private boolean isFaceForcePresent(){
		return !sampleGroupsForExport.stream()
				.anyMatch(
						sampleGroup -> sampleGroup.groupSamples.stream().anyMatch(
								sample -> sample.getResults().stream().anyMatch(
										result -> !result.isFaceForceGraphable()
								)
						)
				);
	}

	private JSONObject buildJSONDatasetDescriptor(String unit, String name, String trueEng, double[] savedData ){
		JSONObject description = new JSONObject();
		description.put("unit", unit);
		description.put("name", name);
		JSONArray jsonDataEntry  = new JSONArray();
		for(double entry : savedData){
			jsonDataEntry.add(entry);
		}
		description.put("engineering_or_true", trueEng);
		description.put("data", jsonDataEntry);
		return description;
	}
	private File writeConsoleExcelFileMakerJobFile(String path, Optional<Integer> pointsToKeep) {
		File jobFile =new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/JobFile");
		if(jobFile.exists())
			SPOperations.deleteFolder(jobFile);
		jobFile.mkdir();
		JSONObject excelJobDescription = new JSONObject();
		//Check to see if faceforces exist in all samples
		boolean faceForcePresent = isFaceForcePresent();

		String timeUnit = homeController.getDisplayedTimeUnit();
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		String faceForceUnit = getDisplayedFaceForceUnit();

		String timeName = "Time";//always
		String stressName = isLoadDisplacement.get() ? "Load" : "Stress";
		String strainName = isLoadDisplacement.get() ? "Displacement" : "Strain";
		String trueEng = isLoadDisplacement.get() ? "" : (isEngineering.get() ? "Engineering" : "True");
		excelJobDescription.put("JSON_Version", 1);
		excelJobDescription.put("Export_Location", path);
		excelJobDescription.put("Summary_Page", includeSummaryPage.isSelected());
		JSONArray groups = new JSONArray();
		for(net.relinc.libraries.sample.SampleGroup group : sampleGroupsForExport){
			groups.add(group.groupName);
		}

		excelJobDescription.put("groups", groups);

		if(homeController.getCheckedReferenceSamples().size() > 0) {
			// Put reference sample data/info into Description.json.
			// Really, it's dumb to have multiple json files in a directory structure, it should be all one json file.
			JSONArray referenceSamples = new JSONArray();

			for(XYChart.Series<Number, Number> series: homeController.chartsGUI.getReferenceSampleStressStrainSerie(Optional.empty())) {
				JSONObject ob = new JSONObject();
				JSONArray strainData = new JSONArray();
				series.getData().stream().forEach(d -> strainData.add(d.getXValue()));
				JSONArray stressData = new JSONArray();
				series.getData().stream().forEach(d -> stressData.add(d.getYValue()));

				ob.put("strain", buildJSONDatasetDescriptor(strainUnit, "Strain",trueEng, series.getData().stream().mapToDouble(d -> d.getXValue().doubleValue()).toArray()));
				ob.put("stress", buildJSONDatasetDescriptor(stressUnit, "Stress",trueEng, series.getData().stream().mapToDouble(d -> d.getYValue().doubleValue()).toArray()));
				ob.put("name", series.getName());

				referenceSamples.add(ob);
			}

			excelJobDescription.put("reference_samples", referenceSamples);
		}

		SPOperations.writeStringToFile(excelJobDescription.toJSONString(), jobFile.getPath() + "/Description.json");



		for(net.relinc.libraries.sample.SampleGroup group : sampleGroupsForExport){
			File groupDir = new File(jobFile.getPath() + "/" + group.groupName);
			groupDir.mkdir();
			JSONArray groupDescription = new JSONArray();
			for(Sample sample : group.groupSamples){
				JSONObject sampleInfo = new JSONObject();
				for(int resultIdx = 0; resultIdx < sample.getResults().size(); resultIdx++) {
					String sampleName = sample.getName() + (sample.getResults().size() > 1 ? sample.getResults().get(resultIdx).getChartLegendPostFix() : "");
					double[] timeData;
					double[] stressData;
					double[] strainData;
					double[] strainRateData;
                    double[] frontFaceForceData = new double[0];
                    double[] backFaceForceData = new double[0];


					ScaledResults results = new ScaledResults(sample, resultIdx);
					timeData = results.getTime();
					stressData = results.getLoad();
					strainData = results.getDisplacement();
					strainRateData = results.getStrainRate();
					if(faceForcePresent) {
                        frontFaceForceData = results.getFrontFaceForce();
                        backFaceForceData = results.getBackFaceForce();

                        if(pointsToKeep.isPresent()) {
							frontFaceForceData= Resampler.sampleData(frontFaceForceData,pointsToKeep.get());
							backFaceForceData = Resampler.sampleData(backFaceForceData,pointsToKeep.get());
						}

                    }
					if(pointsToKeep.isPresent()) {
						timeData = Resampler.sampleData(timeData, pointsToKeep.get());
						stressData = Resampler.sampleData(stressData, pointsToKeep.get());
						strainData = Resampler.sampleData(strainData, pointsToKeep.get());
						strainRateData = Resampler.sampleData(strainRateData, pointsToKeep.get());
					}


					JSONObject datasets = new JSONObject();
					JSONObject strainDescription = buildJSONDatasetDescriptor( strainUnit, strainName, trueEng, strainData );
					datasets.put("strain", strainDescription);
					JSONObject stressDescription = buildJSONDatasetDescriptor(stressUnit, stressName, trueEng, stressData );
					datasets.put("stress", stressDescription);
					JSONObject strainRateDescription = buildJSONDatasetDescriptor( strainRateUnit, strainName+" Rate", trueEng, strainRateData );
					datasets.put("strainRate", strainRateDescription);
					JSONObject time = buildJSONDatasetDescriptor(timeUnit,  timeName,"", timeData );
					datasets.put("time",time);

					if (faceForcePresent)
					{
						JSONObject frontFaceForce = buildJSONDatasetDescriptor( faceForceUnit,"Front Face Force", "", frontFaceForceData );
						datasets.put("frontFaceForce", frontFaceForce);
						JSONObject backFaceForce = buildJSONDatasetDescriptor(faceForceUnit,"Back Face Force", "", backFaceForceData );
						datasets.put("backFaceForce", backFaceForce);
					}
					else{
						datasets.put("frontFaceForce", "");
						datasets.put("backFaceForce", "");
					}
					Color color = ChartsGUI.getColor(getSampleIndex(sample), resultIdx, sample.getResults().size(), false);
					String colorString = String.format("%02x%02x%02x", (int)(color.getRed() * 255), (int)(color.getGreen() * 255), (int)(color.getBlue() * 255));
					datasets.put("color", colorString);
					SPOperations.writeStringToFile(datasets.toJSONString(), groupDir.getPath() + "/"+sampleName+".json");
					groupDescription.add(sampleName);
				}
			}
			SPOperations.writeStringToFile(groupDescription.toJSONString(), jobFile.getPath() + "/"+group.groupName+".json");

		}

		return jobFile;
	}
}
