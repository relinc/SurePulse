package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import net.relinc.libraries.sample.CompressionSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
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
	public void testGetTrueStrainTension(){
		double[] displacement = {.01, .02, .04, .08, .16, .32};
		TensionRoundSample c = new TensionRoundSample();
		c.setLength(1.0);
		LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(c);
		results.displacement = displacement;
		double[] trueStrain = results.getTrueStrain();
		assertArrayEquals(new double[]{.0099, .0198, .0392, .0769, .148, 0.278 }, trueStrain, .001);
	}

}
