package net.relinc.fitter.application;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;

public class FitableDataset {
	public ArrayList<Double> origX;
	public ArrayList<Double> origY;
	public ArrayList<Double> fittedX;
	public ArrayList<Double> fittedY;
	public ArrayList<Integer> omittedIndices = new ArrayList<Integer>();
	int polynomialFit = 1;
	int pointsToRemove = 0;
	int beginFit = 0;
	int endFit = 0;
	private String name;
	
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
	public void setBeginFit(int startFit) {
		this.beginFit = startFit;
	}
	public int getEndFit() {
		return endFit;
	}
	public void setEndFit(int endFit) {
		this.endFit = endFit;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public FitableDataset(List<Double> x, List<Double> y, String name) {
		origX = (ArrayList<Double>) x;
		origY = (ArrayList<Double>) y;
		beginFit = 0;
		endFit = origX.size() - 1;
		renderFittedData();
		setName(name);
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
		
		
		final double[] coeff2 = fitter2.fit(obs2.toList());
		
		func = new PolynomialFunction(coeff2);
		
		
		fittedY = new ArrayList<>(origY.size());
		fittedX = origX;
		for(int i = 0; i < fittedX.size(); i++){
			if(i >= beginFit && i <= endFit)
				fittedY.add(func.value(fittedX.get(i)));
			else
				fittedY.add(origY.get(i));
			
		}
		
	}
	public void setBeginFromXValue(double val) {
		int a = findFirstIndexGreaterorEqualToValue(origX, val);
		if(a != -1 && a < endFit)
			beginFit = a;
	}
	public void setEndFromXValue(double val) {
		int a = findFirstIndexGreaterorEqualToValue(origX, val);
		if(a != -1 && a > beginFit)
			endFit = a;
	}
	
	public static int findFirstIndexGreaterorEqualToValue(ArrayList<Double> data, double val){
		for(int i = 0; i < data.size(); i++){
			if(data.get(i) >= val){
				return i;
			}
		}
		System.out.println("Value: " + val + " not found in array. Returning -1.");
		return -1;
	}
	
	public int getFittedNumberOfPoints(){
		return origX.size() - pointsToRemove - beginFit - (origX.size() - endFit - 1);
	}
	
	
}
