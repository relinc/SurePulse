package net.relinc.processor.staticClasses;


import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;


public final class SPSettings {
	
	public static BooleanProperty metricMode = new SimpleBooleanProperty(false);

	public static String applicationSupportDirectory = getAppDataDirectoryLocation(System.getProperty("os.name"));
	public static String currentOS = System.getProperty("os.name");
	public static String programFilesFolder = System.getenv("ProgramFiles");
	//thi should only get called once...
	//public static boolean isMetric = false;
	public static String lineSeperator = System.getProperty("line.separator");
	public static File Workspace;
	private static String workspaceDescription = "Workspace";
	private static String sendUsageStatsDescription = "Send Usage Statistics";
	public static String tensionRectangularExtension = ".samtrec";
	public static String tensionRoundExtension = ".samtrnd";
	public static String shearCompressionExtension = ".samscmp";
	public static String compressionExtension = ".samcomp";
	public static String loadDisplacementExtension = ".samlds";
	
	public static String getAppDataDirectoryLocation(String operatingSystemName) {
		if(operatingSystemName.contains("Win")) {
			return System.getenv("APPDATA" ).replace("\\","/");
		}
		else if(operatingSystemName.contains("Mac"))
			return System.getProperty("user.home") + "/Library/Application Support";
		else
			return null;
	}
	
	public static void writeSPSettings(){
		String settings = "SPSettings File\nVersion~1\n";
		settings += workspaceDescription + "~" + Workspace.getPath() + "\n";
		settings += sendUsageStatsDescription + "~" + SPTracker.initiallyEnabled + "\n";
		SPOperations.writeStringToFile(settings, applicationSupportDirectory + "/RELFX/SUREPulse/SPSettings.txt");
	}
	
	public static boolean readSPSettings(){
		File settingsFile = new File(applicationSupportDirectory + "/RELFX/SUREPulse/SPSettings.txt");
		if(!settingsFile.exists())
			return false;
		String settings = SPOperations.readStringFromFile(settingsFile.getPath());
		for(String line : settings.split(lineSeperator)){
			if(line.split("~").length > 1){
				String descrip = line.split("~")[0];
				String val = line.split("~")[1];
				if(descrip.equals(workspaceDescription)){
					Workspace = new File(val);
				}
				else if(descrip.equals(sendUsageStatsDescription)){
					SPTracker.initiallyEnabled = Boolean.parseBoolean(val);
					SPTracker.setEnabled(SPTracker.initiallyEnabled);
				}
			}
		}
		
		if(!Workspace.exists())
			return false;
		
		return true;
	}
	
	public static Image getRELLogo(){
		return new Image("/net/relinc/processor/images/rel-logo.png");
	}
	

}
