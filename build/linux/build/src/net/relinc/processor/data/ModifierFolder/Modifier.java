package net.relinc.processor.data.ModifierFolder;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.processor.data.DataSubset;

public abstract class Modifier {
	public ModifierEnum modifierEnum;
	public SimpleBooleanProperty activated = new SimpleBooleanProperty(false); //when activated, effects the data calculation.
	public SimpleBooleanProperty enabled = new SimpleBooleanProperty(false); //the user has seen the effects. It is possible to activate.
	public CheckBox checkBox;
	
	public Modifier() {
		
	}
	
	public enum ModifierEnum {
		ZERO, LOWPASS, FITTER, POCHAMMER; //Order matters. Determines order that modifiers are applied.
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
		case FITTER:
			return new Fitter();
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
	
	public abstract void readModifierFromString(String line);

	public abstract void configureModifier(DataSubset dataSubset); //configure modifier vals from UI controls.

	public ObservableList<Node> getViewerControls() {
		ObservableList<Node> list = FXCollections.observableArrayList();
		list.add(checkBox);
		return list; 
	}
	
	public void setValuesFromLine(String line){
		String[] split = line.split(":");
		if(split.length < 2)
			return;
		setValuesFromDescriptorValue(split[0], split[1]);
	}
	
}
