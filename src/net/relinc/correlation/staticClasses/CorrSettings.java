package net.relinc.correlation.staticClasses;

public final class CorrSettings {
	public static String appDataName = "RELFX/SURE-DIC";
	private static double binarizationAdjust = .5;
	public static int stepSize = 5;
	public static int binarizeAdjustTriesBeforeFail = 20;

	public static double getBinarizationAdjust() {
		return binarizationAdjust;
	}

	public static void setBinarizationAdjust(double b) {
		binarizationAdjust = b;
	}
}
