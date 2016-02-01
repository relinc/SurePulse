package net.relinc.libraries.sample;

import net.relinc.libraries.data.DescriptorDictionary;

public class LoadDisplacementSample extends Sample {

//	@Override
//	public double getLength(){
//		return 0;
//	}
	
	@Override
	public String getSpecificString() {
		return "";
	}

	@Override
	public void setSpecificParameters(String des, String val) {
		//none yet
	}

//	@Override
//	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain) {
//		//no cross sectional area
//		return null;
//	}

//	@Override
//	public double getInitialCrossSectionalArea(){
//		System.out.println("This should never get called.");
//		return 0.0;
//	}

	@Override
	public DescriptorDictionary createAllParametersDecriptorDictionary() {
		DescriptorDictionary d = descriptorDictionary;
		addCommonRequiredSampleParametersToDescriptionDictionary(d);
		return d;
	}
	
//	@Override 
//	public double getHopkinsonBarTransmissionPulseSign(){
//		System.out.println("This should never get called");
//		return 0.0;
//	}
//	
//	@Override 
//	public double getHopkinsonBarReflectedPulseSign(){
//		System.out.println("This should never get called");
//		return 0.0;
//	}

}
