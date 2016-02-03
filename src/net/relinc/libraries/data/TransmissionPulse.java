package net.relinc.libraries.data;

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
}
