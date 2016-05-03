package net.relinc.libraries.data;

public class Displacement extends DataSubset {

	public Displacement(double[] t, double[] d){
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		double[] displacement = new double[getTrimmedData().length];
		double[] data = getTrimmedData();
		for(int i = 0; i < displacement.length; i++){
			displacement[i] = Math.abs(data[i]);
		}
		return displacement;
	}

	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.DISPLACEMENT;
	}
	
}
