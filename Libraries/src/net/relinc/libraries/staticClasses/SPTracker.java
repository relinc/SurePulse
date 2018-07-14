package net.relinc.libraries.staticClasses;

import com.dmurph.tracking.AnalyticsConfigData;
import com.dmurph.tracking.JGoogleAnalyticsTracker;
import com.dmurph.tracking.JGoogleAnalyticsTracker.GoogleAnalyticsVersion;

public final class SPTracker {
	public static String surepulseProcessorCategory = "SurePulseDataProcessor";
	public static String surepulseViewerCategory = "SurePulseViewer";
	public static boolean initiallyEnabled = true; //this should always stay true. Gets overridden by reading from settings file.
	private static AnalyticsConfigData config = new AnalyticsConfigData("UA-70430033-2");
	private static JGoogleAnalyticsTracker tracker = new JGoogleAnalyticsTracker(config, GoogleAnalyticsVersion.V_4_7_2);

	public static void track(String category, String action){
		tracker.trackEvent(category, action);
		JGoogleAnalyticsTracker.completeBackgroundTasks(1000);
	}
	public static void setEnabled(boolean b){
		tracker.setEnabled(b);
	}
	public static boolean getEnabled(){
		return tracker.isEnabled();
	}
	
}
