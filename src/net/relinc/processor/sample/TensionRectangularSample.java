package net.relinc.processor.sample;


import net.relinc.processor.staticClasses.SPSettings;


public class TensionRectangularSample extends Sample {
	
	private double width;
	private double height;
	private String widthDescriptor = "Width";
	private String heightDescriptor = "Height";
	
	public TensionRectangularSample() {
		//setSampleType("Tension Rectangular Sample");
	}
	
	
	public void setSpecificParameters(String des, String val) {
		if(des.equals(widthDescriptor))
			setWidth(Double.parseDouble(val));
		if(des.equals(heightDescriptor))
			setHeight(Double.parseDouble(val));
	}
	
	@Override
	public String getSpecificString() {
		String s = "";
		if(getWidth() > 0)
			s += widthDescriptor + delimiter + getWidth() + SPSettings.lineSeperator;
		if(getHeight() > 0)
			s += heightDescriptor + delimiter + getHeight() + SPSettings.lineSeperator;
		
		return s;
	}
	
	public double getWidth() {
		return width;
	}
	public void setWidth(double width) {
		this.width = width;
	}
	
	public double getHeight(){
		return height;
	}
	public void setHeight(double h){
		height = h;
	}
	
	@Override
	public double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain)  {
		//eng stress and strain must be equal length and time-matched. 
		double[] trueStress = new double[engStress.length];
		for(int i = 0; i < trueStress.length; i++){
			trueStress[i] = engStress[i] * (1 + engStrain[i]);
		}
		return trueStress;
	}
	
	public double getInitialCrossSectionalArea(){
		return width * height;
	}
	
	@Override
	public double[] getEngineeringStressFromForce(double[] force){
		double[] stressValues = new double[force.length];
		for(int i = 0; i < stressValues.length; i++){
			stressValues[i] = force[i] / getInitialCrossSectionalArea(); //method is above
		}
		return stressValues;
	}
	
	

}
