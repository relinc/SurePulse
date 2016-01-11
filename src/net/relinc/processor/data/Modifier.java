package net.relinc.processor.data;

public class Modifier {
	public ModifierEnum mod;
	
	public Modifier(ModifierEnum en){
		mod = en;
	}
	
	public enum ModifierEnum {
		LOWPASS, POCHAMMER, ZERO;
	}
	
	@Override
	public String toString(){
		//shows up in choice box
		switch(mod){
		case LOWPASS:
			return "Lowpass Filter";
		case POCHAMMER:
			return "Pochammer-Chree Dispersion";
		case ZERO:
			return "Zero";
		default:
			return "Not Implemented";
		}
	}

	public void applyModifier(DataSubset activatedData, double filterVal) {
		switch(mod){
		case LOWPASS:
			activatedData.filter.lowPass = filterVal * 1000;
			break;
		case POCHAMMER:
			activatedData.pochammerActivated = true;
			break;
		case ZERO:
			//find avg between begin and end, that's the zero.
			double sum = 0.0;
			for(int i = activatedData.getBegin(); i <= activatedData.getEnd(); i++)
				sum += activatedData.Data.data[i];
			double avg = sum / (activatedData.getEnd() - activatedData.getBegin() + 1);
			activatedData.zero = avg;
			activatedData.zeroActivated = true;
			break;
		}
	}

	public void removeModifier(DataSubset activatedData) {
		switch(mod){
		case LOWPASS:
			activatedData.filter.lowPass = -1;
			break;
		case POCHAMMER:
			activatedData.pochammerActivated = false;
			break;
		case ZERO:
			activatedData.zeroActivated = false;
			break;
		}
	}
}
