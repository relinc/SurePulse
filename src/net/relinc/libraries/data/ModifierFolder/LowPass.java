package net.relinc.libraries.data.ModifierFolder;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.DataSubset.baseDataType;
import net.relinc.libraries.staticClasses.SPMath;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.fxControls.NumberTextField;

public class LowPass extends Modifier {

	private String lowPassDescription = "Lowpass Filter";
	private double lowPassValue;
	public NumberTextField valueTF;
	HBox holdGrid = new HBox();
	
	public LowPass() {
		init();
	}
	
	public LowPass(double val)
	{
		init();
		setLowPassValue(val);
	}
	
	public static double getIncrease(double currentVal, boolean up)
	{
		// .3 -> .1
		// 3 -> 1
		// 30 -> 10
		// 300 -> 100
		if(currentVal <= 0.0)
			return 1.0;
		int count = 0;
		if(currentVal >= 1)
		{
			while(up ? currentVal >= 1 : currentVal > 1)
			{
				count++;
				currentVal = currentVal / 10;
			}
			count--;
		}
		else
		{
			while(up ? currentVal < 1 : currentVal <= 1)
			{
				count--;
				currentVal = currentVal * 10;
			}
		}
		return Math.pow(10, count);
	}
	
	private void init()
	{
		modifierEnum = ModifierEnum.LOWPASS;
		valueTF = new NumberTextField("KHz", "KHz");
		valueTF.setText("1000");
		valueTF.updateLabelPosition();
		valueTF.textProperty().addListener((observable, oldValue, newValue) -> {
			valueTF.updateLabelPosition();
		});
		GridPane grid = new GridPane();

		grid.add(valueTF, 0, 0);
		grid.add(valueTF.unitLabel, 0, 0);
		VBox arrowsVBox = new VBox();
		Button upButton = new Button("▲");
		upButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double currentVal = Double.parseDouble(valueTF.getText());
				currentVal += getIncrease(currentVal, true);
				valueTF.setText(new DecimalFormat(".#####").format(currentVal));
			}
		});
		Button downButton = new Button("▼");
		downButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double currentVal = Double.parseDouble(valueTF.getText());
				currentVal -= getIncrease(currentVal, false);
				valueTF.setText(new DecimalFormat(".#####").format((currentVal)));
			}
		});
		arrowsVBox.getChildren().add(upButton);
		arrowsVBox.getChildren().add(downButton);
		arrowsVBox.setAlignment(Pos.CENTER);
		grid.add(arrowsVBox, 1, 0);
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(3);
		
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
		if(activatedData.getBaseDataType() == baseDataType.LOAD && SPSettings.globalLoadDataLowpassFilter != null){
			return SPMath.fourierLowPassFilter(fullData, SPSettings.globalLoadDataLowpassFilter.getLowPassValue(), 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
		}
		else if(activatedData.getBaseDataType() == baseDataType.DISPLACEMENT && SPSettings.globalDisplacementDataLowpassFilter != null){
			return SPMath.fourierLowPassFilter(fullData, SPSettings.globalDisplacementDataLowpassFilter.getLowPassValue(), 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
		}
		else if(activated.get()){
			return SPMath.fourierLowPassFilter(fullData, lowPassValue, 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
		}
		else {
			return fullData;
		}
//		if(SPSettings.globalLoadDataLowpassFilter != null) //global overrides.
//			return SPMath.fourierLowPassFilter(fullData, SPSettings.globalLoadDataLowpassFilter.getLowPassValue(), 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
//		else if(activated.get())
//			return SPMath.fourierLowPassFilter(fullData, lowPassValue, 1 / (activatedData.Data.timeData[1] - activatedData.Data.timeData[0]));
//		else
//			return fullData;
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
