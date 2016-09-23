package net.relinc.libraries.data;

import net.relinc.libraries.data.DataSubset.baseDataType;

public class IncidentBarVoltagePulse extends IncidentPulse {

	public IncidentBarVoltagePulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return strainGauge.getStrain(super.getTrimmedData());
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.DISPLACEMENT;
	}

	@Override
	public String getUnitAbbreviation(){
		return "V";
	}
	
	@Override
	public String getUnitName(){
		return "Volts";
	}
}
