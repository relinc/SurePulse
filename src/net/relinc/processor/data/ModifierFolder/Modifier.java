package net.relinc.processor.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import com.sun.javafx.binding.StringFormatter;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.ModifierFolder.Modifier.ModifierEnum;

public abstract class Modifier {
	public ModifierEnum modifierEnum;
	public SimpleBooleanProperty activated = new SimpleBooleanProperty(false); //when activated, effects the data calculation.
	public SimpleBooleanProperty enabled = new SimpleBooleanProperty(false); //the user has seen the effects. It is possible to activate.
	public CheckBox checkBox;
	
	public Modifier() {
	}
	
	public enum ModifierEnum {
		LOWPASS, ZERO, POCHAMMER;
	}
	
	public static ModifierListWrapper getModifierList(){
		ModifierListWrapper list = new ModifierListWrapper();
		for(ModifierEnum en : ModifierEnum.values())
			list.add(Modifier.getNewModifier(en)); //initializes the modifier list with all modifiers.
		return list;
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
		activated.set(false);
	}
	
	public void activateModifier(){
		activated.set(true);
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

	public ObservableList<Node> getViewerControls() {
		ObservableList<Node> list = FXCollections.observableArrayList();
		list.add(checkBox);
		return list; 
	}
	
	
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
