package net.relinc.libraries.application;

import java.io.File;
import java.util.UUID;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class BarSetup {

	public Bar IncidentBar;
	public Bar TransmissionBar;
	public String name;
	
	public BarSetup(Bar incid, Bar trans){
		IncidentBar = incid;
		TransmissionBar = trans;
	}

	public BarSetup(String path) {
		//path is a .zip file.
	    //these are temp, no working directory //TODO: This aint that sweet
		String uuid = UUID.randomUUID().toString();
	    File incidentDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/tmp/" + uuid + "/Incident Bar");
		File tranmissionDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/tmp/" + uuid + "/Transmission Bar");
		SPOperations.deleteFolder(incidentDir);
		SPOperations.deleteFolder(tranmissionDir);
		String fullName = new File(path).getName(); //has .zip
		name = fullName.substring(0, fullName.length() - 4);
	    try {
	         ZipFile zipFile = new ZipFile(path);
	         zipFile.extractAll(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/tmp/" + uuid);
	    } catch (ZipException e) {
	        e.printStackTrace();
	    }
	    
	    IncidentBar = new Bar();
	    TransmissionBar = new Bar();
	    IncidentBar.setParametersFromString(SPOperations.readStringFromFile(incidentDir.getPath() + "/Parameters.txt"));
	    TransmissionBar.setParametersFromString(SPOperations.readStringFromFile(tranmissionDir.getPath() + "/Parameters.txt"));
	    
	    for(File file : incidentDir.listFiles()){
	    	if(!file.getName().equals("Parameters.txt")){
	    		IncidentBar.strainGauges.add(new StrainGaugeOnBar(file.getPath()));
	    	}
	    }
	    
	    for(File file : tranmissionDir.listFiles()){
	    	//System.out.println("Finding sg files. On File: " + file.getPath());
	    	if(!file.getName().equals("Parameters.txt")){
	    		TransmissionBar.strainGauges.add(new StrainGaugeOnBar(file.getPath()));
	    	}
	    }
	}

	public BarSetup() {
		// TODO Auto-generated constructor stub
		//TODO delete this constructor. Testing purposes
	}
	
	public ZipFile createZipFile(String path){
		
		String incidentBarFile = IncidentBar.stringForFile();
		String transmissionBarFile = TransmissionBar.stringForFile();

		ZipFile zipFile = null;// TODO this sucks

		try {
			zipFile = new ZipFile(path + ".zip");
			//System.out.println("Created zip file at: " + zipFile.getFile().getPath()) ;

			// Initiate Zip Parameters which define various properties such
			// as compression method, etc.
			ZipParameters parameters = new ZipParameters();

			// set compression method to store compression
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

			// Set the compression level
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);

			// Add folder to the zip file
			File tempDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/");
			File incidentDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/Incident Bar");
			File tranmissionDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/Transmission Bar");
			SPOperations.deleteFolder(tempDir);

			incidentDir.mkdirs();
			tranmissionDir.mkdirs();

			SPOperations.writeStringToFile(incidentBarFile, incidentDir + "/Parameters.txt");
			for (StrainGaugeOnBar sg : IncidentBar.strainGauges)
				SPOperations.writeStringToFile(sg.stringForFile(), incidentDir + "/" + sg.getNameForFile() + ".txt");

			SPOperations.writeStringToFile(transmissionBarFile, tranmissionDir + "/Parameters.txt");
			for (StrainGaugeOnBar sg : TransmissionBar.strainGauges)
				SPOperations.writeStringToFile(sg.stringForFile(), tranmissionDir + "/" + sg.getNameForFile() + ".txt");

			zipFile.addFolder(incidentDir, parameters);
			zipFile.addFolder(tranmissionDir, parameters);
			
			
			SPOperations.deleteFolder(incidentDir);
			SPOperations.deleteFolder(tranmissionDir);
			
		} catch (ZipException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return zipFile;
	}

	public void writeToFile(String path) {
		createZipFile(path);
	}
	

	
}
