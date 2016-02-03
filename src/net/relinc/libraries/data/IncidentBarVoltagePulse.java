package net.relinc.libraries.data;

public class IncidentBarVoltagePulse extends IncidentPulse {

	public IncidentBarVoltagePulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return strainGauge.getStrain(super.getTrimmedData());
	}

}
