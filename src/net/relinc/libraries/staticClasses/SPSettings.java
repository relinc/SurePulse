package net.relinc.libraries.staticClasses;


import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;
import net.relinc.libraries.data.ModifierFolder.LowPass;


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
	public static File lastUploadDirectory;
	private static String lastUploadDirectoryDescription = "Last Upload Directory";
	private static String sendUsageStatsDescription = "Send Usage Statistics";
	public static String tensionRectangularExtension = ".samtrec";
	public static String tensionRoundExtension = ".samtrnd";
	public static String shearCompressionExtension = ".samscmp";
	public static String compressionExtension = ".samcomp";
	public static String loadDisplacementExtension = ".samlds";
	public static LowPass globalLoadDataLowpassFilter;
	public static LowPass globalDisplacementDataLowpassFilter;
	public static String surePulseLocation = "/RELFX/SUREPulse";
	public static String globalStrainGaugeLocation = "/Strain Gauges";
	
	public static String getAppDataDirectoryLocation(String operatingSystemName) {
		if(operatingSystemName.contains("Win")) {
			return System.getenv("APPDATA" ).replace("\\","/");
		}
		else if(operatingSystemName.contains("Mac"))
			return System.getProperty("user.home") + "/Library/Application Support";
		else //linux
			return "/var/lib";
	}
	
	public static void writeSPSettings(){
		String settings = "SPSettings File\nVersion~1\n";
		settings += workspaceDescription + "~" + (Workspace == null ? "" : Workspace.getPath()) + "\n";
		settings += sendUsageStatsDescription + "~" + SPTracker.initiallyEnabled + "\n";
		settings += lastUploadDirectoryDescription + "~" + (lastUploadDirectory == null ? "" : lastUploadDirectory.getPath()) + "\n";
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
				else if(descrip.equals(lastUploadDirectoryDescription)){
					lastUploadDirectory = new File(val);
				}
			}
		}
		
		if(Workspace == null || !Workspace.exists())
			return false;
		
		return true;
	}
	
	public static Image getRELLogo(){
		return new Image(SPOperations.relLogoImageLocation);
	}
	
	public static Image getSurePulseLogo(){
		return new Image(SPOperations.surePulseLogoImageLocation);
	}
	
	public static String getFFMpegBinary() {
		//TODO: Add binary to libs
		if(currentOS.contains("Win"))
			return "ffmpeg";
		else if(currentOS.contains("Mac"))
			return "/usr/local/bin/ffmpeg";
		else 
			return null;
	}
	

}
