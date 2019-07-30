package net.relinc.libraries.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

import com.google.gson.Gson;

import net.relinc.libraries.application.Pair;

public class FitableDataset {
	public transient List<Double> origX;//transient = not saved in Gson.
	public transient List<Double> origY;
	public transient List<Double> fittedX;
	public transient List<Double> fittedY;
	public transient double[] coefficients;
	public transient List<Integer> omittedIndices = new ArrayList<Integer>();
	private transient PolynomialFunction function;

	int polynomialFit = 1;
	//String polynomialFitDescriptor = "Polynomial Fit";
	int pointsToRemove = 0;
	//String pointsToRemoveDescriptor = "Points To Remove";
	int beginFit = 0;
	//String beginFitDescriptor = "Begin Fit";
	int endFit = 0;
	//String endFitDescriptor = "End Fit";
	private transient String name;
	//String delimeter = ":";
	private boolean smoothAllPointsMode = true;
	
	public int getPolynomialFit() {
		return polynomialFit;
	}
	public void setPolynomialFit(int polynomialFit) {
		this.polynomialFit = polynomialFit;
	}
	public int getPointsToRemove() {
		return pointsToRemove;
	}
	public void setPointsToRemove(int pointsToRemove) {
		this.pointsToRemove = pointsToRemove;
	}
	public int getBeginFit() {
		return beginFit;
	}
	public void setBeginFit(int begin) {
		if(begin < endFit && begin >= 0)
			this.beginFit = begin;
	}
	public int getEndFit() {
		return endFit;
	}
	public void setEndFit(int end) {
		if(end > beginFit && end < origX.size())
			this.endFit = end;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public FitableDataset(List<Double> x, List<Double> y, String name) {
		origX = x;
		origY = y;
		beginFit = 0;
		endFit = origX.size() - 1;
		renderFittedData();
		setName(name);
	}
	
	public FitableDataset() {
		//For the Gson library. When using Gson, must populate arrays and render fitted data. Do not use otherwise.
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	public void renderFittedData(){
		final WeightedObservedPoints obs = new WeightedObservedPoints();
		final PolynomialCurveFitter fitter = PolynomialCurveFitter.create(polynomialFit);
		for(int i = 0; i < origX.size(); i++){
			if(i >= beginFit && i <= endFit)
				obs.add(origX.get(i), origY.get(i));
		}
		
		//preliminary fit
		final double[] coeff = fitter.fit(obs.toList());
		
		PolynomialFunction func = new PolynomialFunction(coeff);
		
		Pair[] residuals = new Pair[origX.size() - beginFit - (origX.size() - endFit - 1)];
		
		for(int i = 0; i < residuals.length; i++){
			residuals[i] = new Pair(i + beginFit, Math.abs(origY.get(i + beginFit) - func.value(origX.get(i + beginFit))));
		}
		Arrays.sort(residuals);
		omittedIndices = new ArrayList<Integer>(pointsToRemove);
		for(int i = 0; i < pointsToRemove; i++)
			omittedIndices.add(residuals[pointsToRemove - 1 - i].index);
		
		//now fit again without the omitted indices.
		final WeightedObservedPoints obs2 = new WeightedObservedPoints();
		final PolynomialCurveFitter fitter2 = PolynomialCurveFitter.create(polynomialFit);
		for(int i = 0; i < origX.size(); i++){
			if(!omittedIndices.contains(i) && i >= beginFit && i <= endFit)
				obs2.add(origX.get(i), origY.get(i));
		}
		
		
		coefficients = fitter2.fit(obs2.toList());
		
		function = new PolynomialFunction(coefficients);
		
		
		fittedY = new ArrayList<>(origY.size());
		fittedX = origX;
		for(int i = 0; i < fittedX.size(); i++){
			if(i >= beginFit && i <= endFit && (omittedIndices.contains(new Integer(i)) || getSmoothAllPointsMode()))
				fittedY.add(function.value(fittedX.get(i)));
			else
				fittedY.add(origY.get(i));
			
		}
	}
	public void setBeginFromXValue(double val) {
		int a = findFirstIndexGreaterorEqualToValue(origX, val);
		setBeginFit(a);
	}
	public void setEndFromXValue(double val) {
		int a = findFirstIndexGreaterorEqualToValue(origX, val);
		setEndFit(a);
	}
	
	public static int findFirstIndexGreaterorEqualToValue(List<Double> data, double val){
		for(int i = 0; i < data.size(); i++){
			if(data.get(i) >= val){
				return i;
			}
		}
		return -1;
	}
	
	public int getFittedNumberOfPoints(){
		return origX.size() - pointsToRemove - beginFit - (origX.size() - endFit - 1);
	}
	public void resetBeginAndEnd() {
		setBeginFit(0);
		setEndFit(origX.size() - 1);
	}
	public boolean getSmoothAllPointsMode() {
		return smoothAllPointsMode;
	}
	public void setSmoothAllPointsMode(boolean smoothAllPointsMode) {
		this.smoothAllPointsMode = smoothAllPointsMode;
	}
	
	public String getStringForFileWriting(){
		Gson gson = new Gson();
		return gson.toJson(this);
	}



	public double computeY(double x) {
		return function.value(x);
	}
	
}
