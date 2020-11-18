package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import net.relinc.libraries.sample.CompressionSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.TensionRectangularSample;
import net.relinc.libraries.sample.TensionRoundSample;

public class LoadDisplacementSampleResultsTest extends BaseTest{

	@Test
	public void test() {
		//fail("Not yet implemented");
	}
	
	@Test
	public void testGetTrueStrainCompression(){
		double[] displacement = {.01, .02, .04, .08, .16, .32};
		CompressionSample c = new CompressionSample();
		c.setLength(1.0);
		LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(c);
		results.displacement = displacement;
		double[] trueStrain = results.getTrueStrain();
		assertArrayEquals(new double[]{.01, .02, .0408, .0834, .174, 0.385 }, trueStrain, .001);
	}
	
	@Test
	public void testGetTrueStrainTensionRound(){
		double[] displacement = {.01, .02, .04, .08, .16, .32};
		TensionRoundSample c = new TensionRoundSample();
		c.setLength(1.0);
		LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(c);
		results.displacement = displacement;
		double[] trueStrain = results.getTrueStrain();
		assertArrayEquals(new double[]{.0099, .0198, .0392, .0769, .148, 0.278 }, trueStrain, .001);
	}
	
	@Test
	public void testGetTrueStrainTensionRectangular(){
		double[] displacement = {.01, .02, .04, .08, .16, .32};
		TensionRectangularSample c = new TensionRectangularSample();
		c.setLength(1.0);
		LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(c);
		results.displacement = displacement;
		double[] trueStrain = results.getTrueStrain();
		assertArrayEquals(new double[]{.0099, .0198, .0392, .0769, .148, 0.278 }, trueStrain, .001);
	}

	@Test
	public void testInterpolate() {
		double[] data = {.01, .02, .03, .04, .05};
		double[] time = {1, 2, 3,4,5};
		double[] neededTime = {3, 4};
		try {
			double[] res = LoadDisplacementSampleResults.interpolateValues(data, time, neededTime);
			assertArrayEquals(new double[]{.03, .04}, res, .001);
		} catch(Exception e) {
			fail();
		}
	}

	@Test
	public void testInterpolate2() {
		double[] data = {.01, .02, .03, .04, .05};
		double[] time = {1, 2, 3,4,5};
		double[] neededTime = {3, 4.999};
		try {
			double[] res = LoadDisplacementSampleResults.interpolateValues(data, time, neededTime);
			assertArrayEquals(new double[]{.03, .05}, res, .001);
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testInterpolate3() {
		double[] data = {.01, .02, .03, .04, .05, .06, .07, .08, .09, .1};
		double[] time = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		double[] neededTime = {3, 9.001};
		try {
			double[] res = LoadDisplacementSampleResults.interpolateValues(data, time, neededTime);
			assertArrayEquals(new double[]{.03, .09}, res, .001);
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testInterpolate4() {
		double[] data = {.01, .02, .03, .04, .05, .06, .07, .08, .09, .1};
		double[] time = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
		double[] neededTime = {-1, 9.001, 11};
		try {
			double[] res = LoadDisplacementSampleResults.interpolateValues(data, time, neededTime);
			assertArrayEquals(new double[]{-.01, .09, .11}, res, .001);
		} catch(Exception e) {
			e.printStackTrace();
			fail();
		}
	}

}
