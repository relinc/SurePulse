package net.relinc.libraries.unitTests;


import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import org.junit.Test;

import net.relinc.libraries.application.StrainGauge;
import net.relinc.libraries.application.StrainGaugeOnBar;
import net.relinc.libraries.staticClasses.SPOperations;

public class StrainGaugeTest extends BaseTest{
	@Test
	public void testLoadStrainGauge(){
		StrainGauge sg = new StrainGauge("SG", 1.0, 2.0, 3.0, 4.0, 5.0);
		String fileString = sg.stringForFile();
		File file = new File(TestingSettings.testingOutputLocation + "/" + sg.genericName + ".txt");
		SPOperations.writeStringToFile(fileString, file.getPath());
		StrainGauge loadSG = new StrainGauge(file.getPath());
		
		assertTrue(loadSG.gaugeFactor == sg.gaugeFactor);
		assertTrue(loadSG.genericName.equals(sg.genericName));
		assertTrue(loadSG.length == sg.length);
		assertTrue(loadSG.resistance == sg.resistance);
		assertTrue(loadSG.shuntResistance == sg.shuntResistance);
		assertTrue(loadSG.voltageCalibrated == sg.voltageCalibrated);
		
		file.delete();
	}
	
	@Test
	public void testStrainGaugeGaugeFactor(){
		StrainGauge sg = new StrainGauge("SG", 1.0, 2.0, 3.0, 4.0, 5.0);
		String fileString = sg.stringForFile();
		File file = new File(TestingSettings.testingOutputLocation + "/" + sg.genericName + ".txt");
		SPOperations.writeStringToFile(fileString, file.getPath());
		StrainGaugeOnBar sGaugeOnBar = new StrainGaugeOnBar(file.getPath(), 1, "Specific");
		assertTrue(sGaugeOnBar.getVoltageFactor() == .08);
		double[] voltage = {1,2,3,4,5};
		double[] strain = Arrays.stream(voltage).map(x -> x * sGaugeOnBar.getVoltageFactor()).toArray();
		assertArrayEquals(strain, sGaugeOnBar.getStrain(voltage), 0);
		file.delete();
	}
	
}
