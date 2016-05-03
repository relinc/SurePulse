package net.relinc.processor.data;

import net.relinc.processor.application.BarSetup;
import net.relinc.processor.application.StrainGaugeOnBar;

public abstract class HopkinsonBarPulse extends DataSubset {
	public StrainGaugeOnBar strainGauge;
	public String strainGaugeName;
	
	public HopkinsonBarPulse(double[] timed, double[] datad) {
		super(timed, datad);
		//don't use this
	}
	
	public abstract double[] getPochammerAdjustedArray(BarSetup setup);
	
}
