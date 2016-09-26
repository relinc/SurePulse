package net.relinc.libraries.data;

import net.relinc.libraries.data.DataSubset.baseDataType;

public class EngineeringStress extends DataSubset {

	public EngineeringStress(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return getTrimmedData();
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.LOAD;
	}
	
	@Override
	public String getUnitAbbreviation(){
		return "σ";
	}
	
	@Override
	public String getUnitName(){
		return "Engineering Stress";
	}
}
