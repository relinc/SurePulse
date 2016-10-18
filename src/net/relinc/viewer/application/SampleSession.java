package net.relinc.viewer.application;

import net.relinc.libraries.data.DataLocation;

public class SampleSession {
	public String path;
	public DataLocation loadLocation;
	public DataLocation displacementLocation;
	
	public SampleSession(String path, DataLocation selectedDisplacementLocation, DataLocation selectedLoadLocation)
	{
		this.path = path;
		this.loadLocation = selectedLoadLocation;
		this.displacementLocation = selectedDisplacementLocation;
	}
}
