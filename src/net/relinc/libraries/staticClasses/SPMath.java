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
		
//		for(int i = 0; i < padded.length; i++){
//			System.out.println(padded[i] + ",");
//		}
		
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		return transformer.transform(padded, TransformType.FORWARD);
	}
	
	public static double[] fourierLowPassFilter(double[] data, double lowPass, double frequency){
		//try the butterworth method
		//return butterworthFilter(data, frequency, 3, lowPass, 1.0);
		return customBuiltLowPassFilter(data, lowPass, frequency);
	}
	
	public static double[] customBuiltLowPassFilter(double[] data, double lowPass, double frequency) {
		//this is the old lowpass filter. It didn't treat sparse datasets well.
		
		// data: input data, must be spaced equally in time.
		// lowPass: The cutoff frequency at which
		// frequency: The frequency of the input data.
		// The apache Fft (Fast Fourier Transform) accepts arrays that are
		// powers of 2.

		Complex[] fourierTransform = fft(data);

		// for(int i = 0; i < fourierTransform.length; i++){
		// System.out.println(fourierTransform[i].getReal() + " + " +
		// fourierTransform[i].getImaginary() + "I" + ",");
		// }

		// build the frequency domain array
		double[] frequencyDomain = new double[fourierTransform.length];
		for (int i = 0; i < frequencyDomain.length; i++)
			frequencyDomain[i] = frequency * i / (double) fourierTransform.length;

		// build the classifier array, 2s are kept and 0s do not pass the filter
		double[] keepPoints = new double[frequencyDomain.length];
		keepPoints[0] = 1;
		for (int i = 1; i < frequencyDomain.length; i++) {
			if (frequencyDomain[i] < lowPass) {
				// System.out.println("Keeping: " + i);
				keepPoints[i] = 2;
			} else {
				// System.out.println("Not Keeping: " + i);
				keepPoints[i] = 0;
			}
		}

		// filter the fft
		for (int i = 0; i < fourierTransform.length; i++)
			fourierTransform[i] = fourierTransform[i].multiply((double) keepPoints[i]);

		// invert back to time domain
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] reverseFourier = transformer.transform(fourierTransform, TransformType.INVERSE);

		// get the real part of the reverse
		double[] result = new double[data.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = reverseFourier[i].getReal();
		}
		// if it keeps all the data, then don't filter it because it gets
		// screwed.
		return keepPoints[keepPoints.length - 1] == 2 ? data : result;
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
	
	/*
	 * time smoothing constant for low-pass filter
	 * 0 ≤ α ≤ 1 ; a smaller value basically means more smoothing
	 * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
	 */
	static final float ALPHA = 0.2f;

	protected float[] accelVals;

	public void onSensorChanged(float[] val) {
	    accelVals = lowPass( val, accelVals );

	    // use smoothed accelVals here; see this link for a simple compass example:
	    // http://www.codingforandroid.com/2011/01/using-orientation-sensors-simple.html
	}

	/**
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
	 * @see http://en.wikipedia.org/wiki/Low-pass_filter#Simple_infinite_impulse_response_filter
	 */
	protected float[] lowPass( float[] input, float[] output ) {
	    if ( output == null ) return input;

	    for ( int i=0; i<input.length; i++ ) {
	        output[i] = output[i] + ALPHA * (input[i] - output[i]);
	    }
	    return output;
	}
	
	public static double[] butterworthFilter(double[] signal, double sampleFrequency, int order, double f0, double DCGain) {

		int N = signal.length;

		// Apply forward FFT
		Complex[] signalFFT = fft(signal);

		if (f0 > 0) {

			//int numBins = N / 2; // Half the length of the FFT by symmetry
			double binWidth = sampleFrequency / N; // Hz

			// Filter
			// System.Threading.Tasks.Parallel.For( 1, N / 2, i =>
			// {
			// var binFreq = binWidth * i;
			// var gain = DCGain / ( Math.Sqrt( ( 1 +
			// Math.Pow( binFreq / f0, 2.0 * order ) ) ) );
			// signalFFT[i] *= gain;
			// signalFFT[N - i] *= gain;
			// } );

			for (int i = 1; i <= N / 2; i++) {
				double binFreq = binWidth * i;
				double gain = DCGain / (Math.sqrt((1 + Math.pow(binFreq / f0, 2.0 * order))));
				signalFFT[i] = signalFFT[i].multiply(new Complex(gain));
				signalFFT[N - i] = signalFFT[N - i].multiply(new Complex(gain));
			}

		}
		FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
		Complex[] reverseFourier = transformer.transform(signalFFT, TransformType.INVERSE);

		// get the real part of the reverse
		double[] result = new double[signal.length];
		for (int i = 0; i < result.length; i++) {
			result[i] = reverseFourier[i].getReal();
		}
		return result;
		// Reverse filtered signal
		// var ifft = new DoubleComplexBackward1DFFT( N );
		// ifft.SetScaleFactorByLength(); // Needed to get the correct amplitude
		// signal = ifft.FFT( signalFFT );
		//
		// return signal;
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
