package net.relinc.processor.data;

public class Force extends DataSubset {

	public Force(double[] t, double[] d){
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		double[] force = new double[getTrimmedData().length];
		double[] data = getTrimmedData();
		for(int i = 0; i < force.length; i++){
			force[i] = data[i];
		}
		return force;
	}
	
}
