package net.relinc.libraries.sample;

import net.relinc.libraries.data.DescriptorDictionary;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;

public class LoadDisplacementSample extends Sample {

	@Override
	public String getSpecificString() {
		return "";
	}

	@Override
	public void setSpecificParameters(String des, String val) {
		//none yet
	}

	@Override
	public String getParametersForPopover(boolean metric) {
		String des = "";
		if(metric){
			des += "Length: " + SPOperations.round(Converter.mmFromM(length), 3) + " mm\n";
		}
		else{
			des += "Length: " + SPOperations.round(Converter.InchFromMeter(length), 3) + " in\n";
		}
		
		return des + getCommonParametersForPopover(metric);
	}

	@Override
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		//no additional parameters
		return i;
	}

}
