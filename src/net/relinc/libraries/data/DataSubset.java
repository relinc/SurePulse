package net.relinc.libraries.data;

import com.google.gson.Gson;

import net.relinc.libraries.application.FitableDataset;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.data.ModifierFolder.ModifierListWrapper;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public abstract class DataSubset {
	private int begin;
	private int end;
	private Integer beginTemp; //set in viewer
	private Integer endTemp; //set in viewer
	public String name = "";
	public Dataset Data;
	public DataFileInfo fileInfo;
	public ModifierListWrapper modifiers;
	
	public int getBegin(){
		return beginTemp == null ? begin : beginTemp;
	}
	public int getEnd() {
		return endTemp == null ? end : endTemp;
	}
	public void setBegin(int b){// throws Exception{
		if(b >= 0 && b < end)
			begin = b;
		else
			System.out.println("Failed to set begin to: " + b);
		//throw new Exception("Begin cannot be set to anything outside >0 and less than end");
	}
	
	public void setBeginTemp(Integer b){
		if(b == null){
			beginTemp = null;
			return;
		}
		if(b >= 0 && b >= begin && b < getEnd())
			beginTemp = b;
	}
	
	public void setEnd(int a){// throws Exception{
		if(a > getBegin() && a < Data.data.length)
			end = a;
		else
			System.out.println("Failed to set end to: " + a);
		//throw new Exception("End cannot be set to anything outside < data.length and greater than begin");
	}
	
	public void setEndTemp(Integer e){
		if(e == null){
			endTemp = null;
			return;
		}
		if(e > getBegin() && e < end && e < Data.data.length)
			endTemp = e;
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
	
	public int getIndexFromTimeValue(double timeValue){
		int index = 0;
		for(int i = 0; i < Data.timeData.length; i++){
			if(Data.timeData[i] > timeValue){
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
	
	public double getDurationTrimmed(){
		return Data.timeData[getEnd()] - Data.timeData[getBegin()];
	}
	
	public double[] getTrimmedTime() {
		double[] time = new double[getEnd() - getBegin() + 1];
		for(int i = 0; i < time.length; i++){
			time[i] = Data.timeData[i + getBegin()] - Data.timeData[getBegin()];
		}
		return time;
	}
	
	public double[] getModifiedData(){
		double[] fullData = Data.data.clone();
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
		
//		if(this instanceof HopkinsonBarPulse){
//			if(modifiers.getPochammerModifier().activated){
//				data = ((HopkinsonBarPulse)this).getPochammerAdjustedArray()
//			}
//		}
		
		return data;
	}
	
	abstract public double[] getUsefulTrimmedData();
		
	@Override
	public String toString() {
		return name;
	}

}
