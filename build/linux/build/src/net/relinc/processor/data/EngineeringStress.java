package net.relinc.processor.data;

public class EngineeringStress extends DataSubset {

	public EngineeringStress(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return getTrimmedData();
	}
}
