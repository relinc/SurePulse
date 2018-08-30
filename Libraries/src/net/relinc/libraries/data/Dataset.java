package net.relinc.libraries.data;

public class Dataset {
	public double[] data;
	public double[] timeData;
	
	public Dataset(double[] time, double[] d){
		timeData = time;
		data =  d;
	}
	
}
