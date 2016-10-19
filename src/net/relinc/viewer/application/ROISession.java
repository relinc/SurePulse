package net.relinc.viewer.application;

public class ROISession {
	public double beginTime;
	public double endTime;
	public String selectedROISample;
	public String selectedData;
	public boolean holdROIAnnotations;
	public boolean zoomToROI;
	
	public ROISession(double beginTime, double endTime, String selectedROISample, String selectedData, boolean holdROIAnnotations, boolean zoomToROI)
	{
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.selectedROISample = selectedROISample;
		this.selectedData = selectedData;
		this.holdROIAnnotations = holdROIAnnotations;
		this.zoomToROI = zoomToROI;
	}
}
