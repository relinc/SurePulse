package net.relinc.libraries.data;

import net.relinc.libraries.application.Bar;

public class TransmissionBarVoltagePulse extends TransmissionPulse {
	public TransmissionBarVoltagePulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return strainGauge.getStrain(super.getTrimmedData());
	}
	

}
