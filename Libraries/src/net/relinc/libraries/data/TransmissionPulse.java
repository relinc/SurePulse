package net.relinc.libraries.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public abstract class TransmissionPulse extends HopkinsonBarPulse {
	
	public CheckBox oneWaveCheckBox = new CheckBox("One Wave");
	public CheckBox twoWaveCheckBox = new CheckBox("Two Wave");
	public CheckBox threeWaveCheckBox = new CheckBox("Three Wave");

	public TransmissionPulse(double[] t, double[] d){
		super(t, d);
		oneWaveCheckBox.setSelected(true); //default is one wave
	}
	
	@Override
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.getTimeData()[1] - Data.getTimeData()[0],
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
		list.add(oneWaveCheckBox);
		list.add(twoWaveCheckBox);
		list.add(threeWaveCheckBox);
		return list;
	}
	
}
