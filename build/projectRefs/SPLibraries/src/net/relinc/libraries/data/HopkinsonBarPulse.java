package net.relinc.libraries.data;

import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.StrainGaugeOnBar;

public abstract class HopkinsonBarPulse extends DataSubset {
	public StrainGaugeOnBar strainGauge;
	public String strainGaugeName;
	
	public HopkinsonBarPulse(double[] timed, double[] datad) {
		super(timed, datad);
		//don't use this
	}
	
	public abstract double[] getPochammerAdjustedArray(BarSetup setup);
	
}
