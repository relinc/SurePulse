package net.relinc.processor.data;

import net.relinc.processor.application.StrainGaugeOnBar;

public class ReflectedPulse extends DataSubset {
	public StrainGaugeOnBar strainGauge;
	public String strainGaugeName;
	public ReflectedPulse(double[] t, double[] d){
		super(t, d);
	}
	@Override
	public double[] getUsefulTrimmedData() {
		double[] voltage = super.getTrimmedData();
		return strainGauge.getStrain(voltage);
	}
}
