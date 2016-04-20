package net.relinc.libraries.data;

import org.apache.commons.math3.ml.clustering.DoublePoint;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.data.DataSubset.baseDataType;

public class ReflectedBarVoltagePulse extends ReflectedPulse {

	public ReflectedBarVoltagePulse(double[] t, double[] d) {
		super(t, d);
	}

	@Override
	public double[] getUsefulTrimmedData() {
		return strainGauge.getStrain(super.getTrimmedData());
	}
	
	@Override
	public baseDataType getBaseDataType() {
		return baseDataType.DISPLACEMENT;
	}

}
