package net.relinc.libraries.sample;

public class SampleConstants {
	private final String shortName;
	private final String name;
	private final String iconLocation;
	private final String extension;

	public SampleConstants(String shortName, String name, String iconLocation, String extension) {
		this.shortName = shortName;
		this.name = name;
		this.iconLocation = iconLocation;
		this.extension = extension;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIconLocation() {
		return iconLocation;
	}

	public String getExtension() {
		return extension;
	}


}
