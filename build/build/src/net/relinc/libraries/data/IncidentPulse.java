package net.relinc.libraries.data;

import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public abstract class IncidentPulse extends HopkinsonBarPulse {
	
	public IncidentPulse(double[] t, double[] d){
		super(t, d);
	}
	
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.timeData[1] - Data.timeData[0],
				-strainGauge.distanceToSample, setup.IncidentBar.getWaveSpeed());
	}
}
