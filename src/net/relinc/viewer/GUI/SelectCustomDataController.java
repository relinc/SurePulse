package net.relinc.viewer.GUI;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import net.relinc.processor.data.DataLocation;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.Displacement;
import net.relinc.processor.data.EngineeringStrain;
import net.relinc.processor.data.Force;
import net.relinc.processor.data.ReflectedPulse;
import net.relinc.processor.data.TransmissionPulse;
import net.relinc.processor.data.TrueStrain;
import net.relinc.processor.sample.Sample;

public class SelectCustomDataController {
	@FXML ListView<String> samplesListView;
	@FXML ListView<String> stressDataListView;
	@FXML ListView<String> strainDataListView;
	List<Sample> currentSamples;
	
	@FXML
	public void initialize(){
		samplesListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				renderStressData();
				renderStrainData();
			}
		});
		stressDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				DataLocation newDataLocation = getSelectedSample().getLocationOfDataSubset(stressDataListView.getSelectionModel().getSelectedItem());
				if(newDataLocation != null){
					getSelectedSample().results.loadDataLocation = newDataLocation;
				}
			}
		});
		strainDataListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				DataLocation newDataLocation = getSelectedSample().getLocationOfDataSubset(strainDataListView.getSelectionModel().getSelectedItem());
				if(newDataLocation != null){
					getSelectedSample().results.displacementDataLocation = newDataLocation;
				}
			}
		});
		
		
	}
	
	public void applyToOtherSamplesButtonFired(){
		Sample currentSample = getSelectedSample();
		if(currentSample == null)
			return;
		boolean isValid = true;
		//only apply if the same type of data is in the same location.
		for(Sample sample : currentSamples){
			if(!(sample.getDataSubsetAtLocation(currentSample.results.loadDataLocation).getClass() 
					.equals(currentSample.getDataSubsetAtLocation(currentSample.results.loadDataLocation).getClass()))){
				isValid = false;
				break;
			}
			if(!(sample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation).getClass() 
					.equals(currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation).getClass()))){
				isValid = false;
				break;
			}
			
			
			//sample.results.stressLocation = currentSample.results.stressLocation;
			//sample.results.strainLocation = currentSample.results.strainLocation;
		}

		if(isValid){
			for(Sample sample : currentSamples){
				sample.results.loadDataLocation = currentSample.results.loadDataLocation;
				sample.results.displacementDataLocation = currentSample.results.displacementDataLocation;
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
		currentSamples.stream().forEach(s -> samplesListView.getItems().add(s.getName()));
		renderStressData();
		renderStrainData();
	}

	private void renderStrainData() {
		Sample selectedSample = getSelectedSample();
		if(selectedSample == null)
			return;
		strainDataListView.getItems().clear();
		for(DataSubset D : selectedSample.DataFiles.getAllDatasets()){
			if(D instanceof ReflectedPulse || D instanceof TrueStrain || D instanceof EngineeringStrain || D instanceof Displacement){
				strainDataListView.getItems().add(D.name);
				if(selectedSample.results.displacementDataLocation.compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
					strainDataListView.getSelectionModel().select(strainDataListView.getItems().size() - 1);
				}
			}
		}
	}

	private void renderStressData() {
		Sample selectedSample = getSelectedSample();
		if(selectedSample == null)
			return;
		stressDataListView.getItems().clear();
		for(DataSubset D : selectedSample.DataFiles.getAllDatasets()){
			System.out.println("Looking at : " + D.name);
			if(D instanceof TransmissionPulse || D instanceof Force){
				stressDataListView.getItems().add(D.name);
				if(selectedSample.results.loadDataLocation.compareTo(selectedSample.getLocationOfDataSubset(D)) == 0){
					//selects the item that is the current stressLocation in sampleResults
					stressDataListView.getSelectionModel().select(stressDataListView.getItems().size() - 1);
				}
			}
		}
	}
	
	private Sample getSelectedSample(){
		int idx = samplesListView.getSelectionModel().getSelectedIndex();
		if(idx == -1)
			return null;
		Sample selectedSample = currentSamples.get(idx);
		return selectedSample;
	}
	
	
	
	
	
}
