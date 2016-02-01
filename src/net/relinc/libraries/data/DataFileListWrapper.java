package net.relinc.libraries.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sun.prism.PixelFormat.DataType;

import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.application.*;
import net.relinc.libraries.staticClasses.SPOperations;

public class DataFileListWrapper extends ArrayList<DataFile> {
	//public List<DataFile> dataFiles = new ArrayList<>();
	//From here you can iterate through the dataFiles and save a modifier .txt for each one.

	public List<TransmissionPulse> getTransmissionStrainGaugeDatasets(){
		ArrayList<TransmissionPulse> sgData = new ArrayList<TransmissionPulse>();
		for(DataFile D : this){
			for(DataSubset subset : D.dataSubsets){
				if(subset instanceof TransmissionPulse)
					sgData.add((TransmissionPulse)subset);
			}
		}
		return sgData;
	}
	
	public List<ReflectedPulse> getReflectedStrainGaugeDatasets(){
		ArrayList<ReflectedPulse> sgData = new ArrayList<ReflectedPulse>();
		for(DataFile D : this){
			for(DataSubset subset : D.dataSubsets){
				if(subset instanceof ReflectedPulse)
					sgData.add((ReflectedPulse)subset);
			}
		}
		return sgData;
	}
	
	public List<IncidentPulse> getIncidentStrainGaugeDatasets(){
		ArrayList<IncidentPulse> sgData = new ArrayList<IncidentPulse>();
		for(DataFile D : this){
			for(DataSubset subset : D.dataSubsets){
				if(subset instanceof IncidentPulse)
					sgData.add((IncidentPulse)subset);
			}
		}
		return sgData;
	}
	
	
	public List<DataSubset> getAllDatasets() {
		ArrayList<DataSubset> allData = new ArrayList<DataSubset>();
		for(DataFile D : this){
			allData.addAll(D.dataSubsets);
		}
		return allData;
	}
	public List<DataSubsetListWrapper> getAllDatasetListWrappers(){
		ArrayList<DataSubsetListWrapper> list = new ArrayList<>();
		for(DataFile D : this){
			list.add(D.dataSubsets);
		}
		return list;
	}
	public int countDataType(dataType type) {
		int c = 0;
		for(DataFile file : this){
			c += file.dataSubsets.countDataType(type);
		}
		return c;
	}

	public void addDataFile(File f, BarSetup bar) {
		// load a dataFile from file...Contains "Interpreter.txt",
		// "Modifier.txt" and then "rawData.txt/.csv"
		File interpreter = new File(f.getPath() + "/Interpreter.txt");
		File modifier = new File(f.getPath() + "/Modifier.txt");
		File rawData = new File("");
		for (File file : f.listFiles()) {
			if (!file.getName().equals("Interpreter.txt") && !file.getName().equals("Modifier.txt"))
				rawData = file;
		}

		DataModel model = new DataModel();
		
		try {
			model.readDataFromFile(rawData.toPath());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		model.currentFile = rawData;

		model.applyDataInterpreter(new DataFileInterpreter(interpreter.getPath(), bar));
		
		
		
		DataFile D;
		try {
			D = model.exportToDataFile(true);
			D.readModifier(SPOperations.readStringFromFile(modifier.getPath()));
			this.add(D);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	public boolean dataFileExists(String name){
		int pos = name.lastIndexOf(".");
		String justName = pos > 0 ? name.substring(0, pos) : name;
		return this.stream().filter(data -> data.tempDataFolder.getName().equals(justName)).count() != 0;
	}
}
