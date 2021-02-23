package net.relinc.libraries.data;

import java.io.File;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javafx.stage.Stage;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPSettings;

public class DataModel {

	File dataFile;
	Stage stage;
	String frameDelimiter = SPSettings.lineSeperator; //not implemented. //TODO
	public String dataTypeDelimiter = "\t";
	public int startFrameSplitter = 0;
	public int startDataSplitter = 0;
	//public NewDataFileController controller;
	public List<RawDataset> rawDataSets = new ArrayList<RawDataset>();
	private double collectionRate = -1;
	//public int TimeDataSetIndex = -1;
	public File currentFile;
	public List<String> lines = null;
	public List<String> origLines = null;
	private List<Integer> removedIndices = new ArrayList<Integer>();


	public DataModel() {
		// TODO Auto-generated constructor stub
	}

	public String readDataFromFile(Path file) throws IOException {
		// populate dataset arrays.
		rawDataSets = new ArrayList<RawDataset>();
		origLines = null;
		try{
			origLines = Files.readAllLines(file);
		}
		catch(Exception e){
			//it's a European file
			origLines = Files.readAllLines(file, Charset.forName("ISO-8859-1"));
		}

		//some files have alternating blank lines!
		List<String> withoutBlanks = new ArrayList<String>();
		for(int i = 0; i < origLines.size(); i++){
			String line = origLines.get(i);
			if(line.matches((".*\\d+.*"))){ //This matches with anything with one or more digits [0-9]
				withoutBlanks.add(line);
			}
			else{
				removedIndices.add(i);
			}
		}
		lines = withoutBlanks;
		
		boolean converts = false;
		
		//first, find where legitimate data splitting occurs. IE second column of data when split on \t
		//look in the middle of the data.
		List<String> delimiters = new ArrayList<>();
		delimiters.add("\t");
		delimiters.add(",");
		delimiters.add(" ");
		delimiters.add(";");
		delimiters.add("~");
		int delimeterIndex = 0;
		
		int lookAtFrame = lines.size() / 2;
		while(!converts){
			if(lines.get(lookAtFrame).split(dataTypeDelimiter).length <= startDataSplitter){
				//the data type splitter is wrong, change to comma, semicolon etc and try again.
				converts = false;
				startDataSplitter = 0;
				if(delimeterIndex < delimiters.size())
					dataTypeDelimiter = delimiters.get(delimeterIndex);
				else
					return "Failed to determine dataTypeDelimiter, please inspect input file!";
				delimeterIndex++;
				dataTypeDelimiter = ",";
				continue;
			}
			try {
				Double.parseDouble(lines.get(lookAtFrame).split(dataTypeDelimiter)[startDataSplitter]);
			} catch (Exception e) {
				startDataSplitter++;
				continue;
			}
			if (lines.get(lookAtFrame).split(dataTypeDelimiter)[startDataSplitter].equals("")) {
				startDataSplitter++;
			} else
				converts = true;
		}
		converts = false;
		//then find where legit frame splitting occurs. ie third line splitting on \n
		while (!converts) {
			try {
				Double.parseDouble(lines.get(startFrameSplitter).split(dataTypeDelimiter)[startDataSplitter]);
			} catch (Exception e) {
				startFrameSplitter++;
				continue;
			}
			if (lines.get(startFrameSplitter).split(dataTypeDelimiter)[startDataSplitter].equals("")) {
				startFrameSplitter++;
			} else
				converts = true;
		}
		//startDataSplitterOrigFile = origLines.indexOf(lines.get(0)) + startFrameSplitter;
		
		int numDataPoints = lines.size() - startFrameSplitter;
		int numDataSets = lines.get(0).split(dataTypeDelimiter).length - startDataSplitter;
		if(lines.size() > 10)//this happens most of the time
			numDataSets = lines.get(10).split(dataTypeDelimiter).length - startDataSplitter;
		// initialize data double arrays
		for (int i = 0; i < numDataSets; i++) {
			rawDataSets.add(new RawDataset(new double[numDataPoints]));
		}
		for (int j = 0; j < numDataPoints; j++) {
			if(lines.get(j).split(dataTypeDelimiter).length != numDataSets) {
				Dialogs.showErrorDialog("Ragged Data: All rows must have equal columns!", stage);
			}
			for (int i = 0; i < numDataSets; i++) {
				String num = lines.get(j + startFrameSplitter).split(dataTypeDelimiter)[i + startDataSplitter];
				
				//I have no clue why this is in a try-catch block.
				try{
					if(num.equals("-Infinity") || num.equals("Infinity"))
						rawDataSets.get(i).data[j] = 0;
					//rawDataSets.get(i).data[j] = Double.parseDouble(num);
					rawDataSets.get(i).data[j] = Double.parseDouble(num);
				}
				catch(Exception e){
					// Dialogs.showErrorDialog("Error", );
					return "Failed to parse number at row index " + j + " and column index " + i + ". Failed to parse \"" + num + "\" to a number.";
				}
				
//				if(!(num.equals("-Infinity") || num.equals("Infinity")))
//					rawDataSets.get(i).data[j] = Double.parseDouble(num);
//				else 
//					rawDataSets.get(i).data[j] = 0;
				
			}
		}
		return "";
	}

	public void createTimeData(double FrameRate) throws Exception{
		if(this.hasTimeData())
			throw new Exception("Cannot create time data from frame rate if time data already exists");
		if(rawDataSets.size() == 0)
			throw new Exception("Cannot create data array from frame rate if no other data exists");
		
		double[] time = new double[rawDataSets.get(0).data.length];
		double atTime = 0;
		for(int i = 0; i < time.length; i++){
			time[i] = atTime;
			atTime += 1 / FrameRate;
		}
		RawDataset timeDataSet = new RawDataset(time);
		timeDataSet.interpreter.DataType = dataType.TIME;
		rawDataSets.add(timeDataSet);
	}

	public void setCollectionRate(double d){
		collectionRate = d;
	}
	
	public double getCollectionRate(){
		return collectionRate;
	}
	
	public boolean hasTimeData() {
		return timeRawDatasets() == 1 || collectionRate != -1;
	}
	
	private int timeRawDatasets(){
		int count = 0;
		for(RawDataset set : rawDataSets){
			if(set.interpreter == null)
				continue;
			if(set.interpreter.DataType != null && set.interpreter.DataType == dataType.TIME)
				count++;
		}
		return count;
	}
	
	public double[] getTimeData() throws Exception{
		if(!hasTimeData())
			throw new Exception("Cannot get time data. Doesn't exist!.");
		if(collectionRate > 0){
			double[] time = new double[rawDataSets.get(0).data.length];
			double atTime = 0;
			for(int i = 0; i < time.length; i++){
				time[i] = atTime;
				atTime += 1 / collectionRate;
			}
			return time;
		}
		
		for(RawDataset set : rawDataSets){
			if(set.interpreter.DataType != null && set.interpreter.DataType == dataType.TIME){
				for(int i = 0; i < set.data.length; i++){
					set.data[i] = set.data[i] / set.interpreter.multiplier;
				}
				return set.data;
			}
		}
		
		throw new Exception("Somehow failed to find the time data.");
	}


	public void writeToPath(String path) {
		DataFileInterpreter fileInterpreter = new DataFileInterpreter();
		rawDataSets.stream().forEach(data -> fileInterpreter.interpreters.add(data.interpreter));
		fileInterpreter.collectionRate = collectionRate;
		fileInterpreter.writeToFile(path);
	}

	public boolean interpreterIsCompatible(DataFileInterpreter d, BarSetup barSetup) {
		if(barSetup != null && barSetup.IncidentBar != null && barSetup.TransmissionBar != null && (barSetup.IncidentBar.strainGauges.size() > 1 ||
				barSetup.TransmissionBar.strainGauges.size() > 1))
				return false; //no interpreters for multiple strain gauges
		
		// Do we need to have a strain gauge for bar strain??
		if(d.countDataType(dataType.INCIDENTSG) + d.countDataType(dataType.INCIDENTBARSTRAIN) >= 1 && (barSetup == null || 
				barSetup.IncidentBar == null || barSetup.IncidentBar.strainGauges.size() < 1))
			return false; //no strain gauge on bar setup
		if(d.countDataType(dataType.TRANSMISSIONSG) + d.countDataType(dataType.TRANSMISSIONBARSTRAIN) >= 1 && 
				(barSetup == null || barSetup.TransmissionBar == null || barSetup.TransmissionBar.strainGauges.size() < 1))
			return false;
		
		if(rawDataSets.size() == d.interpreters.size() || (rawDataSets.size() == d.interpreters.size() - 1 && d.collectionRate != -1))
			return true;
		return false;
	}

	public void applyDataInterpreter(DataFileInterpreter d) {
		for(int i = 0; i < d.interpreters.size(); i++){
			rawDataSets.get(i).interpreter = d.interpreters.get(i);
		}
		collectionRate = d.collectionRate;
	}

	public int countDataType(dataType type) {
		int count = 0;
		for(RawDataset set : rawDataSets){
			if(set.interpreter.DataType != null && set.interpreter.DataType == type)
				count++;
		}
		return count;
	}

	public DataFile exportToDataFile(boolean copyToTempData, boolean calibrationMode) throws Exception {
		double[] time = null;
		try {
			time = getTimeData();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		DataSubsetListWrapper setsToAdd = new DataSubsetListWrapper();
		for (RawDataset r : rawDataSets) {
			try {
				if(r.interpreter == null || r.interpreter.DataType == null || r.interpreter.DataType == dataType.NULL)
					continue;
				
				if (!(r.interpreter.DataType == dataType.TIME))
				{
					if(!calibrationMode)
						setsToAdd.addAll(r.extractDataset(time));
					else
						setsToAdd.add(r.extractDataset(time).get(0)); //why is this? Becuase only want incident pulse. Not incident and reflected.
				}
				// existingSampleData.datasets.addAll(r.extractDataset(time));
			} catch (Exception e) {
				System.out.println(r.interpreter.name);
				System.out.println(r.interpreter.DataType);
				e.printStackTrace();
			}
		}
		
		DataFileInfo dataInfo = new DataFileInfo();
		String fileNameWithoutExtension = currentFile.getName().substring(0, currentFile.getName().length() - 4);
		File upperTempFolder = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/TempSampleData");
		File tempFolder = new File(
				SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/TempSampleData/" + fileNameWithoutExtension);
		
		upperTempFolder.mkdir();
		tempFolder.mkdir();
		if(copyToTempData){
			Path dest = tempFolder.toPath().resolve(currentFile.getName());
			File destFile = dest.toFile();
			if(destFile.exists())
				destFile.delete();
			Files.copy(currentFile.toPath(), dest);//this works, copies raw data
		}
		if(copyToTempData)
			writeToPath(tempFolder.getPath() + "/Interpreter.txt");//this doesn't have a sg so doesn't write correctly
		dataInfo.tempDataFolder = tempFolder;
		for (DataSubset d : setsToAdd) {
			d.fileInfo = dataInfo; // this sucks.
		}

		DataFile data = new DataFile();
		data.originalLocation = currentFile;
		data.tempDataFolder = tempFolder;

		DataSubsetListWrapper listOfDataSets = new DataSubsetListWrapper();
		listOfDataSets = setsToAdd;

		data.dataSubsets = listOfDataSets;
		
		if(copyToTempData)
			data.WriteModifierTo(tempFolder.getPath());

		return data;

		}

}
