package net.relinc.viewer.application;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.SPLogger;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.viewer.GUI.CommonGUI;
import net.relinc.viewer.GUI.HomeController;

public class Session{
	
	public List<SampleSession> samplePaths;
	public ChartingAreaSession chartingAreaSession;
	public Double globalDisplacementLowpassValue;
	public Double globalLoadLowpassValue;
	public ROISession roiSession;

	public List<SampleGroupSession> sampleGroups = new ArrayList<>();
	
	public Session(){
		
	}
	
	public static Session getSessionFromJSONString(String json)
	{
		Gson gson = new Gson();
		Session session = gson.fromJson(json, Session.class);
		SPLogger.logger.info("Created Session from json string");
		return session;
	}

	public static String getSamplePathForId(Sample s) {
		return s.loadedFromLocation.getPath().substring(CommonGUI.treeViewHomePath.length());
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

		sampleGroups = hc.sampleGroupsList.getItems().stream().map(sg -> new SampleGroupSession(
				sg.groupSamples.stream().map(s -> getSamplePathForId(s)).collect(Collectors.toList()),
				sg.color,
				sg.groupName
		)).collect(Collectors.toList());
		
		GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String s = gson.toJson(this);
        SPLogger.logger.info("Created json string from HomeController Object.");
		return s;
	}

}
