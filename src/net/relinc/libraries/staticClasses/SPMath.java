package net.relinc.libraries.staticClasses;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import javafx.beans.property.DoubleProperty;

public final class SPMath {
	
	public static Complex[] fft(double[] data){
		int minPowerOf2 = 1;
		while(minPowerOf2 < data.length)
			minPowerOf2 = 2 * minPowerOf2;
		
		//pad with zeros
		double[] padded = new double[minPowerOf2];
		for(int i = 0; i < data.length; i++)
			padded[i] = data[i];
		
		
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		return transformer.transform(padded, TransformType.FORWARD);
	}
	
	public static double[] fourierLowPassFilter(double[] data, double lowPass, double frequency){
		//data: input data, must be spaced equally in time.
		//lowPass: The cutoff frequency at which 
		//frequency: The frequency of the input data.
		System.out.println("Frequency: " + frequency);
		//The apache Fft (Fast Fourier Transform) accepts arrays that are powers of 2.
		
		Complex[] fourierTransform = fft(data);

		//build the frequency domain array
		double[] frequencyDomain = new double[fourierTransform.length];
		for(int i = 0; i < frequencyDomain.length; i++)
			frequencyDomain[i] = frequency * i / (double)fourierTransform.length;
		
		//build the classifier array, 2s are kept and 0s do not pass the filter
		double[] keepPoints = new double[frequencyDomain.length];
		keepPoints[0] = 1; 
		for(int i = 1; i < frequencyDomain.length; i++){
			if(frequencyDomain[i] < lowPass)
				keepPoints[i] = 2;
			else
				keepPoints[i] = 0;
		}
		
		//filter the fft
		for(int i = 0; i < fourierTransform.length; i++)
			fourierTransform[i] = fourierTransform[i].multiply((double)keepPoints[i]);
				
		//invert back to time domain
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] reverseFourier = transformer.transform(fourierTransform, TransformType.INVERSE);
		
		//get the real part of the reverse 
		double[] result = new double[data.length];
		for(int i = 0; i< result.length; i++){
			result[i] = reverseFourier[i].getReal();
		}
		
		return result;
	}
	
	public static ArrayList<double[]> diluteData(double[] input, int keepPoints){
		double[] diluted = new double[keepPoints];
		double[] oldIndices = new double[keepPoints];
		int index = 0;
		int increase = input.length / keepPoints;
		if(increase == 0)
			increase++;
		for(int i = 0; i < diluted.length; i++){
			diluted[i] = input[index];
			oldIndices[i] = index;
			
			
			index += increase;
		}
		ArrayList<double[]> val = new ArrayList<>();
		val.add(diluted);
		val.add(oldIndices);
		return val;
	}

	public static double[] subtractFrom(double[] zeroedData, double zero) {
		for(int i = 0 ; i < zeroedData.length; i++)
			zeroedData[i] = zeroedData[i] - zero;
		return zeroedData;
	}
	

}
