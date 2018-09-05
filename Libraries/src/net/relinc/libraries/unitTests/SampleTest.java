package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.io.File;

import net.relinc.libraries.sample.*;
import org.junit.Test;

import net.relinc.libraries.staticClasses.SPOperations;

public class SampleTest extends BaseTest{
	//TODO: Testing (for customers?) (ha)

	@Test
	public void checkCompressionSampleCreated() {
		
		CompressionSample compressionSample = new CompressionSample();
		compressionSample.setName("jUnit Test");
		compressionSample.setDensity(1);
		compressionSample.setDiameter(2);
		compressionSample.setYoungsModulus(3);
		compressionSample.setHeatCapacity(5);
		File samplePath = new File(TestingSettings.testingOutputLocation.getPath() + "/WhereIsThis.samcomp");
		compressionSample.writeSampleToFile(samplePath.getPath());
		
		File file = new File(TestingSettings.testingOutputLocation.getPath() + "/WhereIsThis.samcomp");
		assertTrue("Create Zip File Success", file.exists());
		
		CompressionSample c = null;
		try {
			c = (CompressionSample)SPOperations.loadSample(samplePath.getPath());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		assertEquals(c.getName(), "jUnit Test");
		assertTrue(c.getDensity() == 1.0);
		assertTrue(c.getDiameter() == 2.0);
		assertTrue(c.getYoungsModulus() == 3.0);
		assertTrue(c.getHeatCapacity() == 5.0);
		
		
		if(file.exists())
			file.delete();
	}

	@Test
	public void checkSheerCompressionSampleCreated() {
		ShearCompressionSample shearCompSample = new ShearCompressionSample();
		shearCompSample.setName("Shear Compression Sample");
		shearCompSample.setGaugeWidth(5);
		shearCompSample.setGaugeHeight(4);
		shearCompSample.setDensity(3);
		shearCompSample.setLength(2);
		shearCompSample.setYoungsModulus(1);
		shearCompSample.setHeatCapacity(1);

		File samplePath = new File( TestingSettings.testingOutputLocation.getPath() + "/ShearCompression.samcomp");
		shearCompSample.writeSampleToFile(samplePath.getPath());

		ShearCompressionSample s = null;
		try {
			s = (ShearCompressionSample)SPOperations.loadSample(samplePath.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(s.getName(),"Shear Compression Sample");
		assertTrue( s.getGaugeWidth() == 5.0);
		assertTrue( s.getGaugeHeight() == 4.0);
		assertTrue( s.getDensity() == 3.0);
		assertTrue( s.getLength() == 2.0);
		assertTrue(s.getYoungsModulus() == 1.0);
		assertTrue(s.getHeatCapacity() == 1.0);
	}
	
	@Test
	public void checkTensionRectangularSampleCreated() {
		TensionRectangularSample tensionRectangularSample = new TensionRectangularSample();
		tensionRectangularSample.setName("Tension Rectangular Sample");
		tensionRectangularSample.setWidth(5);
		tensionRectangularSample.setHeight(4);
		tensionRectangularSample.setDensity(3);
		tensionRectangularSample.setLength(2);
		tensionRectangularSample.setYoungsModulus(1);
		tensionRectangularSample.setHeatCapacity(1);

		File samplePath = new File( TestingSettings.testingOutputLocation.getPath() + "/TensionRectangular.samcomp");
		tensionRectangularSample.writeSampleToFile(samplePath.getPath());

		TensionRectangularSample tRect = null;
		try {
			tRect = (TensionRectangularSample)SPOperations.loadSample(samplePath.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(tRect.getName(),"Tension Rectangular Sample");
		assertTrue( tRect.getWidth() == 5.0);
		assertTrue(tRect.getHeight() == 4.0);
		assertTrue( tRect.getDensity() == 3.0);
		assertTrue( tRect.getLength() == 2.0);
		assertTrue(tRect.getYoungsModulus() == 1.0);
		assertTrue(tRect.getHeatCapacity() == 1.0);
	}

	@Test
	public void checkTensionRoundSampleCreated() {
		TensionRoundSample tensionRoundSample = new TensionRoundSample();
		tensionRoundSample.setName("Tension Round Sample");
		tensionRoundSample.setDiameter(4);
		tensionRoundSample.setDensity(3);
		tensionRoundSample.setLength(2);
		tensionRoundSample.setYoungsModulus(1);
		tensionRoundSample.setHeatCapacity(1);

		File samplePath = new File( TestingSettings.testingOutputLocation.getPath() + "/TensionRound.samcomp");
		tensionRoundSample.writeSampleToFile(samplePath.getPath());

		TensionRoundSample tRound = null;
		try {
			tRound = (TensionRoundSample)SPOperations.loadSample(samplePath.getPath());
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertEquals(tRound.getName(),"Tension Round Sample");
		assertTrue(tRound.getDiameter() == 4.0);
		assertTrue( tRound.getDensity() == 3.0);
		assertTrue( tRound.getLength() == 2.0);
		assertTrue(tRound.getYoungsModulus() == 1.0);
		assertTrue(tRound.getHeatCapacity() == 1.0);
	}

	
}
