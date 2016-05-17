package net.relinc.libraries.unitTests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import net.relinc.libraries.application.FitableDataset;
import net.relinc.libraries.data.DataInterpreter.dataType;

public class FitableDatasetTests {
	@Test
	public void testFitableDataset(){
		double[] x = new double[]{0,1,2,3,4,5,6,7,8,9};
		double[] y = new double[]{0,2,4,6,8,10,12,14,16,18};
		List<Double> xList = new ArrayList<Double>();
		Arrays.stream(x).forEach(d -> xList.add(d));
		List<Double> yList = new ArrayList<Double>();
		Arrays.stream(y).forEach(d -> yList.add(d));
		FitableDataset set = new FitableDataset(xList, yList, "Hello");
		
	}
}
