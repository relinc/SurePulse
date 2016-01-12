package net.relinc.processor.data.ModifierFolder;

import java.util.List;

import com.sun.javafx.binding.StringFormatter;

import javafx.scene.Node;
import net.relinc.processor.data.DataSubset;

public abstract class Modifier {
	public ModifierEnum modifierEnum;
	public boolean activated = false;
	
	public Modifier() {
	}
	
	public enum ModifierEnum {
		LOWPASS, POCHAMMER, ZERO;
	}
	
	public static Modifier getNewModifier(ModifierEnum en){
		switch(en){
		case LOWPASS:
			return new LowPass();
		case POCHAMMER:
			return new PochammerChreeDispersion();
		case ZERO:
			return new ZeroOffset();
		default:
			return null;
		}
	}
	
	@Override
	public abstract String toString();

	public abstract double[] applyModifierToData(double[] fullData, DataSubset activatedData);

	public void removeModifier(){
		activated = false;
	}
	
	public void activateModifier(){
		activated = true;
	}

	public abstract List<Node> getTrimDataHBoxControls();
	
	public abstract String getStringForFileWriting();

	public abstract void setValuesFromDescriptorValue(String descrip, String val);
	
	public void setValuesFromLine(String line){
		String[] split = line.split(":");
		if(split.length < 2)
			return;
		setValuesFromDescriptorValue(split[0], split[1]);
	}

	public abstract void configureModifier(DataSubset dataSubset); //configure modifier vals from UI controls.
	
	
	// {
//		switch(mod){
//		case LOWPASS:
//			activatedData.filter.lowPass = -1;
//			break;
//		case POCHAMMER:
//			activatedData.pochammerActivated = false;
//			break;
//		case ZERO:
//			activatedData.zeroActivated = false;
//			break;
//		}
//	}
}
