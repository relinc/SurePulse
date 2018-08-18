package net.relinc.libraries.staticClasses;

public final class TriangularLowPassFilter {

	private static double findTriangularMovingAverage(double[] data, int range, int idx) {
		double value = 0;
		for (int idxFilter = -range; idxFilter < range; idxFilter++) {
			double weight = range - Math.abs(idxFilter);
			if (idx + idxFilter <= 0) {
				value += data[0] * weight;
			} else if (idx + idxFilter >= data.length - 1) {
				value += data[data.length - 1] * weight;
			} else {
				value += data[idx + idxFilter] * weight;
			}
		}
		return value;
	}

	public static double[] triangularLowPass(double[] data, double lowpassToFrequencyRatio) {

		double[] filtered_data = new double[data.length];
		int range = (int) (.25 / lowpassToFrequencyRatio);

		double total = 0;

		for (int idxFilter = -range; idxFilter < range; idxFilter++) {
			total += range - Math.abs(idxFilter);
		}

		for (int idx = 0; idx < data.length; idx++) {
			filtered_data[idx] = findTriangularMovingAverage(data, range, idx) / total;
		}

		return filtered_data;
	}
}
