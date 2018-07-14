package net.relinc.libraries.sample;

import java.util.ArrayList;

import net.relinc.libraries.sample.Sample;

public class SampleGroup {
	public String groupName;
	public ArrayList<Sample> groupSamples;
	
	public SampleGroup(String groupName) {
		groupSamples = new ArrayList<Sample>();
		this.groupName = groupName;
	}
}
