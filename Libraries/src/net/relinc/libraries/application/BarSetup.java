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
		//handle both systems

		parse_bar_from_file(IncidentBar, incidentDir);
		parse_bar_from_file(TransmissionBar, tranmissionDir);

		read_directory_for_strain_gauges(IncidentBar, incidentDir);
		read_directory_for_strain_gauges(TransmissionBar, tranmissionDir);
	}

	private void parse_bar_from_file( Bar bar, File dir ) {
		File file = new File(dir.getPath() + "/Parameters.json");

		if( file.exists() ) {
			bar.parseJSONtoParameters(SPOperations.readStringFromFile(dir.getPath() + "/Parameters.json"));
		}
		else {
			bar.setParametersFromString(SPOperations.readStringFromFile(dir.getPath() + "/Parameters.txt"));
		}
	}

	private void read_directory_for_strain_gauges( Bar bar, File directory ) {
		for(File file : directory.listFiles() ) {
			if(!file.getName().contains("Parameters")) {
				//file.getPath().endsWith(".json")
				bar.strainGauges.add(new StrainGaugeOnBar(file.getPath()));
			}
		}
	}

		/*
	    for(File file : incidentDir.listFiles()){
	    	if(!file.getName().contains("Parameters")){
	    		IncidentBar.strainGauges.add(new StrainGaugeOnBar(file.getPath()));
	    	}
	    }

	    for(File file : tranmissionDir.listFiles()){
	    	//System.out.println("Finding sg files. On File: " + file.getPath());
	    	if(!file.getName().contains("Parameters")){
	    		TransmissionBar.strainGauges.add(new StrainGaugeOnBar(file.getPath()));
	    	}
	    }
	    */

		/*
		if (is_a_json_file) {
			IncidentBar.parseJSONtoParameters(SPOperations.readStringFromFile(incidentDir.getPath() + "/Parameters.json"));
			TransmissionBar.parseJSONtoParameters(SPOperations.readStringFromFile(incidentDir.getPath() + "/Parameters.json"));
		} else {
			IncidentBar.setParametersFromString(SPOperations.readStringFromFile(incidentDir.getPath() + "/Parameters.txt"));
			TransmissionBar.setParametersFromString(SPOperations.readStringFromFile(tranmissionDir.getPath() + "/Parameters.txt"));
		}
		*/

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

			SPOperations.writeStringToFile(incidentBarFile, incidentDir + "/Parameters.json");
			for (StrainGaugeOnBar sg : IncidentBar.strainGauges)
				SPOperations.writeStringToFile(sg.stringForFile(), incidentDir + "/" + sg.getNameForFile() + ".json");

			SPOperations.writeStringToFile(transmissionBarFile, tranmissionDir + "/Parameters.json");
			for (StrainGaugeOnBar sg : TransmissionBar.strainGauges)
				SPOperations.writeStringToFile(sg.stringForFile(), tranmissionDir + "/" + sg.getNameForFile() + ".json");

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
