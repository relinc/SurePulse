package net.relinc.libraries.data;

import net.relinc.libraries.application.BarSetup;

public class TransmissionBarStrain extends HopkinsonBarPulse{

	public TransmissionBarStrain(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getPochammerAdjustedArray(BarSetup setup) {
		System.err.println("Dispersion not implemented for transmission bar strain.");
		return null;
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return super.getTrimmedData();
	}
	
}
