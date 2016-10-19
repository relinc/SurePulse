package net.relinc.viewer.application;

import net.relinc.libraries.data.DataLocation;
import net.relinc.libraries.sample.Sample;

public class SampleSession {
	public String path;
	public DataLocation loadLocation;
	public Integer loadTempTrimBeginIndex;
	public Integer loadTempTrimEndIndex;
	public DataLocation displacementLocation;
	public Integer displacementTempTrimBeginIndex;
	public Integer displacementTempTrimEndIndex;
	public boolean checked;
	public double beginROITime;
	public double endROITime;
	
	public SampleSession(Sample sample)
	{
		this.path = sample.loadedFromLocation.getPath();
		this.loadLocation = sample.getCurrentLoadLocation();
		this.displacementLocation = sample.getCurrentDisplacementLocation();
		this.checked = sample.isSelected();
		this.loadTempTrimBeginIndex = sample.getCurrentLoadDatasubset().getBeginTemp();
		this.loadTempTrimEndIndex = sample.getCurrentLoadDatasubset().getEndTemp();
		this.displacementTempTrimBeginIndex = sample.getCurrentDisplacementDatasubset().getBeginTemp();
		this.displacementTempTrimEndIndex = sample.getCurrentDisplacementDatasubset().getEndTemp();
		this.beginROITime = sample.getBeginROITime();
		this.endROITime = sample.getEndROITime();
	}
}
