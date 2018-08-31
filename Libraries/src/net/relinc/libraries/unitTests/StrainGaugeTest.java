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
		File file = new File(TestingSettings.testingOutputLocation + "/" + sg.genericName + ".json");
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
		File file = new File(TestingSettings.testingOutputLocation + "/" + sg.genericName + ".json");
		SPOperations.writeStringToFile(fileString, file.getPath());

		StrainGaugeOnBar sGaugeOnBar = new StrainGaugeOnBar(file.getPath(), 1, "Specific");
		StrainGaugeOnBar sg_bar_copy = new StrainGaugeOnBar(sg,1,"Specific");

		assertTrue(sGaugeOnBar.getVoltageFactor() == sg_bar_copy.getVoltageFactor());//.08);
		double[] voltage = {1,2,3,4,5};
		double[] strain = Arrays.stream(voltage).map(x -> x * sGaugeOnBar.getVoltageFactor()).toArray();
		assertArrayEquals(strain, sGaugeOnBar.getStrain(voltage), 0);

		file.delete();

	}
	@Test
	public void jsonStrainGaugeOnBarWorks(){
		//create stain gauge
		StrainGauge sg = new StrainGauge("SG", 1.0, 2.0, 3.0, 4.0, 5.0);
		StrainGaugeOnBar sGaugeOnBar = new StrainGaugeOnBar(sg, 1, "Specific");
		//write strain gauge
		String fileString = sGaugeOnBar.stringForFile();
		File on_bar_file = new File(TestingSettings.testingOutputLocation + "/" + sGaugeOnBar.specificName + ".json");
		SPOperations.writeStringToFile(fileString, on_bar_file.getPath());
		//read strain gauge
		StrainGaugeOnBar read_fully_speced = new StrainGaugeOnBar(on_bar_file.getPath());


		assertTrue(read_fully_speced.specificName.equals(sGaugeOnBar.specificName));

		on_bar_file.delete();
	}

	@Test
	public void testStrainGaugeResistance() {
		StrainGauge sg = new StrainGauge( "SG_RT", 1.0, 3.14, 2.0, 3.0, 4.0);
		String fileString = sg.stringForFile();
		File file = new File(TestingSettings.testingOutputLocation + "/" + sg.genericName + ".json");
		SPOperations.writeStringToFile(fileString, file.getPath());
		file.delete();

	}


}
