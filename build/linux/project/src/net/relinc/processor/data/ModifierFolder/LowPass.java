package net.relinc.processor.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.fxControls.NumberTextField;
import net.relinc.processor.staticClasses.SPMath;
import net.relinc.processor.staticClasses.SPSettings;

public class LowPass extends Modifier {

	private String lowPassDescription = "Lowpass Filter";
	private double lowPassValue;
	NumberTextField valueTF;
	HBox holdGrid = new HBox();
	
	public LowPass() {
		modifierEnum = ModifierEnum.LOWPASS;
		valueTF = new NumberTextField("KHz", "KHz");
		valueTF.setText("1000");
		valueTF.updateLabelPosition();
		GridPane grid = new GridPane();

		grid.add(valueTF, 0, 0);
		grid.add(valueTF.unitLabel, 0, 0);
		
		holdGrid.getChildren().add(grid);
		holdGrid.setAlignment(Pos.CENTER);
		
		checkBox = new CheckBox("Enable Lowpass Filter");
		checkBox.selectedProperty().bindBidirectional(activated);
		checkBox.disableProperty().bind(enabled.not());
	}

	//controls for trim data HBox
	
	
	@Override
	public String toString() {
		return "Lowpass Filter";
	}

	@Override
	public double[] applyModifierToData(double[] fullData, DataSubset activatedData) {
		if(SPSettings.globalLowpassFilter != null) //global overrides.
			return SPMath.fourierLowPassFilter(fullData, SPSettings.globalLowpassFilter.getLowPassValue(), 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
		else if(activated.get())
			return SPMath.fourierLowPassFilter(fullData, lowPassValue, 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
		else
			return fullData;
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		ArrayList<Node> list = new ArrayList<>();
		list.add(holdGrid);
		return list;
	}

	@Override
	public String getStringForFileWriting() {
		//legacy: for a short time, -1 was saved when not enabled. New protocol is to save nothing.
		return enabled.get() ? lowPassDescription + ":" + lowPassValue + SPSettings.lineSeperator : "";
	}

	public double getLowPassValue() {
		return lowPassValue;
	}

	public void setLowPassValue(double lowPass) {
		this.lowPassValue = lowPass;
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(lowPassDescription)){
			//it was saved, so it is enabled
			if(Double.parseDouble(val) != -1) //legacy. For a short time -1 was saved. Now nothing is saved if not enabled.
				enabled.set(true);
			lowPassValue = Double.parseDouble(val);
			activated.set(true);
		}
	}

	@Override
	public void configureModifier(DataSubset sub) {
		lowPassValue = valueTF.getDouble() * 1000;
	}
	
	@Override
	public void readModifierFromString(String line) {
		setValuesFromLine(line);
	}


}
