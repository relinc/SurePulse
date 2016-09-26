package net.relinc.libraries.data;


public class TrueStrain extends DataSubset{

	public TrueStrain(double[] timed, double[] datad) {
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
		return "True Strain";
	}
}
