package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import net.relinc.libraries.sample.CompressionSample;

public class CompressionSampleTests {

	@Test
	public void testGetTrueStressFromEngStressAndEngStrain() {
		CompressionSample sample = new CompressionSample();
		double[] engStrain = {0, .001, .002, .003, .005};
		double[] engStress = {0, 2, 4, 6, 7};
		double[] expectedTrueStress = {0, 2 * .999, 4 * .998, 6 * .997, 7 * .995};
		double[] trueStress = sample.getTrueStressFromEngStressAndEngStrain(engStress, engStrain);
		assertArrayEquals(expectedTrueStress, trueStress, 0);
	}
	
	@Test
	public void testGetInitialCrossSectionalArea(){
		CompressionSample s = new CompressionSample();
		s.setDiameter(1.5);
		assertTrue(s.getInitialCrossSectionalArea() == Math.pow(1.5 / 2, 2) * Math.PI); 
	}

}
