package net.relinc.libraries.data;

import java.io.File;
import java.util.List;

public class DataFileInfo {
	public File tempDataFolder;
	public File savedSampleFolder;
	public File originalLocation;
	public List<DataSubsetListWrapper> dataSubsets; //don't think this is ever used
}
