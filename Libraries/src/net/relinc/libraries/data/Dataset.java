package net.relinc.libraries.data;

public class Dataset {
	private double[] data;
	private double[] timeData;

	private Integer userDataPoints; // The user might choose to have more or less data points, in which case the data is interpolated from the original.
	private double[] sampledData;
	private double[] sampledTimeData;


	public Dataset(double[] time, double[] d){
		if(time.length != d.length) {
			throw new RuntimeException("Cannot create a dataset with different lengths");
		}
		timeData = time;
		data =  d;
		this.setUserDataPoints(time.length);
	}

	public void setUserDataPoints(int points) {
		this.userDataPoints = points;

		this.sampledData = sampleData(this.data, points);
		this.sampledTimeData = sampleData(this.timeData, points);
	}

	private double[] sampleData(double[] source, int numDataPoints) {
		if(numDataPoints == source.length) {
			return source;
		} else if(numDataPoints == 0) {
			return new double[0];
		} else {
			double[] result = new double[numDataPoints];
			double indexRatio = (source.length-1) / (1.0 * (numDataPoints - 1));
			for(int i = 0; i < result.length; i++) {
				double origIndex = i * indexRatio;
				int origIndexLower = (int)origIndex;
				int origIndexUpper = origIndexLower + 1;
				double lowerPart = 1.0 - (origIndex - origIndexLower);
				double upperPart = 1.0 - lowerPart;

				if(i == result.length - 1) {
					result[i] = source[origIndexLower];
				} else {
					try{
						result[i] = lowerPart * source[origIndexLower] + upperPart * source[origIndexUpper];
					} catch(Exception e) {
						e.printStackTrace();
					}
				}


			}
			return result;
		}
	}

	public double[] getTimeData() {
		return this.sampledTimeData;
	}

	public double[] getData() {
		return this.sampledData;
	}

	private int getUserDataPoints() {
		return this.userDataPoints;
	}

	public int getOriginalDataPoints() {
		return this.timeData.length;
	}

	public int userIndexToOriginalIndex(int userIndex) {
		if(getUserDataPoints() == getOriginalDataPoints()) { // this just avoids potential rounding errors from doubles, pry not necessary
			return userIndex;
		}

		return (int)(userIndex * (getOriginalDataPoints() - 1.0) / (getUserDataPoints() - 1));
	}

	public int originalIndexToUserIndex(int origIndex) {
		if(getUserDataPoints() == getOriginalDataPoints()) {
			return origIndex;
		}

		return (int) (origIndex * (getUserDataPoints() - 1.0) / (getOriginalDataPoints() - 1));
	}
}
