package net.relinc.processor.data;

import net.relinc.processor.application.StrainGaugeOnBar;

public class TransmissionPulse extends DataSubset {
	public StrainGaugeOnBar strainGauge;
	public String strainGaugeName;

	public TransmissionPulse(double[] t, double[] d){
		super(t, d);
	}
	
	@Override
	public double[] getUsefulTrimmedData(){
		double[] voltage = super.getTrimmedData();
		return strainGauge.getStrain(voltage);
	}
}
