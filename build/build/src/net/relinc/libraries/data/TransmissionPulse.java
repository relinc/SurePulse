package net.relinc.libraries.data;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public abstract class TransmissionPulse extends HopkinsonBarPulse {
	
	public RadioButton oneWaveRadioButton = new RadioButton("One Wave");
	public RadioButton twoWaveRadioButton = new RadioButton("Two Wave");
	public RadioButton threeWaveRadioButton = new RadioButton("Three Wave");
	private ToggleGroup group = new ToggleGroup();
	
	public TransmissionPulse(double[] t, double[] d){
		super(t, d);
		oneWaveRadioButton.setSelected(true); //default is one wave
		oneWaveRadioButton.setToggleGroup(group);
		twoWaveRadioButton.setToggleGroup(group);
		threeWaveRadioButton.setToggleGroup(group);
	}
	
	@Override
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.timeData[1] - Data.timeData[0],
				strainGauge.distanceToSample, setup.TransmissionBar.getWaveSpeed());
	}
	
	public double[] getBackFaceForcePulse(Bar transmissionBar, double sign){
		double[] strain = getUsefulTrimmedData();
		double[] force = new double[strain.length];
		for(int i = 0; i < force.length; i++){
			force[i] = sign * strain[i] * transmissionBar.youngsModulus * Math.pow(transmissionBar.diameter / 2, 2) * Math.PI;
		}
		return force;
	}
	
	public ObservableList<Node> getCalculationRadioButtons(){
		//ArrayList<RadioButton> list = new ArrayList<RadioButton>();
		ObservableList<Node> list = FXCollections.observableArrayList();
		list.add(oneWaveRadioButton);
		list.add(twoWaveRadioButton);
		list.add(threeWaveRadioButton);
		return list;
	}
	
}
