package net.relinc.datafileparser.application;

import java.util.ArrayList;
import java.util.List;

public class Model {
	private String dataFile;
	private String frameDelimiter;
	private int startFrameDelimiter;
	private int endFrameDelimiter;
	private String datapointDelimiter;
	private int startDatapointDelimiter;
	private int endDatapointDelimiter;
	
	public Model(String frameDelimiter, String datapointDelimiter)
	{
		this.setFrameDelimiter(frameDelimiter);
		this.setDatapointDelimiter(datapointDelimiter);
	}
	
	public String getDataFile() {
		return dataFile;
	}

	public void setDataFile(String dataFile) {
		this.dataFile = dataFile;
	}

	public String getFrameDelimiter() {
		return frameDelimiter;
	}

	public void setFrameDelimiter(String frameDelimiter) {
		this.frameDelimiter = frameDelimiter;
	}

	public int getStartFrameDelimiter() {
		return startFrameDelimiter;
	}

	public void setStartFrameDelimiter(int startFrameDelimiter) {
		this.startFrameDelimiter = startFrameDelimiter;
	}

	public int getEndFrameDelimiter() {
		return endFrameDelimiter;
	}

	public void setEndFrameDelimiter(int endFrameDelimiter) {
		this.endFrameDelimiter = endFrameDelimiter;
	}

	public String getDatapointDelimiter() {
		return datapointDelimiter;
	}

	public void setDatapointDelimiter(String datapointDelimiter) {
		this.datapointDelimiter = datapointDelimiter;
	}

	public int getStartDatapointDelimiter() {
		return startDatapointDelimiter;
	}

	public void setStartDatapointDelimiter(int startDatapointDelimiter) {
		this.startDatapointDelimiter = startDatapointDelimiter;
	}

	public int getEndDatapointDelimiter() {
		return endDatapointDelimiter;
	}

	public void setEndDatapointDelimiter(int endDatapointDelimiter) {
		this.endDatapointDelimiter = endDatapointDelimiter;
	}
	
	//-----------------------------------------------------------------
	// End of getter/setter
	//-----------------------------------------------------------------

	
	/**
	* Parses the data
	* 
	* @return each data column as a list of strings
	*/
	public List<List<String>> parse(){
		List<List<String>> list = new ArrayList<List<String>>();
		String[] frames = dataFile.split(getFrameDelimiter());
		int columnCount = frames[getStartFrameDelimiter()].split(getDatapointDelimiter()).length;
		while(columnCount-- > 0)
			list.add(new ArrayList<String>());
		for(int i = getStartFrameDelimiter(); i <= getEndFrameDelimiter(); i++)
		{
			for(int ii = getStartDatapointDelimiter(); ii <= getEndDatapointDelimiter(); ii++)
			{
				int columnIndex = ii - getStartDatapointDelimiter();
				list.get(columnIndex).add(frames[i].split(getDatapointDelimiter())[ii]);
			}
		}
		return list;
	}
	
	/**
	* Attempts to automatically determine all the parsing parameters
	* <p>
	* Assumes that the data has more frames than datapoints (columns than headers). 
	* Tries to cast data to doubles to determine legitimacy.
	* </p>
	* 
	*/
	public boolean setParsingParametersAutomatically(){
		if(dataFile == null)
			return false;
		
		String[] frameDelimiterCandidates = new String[]{"\n", System.lineSeparator()}; //TODO: Are there more?
		String[] datapointDelimiterCandidates = new String[]{",", " ", "\t", "|", ":", "#", "$"}; //TODO: More?
		
		String winningFrameDelimiter = frameDelimiterCandidates[0];
		String winningDatapointDelimiter = datapointDelimiterCandidates[0];
		
		for(int i = 0; i < frameDelimiterCandidates.length; i++){
			
		}
		
		return true;
	}
}
