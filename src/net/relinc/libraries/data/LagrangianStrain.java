package net.relinc.libraries.data;

import net.relinc.libraries.data.DataSubset.baseDataType;

public class LagrangianStrain extends DataSubset {
	
	public LagrangianStrain(double[] timed, double[] datad) {
		super(timed, datad);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return getTrimmedData();
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.DISPLACEMENT;
	}
	
	@Override
	public String getUnitAbbreviation(){
		return "ε";
	}
	
	@Override
	public String getUnitName(){
		return "Lagrangian Strain";
	}
}
