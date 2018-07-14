package net.relinc.libraries.data;

public class Dataset {
	public double[] data;
	public double[] timeData;
	
	public Dataset(double[] time, double[] d){
		timeData = time;
		data =  d;
	}
	
	public double[] getDerivative(){
		double[] derivative = new double[data.length];
		for(int i = 0; i < derivative.length - 1; i++){
			derivative[i] = (data[i + 1] - data[i]) / (timeData[i + 1] - timeData[1]);
		}
		return derivative;
	}
	
}
