package net.relinc.libraries.data;

public class Force extends DataSubset {

	public Force(double[] t, double[] d){
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		double[] force = new double[getTrimmedData().length];
		double[] data = getTrimmedData();
		for(int i = 0; i < force.length; i++){
			force[i] = Math.abs(data[i]);
		}
		return force;
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.LOAD;
	}
	
	@Override
	public String getUnitAbbreviation(){
		return "N";
	}
	
	@Override
	public String getUnitName(){
		return "Force";
	}
}
