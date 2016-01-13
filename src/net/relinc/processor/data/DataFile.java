package net.relinc.processor.data;

import java.io.File;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class DataFile {
	public File tempDataFolder; //Info is stored here from adding data file until saving from new sample window
	public File savedSampleFolder; //This is where the .zip sample file is containing this datafile
	public File originalLocation; //This is where the data file was originally loaded from
	//public DataFileModifier modifier;
	public DataSubsetListWrapper dataSubsets;

	public DataFile() {
		// TODO Auto-generated constructor stub
	}

	public void WriteModifierTo(String path) {
		File file = new File(path + "/Modifier.txt");
		if(file.exists())
			file.delete();
		//System.out.println("Writing " + getDataFileModifierString() + " to " + file.getPath());
		SPOperations.writeStringToFile(getDataFileModifierString(),file.getPath());
	}
	
	public String getDataFileModifierString(){
		String file = "";
		for(DataSubset d : dataSubsets){
			file += d.getModifierString();
			file += "€";
		}
		return file;
	}

	public void WriteModifier(){
		//the sample is in a zip, so unzip, make changes, and then zip again
		File tempModifierFileLocation = new File(savedSampleFolder.getParentFile().getPath());
		
		WriteModifierTo(tempModifierFileLocation.getPath());
		
		tempModifierFileLocation = new File(tempModifierFileLocation.getPath() + "/Modifier.txt");
		
		ZipFile originalZip = null;
		try {
			originalZip = new ZipFile(savedSampleFolder);
		} catch (ZipException e) {
			e.printStackTrace();
		}
		ZipParameters parameters = new ZipParameters();
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE); // set compression method to deflate compression
		
		// Set the compression level.
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		
		parameters.setRootFolderInZip("Data/" + getName() + "/");
		try {
			originalZip.removeFile("Data/" + getName() + "/Modifier.txt");
			originalZip.addFile(tempModifierFileLocation, parameters);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		tempModifierFileLocation.delete();
		
	}
	
	public void readModifier(String modifierString){
		String[] zones = modifierString.split("€");
		int atDataSet = 0;
		//TODO: Should have column labels for this to make sure.. relying on order
		for(String s : zones){
			if(s.split(SPSettings.lineSeperator).length < 1)
				continue;
			dataSubsets.get(atDataSet).readModifier(s);
			atDataSet++;
		}
	}
	
	public String getName(){
		return originalLocation.getName().substring(0, originalLocation.getName().length() - 4);
	}
	
	@Override
	public String toString(){
		return getName();
	}

}
