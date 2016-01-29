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
import net.relinc.libraries.data.DataLocation;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Displacement;
import net.relinc.libraries.data.EngineeringStrain;
import net.relinc.libraries.data.Force;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.TransmissionPulse;
import net.relinc.libraries.data.TrueStrain;
import net.relinc.libraries.data.ModifierFolder.Modifier;
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
					getSelectedSample().results.loadDataLocation = newDataLocation;
				}
				renderStressModifiersVBox();
			}
		});
		
		strainDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
			@Override
			public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue, DataSubset newValue) {
				DataLocation newDataLocation = getSelectedSample().getLocationOfDataSubset(strainDataListView.getSelectionModel().getSelectedItem());
				if(newDataLocation != null){
					getSelectedSample().results.displacementDataLocation = newDataLocation;
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
		if(currentSample == null)
			return;
		boolean isValid = true;
		//only apply if the same type of data is in the same location.
		//also, modifier settings must be applicable for it to run. That is, those modifiers that are activated must be enabled in the other samples.
		for(Sample sample : currentSamples){
			if(sample.equals(currentSample))
				continue;
			//tests if the same type of data is in the current location. If it's not the same type e.g. one is stress and the other is strain, it fails.
			DataSubset loadDataset = sample.getDataSubsetAtLocation(currentSample.results.loadDataLocation);
			DataSubset displacementDataset = sample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
			
			DataSubset masterLoadDataset = currentSample.getDataSubsetAtLocation(currentSample.results.loadDataLocation);
			DataSubset masterDisplacementDataset = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
			//these should be the same thing.
			//System.out.println("These should be True: " + masterStrainDataset.equals(currentStrainDatasubset) + " and " + masterStressDataset.equals(currrentStressDatasubset));
			
			if(!(loadDataset.getClass() 
					.equals(masterLoadDataset.getClass()))){
				isValid = false;
				break;
			}
			if(!(displacementDataset.getClass() 
					.equals(masterDisplacementDataset.getClass()))){
				isValid = false;
				break;
			}
			
			//for all the activated modifiers in the master, the slave must be enabled so it can be activated later.
			//** the master can be deactivated but enabled and will work because the slave doesn't need to be activated
			Iterator<Modifier> masterStressModifiers = masterLoadDataset.modifiers.iterator();
			Iterator<Modifier> slaveStressModifiers = loadDataset.modifiers.iterator();
			while(masterStressModifiers.hasNext() && slaveStressModifiers.hasNext()){
				Modifier master = masterStressModifiers.next();
				Modifier slave = slaveStressModifiers.next();
				if(master.activated.get() && !slave.enabled.get())
					isValid = false;
			}
			
			Iterator<Modifier> masterStrainModifiers = masterDisplacementDataset.modifiers.iterator();
			Iterator<Modifier> slaveStrainModifiers = displacementDataset.modifiers.iterator();
			
			while(masterStrainModifiers.hasNext() && slaveStrainModifiers.hasNext()){
				Modifier master = masterStrainModifiers.next();
				Modifier slave = slaveStrainModifiers.next();
				if(master.activated.get() && !slave.enabled.get())
					isValid = false;
			}
			
		}
		

		//implement the 'applying to other samples.'
		if(isValid){
			for(Sample sample : currentSamples){
				sample.results.loadDataLocation = currentSample.results.loadDataLocation;
				sample.results.displacementDataLocation = currentSample.results.displacementDataLocation;
				
				//activate necessary modifiers.
				DataSubset slaveSampleLoadDatasubset = sample.getDataSubsetAtLocation(sample.results.loadDataLocation);
				DataSubset slaveSampleDisplacementDatasubset = sample.getDataSubsetAtLocation(sample.results.displacementDataLocation);
				
				DataSubset masterSampleLoadDatasubset = currentSample.getDataSubsetAtLocation(currentSample.results.loadDataLocation);
				DataSubset masterSampleDisplacementDatasubet = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
				
				Iterator<Modifier> masterLoadIterator = masterSampleLoadDatasubset.modifiers.iterator();
				Iterator<Modifier> slaveLoadIterator = slaveSampleLoadDatasubset.modifiers.iterator();
				
				while(masterLoadIterator.hasNext() && slaveLoadIterator.hasNext()){
					Modifier master = masterLoadIterator.next();
					Modifier slave = slaveLoadIterator.next();
					if(master.enabled.get())
						slave.activated.set(master.activated.get()); //Previous check doesn't allow activating a disabled modifier
				}
				
				Iterator<Modifier> masterDisplacementIterator = masterSampleDisplacementDatasubet.modifiers.iterator();
				Iterator<Modifier> slaveDisplacementIterator = slaveSampleDisplacementDatasubset.modifiers.iterator();
				
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
			alert.setContentText("Unable to apply to other samples, data does not match!");
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
			if(D instanceof ReflectedPulse || D instanceof TrueStrain || D instanceof EngineeringStrain || D instanceof Displacement){
				strainDataListView.getItems().add(D);
				if(selectedSample.results.displacementDataLocation.compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
					strainDataListView.getSelectionModel().select(strainDataListView.getItems().size() - 1);
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
			if(D instanceof TransmissionPulse || D instanceof Force){
				stressDataListView.getItems().add(D);
				if(selectedSample.results.loadDataLocation.compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
					//selects the item that is the current stressLocation in sampleResults
					stressDataListView.getSelectionModel().select(stressDataListView.getItems().size() - 1);
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
		for (Modifier mod : strainData.modifiers)
			strainModifierControlsVBox.getChildren().addAll(mod.getViewerControls()); //put in Hbox if more controls arise
	}
	
	private void renderStressModifiersVBox(){
		stressModifierControlsVBox.getChildren().clear();
		Label modifierLabel = new Label("Modifier Settings:");
		stressModifierControlsVBox.getChildren().add(modifierLabel);
		DataSubset stressData = stressDataListView.getSelectionModel().getSelectedItem();
		if(stressData == null)
			return;
		for(Modifier mod : stressData.modifiers)
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
