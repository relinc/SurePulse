package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.libraries.application.FitableDataset;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.staticClasses.SPSettings;

public class Fitter extends Modifier{
	public FitableDataset fitable;
	private String fitterDescriptor = "FitableDataset";
	
	public Fitter(){
		modifierEnum = ModifierEnum.FITTER;
		checkBox = new CheckBox("Enable Fitter");
		checkBox.selectedProperty().bindBidirectional(activated);
		checkBox.disableProperty().bind(enabled.not());
	}
	
	@Override
	public String toString() {
		return "Fitter";
	}

	@Override
	public double[] applyModifierToData(double[] fullData, DataSubset activatedData) {
		if(enabled.get() && activated.get()){
			ArrayList<Double> x = new ArrayList<Double>(activatedData.Data.timeData.length);
			ArrayList<Double> y = new ArrayList<Double>(activatedData.Data.timeData.length);
			for(int i = 0; i < activatedData.Data.timeData.length; i++){
				x.add(new Double(activatedData.Data.timeData[i]));
				y.add(new Double(fullData[i]));
			}
			fitable.origX = x;
			fitable.origY = y;
			fitable.renderFittedData();
			return fitable.fittedY.stream().mapToDouble(d -> d).toArray();
		}
		return fullData;
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		return new ArrayList<Node>(); //no controls
	}

	@Override
	public String getStringForFileWriting() {
		return fitable == null ? "" : fitterDescriptor + ":" + fitable.getStringForFileWriting() + SPSettings.lineSeperator;
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void configureModifier(DataSubset dataSubset) {
		
	}

	@Override
	public void readModifierFromString(String line) {
		if (line.split(":")[0].equals(fitterDescriptor)) {
			Gson gson = new Gson();
			String val = line.substring(line.split(":")[0].length() + 1);
			System.out.println(val);
			fitable = gson.fromJson(val, FitableDataset.class);
			enabled.set(true);
			activated.set(true);
		}
	}
	
}
