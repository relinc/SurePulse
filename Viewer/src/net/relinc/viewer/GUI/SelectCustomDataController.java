package net.relinc.viewer.GUI;
import java.util.Iterator;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.relinc.libraries.data.*;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;

public class SelectCustomDataController {
	@FXML ListView<Sample> samplesListView;
	@FXML ListView<DataSubset> stressDataListView;
	@FXML ListView<DataSubset> strainDataListView;
	@FXML VBox stressDataVBox;
	@FXML VBox strainDataVBox;
	List<Sample> currentSamples;
	VBox strainModifierControlsVBox = new VBox();
	VBox stressModifierControlsVBox = new VBox();
	
	@FXML
	public void initialize(){
		samplesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
			@Override
			public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newValue) {
				renderStressData();
				renderStrainData();
			}
		});
		stressDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
			@Override
			public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue, DataSubset newValue) {
				DataLocation newDataLocation = getSelectedSample().getLocationOfDataSubset(stressDataListView.getSelectionModel().getSelectedItem());
				if(newDataLocation != null){
					Sample sample = getSelectedSample();
					DataLocation oldDisplacementDataLocation = sample.getResults().get(0).getDisplacementDataLocation();

					sample.getResults().clear();
					sample.getResults().addAll(LoadDisplacementSampleResults.createResults(sample, newDataLocation, oldDisplacementDataLocation));

				}
				renderStressModifiersVBox();
			}
		});
		
		strainDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
			@Override
			public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue, DataSubset newValue) {
				DataLocation newDataLocation = getSelectedSample().getLocationOfDataSubset(strainDataListView.getSelectionModel().getSelectedItem());
				if(newDataLocation != null){
					Sample sample = getSelectedSample();
					DataLocation oldLoadDataLocation = sample.getResults().get(0).getLoadDataLocation();

					sample.getResults().clear();
					sample.getResults().addAll(LoadDisplacementSampleResults.createResults(sample, oldLoadDataLocation, newDataLocation));
				}
				renderStrainModifiersVBox();
			}
		});
		
		strainDataVBox.getChildren().add(strainModifierControlsVBox);
		stressDataVBox.getChildren().add(stressModifierControlsVBox);
		Tooltip t = new Tooltip("Modifiers must be enabled and configured in the \"Trim Data\" form in the SURE-Pulse Data Processor");
		Tooltip.install(strainDataVBox, t);
		Tooltip.install(stressDataVBox, t);
		strainModifierControlsVBox.setSpacing(5);
		stressModifierControlsVBox.setSpacing(5);
	}
	
	public void applyToOtherSamplesButtonFired(){
		Sample currentSample = getSelectedSample();
		if(currentSample == null || currentSample.getResults().size() != 1)
			return;
		String error = "";

		LoadDisplacementSampleResults currentSampleResults = currentSample.getResults().get(0);
		//only apply if the same type of data is in the same location.
		//also, modifier settings must be applicable for it to run. That is, those modifiers that are activated must be enabled in the other samples.
		for(Sample sample : currentSamples){
			if(sample.equals(currentSample))
				continue;
			//tests if the same type of data is in the current location. If it's not the same type e.g. one is rawStressData and the other is rawStrainData, it fails.

			DataSubset loadDataset = sample.getDataSubsetAtLocation(currentSampleResults.getLoadDataLocation());
			DataSubset displacementDataset = sample.getDataSubsetAtLocation(currentSampleResults.getDisplacementDataLocation());

			DataSubset masterLoadDataset = currentSample.getDataSubsetAtLocation(currentSampleResults.getLoadDataLocation());
			DataSubset masterDisplacementDataset = currentSample.getDataSubsetAtLocation(currentSampleResults.getDisplacementDataLocation());

			if(loadDataset == null) {
				error = "Failed to find matching load dataset for sample " + sample.getName();
				break;
			}

			if(displacementDataset == null) {
				error = "Failed to find matching displacement dataset for sample " + sample.getName();
				break;
			}



			//these should be the same thing.
			//System.out.println("These should be True: " + masterStrainDataset.equals(currentStrainDatasubset) + " and " + masterStressDataset.equals(currrentStressDatasubset));

			if(!(loadDataset.getClass()
					.equals(masterLoadDataset.getClass()))){
				error = "Unable to detect matching load dataset for sample " + sample.getName();
				break;
			}
			if(!(displacementDataset.getClass()
					.equals(masterDisplacementDataset.getClass()))){
				error = "Unable to detect matching displacement dataset for sample " + sample.getName();
				break;
			}

			//for all the activated modifiers in the master, the slave must be enabled so it can be activated later.
			//** the master can be deactivated but enabled and will work because the slave doesn't need to be activated
			Iterator<Modifier> masterStressModifiers = masterLoadDataset.getModifiers().iterator();
			Iterator<Modifier> slaveStressModifiers = loadDataset.getModifiers().iterator();
			while(masterStressModifiers.hasNext() && slaveStressModifiers.hasNext()){
				Modifier master = masterStressModifiers.next();
				Modifier slave = slaveStressModifiers.next();
				if(master.activated.get() && !slave.enabled.get())
					error = "Unable to match load data modifiers, modifier" + slave.modifierEnum + " for sample " + sample.getName() + " is not enabled.";
			}

			Iterator<Modifier> masterStrainModifiers = masterDisplacementDataset.getModifiers().iterator();
			Iterator<Modifier> slaveStrainModifiers = displacementDataset.getModifiers().iterator();

			while(masterStrainModifiers.hasNext() && slaveStrainModifiers.hasNext()){
				Modifier master = masterStrainModifiers.next();
				Modifier slave = slaveStrainModifiers.next();
				if(master.activated.get() && !slave.enabled.get())
					error = "Unable to match displacement data modifiers, modifier" + slave.modifierEnum + " for sample " + sample.getName() + " is not enabled.";
			}
			
		}
		

		//implement the 'applying to other samples.'
		if(error.equals("")){
			for(Sample sample : currentSamples){
				// TODO: Implement this for multiple sample results
				LoadDisplacementSampleResults results = sample.getResults().get(0);
				sample.getResults().clear();
				sample.getResults().addAll(LoadDisplacementSampleResults.createResults(sample, currentSampleResults.getLoadDataLocation(), currentSampleResults.getDisplacementDataLocation()));

				//activate necessary modifiers.
				DataSubset slaveSampleLoadDatasubset = sample.getDataSubsetAtLocation(results.getLoadDataLocation());
				DataSubset slaveSampleDisplacementDatasubset = sample.getDataSubsetAtLocation(results.getDisplacementDataLocation());
				
				DataSubset masterSampleLoadDatasubset = currentSample.getDataSubsetAtLocation(currentSampleResults.getLoadDataLocation());
				DataSubset masterSampleDisplacementDatasubet = currentSample.getDataSubsetAtLocation(currentSampleResults.getDisplacementDataLocation());
				
				Iterator<Modifier> masterLoadIterator = masterSampleLoadDatasubset.getModifiers().iterator();
				Iterator<Modifier> slaveLoadIterator = slaveSampleLoadDatasubset.getModifiers().iterator();
				
				while(masterLoadIterator.hasNext() && slaveLoadIterator.hasNext()){
					Modifier master = masterLoadIterator.next();
					Modifier slave = slaveLoadIterator.next();
					if(master.enabled.get())
						slave.activated.set(master.activated.get()); //Previous check doesn't allow activating a disabled modifier
				}
				
				Iterator<Modifier> masterDisplacementIterator = masterSampleDisplacementDatasubet.getModifiers().iterator();
				Iterator<Modifier> slaveDisplacementIterator = slaveSampleDisplacementDatasubset.getModifiers().iterator();
				
				while(masterDisplacementIterator.hasNext() && slaveDisplacementIterator.hasNext()){
					Modifier master = masterDisplacementIterator.next();
					Modifier slave = slaveDisplacementIterator.next();
					if(master.enabled.get())
						slave.activated.set(master.activated.get());
				}
				
			}
		}
		else{
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");
			alert.setHeaderText("Error Applying to Other Samples");
			alert.setContentText(error);
			alert.showAndWait();
		}
	}
	
	public void doneButtonFired(){
		Stage stage = (Stage) samplesListView.getScene().getWindow();
	    stage.close();
	}
	
	public void render(){
		samplesListView.getItems().clear();
		samplesListView.setItems(FXCollections.observableArrayList(currentSamples));
		renderStressData();
		renderStrainData();
		if(samplesListView.getSelectionModel().getSelectedItem() == null)
			samplesListView.getSelectionModel().select(0);
	}

	private void renderStrainData() {
		Sample selectedSample = getSelectedSample();
		if(selectedSample == null)
			return;
		strainDataListView.getItems().clear();
		for(DataSubset D : selectedSample.DataFiles.getAllDatasets()){
			if(D instanceof ReflectedPulse || D instanceof TrueStrain || D instanceof EngineeringStrain || D instanceof Displacement || D instanceof LagrangianStrain){
				strainDataListView.getItems().add(D);
				for(LoadDisplacementSampleResults results: selectedSample.getResults()) {
					if(results.getDisplacementDataLocation().compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
						strainDataListView.getSelectionModel().select(strainDataListView.getItems().size() - 1);
					}
				}
			}
		}
		// populate modifier section.
		renderStrainModifiersVBox();
	}

	private void renderStressData() {
		Sample selectedSample = getSelectedSample();
		if(selectedSample == null)
			return;
		stressDataListView.getItems().clear();
		for(DataSubset D : selectedSample.DataFiles.getAllDatasets()){
			if(D instanceof TransmissionPulse || D instanceof Force || D instanceof TrueStress || D instanceof EngineeringStress){
				stressDataListView.getItems().add(D);
				for(LoadDisplacementSampleResults results: selectedSample.getResults()) {
					if(results.getLoadDataLocation().compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
						//selects the item that is the current stressLocation in sampleResults
						stressDataListView.getSelectionModel().select(stressDataListView.getItems().size() - 1);
					}
				}

			}
		}
		//populate modifier section.
		renderStressModifiersVBox();
	}
	
	private void renderStrainModifiersVBox(){
		strainModifierControlsVBox.getChildren().clear();
		Label modifierLabel = new Label("Modifier Settings:");
		strainModifierControlsVBox.getChildren().add(modifierLabel);
		DataSubset strainData = strainDataListView.getSelectionModel().getSelectedItem();
		if(strainData == null)
			return;
		for (Modifier mod : strainData.getModifiers())
			strainModifierControlsVBox.getChildren().addAll(mod.getViewerControls()); //put in Hbox if more controls arise
	}
	
	private void renderStressModifiersVBox(){
		stressModifierControlsVBox.getChildren().clear();
		Label modifierLabel = new Label("Modifier Settings:");
		stressModifierControlsVBox.getChildren().add(modifierLabel);
		DataSubset stressData = stressDataListView.getSelectionModel().getSelectedItem();
		if(stressData == null)
			return;
		if(stressData instanceof TransmissionPulse){
			//add 1,2, and 3-wave calculation methods
			if(getSelectedSample().getResults().get(0).getCurrentDisplacementDatasubset() instanceof ReflectedPulse)
				stressModifierControlsVBox.getChildren().addAll(((TransmissionPulse)stressData).getCalculationRadioButtons());
		}
		for(Modifier mod : stressData.getModifiers())
			stressModifierControlsVBox.getChildren().addAll(mod.getViewerControls()); //put in HBox if more controls arise
	}
	
	private Sample getSelectedSample(){
		int idx = samplesListView.getSelectionModel().getSelectedIndex();
		if(idx == -1)
			return null;
		Sample selectedSample = currentSamples.get(idx);
		return selectedSample;
	}
	
	
	
	
	
}
