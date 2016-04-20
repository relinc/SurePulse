package net.relinc.libraries.data;

import net.relinc.libraries.data.DataSubset.baseDataType;

public class ReflectedBarStrainPulse extends ReflectedPulse {

	public ReflectedBarStrainPulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return super.getTrimmedData();
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.DISPLACEMENT;
	}

}
