package net.relinc.libraries.data.ModifierFolder;

import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import net.relinc.libraries.data.DataSubset;

public abstract class Modifier {
	public ModifierEnum modifierEnum;
	public SimpleBooleanProperty activated = new SimpleBooleanProperty(false); //when activated, effects the data calculation.
	public SimpleBooleanProperty enabled = new SimpleBooleanProperty(false); //the user has seen the effects. It is possible to activate.
	public CheckBox checkBox;
	
	public Modifier() {
		
	}

	public abstract ModifierResult applyModifier(double[] x, double[] y, DataSubset data);


	public enum ModifierEnum {
		ZERO, LOWPASS, FITTER, POCHAMMER, RESAMPLER; //Order matters. Determines order that modifiers are applied.
	}
	
	
	public static ModifierListWrapper getModifierList(){
		ModifierListWrapper list = new ModifierListWrapper();
		for(ModifierEnum en : ModifierEnum.values()){
			list.add(Modifier.getNewModifier(en)); //initializes the modifier list with all modifiers.
		}
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
		case RESAMPLER:
			return new Resampler();
		default:
			return null;
		}
	}
	
	@Override
	public abstract String toString();

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

	public boolean shouldApply() {
		return this.enabled.get() && this.activated.get();
	}
}
