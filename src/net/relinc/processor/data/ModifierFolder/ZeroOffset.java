package net.relinc.processor.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.staticClasses.SPMath;
import net.relinc.processor.staticClasses.SPSettings;

public class ZeroOffset extends Modifier {

	private double zero = 0.0;
	private String zeroDescriptor = "Zero";
	
	public ZeroOffset() {
		modifierEnum = ModifierEnum.ZERO;
		checkBox = new CheckBox("Enable Zero Offset");
		checkBox.selectedProperty().bindBidirectional(activated);
	}
	
	@Override
	public String toString() {
		return "Zero";
	}


	@Override
	public List<Node> getTrimDataHBoxControls() {
		return new ArrayList<Node>();
	}

	public double getZero() {
		return zero;
	}

	public void setZero(double zero) {
		this.zero = zero;
	}

	@Override
	public String getStringForFileWriting() {
		return zeroDescriptor + ":" + zero + SPSettings.lineSeperator;
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(zeroDescriptor))
			zero = Double.parseDouble(val);
	}

	@Override
	public double[] applyModifierToData(double[] fullData, DataSubset activatedData) {
		if(activated.get())
			return SPMath.subtractFrom(fullData, zero);
		else
			return fullData;
	}

	@Override
	public void configureModifier(DataSubset activatedData) {
		double sum = 0.0;
		for(int i = activatedData.getBegin(); i <= activatedData.getEnd(); i++)
			sum += activatedData.Data.data[i];
		double avg = sum / (activatedData.getEnd() - activatedData.getBegin() + 1);
		zero = avg;
	}

}
