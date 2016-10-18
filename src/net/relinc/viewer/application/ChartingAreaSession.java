package net.relinc.viewer.application;

import java.util.List;

public class ChartingAreaSession {
	public boolean loadDisplacementSelected;
	public boolean isEngineering;
	public boolean isEnglish;
	public String timeUnit;
	public List<String> checkedCharts;
	
	public ChartingAreaSession(boolean loadDisplacementSelected, boolean isEngineering, boolean isEnglish, String timeUnit, List<String> checkedCharts)
	{
		this.loadDisplacementSelected = loadDisplacementSelected;
		this.isEngineering = isEngineering;
		this.isEnglish = isEnglish;
		this.timeUnit = timeUnit;
		this.checkedCharts = checkedCharts;
	}
}
