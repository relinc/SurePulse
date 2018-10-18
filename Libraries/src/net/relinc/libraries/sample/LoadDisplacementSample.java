package net.relinc.libraries.sample;

import net.relinc.libraries.data.DescriptorDictionary;
import org.json.simple.JSONObject;

public class LoadDisplacementSample extends Sample {

	public LoadDisplacementSample() {
		super();
	}

	@Override
	public JSONObject getSpecificJSON(){
		return new JSONObject();
	}

	@Override
	public void setSpecificParameters(String des, String val) {
		//none yet
	}

	@Override
	public void setSpecificParametersJSON(JSONObject jsonObject) {
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

	@Override
	public String getFileExtension() {
		return getSampleConstants().getExtension();
	}

	public static SampleConstants getSampleConstants() {
		return new SampleConstants(
				"Load Displacement",
				"Load Displacement Sample", 
				"/net/relinc/libraries/images/LD.png", 
				".samlds"
				);
	}
}
