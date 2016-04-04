package net.relinc.libraries.data;

import javafx.scene.chart.PieChart.Data;
import net.relinc.libraries.application.*;
import net.relinc.libraries.staticClasses.*;

public class DataInterpreter {
	public int column;
	public dataType DataType;
	public double multiplier = 1;
	
	public String name = "";
	
	private String nameDescrip = "Name";
	private String columnDescrip = "Column";
	private String dataTypeDescrip = "DataType";
	private String multiplierDescrip = "Multiplier";
	private String strainGaugeDescrip = "Strain Gauge";
	
	public StrainGaugeOnBar strainGauge;
	public String strainGaugeName;
	
	
	public enum dataType{
		TIME, ENGINEERINGSTRAIN, TRUESTRAIN, FORCE, INCIDENTSG, TRANSMISSIONSG, LOADCELL, DISPLACEMENT, INCIDENTBARSTRAIN, TRANSMISSIONBARSTRAIN, LAGRANGIANSTRAIN, NULL;
	}
	
	
	public String getStringForFile(){
		String file = "";
		file += columnDescrip + ":" + column + SPSettings.lineSeperator;
		file += dataTypeDescrip + ":" + DataType + SPSettings.lineSeperator;
		file += multiplierDescrip + ":" + multiplier + SPSettings.lineSeperator;
		file += nameDescrip + ":" + name + SPSettings.lineSeperator;
		if(strainGauge != null)
			file += strainGaugeDescrip + ":" + strainGauge.getNameForFile() + SPSettings.lineSeperator;
		else
			System.out.println("Strain gauge is null");
		return file;
	}
	
	public void setParametersFromString(String file, BarSetup bar){
		String[] lines = file.split(SPSettings.lineSeperator);
		for(String line : lines){
			setParameterFromLine(line, bar);
		}
	}

	private void setParameterFromLine(String line, BarSetup bar) {
		if(line.split(":").length < 2)
			return;
		String des = line.split(":")[0];
		String val = line.split(":")[1];
		
		if(des.equals(columnDescrip))
			column = Integer.parseInt(val);
		if(des.equals(dataTypeDescrip)){
			if(val.equals("null"))
				DataType = null;
			else
				DataType = dataType.valueOf(val);
		}
		if(des.equals(multiplierDescrip))
			multiplier = Double.parseDouble(val);
		if(des.equals(strainGaugeDescrip)){
			strainGaugeName = val;
			if(DataType == dataType.INCIDENTSG && bar != null)
				strainGauge = bar.IncidentBar.getStrainGauge(val);
			else if(DataType == dataType.TRANSMISSIONSG && bar != null)
				strainGauge = bar.TransmissionBar.getStrainGauge(val);
			else if(DataType == dataType.INCIDENTBARSTRAIN && bar != null)
				strainGauge = bar.IncidentBar.getStrainGauge(val);
			else if(DataType == dataType.TRANSMISSIONBARSTRAIN && bar != null)
				strainGauge = bar.TransmissionBar.getStrainGauge(val);
		}
		if(des.equals(nameDescrip))
			name = val;
	}

	

//	public Dataset getDataSet(RawDataset rawData) throws Exception{
//		if(DataType == null)
//			throw new Exception("Cannot create a dataset if the interpreter dataType is null.");
//		if(DataType == dataType.ENGINEERINGSTRAIN){
//			
//		}
//		
//	}
}
