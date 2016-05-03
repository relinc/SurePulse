package net.relinc.processor.staticClasses;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;


public final class PochammerChreeDispersion {
	static int lengthPadded = -1;
	public static double[] SteelParameters = {.57594, 0.42555, 21.3260, 19.2240, - 7.3258, 
			   2.4713};
	public static double[] AluminumParameters = {0.56855, 0.43254, 14.6020, 19.8090, -5.0851, 
			   1.9895};
	public static double skip = 20.0;//20.0;
	static double radius = -1;//.75 * .0254 / 2;
	static double deltaT = -1;//12.5 * Math.pow(10, -9) * skip;
	static double deltaX = -1;//-1.89;
	static double c0 = -1;//5100;
	
	private static double getTimeEvent(){
		return lengthPadded * deltaT;
	}
	
	private static double getNum(){
		return (double)lengthPadded / 2;
	}
	
	private static double getw0(){
		return 2 * Math.PI / getTimeEvent();
	}
	
	public static double[] runPochammer(double[] pulse, double[] parameters, double radius, double deltaT, double deltaX, double barWavespeed){
		double[] sparse = new double[(int)(pulse.length / skip)];
		for(int i = 0; i < sparse.length; i++){
			sparse[i] = pulse[(int)(i * skip)];
		}
		
		Complex[] fft = fourierTransform(sparse);
		lengthPadded = fft.length;
		System.out.println("Length Padded: " + lengthPadded);
		PochammerChreeDispersion.radius = radius;
		PochammerChreeDispersion.deltaT = deltaT;
		PochammerChreeDispersion.deltaX = deltaX;
		PochammerChreeDispersion.c0 = barWavespeed;
		return getReconstructedPulse(fft, parameters);
	}
	
	private static double[] getReconstructedPulse(Complex[] fft, double[] parameters){
		double[] realPart = getRealPart(fft);
		double[] imaginaryPart = getImaginaryPart(fft);
		
		double[] waveShift = new double[(int)getNum()];
		for(int i = 0 ; i < waveShift.length; i++){
			waveShift[i] = omega(i + 1, parameters);
		}
		
		double[] recon = new double[2 * (int)getNum()];
		for(int n = 0; n < recon.length; n++){
			double first = fft[0].getReal() * Math.pow(2 / getNum(), .5) / 2;
			double second = 0;
			for(int i = 0; i < (int)getNum(); i++){
				second += realPart[i] * Math.cos((n + 1) * (i + 1) * getw0() * deltaT + waveShift[i]);
				
				second += -imaginaryPart[i] * Math.sin((n + 1) * (i + 1) * getw0() * deltaT + waveShift[i]);
			}
			
			second = second * Math.sqrt(2 / getNum());
			//System.out.println(second + first);
			recon[n] = first + second;
		}
		return recon;
	}
	
	private static void printArray(double[] arr){
		for(int i = 0 ; i< arr.length; i++)
			System.out.print(arr[i] + ", ");
	}
	
	private static double getWavelength(double k){
		return Math.abs(getTimeEvent() * c0) / k;
	}
	
	private static double c(double k, double[] MaterialParameters){
		int offset = 1;
		return c0 *(MaterialParameters[1 - offset] + 
			    MaterialParameters[
			      2 - offset]/(MaterialParameters[3 - offset] * Math.pow(radius/getWavelength(k),4) + 
			       MaterialParameters[4 - offset] * Math.pow(radius/getWavelength(k),3) + 
			       MaterialParameters[5 - offset] * Math.pow(radius/getWavelength(k),2) + 
			       MaterialParameters[6 - offset] * Math.pow(radius/getWavelength(k),1.5) + 1));
	}
	
	private static double omega(double k, double[] parameters){
		return k * getw0() * (deltaX * (Math.pow(c(k, parameters),-1) - Math.pow(c0, -1)));
	}
	
	private static Complex[] fourierTransform(double[] data){
		int minPowerOf2 = 1;
		while(minPowerOf2 < data.length)
			minPowerOf2 = 2 * minPowerOf2;
		
		//pad with zeros
		double[] padded = new double[minPowerOf2];
		for(int i = 0; i < data.length; i++)
			padded[i] = data[i];

		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.UNITARY);
		return transformer.transform(padded, TransformType.FORWARD);
	}
	
//	public static Complex[] fourierTransform2(double[] data){
//		int minPowerOf2 = 1;
//		while(minPowerOf2 < data.length)
//			minPowerOf2 = 2 * minPowerOf2;
//		
//		//pad with zeros
//		double[] padded = new double[minPowerOf2];
//		for(int i = 0; i < data.length; i++)
//			padded[i] = data[i];
//		
//		
//		double[] zeros = new double[padded.length];
//		double[] fftBase = FFTbase.fft(padded, zeros, true);
//		Complex[] c = new Complex[padded.length];
//		
//		for(int i = 0; i < c.length; i++){
//			c[i] = new Complex(fftBase[2 * i], fftBase[2 * i + 1]);
//		}
//		
//		return c;
//	}
	
	private static double[] getRealPart(Complex[] complex){
		double[] real = new double[(int)getNum()];
		
		for(int i = 0; i < real.length; i++){
			real[i] = complex[i + 1].getReal();
		}
		return real;
	}
	
	private static double[] getImaginaryPart(Complex[] complex){
		double[] imaginary = new double[(int)getNum()];
		for(int i = 0; i < imaginary.length; i++){
			imaginary[i] = complex[i + 1].getImaginary();
		}
		return imaginary;
	}
}
