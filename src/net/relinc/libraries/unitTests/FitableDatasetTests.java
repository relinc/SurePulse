package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.relinc.libraries.application.FitableDataset;

public class FitableDatasetTests {
	
	private double[] x = new double[]{0,1,2,3,4,5,6,7,8,9};
	private double[] y = new double[]{0,2,4,6,8,10,12,14,16,18};
	
	private ArrayList<Double> getArrayListFromArray(double[] arr){
		ArrayList<Double> list = new ArrayList<>(arr.length);
		Arrays.stream(arr).forEach(i -> list.add(i));
		return list;
	}
	
	@Test
	public void testCleanData(){
		List<Double> xList = getArrayListFromArray(x);
		List<Double> yList = getArrayListFromArray(y);
		FitableDataset set = new FitableDataset(xList, yList, "Hello");
		set.setPolynomialFit(1);
		assertTrue(Arrays.equals(set.coefficients, new double[]{0,2}));
		assertTrue(Arrays.equals(set.fittedX.stream().mapToDouble(i -> i).toArray(), x));
		assertTrue(Arrays.equals(set.fittedY.stream().mapToDouble(i -> i).toArray(), y));
	}
	
	@Test
	public void testOneOutlier(){
		double[] xOutlier = Arrays.copyOf(x, x.length);
		xOutlier[xOutlier.length / 2] = 99; //outlier
		List<Double> xList = getArrayListFromArray(x);
		List<Double> yList = getArrayListFromArray(y);
		FitableDataset set = new FitableDataset(xList, yList, "Hello");
		set.setPolynomialFit(1);
		set.setPointsToRemove(1);
		set.setSmoothAllPointsMode(false);
		assertTrue(Arrays.equals(set.fittedX.stream().mapToDouble(i -> i).toArray(), x));
	}
}
