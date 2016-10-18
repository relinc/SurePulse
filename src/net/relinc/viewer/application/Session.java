package net.relinc.viewer.application;

import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.viewer.GUI.CommonGUI;
import net.relinc.viewer.GUI.HomeController;

public class Session{
	public List<SampleSession> samplePaths;
	public ChartingAreaSession chartingAreaSession;
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
				s -> new SampleSession(s.loadedFromLocation.getPath(), 
						s.getCurrentDisplacementLocation() , 
						s.getCurrentLoadLocation())).collect(Collectors.toList());
		chartingAreaSession = new ChartingAreaSession(CommonGUI.isLoadDisplacement.get(), 
				CommonGUI.isEngineering.get(), CommonGUI.isEnglish.get(), hc.getDisplayedTimeUnit(), hc.getCheckedCharts());
		
		GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String s = gson.toJson(this);
		return s;
	}

}
