package net.relinc.libraries.data;

import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.PochammerChreeDispersion;

public class ReflectedPulse extends HopkinsonBarPulse {
//	public StrainGaugeOnBar strainGauge;
//	public String strainGaugeName;
	public ReflectedPulse(double[] t, double[] d){
		super(t, d);
	}
	@Override
	public double[] getUsefulTrimmedData() {
		double[] voltage = super.getTrimmedData();
		return strainGauge.getStrain(voltage);
	}
	
	public double[] getPochammerAdjustedArray(BarSetup setup){
		return PochammerChreeDispersion.runPochammer(getTrimmedData(), 
				PochammerChreeDispersion.SteelParameters,setup.IncidentBar.diameter/2 , Data.timeData[1] - Data.timeData[0],
				strainGauge.distanceToSample, setup.IncidentBar.getWaveSpeed());
	}
}
