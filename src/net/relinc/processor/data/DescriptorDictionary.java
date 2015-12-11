package net.relinc.processor.data;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DescriptorDictionary {
	public ObservableList<Descriptor> descriptors = FXCollections.observableArrayList();

	public void updateDictionary() {
		if(descriptors.size() == 0)
			descriptors.add(new Descriptor("", ""));
		if(!descriptors.get(descriptors.size() - 1).getKey().equals("") || !descriptors.get(descriptors.size() - 1).getValue().equals(""))
			descriptors.add(new Descriptor("", ""));
	}
}
