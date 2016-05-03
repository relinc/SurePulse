package net.relinc.processor.data;

import java.util.ArrayList;

import net.relinc.processor.data.DataInterpreter.dataType;

public class DataSubsetListWrapper extends ArrayList<DataSubset> {

	//this class is a wrapper for the list of datasets that will be in the sample object.
	//public List<DataSubset> datasets = new ArrayList<DataSubset>();
	
	
	public int countDataType(dataType type) {
		int count = 0;
		if(type == dataType.FORCE){
			for(DataSubset sub : this){
				if(sub instanceof Force)
					count++;
			}
		}
		else if(type == dataType.ENGINEERINGSTRAIN){
			for(DataSubset sub : this){
				if(sub instanceof EngineeringStrain)
					count++;
			}
		}
		else if(type == dataType.TRUESTRAIN){
			for(DataSubset sub : this){
				if(sub instanceof TrueStrain)
					count++;
			}
		}
		else if(type == dataType.LOADCELL){
			for(DataSubset sub : this){
				if(sub instanceof LoadCell)
					count++;
			}
		}
		else if(type == dataType.INCIDENTSG){
			for(DataSubset sub : this){
				if(sub instanceof IncidentPulse)
					count++;
			}
		}
		else if(type == dataType.TRANSMISSIONSG){
			for(DataSubset sub : this){
				if(sub instanceof TransmissionPulse)
					count++;
			}
		}
		else if(type == dataType.DISPLACEMENT){
			for(DataSubset sub : this){
				if(sub instanceof Displacement)
					count++;
			}
		}
		else {
			System.out.println("Failed to find the type of dataset in sample");
		}
		return count;
	}
}
