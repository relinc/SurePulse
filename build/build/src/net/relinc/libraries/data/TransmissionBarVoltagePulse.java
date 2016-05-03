package net.relinc.libraries.data;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.data.DataSubset.baseDataType;

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

}
