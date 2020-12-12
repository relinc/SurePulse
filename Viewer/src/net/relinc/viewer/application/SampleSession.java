package net.relinc.viewer.application;

import net.relinc.libraries.data.DataLocation;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.viewer.GUI.CommonGUI;

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
		// Set the path to relative to the workspace.
		this.path = Session.getSamplePathForId(sample);

		// TODO: Support multiple results for a session?
		LoadDisplacementSampleResults results = sample.getResults().get(0);
		this.loadLocation = results.getCurrentLoadLocation();
		this.displacementLocation = results.getCurrentDisplacementLocation();
		this.checked = sample.isSelected();
		this.loadTempTrimBeginIndex = results.getCurrentLoadDatasubset().getBeginTemp();
		this.loadTempTrimEndIndex = results.getCurrentLoadDatasubset().getEndTemp();
		this.displacementTempTrimBeginIndex = results.getCurrentDisplacementDatasubset().getBeginTemp();
		this.displacementTempTrimEndIndex = results.getCurrentDisplacementDatasubset().getEndTemp();
		this.beginROITime = sample.getBeginROITime();
		this.endROITime = sample.getEndROITime();
	}
}
