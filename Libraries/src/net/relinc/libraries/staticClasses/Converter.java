package net.relinc.libraries.staticClasses;

import java.text.SimpleDateFormat;
import java.util.Date;

import net.relinc.libraries.fxControls.NumberTextField;

public final class Converter {

	//length
	public static double mmFromM(double m){
		return m * 1000;
	}
	public static double mFromMm(double mm){
		return mm / 1000;
	}
	public static double MeterFromInch(double inch){
		return inch*.0254;
	}
	public static double InchFromMeter(double meter){
		return meter/.0254;
	}
	//density
	public static double KgM3FromLbin3(double lbin3){
		return lbin3 * 27679.9047;
	}
	public static double Lbin3FromKgM3(double KgM3){
		return KgM3 / 27679.9047;
	}
	public static double gccFromKgm3(double Kgm3){
		return Kgm3 / 1000;
	}
	public static double Kgm3FromGcc(double gcc){
		return gcc * 1000;
	}
	
	//pressure = Force / Area
	public static double psiFromPa(double pa){
		return pa / 6894.75729;
	}
	public static double paFromPsi(double psi){
		return psi * 6894.75729;
	}
	public static double paFromKsi(double ksi){
		return ksi * 6894757.29;
	}
	public static double paFromMpsi(double mpsi){
		return mpsi * 6894757290.0;
	}
	public static double ksiFromPa(double pa){
		return pa / 6894757.29;
	}
	public static double MpsiFromPa(double pa){
		return pa / 6894757290.0;
	}
	public static double MpaFromPa(double pa){
		return pa / Math.pow(10, 6);
	}
	public static double paFromMpa(double mpa){
		return mpa * Math.pow(10, 6);
	}
	public static double GpaFromPa(double pa){
		return pa / Math.pow(10, 9);
	}
	public static double paFromGpa(double gpa){
		return gpa * Math.pow(10, 9);
	}
	
	//heat capacity
	public static double JoulesPerKilogramKelvinFromButanesPerPoundFarenheit(double btuPerLbF){
		return btuPerLbF * 4186.8;
	}
	public static double butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(double JPerKK){
		return JPerKK / 4186.8;
	}
	
	
	public static double MeterFromMm(Double mm) {
		return mm / 1000;
	}
	public static double FootFromMeter(double meter) {
		return InchFromMeter(meter) / 12.0;
	}
	public static double MeterFromFoot(Double foot) {
		return MeterFromInch(foot * 12.0);
	}
	
	public static double LbfFromN(double newton) {
		return newton * 0.224809;
	}
	
	
	public static void convertTBValueFromInchToMeter(NumberTextField TB){
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(Converter.MeterFromInch(Double.parseDouble(TB.getText()))));
	}
	public static void convertTBValueFromMeterToInch(NumberTextField TB){
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(Converter.InchFromMeter(Double.parseDouble(TB.getText()))));
	}
	
	public static void convertTBValueFromInchToMM(NumberTextField TB){
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(Converter.MeterFromInch(Double.parseDouble(TB.getText())) * 1000));
	}
	public static void convertTBValueFromMMToInch(NumberTextField TB){
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(Converter.InchFromMeter(Double.parseDouble(TB.getText())) / 1000));
	}
	
	public static void convertTBValueFromMpStoFpS(NumberTextField TB) {
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(3.28084 * Double.parseDouble(TB.getText())));
	}
	
	public static void convertTBValueFromFpStoMpS(NumberTextField TB) {
		try{
			Double.parseDouble(TB.getText());
		}
		catch(Exception e){
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) / 3.28084));
	}
	
	public static void convertTBValueFromKSItoMPa(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) / .1450377));
	}
	
	public static void convertTBValueFromMPatoKSI(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) * .1450377));
	}
	
	public static void convertTBValueFromLbsPerCubicInchtoGramsPerCC(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) * 27.6799047));
		
	}
	
	public static void convertTBValueFromGramsPerCCtoLbsPerCubicInch(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) / 27.6799047));
	}
	
	public static void convertTBValueFromPsiTimesTenToTheSixthToGigapascals(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) / .145037738007));
	}
	
	public static void convertTBValueFromGigapascalsPsiTimesTenToTheSixth(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(Double.parseDouble(TB.getText()) * .145037738007));
	}
	
	public static void convertTBValueFromButanesPerPoundFarenheitFromJoulesPerKilogramKelvin(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(Double.parseDouble(TB.getText()))));
	}
	
	public static void  convertTBValueFromJoulesPerKilogramKelvinFromButanesPerPoundFarenheit(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(JoulesPerKilogramKelvinFromButanesPerPoundFarenheit(Double.parseDouble(TB.getText()))));
	}

	public static void convertTBValueFromMToFeet(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(FootFromMeter(Double.parseDouble(TB.getText()))));
	}
	public static void convertTBValueFromFeetToM(NumberTextField TB) {
		try {
			Double.parseDouble(TB.getText());
		} catch(Exception e) {
			return;
		}
		TB.setNumberText(Double.toString(MeterFromFoot(Double.parseDouble(TB.getText()))));
	}

	
	public static String getFormattedDate(Date d){
		SimpleDateFormat ft = 
			      new SimpleDateFormat ("yyyy.MM.dd.hh:mm a zzz");
		return ft.format(d);
	}

    public static double psiFromKsi(double ksi) {
		return ksi * Math.pow(10, 3);
    }
}
