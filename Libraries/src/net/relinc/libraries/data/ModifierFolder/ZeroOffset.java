package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.staticClasses.SPMath;
import net.relinc.libraries.staticClasses.SPSettings;

public class ZeroOffset extends Modifier {

	private double zero = 0.0;
	private String zeroDescriptor = "Zero";
	
	public ZeroOffset() {
		modifierEnum = ModifierEnum.ZERO;
		checkBox = new CheckBox("Enable Zero Offset");
		checkBox.selectedProperty().bindBidirectional(activated);
		checkBox.disableProperty().bind(enabled.not());
	}
	
	@Override
	public String toString() {
		return "Zero";
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		return new ArrayList<Node>(); //no controls
	}

	public double getZero() {
		return zero;
	}

	public void setZero(double zero) {
		this.zero = zero;
	}

	@Override
	public String getStringForFileWriting() {
		return enabled.get() ? zeroDescriptor + ":" + zero + SPSettings.lineSeperator : "";
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(zeroDescriptor)){
			zero = Double.parseDouble(val);
			enabled.set(true);
			activated.set(true);
		}
	}

	@Override
	public ModifierResult applyModifier(double[] x, double[] y, DataSubset data) {
		if(shouldApply()) {
			double[] newY = this.applyModifierToData(y, data);
			return new ModifierResult(x, newY, 1.0);
		}
		return new ModifierResult(x, y, 1.0);
	}

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
			sum += activatedData.getModifiedData()[i];
		double avg = sum / (activatedData.getEnd() - activatedData.getBegin() + 1);
		zero = avg;
	}
	
	@Override
	public void readModifierFromString(String line) {
		setValuesFromLine(line);
	}

}
