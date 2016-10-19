package net.relinc.viewer.application;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.viewer.GUI.CommonGUI;
import net.relinc.viewer.GUI.HomeController;

public class Session{
	public List<SampleSession> samplePaths;
	public ChartingAreaSession chartingAreaSession;
	public Double globalDisplacementLowpassValue;
	public Double globalLoadLowpassValue;
	public ROISession roiSession;
	
	public Session(){
		
	}
	
	public static Session getSessionFromJSONString(String json)
	{
		Gson gson = new Gson();
		Session session = gson.fromJson(json, Session.class);
		return session;
	}
			
	
	public void applyJSONString(String json, HomeController hc)
	{
		
	}
	
	public String getJSONString(HomeController hc)
	{
		samplePaths = CommonGUI.realCurrentSamplesListView.getItems().stream().map(
				s -> new SampleSession(s)).collect(Collectors.toList());
		chartingAreaSession = new ChartingAreaSession(CommonGUI.isLoadDisplacement.get(), 
				CommonGUI.isEngineering.get(), CommonGUI.isEnglish.get(), hc.getDisplayedTimeUnit(), hc.getCheckedCharts());
		globalDisplacementLowpassValue = SPSettings.globalDisplacementDataLowpassFilter == null ? null : SPSettings.globalDisplacementDataLowpassFilter.getLowPassValue();
		globalLoadLowpassValue = SPSettings.globalLoadDataLowpassFilter == null ? null : SPSettings.globalLoadDataLowpassFilter.getLowPassValue();
		Sample roiSample = hc.roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
		roiSession = new ROISession(CommonGUI.ROI.beginROITime, CommonGUI.ROI.endROITime, 
				roiSample == null ? null : roiSample.getName(), hc.choiceBoxRoi.getSelectionModel().getSelectedItem(), 
						hc.holdROIAnnotationsCB.isSelected(), hc.zoomToROICB.isSelected());
		
		GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String s = gson.toJson(this);
		return s;
	}

}
