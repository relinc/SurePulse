package net.relinc.libraries.staticClasses;

import java.util.ArrayList;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

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
		return TriangularLowPassFilter.triangularLowPass(data, lowPass/frequency);
	}
	
	public static ArrayList<double[]> diluteData(double[] input, int keepPoints){
		keepPoints = Math.min(input.length, keepPoints); // Don't keep more than the array size.
		
		double[] diluted = new double[keepPoints];
		double[] oldIndices = new double[keepPoints];
		int index = 0;
		if(keepPoints == 0)
			keepPoints++; // Avoid divide by 0
		int increase = input.length / keepPoints;
		if(increase == 0)
			increase++; // Minimum of 1
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

	public static double[] getEngStrainFromLagrangianStrain(double[] langStrain) {
		double[] strain = new double[langStrain.length];
		for(int i = 0; i < strain.length; i++){
			strain[i] = Math.sqrt(1 + 2 * langStrain[i]) - 1;
		}
		return strain;
	}
	
	public static double getPicoArrowIncrease(double currentVal, boolean up)
	{
		if(currentVal <= 0.0)
			return 1.0;
		int count = 0;
		if(currentVal >= 1)
		{
			while(up ? currentVal >= 1 : currentVal > 1)
			{
				count++;
				currentVal = currentVal / 10;
			}
			count--;
		}
		else
		{
			while(up ? currentVal < 1 : currentVal <= 1)
			{
				count--;
				currentVal = currentVal * 10;
			}
		}
		return Math.pow(10, count);
	}
}
