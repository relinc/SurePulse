package net.relinc.libraries.data;

import net.relinc.libraries.data.DataSubset.baseDataType;

public class LoadCell extends DataSubset {
	public double newtonToVoltRatio = 1;
	
	public LoadCell(double[] timed, double[] datad) {
		super(timed, datad);
		// TODO Auto-generated constructor stub
	}
	
	public double[] getUsefulTrimmedData(){
		double[] voltage = super.getTrimmedData();
		double[] force = new double[voltage.length];
		for(int i = 0; i < force.length; i++){
			force[i] = voltage[i] * newtonToVoltRatio;
		}
		return force;
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.LOAD;
	}
}
