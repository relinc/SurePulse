package net.relinc.libraries.data;

import javafx.beans.property.SimpleStringProperty;

public class Descriptor {
	public SimpleStringProperty key; 
	public SimpleStringProperty value;
	
	public Descriptor(String k ,String v){
		key = new SimpleStringProperty(k);
		value = new SimpleStringProperty(v);
	}
	
	public String getKey() {
		return key.get();
	}
	public void setKey(String k) {
		key.set(k);
	}
	public String getValue() {
		return value.get();
	}
	public void setValue(String v) {
		value.set(v);
	}
	
}
