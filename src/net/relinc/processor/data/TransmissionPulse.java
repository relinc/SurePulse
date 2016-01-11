package net.relinc.processor.data;

import net.relinc.processor.application.BarSetup;
import net.relinc.processor.staticClasses.PochammerChreeDispersion;

public class TransmissionPulse extends HopkinsonBarPulse {
	

	public TransmissionPulse(double[] t, double[] d){
		super(t, d);
	}
	
	@Override
	public double[] getUsefulTrimmedData(){
		double[] voltage = super.getTrimmedData();
		return strainGauge.getStrain(voltage);
	}
	
	@Override
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.timeData[1] - Data.timeData[0],
				strainGauge.distanceToSample, setup.TransmissionBar.getWaveSpeed());
	}
}
