package net.relinc.libraries.staticClasses;

public final class TriangularLowPassFilter {

    private static double findTriangularMovingAverage(double [] data, double f_t,int idx){
    	int range=(int)(.25/f_t);
    	double value=0;
    	for(int idxFilter=-range; idxFilter<range;idxFilter++) {
    		if(idx+idxFilter<=0)
    		{
    			value+=data[0]*(range-Math.abs(idxFilter));
    		}
    		else if(idx+idxFilter>=data.length-1)
    		{
    			value+=data[data.length-1]*(range-Math.abs(idxFilter));
    		}
    		else
    		{
    			value+=data[idx+idxFilter]*(range-Math.abs(idxFilter));
    		}
    	}
    	return value;
    }
    public static double [] triangularLowPass(double [] data, double f_t) {
    	
    	double [] filtered_data = new double[data.length];
    	int range=(int)(.25/f_t);

    	double total=0;
    	
    	for(int idxFilter=-range; idxFilter<range;idxFilter++) {
    		total+=range-Math.abs(idxFilter);
    	}
    	
    	for(int idx=0; idx<data.length; idx++){
    		filtered_data[idx]=findTriangularMovingAverage(data,  f_t, idx)/total;
    	}
    	
    	return filtered_data;
    }
}
