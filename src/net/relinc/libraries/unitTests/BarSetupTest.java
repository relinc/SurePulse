package net.relinc.libraries.unitTests;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;

public class BarSetupTest {
	@Test
	public void checkBarSetupCreatedTest(){
		if(!TestingSettings.testingLocation.exists())
			TestingSettings.testingLocation.mkdirs();
		BarSetup setup = new BarSetup(new Bar(), new Bar());
		setup.name = "Bar Name";
		setup.IncidentBar.name = "Incid Name";
		setup.IncidentBar.length = 1.0;
		setup.IncidentBar.density = 2.0;
		setup.IncidentBar.diameter = 3.0;
		setup.IncidentBar.youngsModulus = 4.0;
		setup.IncidentBar.speedLimit = 5.0;
		
		//StrainGaugeOnBar incidSG = new StrainGaugeOnBar(filePath, DistanceToSample, specificname)
		
		setup.TransmissionBar.length = 6.0;
		setup.TransmissionBar.density = 7.0;
		setup.TransmissionBar.diameter = 8.0;
		setup.TransmissionBar.youngsModulus = 9.0;
		setup.TransmissionBar.speedLimit = 10.0;
		
		File setupFile = new File(TestingSettings.testingLocation.getPath() + "/" + setup.name);
		setup.writeToFile(setupFile.getPath());
		File writtenFile = new File(setupFile.getPath() + ".zip");
		assertTrue(writtenFile.exists());
		
		BarSetup loaded = new BarSetup(writtenFile.getPath());
		System.out.println(loaded.name);
		
		assertTrue(loaded.name.equals("Bar Name"));
		assertTrue(loaded.IncidentBar.name.equals("Incid Name"));
		assertTrue(loaded.IncidentBar.length == 1.0);
		assertTrue(loaded.IncidentBar.density == 2.0);
		assertTrue(loaded.IncidentBar.diameter == 3.0);
		assertTrue(loaded.IncidentBar.youngsModulus == 4.0);
		assertTrue(loaded.IncidentBar.speedLimit == 5.0);
		assertTrue(setup.TransmissionBar.length == 6.0);
		assertTrue(loaded.TransmissionBar.density == 7.0);
		assertTrue(loaded.TransmissionBar.diameter == 8.0);
		assertTrue(loaded.TransmissionBar.youngsModulus == 9.0);
		assertTrue(loaded.TransmissionBar.speedLimit == 10.0);
		
		if(writtenFile.exists())
			writtenFile.delete();
	}
	
	
}
