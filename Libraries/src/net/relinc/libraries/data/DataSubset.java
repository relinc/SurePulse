package net.relinc.libraries.data;

import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.data.ModifierFolder.ModifierListWrapper;
import net.relinc.libraries.staticClasses.SPSettings;

public abstract class DataSubset {
	// I need to keep track of the original begin/end so I can save them to disk... then just use getBegin() and getEnd() to scale to sampled data
	private int begin; // always refers to the original data... then they need to get scaled as necessary..
	private int end;
	private Integer beginTemp; //set in viewer
	private Integer endTemp; //set in viewer
	public String name = "";
	public Dataset Data;
	public DataFileInfo fileInfo;
	public ModifierListWrapper modifiers;
	// This gets defaulted to the original amount of data points. The user may choose to make it higher or lower, in which case it is interpolated.
	
	public abstract baseDataType getBaseDataType();
	
	public abstract String getUnitAbbreviation(); // Each DataSubset has its standard units. e.g. Force=N
	public abstract String getUnitName(); // e.g. Force = Newtons
	
	public void reduceDataNonReversible(int pointsToKeep) {
		this.Data.setUserDataPoints(pointsToKeep);
	}
	
	public void reduceDataNonReversibleByFrequency(double frequency) {
		double reductionFactor = this.getFrequency() / frequency;
		int pointsToKeep = this.Data.getData().length / (int)reductionFactor;
		reduceDataNonReversible(pointsToKeep);
	}
	
	public enum baseDataType{
		LOAD, DISPLACEMENT, TIME;
	}
	
	public int getBegin(){
		return beginTemp == null ? Data.originalIndexToUserIndex(begin) : Data.originalIndexToUserIndex(beginTemp);
	}
	public double getBeginTime(){
		return getTimeValueFromIndex(getBegin());
	}
	public Integer getBeginTemp()
	{
		if(beginTemp == null) {
			return beginTemp;
		}
		return Data.originalIndexToUserIndex(beginTemp);
	}
	
	public int getEnd() {
		return endTemp == null ? Data.originalIndexToUserIndex(end) : Data.originalIndexToUserIndex(endTemp);
	}
	public double getEndTime(){
		return getTimeValueFromIndex(getEnd());
	}

	public Integer getEndTemp()
	{
		if (endTemp == null) {
			return endTemp;
		}
		return Data.originalIndexToUserIndex(endTemp);
	}
	
	public void setBegin(int b){
		int origIndex = Data.userIndexToOriginalIndex(b); // TODO: Should this always round down?
		if(origIndex >= 0 && origIndex < end)
			begin = origIndex;
		else
			System.out.println("Failed to set begin to: " + origIndex);
	}
	
	public void setBeginTemp(Integer b){
		if(b == null){
			beginTemp = null;
			return;
		}
		int origIndex = Data.userIndexToOriginalIndex(b);

		if(origIndex >= 0 &&  origIndex < end)
			beginTemp = origIndex;
	}
	
	public void setEnd(int a){
		int origIndex = Data.userIndexToOriginalIndex(a); // TODO: Should this always round down?
		if(origIndex > begin && origIndex < Data.getOriginalDataPoints())
			end = origIndex;
		else
			System.out.println("Failed to set end to: " + origIndex);
	}
	
	public void setEndTemp(Integer e){
		if(e == null){
			endTemp = null;
			return;
		}

		int origIndex = Data.userIndexToOriginalIndex(e);


		if(origIndex > begin && e < Data.getOriginalDataPoints())
			endTemp = origIndex;
	}
	
    public DataSubset(double[] timed, double[] datad) {
		Data = new Dataset(timed, datad);
		setEnd(datad.length - 1);
		setBegin(0);
		modifiers = Modifier.getModifierList();
	}
	
	public void setBeginFromTimeValue(double timeValue) {
		setBegin(getIndexFromTimeValue(timeValue));
	}
	public void setEndFromTimeValue(double timeValue) {
		setEnd(getIndexFromTimeValue(timeValue));
	}
	
	public void setBeginTempFromTimeValue(double timeValue){
		setBeginTemp(getIndexFromTimeValue(timeValue));
	}
	
	public void setEndTempFromTimeValue(double timeValue){
		setEndTemp(getIndexFromTimeValue(timeValue));
	}
	public double getTimeValueFromIndex(int idx){
		return Data.getTimeData()[idx];
	}
	public int getIndexFromTimeValue(double timeValue){
		int index = 0;
		for(int i = 0; i < Data.getTimeData().length; i++){
			if(Data.getTimeData()[i] > timeValue){
				index = i;
				break;
			}
		}
		return index;
	}

	public String getModifierString() {
		String modifier = "Begin:" + begin + SPSettings.lineSeperator; //only used in processor, where beginTemp is always null.
		modifier += "End:" + end + SPSettings.lineSeperator;
//		modifier += "Lowpass Filter:" + filter.lowPass + SPSettings.lineSeperator;
//		if(fitableDataset != null)
//			modifier += "FitableDataset:" + fitableDataset.getStringForFileWriting() + SPSettings.lineSeperator;
//		modifier += zeroDescriptor + ":" + zero + SPSettings.lineSeperator;
		for(Modifier m : modifiers)
			modifier += m.getStringForFileWriting();
		return  modifier;
	}
	public void readModifier(String s) {
		String[] lines = s.split(SPSettings.lineSeperator);
		for(String line : lines){
			if(line.split(":").length < 2)
				continue;
			String description = line.split(":")[0];
			String value = line.substring(description.length() + 1);//line.split(":")[1];
			if(description.equals("Begin"))
				setBegin(Integer.parseInt(value));
			else if(description.equals("End"))
				setEnd(Integer.parseInt(value));
			else{
				//read the modifier
				modifiers.setModifierFromLine(line);
			}
		}
	}
	
	public double[] getTrimmedTime() {
		double[] time = new double[getEnd() - getBegin() + 1];
		for(int i = 0; i < time.length; i++){
			time[i] = Data.getTimeData()[i + getBegin()] - Data.getTimeData()[getBegin()];
		}
		return time;
	}
	
	public double[] getModifiedData(){
		double[] fullData = Data.getData().clone();
		for(Modifier m : modifiers){
			fullData = m.applyModifierToData(fullData, this);
		}
		return fullData;
	}

	public double[] getTrimmedData(){
		double[] fullData = getModifiedData();

		double[] data = new double[getEnd() - getBegin() + 1];
		for(int i = 0; i < data.length; i++){
			data[i] = fullData[i + getBegin()];
		}
		
		return data;
	}
	
	abstract public double[] getUsefulTrimmedData();
		
	@Override
	public String toString() {
		return name;
	}
	
	public double getFrequency() {
		return 1.0 / (this.Data.getTimeData()[1] - this.Data.getTimeData()[0]);
	}

}
