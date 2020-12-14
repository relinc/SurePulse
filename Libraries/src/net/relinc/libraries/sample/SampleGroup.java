package net.relinc.libraries.sample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class SampleGroup {
	public String groupName;
	public Color color;
	public List<Sample> groupSamples;

	private BooleanProperty selected = new SimpleBooleanProperty(true);
	public BooleanProperty selectedProperty() {
		return selected;
	}
	

	public SampleGroup(String groupName, Color color, ChangeListener<Boolean> checkChangedListener) {
		groupSamples = new ArrayList<Sample>();
		this.groupName = groupName;
		this.color = color;
		selectedProperty().addListener(checkChangedListener);
	}

	@Override
	public String toString() {
		return groupName;
	}
}
