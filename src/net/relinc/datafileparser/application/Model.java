package net.relinc.datafileparser.application;

import java.util.ArrayList;
import java.util.List;

import javax.print.CancelablePrintJob;


public class Model {
	private String dataFile;
	private String frameDelimiter;
	private int startFrameDelimiter;
	private int endFrameDelimiter;
	private String datapointDelimiter;
	private int startDatapointDelimiter;
	private int endDatapointDelimiter;
	private String[] frames;
	private List<List<ParseCandidate>> parseCandidates;
	
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
		frames = this.dataFile.split(getFrameDelimiter());
	}

	public String getFrameDelimiter() {
		return frameDelimiter;
	}

	public void setFrameDelimiter(String frameDelimiter) {
		this.frameDelimiter = frameDelimiter;
		if(dataFile != null)
			frames = this.dataFile.split(getFrameDelimiter());
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
		resetParsingParameters();
		
		if(dataFile == null)
			return false;
		
		String[] frameDelimiterCandidates = new String[]{"\n", System.lineSeparator()}; //TODO: Are there more?
		String[] datapointDelimiterCandidates = new String[]{",", " ", "\t", "\\|", ":", "#", "$"}; //TODO: More?
		
		// Find the frame delimiter with the most data frames
		String winningFrameDelimiter = frameDelimiterCandidates[0];
		int winningFrameCount = 0;
		String winningDatapointDelimiter = datapointDelimiterCandidates[0];
		int winningDatapointCount = 0;
		String[] tempFrames = new String[0];
		for(int i = 0; i < frameDelimiterCandidates.length; i++){
			tempFrames = dataFile.split(frameDelimiterCandidates[i]);
			if(tempFrames.length > winningFrameCount)
			{
				winningFrameCount = tempFrames.length;
				winningFrameDelimiter = frameDelimiterCandidates[i];
			}
		}
		
		if(winningFrameCount == 0)
			return false;
		
		
		// Find the datapoint delimiter with the most datapoints. Look at the middle data frame
		String middleFrame = tempFrames[tempFrames.length / 2];
		for(int i = 0; i < datapointDelimiterCandidates.length; i++){
			String[] pts = middleFrame.split(datapointDelimiterCandidates[i]);
			if(pts.length > winningDatapointCount){
				winningDatapointCount = pts.length;
				winningDatapointDelimiter = datapointDelimiterCandidates[i];
			}
		}
		setDatapointDelimiter(winningDatapointDelimiter);
		if(winningDatapointCount == 0)
			return false;
		
		if(winningDatapointDelimiter.equals(winningFrameDelimiter))
			return false;
		
		
		// Now determine the start and end of the delimiters by trial and error numeric casting.
		// Get the datapoint start/end from the middle frame, then work from top and bottom for the frames.
		String[] middleFramePts = middleFrame.split(getDatapointDelimiter());
		boolean converts = false;
		while(!converts){
			converts = isDouble(middleFramePts[getStartDatapointDelimiter()]);
			if(!converts)
				setStartDatapointDelimiter(getStartDatapointDelimiter() + 1);
			if(getStartDatapointDelimiter() >= middleFramePts.length)
				return false;
		}
		
		// Determine the end. Start with max and work backward.
		setEndDatapointDelimiter(middleFramePts.length - 1);
		converts = false;
		while(!converts){
			converts = isDouble(middleFramePts[getEndDatapointDelimiter()]);
			if(!converts)
				setEndDatapointDelimiter(getEndDatapointDelimiter() - 1);
			if(getEndDatapointDelimiter() < 0) // Could be zero. One column
				return false;
		}
		
		// Set the frame begin. First row from the top where all start through end convert for datapoint delimiter.
		converts = false;
		while(!converts){
			converts = true;
			String[] pts = tempFrames[getStartFrameDelimiter()].split(getDatapointDelimiter());
			if(pts.length < getNumDatapoints()){
				converts = false;
			}
			else{
				for(int i = getStartDatapointDelimiter(); i <= getEndDatapointDelimiter(); i++){
					if(!isDouble(pts[i])){
						converts = false;
					}
				}
			}
			if(!converts)
				setStartFrameDelimiter(getStartFrameDelimiter() + 1);
			if(getStartFrameDelimiter() >= tempFrames.length)
				return false;
		}
		
		
		// Set the frame end. First row from the bottom where all start through end convert for datapoint delimiter.
		converts = false;
		setEndFrameDelimiter(tempFrames.length - 1);
		while(!converts){
			converts = true;
			String[] pts = tempFrames[getEndFrameDelimiter()].split(getDatapointDelimiter());
			if(pts.length < getNumDatapoints()){
				converts = false;
			}
			else{
				for(int i = getStartDatapointDelimiter(); i <= getEndDatapointDelimiter(); i++){
					if(!isDouble(pts[i]))
						converts = false;
				}
			}
			if(!converts)
				setEndFrameDelimiter(getEndFrameDelimiter() - 1);
			if(getEndFrameDelimiter() < getStartFrameDelimiter())
				return false;
		}
		
		return true;
	} //setParsingParametersAutomatically
	
	/**
	* Resets the parsing parameters to default
	* 
	*/
	private void resetParsingParameters() {
		setStartDatapointDelimiter(0);
		setStartFrameDelimiter(0);
	} //resetParsingParameters

	private boolean isDouble(String s){
		try{
			Double.parseDouble(s);
			return true;
		}
		catch(Exception e){
		return false;
		}
	} //isDouble
	
	public int getNumDatapoints(){
		return getEndDatapointDelimiter() - getStartDatapointDelimiter() + 1; // Inclusive
	} //getNumDatapoints

	public void printParsingParameters() {
		System.out.println("Frame delimiter: " + getFrameDelimiter());
		System.out.println("Datapoint delimiter: " + getDatapointDelimiter());
		System.out.println("Start frame parsing: " + getStartFrameDelimiter());
		System.out.println("End frame parsing: " + getEndFrameDelimiter());
		System.out.println("Start datapoint parsing: " + getStartDatapointDelimiter());
		System.out.println("End datapoint parsing: " + getEndDatapointDelimiter());
	} //printParsingParameters

	public int getNumFramesFromSplit() {
		return frames.length;
	}
	
	public int getNumDatapointsFromSplit(){
		return frames[frames.length / 2].split(getDatapointDelimiter()).length;
	}

	public String[] getFrames() {
		return frames;
	}
	
	public List<List<ParseCandidate>> getParseCandidates(){
		List<List<ParseCandidate>> listlist = new ArrayList<List<ParseCandidate>>();
		for(int i = 0; i < frames.length; i++){
			ArrayList<ParseCandidate> list = new ArrayList<ParseCandidate>();
			String[] pts = frames[i].split(getDatapointDelimiter());
			for(int ii = 0; ii < pts.length; ii++){
				ParseCandidate can = new ParseCandidate(pts[ii]);
				if(i >= getStartFrameDelimiter() && i <= getEndFrameDelimiter() &&
						ii >= getStartDatapointDelimiter() && ii <= getEndDatapointDelimiter())
					can.setParsable(true);
				list.add(can);
			}
			listlist.add(list);
		}
		if(parseCandidates == null || !listlist.equals(parseCandidates))
			parseCandidates = listlist;
		return parseCandidates;
	}
	
//	public void updateParsingRange(){
//		if(parseCandidates == null){
//			System.err.println("Tried to update parseCandidate range when parseCandidate is null");
//			return;
//		}
//		for(int i = 0; i < parseCandidates.size(); i++){
//			for(int ii = 0; ii < parseCandidates.get(ii).size(); ii++){
//				boolean prevParse = parseCandidates.get(i).get(ii).isParsable();
//				parseCandidates.get(i).get(ii).setParsable(false);
//				if(i >= getStartFrameDelimiter() && i <= getEndFrameDelimiter() &&
//						ii >= getStartDatapointDelimiter() && ii <= getEndDatapointDelimiter()){
//					ParseCandidate parseCandidate = new ParseCandidate(parseCandidates.get(i).get(ii).getText());
//					parseCandidate.setParsable(true);
//					parseCandidates.get(i).set(ii, parseCandidate);
//				}
//				if(prevParse != parseCandidates.get(i).get(ii).isParsable())
//					System.out.println("Changed: " + i + " , " + ii);
//			}
//		}
//		System.out.println("updated parse candidates");
//	}
	
}
