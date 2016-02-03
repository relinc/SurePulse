package net.relinc.libraries.data;

public class TransmissionBarStrainPulse extends TransmissionPulse {
	public TransmissionBarStrainPulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return super.getTrimmedData();
	}
}
