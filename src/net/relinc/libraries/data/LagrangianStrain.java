package net.relinc.libraries.data;

public class LagrangianStrain extends DataSubset {
	
	public LagrangianStrain(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return getTrimmedData();
	}
}
