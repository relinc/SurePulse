package net.relinc.processor.data;


import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DescriptorDictionary {
	public ObservableList<Descriptor> descriptors = FXCollections.observableArrayList();
	private String name;
	public void updateDictionary() {
		if(descriptors.size() == 0)
			descriptors.add(new Descriptor("", ""));
		if(!descriptors.get(descriptors.size() - 1).getKey().equals("") || !descriptors.get(descriptors.size() - 1).getValue().equals(""))
			descriptors.add(new Descriptor("", ""));
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue(String key){
		for(Descriptor d : descriptors){
			if(d.getKey().equals(key))
				return d.getValue();
		}
		return "";// descriptors.indexOf(key) == -1 ? "" : descriptors.get(descriptors.indexOf(key));
	}
	public void setValue(String key, String value){
		for(Descriptor D : descriptors){
			if(D.getKey().equals(key)){
				D.setValue(value);
				break;
			}
		}
	}
}
