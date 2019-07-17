package net.relinc.libraries.data;

import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.data.ModifierFolder.ModifierListWrapper;
import net.relinc.libraries.data.ModifierFolder.ModifierResult;
import net.relinc.libraries.data.ModifierFolder.Reducer;
import net.relinc.libraries.staticClasses.SPSettings;

import java.util.Optional;

public abstract class DataSubset {
	// I need to keep track of the original begin/end so I can save them to disk... then just use getBegin() and getEnd() to scale to sampled data
	private int begin; // always refers to the original data... then they need to get scaled as necessary..
	private int end;
	private Integer beginTemp; //set in viewer
	private Integer endTemp; //set in viewer

	private Optional<ModifierResult> modifierResult = Optional.empty();

	public String name = "";
	private Dataset Data;
	public DataFileInfo fileInfo;
	private ModifierListWrapper modifiers;
	// This gets defaulted to the original amount of data points. The user may choose to make it higher or lower, in which case it is interpolated.
	
	public abstract baseDataType getBaseDataType();
	
	public abstract String getUnitAbbreviation(); // Each DataSubset has its standard units. e.g. Force=N
	public abstract String getUnitName(); // e.g. Force = Newtons

	public ModifierResult getModifierResult() {
		if(modifierResult.isPresent()) {
			return modifierResult.get();
		}
		this.render();
		if(modifierResult.isPresent()) {
			return modifierResult.get();
		} else {
			throw new RuntimeException("Expected modifier result to be rendered!");
		}
	}

	public ModifierListWrapper getModifiers() {
		// they might change a modifier, so we have to invalidate the result.
		this.modifierResult = Optional.empty();
		return this.modifiers;
	}
	
	public void reduceDataNonReversible(int pointsToKeep) {
		Reducer reducer = this.getModifiers().getReducerModifier();
		reducer.setUserDataPoints(pointsToKeep);
		reducer.enabled.set(true);
		reducer.activateModifier();
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
		return beginTemp == null ? this.originalIndexToUserIndex(begin) : this.originalIndexToUserIndex(beginTemp);
	}
	public double getBeginTime(){
		return getTimeValueFromIndex(getBegin());
	}
	public Integer getBeginTemp()
	{
		if(beginTemp == null) {
			return beginTemp;
		}
		return this.originalIndexToUserIndex(beginTemp);
	}
	
	public int getEnd() {
		return endTemp == null ? this.originalIndexToUserIndex(end) : this.originalIndexToUserIndex(endTemp);
	}
	public double getEndTime(){
		return getTimeValueFromIndex(getEnd());
	}

	public Integer getEndTemp()
	{
		if (endTemp == null) {
			return endTemp;
		}
		return this.originalIndexToUserIndex(endTemp);
	}
	
	public void setBegin(int b){
		int origIndex = this.userIndexToOriginalIndex(b); // TODO: Should this always round down?
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
		int origIndex = this.userIndexToOriginalIndex(b);

		if(origIndex >= 0 &&  origIndex < end)
			beginTemp = origIndex;
	}
	
	public void setEnd(int a){
		int origIndex = this.userIndexToOriginalIndex(a); // TODO: Should this always round down?
		if(origIndex > begin && origIndex < Data.getOriginalDataPoints())
			end = origIndex;
		else
			System.out.println("Failed to set end to: " + origIndex);
	}

	private int userIndexToOriginalIndex(int userIndex) {
		// due to modifiers changing the density of data, specifically the Sampler modifier, we need to let them scale the userIndex
		// userIndex is the index of the data in relation to the modified data.
		// original indexes are always kept in this class
		return (int)(userIndex * (1.0 / getModifierResult().getUserIndexToOriginalIndexRatio()));
	}

	private int originalIndexToUserIndex(int originalIndex) {
		return (int)(originalIndex * getModifierResult().getUserIndexToOriginalIndexRatio());
	}
	
	public void setEndTemp(Integer e){
		if(e == null){
			endTemp = null;
			return;
		}

		int origIndex = this.userIndexToOriginalIndex(e);


		if(origIndex > begin && origIndex < Data.getOriginalDataPoints())
			endTemp = origIndex;
	}
	
    public DataSubset(double[] timed, double[] datad) {
		Data = new Dataset(timed, datad);
		modifiers = Modifier.getModifierList();
		this.render();
		setEnd(datad.length - 1);
		setBegin(0);

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
		return getModifiedTime()[idx];
	}
	public int getIndexFromTimeValue(double timeValue){
		int index = 0;
		for(int i = 0; i < getModifiedTime().length; i++){
			if(getModifiedTime()[i] > timeValue){
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
				getModifiers().setModifierFromLine(line);
			}
		}
	}
	
	public double[] getTrimmedTime() {

		double[] modifiedTimeData = getModifiedTime();
		double[] time = new double[getEnd() - getBegin() + 1];
		try{
			for(int i = 0; i < time.length; i++){
				time[i] = modifiedTimeData[i + getBegin()] - modifiedTimeData[getBegin()];
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return time;
	}

	public double[] getModifiedTime() {
		return getModifierResult().getX();
	}
	
	public double[] getModifiedData(){
		return getModifierResult().getY();
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
		return 1.0 / (getModifiedTime()[1] - getModifiedTime()[0]);
	}

	public void render() {
		ModifierResult result = new ModifierResult(this.Data.getTimeData(), this.Data.getData(), 1.0);
		for(Modifier m: this.modifiers) {
			ModifierResult resultStep = m.applyModifier(result.getX(), result.getY(), this);
			result = new ModifierResult(
					resultStep.getX(),
					resultStep.getY(),
					result.getUserIndexToOriginalIndexRatio() * resultStep.getUserIndexToOriginalIndexRatio());
		}

		this.modifierResult = Optional.of(result);
	}

}
