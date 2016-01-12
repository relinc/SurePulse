package net.relinc.processor.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import net.relinc.processor.data.DataSubset;

public class PochammerChreeDispersion extends Modifier {

	public PochammerChreeDispersion() {
		modifierEnum = ModifierEnum.POCHAMMER;
	}

	@Override
	public String toString() {
		return "Pochammer-Chree Dispersion";
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		return new ArrayList<Node>();
	}

	@Override
	public String getStringForFileWriting() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		// TODO Auto-generated method stub
	}

	@Override
	public double[] applyModifierToData(double[] fullData, DataSubset activatedData) {
		return fullData; 
	}

	@Override
	public void configureModifier(DataSubset sub) {
		//nothing to do
	}

}
