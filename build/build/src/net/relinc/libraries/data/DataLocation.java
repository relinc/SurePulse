package net.relinc.libraries.data;


public class DataLocation {
	public int dataFileIndex;
	public int dataSubsetIndex;
	public DataLocation(int fileIndex, int dataIndex){
		dataFileIndex = fileIndex;
		dataSubsetIndex = dataIndex;
	}
	
	public int compareTo(DataLocation loc){
		if(dataFileIndex == loc.dataFileIndex && dataSubsetIndex == loc.dataSubsetIndex)
			return 0;
		return 1;
	}
	
	@Override
	public String toString(){
		return "Data File Location: " + dataFileIndex + " Data subset location: " + dataSubsetIndex;
	}
}
