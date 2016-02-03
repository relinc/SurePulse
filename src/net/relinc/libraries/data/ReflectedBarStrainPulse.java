package net.relinc.libraries.data;

public class ReflectedBarStrainPulse extends ReflectedPulse {

	public ReflectedBarStrainPulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return super.getTrimmedData();
	}

}
