package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.sample.CompressionSample;

public class HopkinsonBarSampleTest extends BaseTest {

	@Test
	public void testGetDisplacementFromEngineeringStrain() {
		double[] engStrain = {.001, .002, .003, .004};
		CompressionSample s = new CompressionSample();
		s.setLength(.5);
		double[] expectedDisplacement = {.0005, .001, .0015, .002};
		assertArrayEquals(expectedDisplacement, s.getDisplacementFromEngineeringStrain(engStrain), 0);
	}
	
	@Test
	public void testGetEngineeringStressFromForce(){
		double[] force = {1, 2, 3, 4};
		CompressionSample s = new CompressionSample();
		s.setDiameter(4);
		double area = s.getInitialCrossSectionalArea();
		double[] expectedStress = Arrays.stream(force).map(f -> f / area).toArray();
		assertArrayEquals(expectedStress, s.getEngineeringStressFromForce(force), 0);
	}
	
	@Test
	public void testGetForceFromTransmissionBarStrain(){
		double[] barStrain = {0, 1, 2, 3, 4};
		CompressionSample s = new CompressionSample();
		BarSetup bar = new BarSetup();
		Bar transmissionBar = new  Bar();
		transmissionBar.youngsModulus = .5;
		transmissionBar.diameter = 4;
		bar.TransmissionBar = transmissionBar;
		s.barSetup = bar;
		double area = Math.pow(2, 2) * Math.PI;
		double[] expectedForce = {-1 * 0 * .5 * area, -1 * 1 * .5 * area, -1 * 2 * .5 * area, -1 * 3 * .5 * area, -1 * 4 * .5 * area};
		assertArrayEquals(expectedForce, s.getForceFromTransmissionBarStrain(barStrain), 0);
	}
	
	@Test
	public void testGetEngineeringStrainFromIncidentBarReflectedPulseStrain(){
//		double[] time = {0, .25, .5, .75, 1};
//		double[] reflectedPulse = {0, .1, .1, .2, .3};
		// A lot more stuff.
		// TODO finish
	}
	
	@Test
	public void testGetFrontFaceForce(){
		// The front face force can only be obtained from an incident pulse and a reflected pulse. (Same SG, different pulses)
		// This is very hard to test. First need to write tests for all steps of loading data.
	}

}
