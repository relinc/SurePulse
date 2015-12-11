package net.relinc.processor.TestCases;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import net.relinc.processor.sample.CompressionSample;
import net.relinc.processor.staticClasses.SPSettings;

public class CreateSampleJunit {
	//TODO: Testing (for customers?) (ha)
	@Test
	public void checkSampleCreatedTest() {
		CompressionSample compressionSample = new CompressionSample();
		compressionSample.setName("jUnit Test");
		compressionSample.setDensity(1);
		compressionSample.setDiameter(2);
		compressionSample.setYoungsModulus(3);
		compressionSample.setHeatCapacity(4);
		compressionSample.writeSampleToFile(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Sample Data/Compression/");
		File file = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Sample Data/Compression/" + compressionSample.getName() + ".zip");
		assertTrue("Create Zip File Success", file.exists());
		
		if(file.exists())
			file.delete();
	}
	
}
