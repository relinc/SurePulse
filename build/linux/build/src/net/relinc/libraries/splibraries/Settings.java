package net.relinc.libraries.splibraries;

import java.io.File;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.image.Image;


public final class Settings {
	
	public static BooleanProperty metricMode = new SimpleBooleanProperty(false);

	public static String applicationSupportDirectory = getAppDataDirectoryLocation(System.getProperty("os.name"));
	public static String imageProcResulstsDir = applicationSupportDirectory+"/RELFX/DataProc/Result";
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
	
	public static String getAppDataDirectoryLocation(String operatingSystemName) {
		if(operatingSystemName.contains("Win")) {
			return System.getenv("APPDATA" ).replace("\\","/");
		}
		else if(operatingSystemName.contains("Mac"))
			return System.getProperty("user.home") + "/Library/Application Support";
		else
			return null;
	}
	
	public static Image getRELLogo(){
		return new Image("/net/relinc/libraries/images/rel-logo.png");
	}
	
//	public static void main(String[] args){
//		getRELLogo();
//	}
}

