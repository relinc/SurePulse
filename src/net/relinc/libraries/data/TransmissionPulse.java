package net.relinc.libraries.data;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public abstract class TransmissionPulse extends HopkinsonBarPulse {
	
	public TransmissionPulse(double[] t, double[] d){
		super(t, d);
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
}
