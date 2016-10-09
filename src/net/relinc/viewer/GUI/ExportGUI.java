package net.relinc.viewer.GUI;

import java.io.File;
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import net.relinc.libraries.data.ModifierFolder.Reducer;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.viewer.application.ScaledResults;

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
					sampleGroups.remove(selected - 1);
				}
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
					// This adds the reference to the sample. So if the selected data is later changed, it changes as well
					// It would be nice to add by copy, but we do not have the means to successfully copy a sample
//					Sample newSample = null;
//
//					if(s instanceof CompressionSample){
//						newSample = new CompressionSample();
//						((CompressionSample)newSample).setDiameter(((CompressionSample)s).getDiameter());
//						
//					}
//					else if(s instanceof TensionRoundSample){
//						newSample = new TensionRoundSample();
//						((TensionRoundSample)newSample).setDiameter(((TensionRoundSample)s).getDiameter());
//					}
//					else if(s instanceof TensionRectangularSample){
//						newSample = new TensionRectangularSample();
//						((TensionRectangularSample)newSample).setWidth(((TensionRectangularSample)s).getWidth());
//						((TensionRectangularSample)newSample).setHeight(((TensionRectangularSample)s).getHeight());
//					}
//					else if(s instanceof ShearCompressionSample){
//						newSample = new ShearCompressionSample();
//						((ShearCompressionSample)newSample).setGaugeWidth(((ShearCompressionSample)s).getGaugeWidth());
//						((ShearCompressionSample)newSample).setGaugeHeight(((ShearCompressionSample)s).getGaugeWidth());
//					} else if(s instanceof LoadDisplacementSample) {
//						newSample = new LoadDisplacementSample();
//					}
//					
//					if(s instanceof HopkinsonBarSample){
//						((HopkinsonBarSample)newSample).setLength(((HopkinsonBarSample)s).getLength());
//					}
//
//
//					LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(newSample);
//
//					results.displacement = Arrays.copyOf(s.results.displacement, s.results.displacement.length);
//					results.load = Arrays.copyOf(s.results.load, s.results.load.length);
//
//					//results.engineeringStrain = Arrays.copyOf(s.results.getEngineeringStrain(), s.results.getEngineeringStrain().length);
//					results.time = Arrays.copyOf(s.results.time, s.results.time.length);
//					//results.engineeringStress = Arrays.copyOf(s.results.getEngineeringStress(), s.results.getEngineeringStress().length);
//
//					newSample.results = results;
//					newSample.setName(s.getName());
					

					//currentSelectedSampleGroup.groupSamples.add(newSample);
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
		sampleGroups.add(sampleGroup);	
		sampleGroupRoot.setExpanded(true);
		refreshSampleGroupTreeView();
	}
	
	private void refreshSampleGroupTreeView() {
		sampleGroupRoot.getChildren().clear();
		for(SampleGroup sampleGroup : sampleGroups) {
			TreeItem<String> treeItemSampleGroup = new TreeItem<>(sampleGroup.groupName);
			sampleGroupRoot.getChildren().add(treeItemSampleGroup);
			for(Sample sample : sampleGroup.groupSamples) {
				TreeItem<String> treeItemSample = new TreeItem<>(sample.getName());
				treeItemSampleGroup.getChildren().add(treeItemSample);
			}
		}

	}
	
	private int findStringInSampleGroups(String find) {

		if(sampleGroups == null || sampleGroups.size() == 0)
			return -1;

		int i = 0;

		for(SampleGroup group : sampleGroups) {
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

		if(findStringInSampleGroups(selectedSampleGroup.getValue()) != -1) {
			currentSelectedSampleGroup = sampleGroups.get(findStringInSampleGroups(selectedSampleGroup.getValue()));
			return;
		}

		if(currentSelectedSampleGroup == null) {
			currentSelectedSampleGroup = new SampleGroup(selectedSampleGroup.getValue());
		}
	}
	
	public void onExportDataButtonClicked() throws Exception {

		if(sampleGroups == null || sampleGroups.size() == 0) {

			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroups) {
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
		
		int pointsToKeep = getPointsToKeepForExcelFileFromUser();

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
	
	private int getPointsToKeepForExcelFileFromUser() {
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
		
		int val = userInputTF.getDouble().intValue();
		if(reduceDataCheckBox.isSelected())
			val = -1;
		
		return val;
	}

	public void exportCSVButtonFired() {
		if(sampleGroups == null || sampleGroups.size() == 0) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroups) {
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
		boolean faceForcePresent = true;
		for(SampleGroup group : sampleGroups)
		{
			for(Sample s : group.groupSamples)
			{
				if(!(s.isFaceForceGraphable())){
					faceForcePresent = false;
				}
			}
		}
		
		for(SampleGroup group : sampleGroups){
			String csv = "";
			int longestData = 0;
			for(Sample s : group.groupSamples){
				if(faceForcePresent)
					csv += s.getName() + ",,,,,,,";
				else
					csv += s.getName() + ",,,,,";
				if(s.results.time.length > longestData)
					longestData = s.results.time.length;
			}
			csv += "\n";
			for(Sample s : group.groupSamples){ //s is unused, only using for loop as a counter.
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
				ScaledResults results = new ScaledResults(sample);
				
				double[] timeData = results.getTime();
				double[] stressData = results.getLoad();
				double[] strainData = results.getDisplacement();
				double[] strainRateData = results.getStrainRate();
				double[] frontFaceForceData = results.getFrontFaceForce();
				double[] backFaceForceData = results.getBackFaceForce();
				
				//List<double[]> data = homeController.getScaledDataArraysFromSample(sample);//, stressData, strainData, strainRateData);

//				timeData = data.get(0);
//				stressData = data.get(1);
//				strainData = data.get(2);
//				strainRateData = data.get(3);

				timeDataList.add(timeData);
				stressDataList.add(stressData);
				strainDataList.add(strainData);
				strainRateDataList.add(strainRateData);
				
				if(faceForcePresent)
				{
//					frontFaceForceData = data.get(4);
//					backFaceForceData = data.get(5);
					frontFaceForceDataList.add(frontFaceForceData);
					backFaceForceDataList.add(backFaceForceData);
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

	}
	
	private File writeConsoleExcelFileMakerJobFile(String path, int pointsToKeep) {
		File jobFile =new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/JobFile");
		if(jobFile.exists())
			SPOperations.deleteFolder(jobFile);
		jobFile.mkdir();

		String timeUnit = homeController.getDisplayedTimeUnit();
		String stressUnit = getDisplayedLoadUnit();
		String strainUnit = getDisplayedDisplacementUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();

		String timeName = "Time";//always
		String stressName = isLoadDisplacement.get() ? "Load" : "Stress";
		String strainName = isLoadDisplacement.get() ? "Displacement" : "Strain";
		String trueEng = isLoadDisplacement.get() ? "" : (isEngineering.get() ? "Engineering" : "True");

		String parametersString = "Version$1\n";
		parametersString += "Export Location$" + path + "\n";
		parametersString += "Summary Page$" + includeSummaryPage.isSelected() + "\n";
		parametersString += "Dataset1$" + timeName + "$" + "(" + timeUnit + ")" + "$" + "\n";
		parametersString += "Dataset2$" + stressName + "$(" + stressUnit + ")" + "$" + trueEng + "\n";
		parametersString += "Dataset3$" + strainName + "$(" + strainUnit + ")"  + "$" + trueEng + "\n";
		parametersString += "Dataset4$" + strainName + " Rate$(" + strainRateUnit + ")" + "$" + trueEng + "\n";

		SPOperations.writeStringToFile(parametersString, jobFile.getPath() + "/Parameters.txt");

		for(net.relinc.libraries.sample.SampleGroup group : sampleGroups){
			File groupDir = new File(jobFile.getPath() + "/" + group.groupName);
			groupDir.mkdir();
			for(Sample sample : group.groupSamples){
				File sampleDir = new File(groupDir.getPath() + "/" + sample.getName());
				sampleDir.mkdir();
				double[] timeData;
				double[] stressData;
				double[] strainData;
				double[] strainRateData;

				ArrayList<String> sampleData = new ArrayList<String>();

				ScaledResults results = new ScaledResults(sample);
				timeData = results.getTime();
				stressData = results.getLoad();
				strainData = results.getDisplacement();
				strainRateData = results.getStrainRate();
				// No front/back faceForce support for Excel export...
				
				Reducer r = new Reducer();
				r.enabled.set(true);
				r.activated.set(true);
				r.setPointsToKeep(pointsToKeep);
				
				timeData = r.applyModifierToData(timeData, null);
				stressData = r.applyModifierToData(stressData, null);
				strainData = r.applyModifierToData(strainData, null);
				strainRateData = r.applyModifierToData(strainRateData, null);
				
				for(int i = 0; i < timeData.length; i++){
					sampleData.add(timeData[i] + "," + stressData[i] + "," + strainData[i] + "," + strainRateData[i] + "\n");
				}
				SPOperations.writeListToFile(sampleData, sampleDir.getPath() + "/Data.txt");
				String parameters = "Color$" + 
						homeController.colorString.get(homeController.getSampleIndexByName(sample.getName()) % homeController.colorString.size()).substring(1) + 
				"\n";
				SPOperations.writeStringToFile(parameters, sampleDir.getPath() + "/Parameters.txt");
			}

		}
		return jobFile;
	}
}
