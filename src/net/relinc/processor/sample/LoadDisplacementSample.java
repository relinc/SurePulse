package net.relinc.processor.sample;

import net.relinc.processor.data.DescriptorDictionary;

public class LoadDisplacementSample extends Sample {

	@Override
	public double getLength(){
		return 0;
	}
	
	@Override
	public String getSpecificString() {
		return "";
	}

	@Override
	public void setSpecificParameters(String des, String val) {
		//none yet
	}

	@Override
	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain) {
		//no cross sectional area
		return null;
	}

	@Override
	public double[] getEngineeringStressFromForce(double[] force) {
		//no cross sectional area
		return null;
	}

	@Override
	public DescriptorDictionary createAllParametersDecriptorDictionary() {
		DescriptorDictionary d = descriptorDictionary;
		int lastIndex = addCommonRequiredSampleParametersToDescriptionDictionary(d);
		return d;
	}

}
