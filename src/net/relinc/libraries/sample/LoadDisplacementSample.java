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
		
		return des + getCommonParametersForPopover(metric);
	}

	@Override
	public int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i) {
		//no additional parameters
		return i;
	}

}
