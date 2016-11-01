package net.relinc.libraries.data;

public class TransmissionBarVoltagePulse extends TransmissionPulse {
	public TransmissionBarVoltagePulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return strainGauge.getStrain(super.getTrimmedData());
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.LOAD;
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
