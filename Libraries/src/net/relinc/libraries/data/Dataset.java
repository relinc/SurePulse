package net.relinc.libraries.data;

public class Dataset {
	private double[] data;
	private double[] timeData;


	public Dataset(double[] time, double[] d){
		if(time.length != d.length) {
			throw new RuntimeException("Cannot create a dataset with different lengths");
		}
		timeData = time;
		data =  d;
	}


	public double[] getTimeData() {
		return this.timeData.clone();
	}

	public double[] getData() {
		return this.data.clone();
	}

	public int getOriginalDataPoints() {
		return this.timeData.length;
	}

}
