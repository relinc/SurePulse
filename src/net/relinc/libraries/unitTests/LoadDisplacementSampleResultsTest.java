package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import net.relinc.libraries.sample.CompressionSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;

public class LoadDisplacementSampleResultsTest extends BaseTest{

	@Test
	public void test() {
		//fail("Not yet implemented");
	}
	
	@Test
	public void testGetTrueStrainCompression(){
		double[] displacement = {.01, .02, .03, .04, .05};
		CompressionSample c = new CompressionSample();
		c.setLength(1.0);
		LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(c);
		results.displacement = displacement;
		double[] trueStrain = results.getTrueStrain();
		assertArrayEquals(new double[]{.05,  .1}, trueStrain, .01);
	}
	
	@Test
	public void testGetTrueStrainTension(){
		
	}

}
