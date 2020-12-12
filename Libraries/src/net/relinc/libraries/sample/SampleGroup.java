package net.relinc.libraries.sample;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class SampleGroup {
	public String groupName;
	public Color color;
	public List<Sample> groupSamples;
	
	public SampleGroup(String groupName) {
		groupSamples = new ArrayList<Sample>();
		this.groupName = groupName;
	}

	public SampleGroup(String groupName, Color color) {
		groupSamples = new ArrayList<Sample>();
		this.groupName = groupName;
		this.color = color;
	}

	@Override
	public String toString() {
		return groupName;
	}
}
