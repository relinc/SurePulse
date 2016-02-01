package net.relinc.libraries.data;

import net.relinc.libraries.application.BarSetup;

public class IncidentBarStrain extends HopkinsonBarPulse {

	public IncidentBarStrain(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return super.getTrimmedData();
	}

	@Override
	public double[] getPochammerAdjustedArray(BarSetup setup) {
		System.err.println("Pochammer not implemented for bar strain.");
		return null;
	}

}
