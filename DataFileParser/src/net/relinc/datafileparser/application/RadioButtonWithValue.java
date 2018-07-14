package net.relinc.datafileparser.application;


import javafx.scene.control.RadioButton;

public class RadioButtonWithValue<T> extends RadioButton {
	private T value;
	
	public RadioButtonWithValue(String displayText, T value){
		super(displayText);
		this.value = value;
	}
	
	public T getValue(){
		return this.value;
	}
	
	public void setValue(T v){
		this.value = v;
	}
}
