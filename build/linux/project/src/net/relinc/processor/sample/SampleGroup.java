package net.relinc.processor.sample;

import java.util.ArrayList;

import net.relinc.processor.sample.Sample;

public class SampleGroup {
	public String groupName;
	public ArrayList<Sample> groupSamples;
	
	public SampleGroup(String groupName) {
		groupSamples = new ArrayList<Sample>();
		this.groupName = groupName;
	}
}
