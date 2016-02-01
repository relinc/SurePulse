package net.relinc.libraries.data;

import java.util.ArrayList;
import java.util.List;

import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.application.*;
import net.relinc.libraries.staticClasses.*;


public class DataFileInterpreter {

	public String firstSplitter = SPSettings.lineSeperator;
	public String secondSplitter = "\t";
	double collectionRate = -1;
	int startLine = 0;
	
	public String firstSplitterDescrip = "First Splitter";
	public String secondSplitterDescrip = "Second Splitter";
	public String numberOfColumnsDescrip = "Number Of Columns";
	public String collectionRateDescrip = "Collection Rate";
	public String startLineDescrip = "Start Line";
	
	public BarSetup barSetup;
	
	
	public List<DataInterpreter> interpreters = new ArrayList<DataInterpreter>();
	
	public DataFileInterpreter(String path, BarSetup bar) {
		String file = SPOperations.readStringFromFile(path);
		barSetup = bar;
		try {
			setParametersFromString(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public DataFileInterpreter() {
		// TODO Auto-generated constructor stub
	}

	public void writeToFile(String path){
		String file = getStringForFile();
		//System.out.println("Writing string: " + file);
		SPOperations.writeStringToFile(file, path);
	}

	private String getStringForFile() {
		String file = "Data File Interpreter Version:1" + SPSettings.lineSeperator;
		file += firstSplitterDescrip + ":" + "NewLine" + SPSettings.lineSeperator;
		file += secondSplitterDescrip + ":" + "Tab" + SPSettings.lineSeperator;
		if(collectionRate != -1){
			file += collectionRateDescrip + ":" + collectionRate + SPSettings.lineSeperator;
		}
		
		for(DataInterpreter d : interpreters){
			file += "€";
			if(d != null)
				file += d.getStringForFile();
		}
		
		return file;
	}
	public void setParametersFromString(String file) throws Exception{
		String[] parts = file.split("€");
		String[] lines = parts[0].split(SPSettings.lineSeperator);
		for(String str : lines){
			setParameterFromLine(str);
		}
		for(int i = 1; i < parts.length; i++){
			//need to check if data interpreter is null first. add null if it's null.
			DataInterpreter d = new DataInterpreter();
			d.setParametersFromString(parts[i], barSetup);
//			if(d.DataType == dataType.NULL)
//				interpreters.add(null);
//			else
			interpreters.add(d);
		}
	}

	private void setParameterFromLine(String str) throws Exception {
		if(str.split(":").length < 2)
			return;
		String des = str.split(":")[0];
		String val = str.split(":")[1];
		if(des.equals(firstSplitterDescrip)){
			if(val.equals("NewLine")){
				firstSplitter = SPSettings.lineSeperator;
			}
			else{
				throw new Exception("Failed to set first splitter");
			}
		}
		else if(des.equals(secondSplitterDescrip)){
			if(val.equals("Tab")){
				secondSplitter = "\t";
			}
			else{
				throw new Exception("Failed to set second splitter");
			}
		}
		else if(des.equals(collectionRateDescrip))
			collectionRate = Double.parseDouble(val);
		else if(des.equals(startLineDescrip))
			startLine = Integer.parseInt(val);
		
			
	}

	public void setDefaultNames(DataFileListWrapper dataList) {
		String name = "";
		for (DataInterpreter d : interpreters) {
			if(d == null)
				continue;
			if (d.DataType == dataType.FORCE) {
				name = "Force";
				int count = countDataType(DataInterpreter.dataType.FORCE) - 1;
				count += dataList.countDataType(dataType.FORCE);
				if (count > 0) {
					// a force already exists, propose incremented name
					name = name + " #" + (count + 1);
				}
				d.name = name;
			} else if (d.DataType == dataType.ENGINEERINGSTRAIN) {
				name = "Engineering Strain";
				int count = countDataType(dataType.ENGINEERINGSTRAIN) - 1;
				count += dataList.countDataType(dataType.ENGINEERINGSTRAIN);
				if (count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			} else if (d.DataType == dataType.TRUESTRAIN) {
				name = "True Strain";
				int count = countDataType(dataType.TRUESTRAIN) - 1;
				count += dataList.countDataType(dataType.TRUESTRAIN);
				if (count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			} else if (d.DataType == dataType.LOADCELL) {
				name = "Load Cell";
				int count = countDataType(dataType.LOADCELL) - 1;
				count += dataList.countDataType(dataType.LOADCELL);
				if (count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			} else if (d.DataType == dataType.INCIDENTSG) {
				name = "Incident Strain Gauge"; // maybe load up specific name
												// of strain gauge?
				int count = countDataType(dataType.INCIDENTSG) - 1;
				count += dataList.countDataType(dataType.INCIDENTSG);
				if (count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			} else if (d.DataType == dataType.TRANSMISSIONSG) {
				name = "Transmission Strain Gauge"; // again
				int count = countDataType(dataType.TRANSMISSIONSG) - 1;
				count += dataList.countDataType(dataType.TRANSMISSIONSG);
				if (count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			}
			else if(d.DataType == dataType.DISPLACEMENT){
				name = "Displacement";
				int count = countDataType(dataType.DISPLACEMENT) - 1;
				count += dataList.countDataType(dataType.DISPLACEMENT);
				if(count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			}
			else if(d.DataType == dataType.INCIDENTBARSTRAIN){
				name = "Incident Bar Strain";
				int count = countDataType(dataType.INCIDENTBARSTRAIN) - 1;
				count += dataList.countDataType(dataType.INCIDENTBARSTRAIN);
				if(count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			}
			else if(d.DataType == dataType.TRANSMISSIONBARSTRAIN){
				name = "Transmission Bar Strain";
				int count = countDataType(dataType.TRANSMISSIONBARSTRAIN) - 1;
				count += dataList.countDataType(dataType.TRANSMISSIONBARSTRAIN);
				if(count > 0)
					name = name + " #" + (count + 1);
				d.name = name;
			}
		}
	}

	public int countDataType(dataType type) {
		int count = 0;
		for(DataInterpreter d : interpreters){
			if(d != null && d.DataType == type)
				count++;
		}
		return count;
		}
			
	
}
