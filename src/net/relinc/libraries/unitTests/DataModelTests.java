package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import org.junit.Test;

import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.application.Bar;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.StrainGauge;
import net.relinc.libraries.application.StrainGaugeOnBar;
import net.relinc.libraries.data.DataFileInterpreter;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

public class DataModelTests extends BaseTest {
	
	public DataModel loadSample52(){
		DataModel dataModel = new DataModel();
		File sample52 = new File(TestingSettings.testingInputLocation, "052/052 Pico.txt");
		try {
			 dataModel.readDataFromFile(sample52.toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return dataModel;
	}

	@Test
	public void testLoadTextFile() {
		DataModel dataModel = loadSample52();
		assertTrue(dataModel.rawDataSets.size() == 3);
		assertTrue(dataModel.dataTypeDelimiter == "\t");
		//assertEquals(SPSettings.lineSeperator, dataModel.); //Frame delimiter is not implemented. Only newline works.
		assertEquals(0, dataModel.startFrameSplitter);
		assertEquals(0, dataModel.startDataSplitter);
		assertFalse(dataModel.hasTimeData());
	}
	
	@Test
	public void testSetTimeData() {
		DataModel dataModel = loadSample52();
		
		// Set the first column to be time.
		dataModel.rawDataSets.get(0).interpreter.DataType = dataType.TIME;
		assertTrue(dataModel.hasTimeData());
		try {
			assertEquals(160004, dataModel.getTimeData().length);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		// Unset the first column
		dataModel.rawDataSets.get(0).interpreter.DataType = dataType.NULL;
		assertFalse(dataModel.hasTimeData());
		dataModel.setCollectionRate(500);
		assertTrue(dataModel.hasTimeData());
		double[] time = null;
		try {
			time = dataModel.getTimeData();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		assertEquals(160004, time.length);
		assertEquals(1.0 / 500 * 160003.0, time[time.length - 1], 0.0000001); // 3rd arg is precision
		assertEquals(0.0, time[0], 0);
	}
	
	@Test
	public void testWriteInterpreterToPath(){
		DataModel dataModel = loadSample52();
		dataModel.rawDataSets.get(0).interpreter.DataType = dataType.TIME;
		
		// Build a bar.
		StrainGauge sg = new StrainGauge("My SG", 1.0, 2.0, 3.0, 4.0, 5.0);
		File sgFile = new File(TestingSettings.testingOutputLocation, "my sg.txt");
		SPOperations.writeStringToFile(sg.stringForFile(), sgFile.getPath());
		
		StrainGaugeOnBar incidSG = new StrainGaugeOnBar(sgFile.getPath(), .5, "Incident SG");
		
		Bar incid = new Bar();
		incid.density = 1.0;
		incid.diameter = 2.0;
		incid.length = 3.0;
		incid.yield = 4.0;
		incid.youngsModulus = 5.0;
		incid.strainGauges.add(incidSG);
		
		
		Bar trans = new Bar();
		trans.density = 1.0;
		trans.diameter = 2.0;
		trans.length = 3.0;
		trans.yield = 4.0;
		trans.youngsModulus = 5.0;
		StrainGaugeOnBar transSG = new StrainGaugeOnBar(sgFile.getPath(), .5, "Transmission SG");
		trans.strainGauges.add(transSG);
		BarSetup setup = new BarSetup(incid, trans);
		
		dataModel.rawDataSets.get(1).interpreter.DataType = dataType.INCIDENTSG;
		dataModel.rawDataSets.get(1).interpreter.strainGauge = incidSG;
		dataModel.rawDataSets.get(1).interpreter.strainGaugeName = "Incident SG #1";
		dataModel.rawDataSets.get(2).interpreter.DataType = dataType.TRANSMISSIONSG;
		dataModel.rawDataSets.get(2).interpreter.strainGauge = transSG;
		dataModel.rawDataSets.get(2).interpreter.strainGaugeName = "Tranmission SG #1";
		
		File interpreterPath = new File(TestingSettings.testingOutputLocation, "interpreter.txt");
		dataModel.writeToPath(interpreterPath.getPath());
		// Interpreter is saved.
		
		DataModel dataModel2 = loadSample52();
		dataModel2.applyDataInterpreter(new DataFileInterpreter(interpreterPath.getPath(), setup));
		assertTrue(dataModel2.rawDataSets.get(0).interpreter.DataType == dataType.TIME);
		assertTrue(dataModel2.rawDataSets.get(1).interpreter.DataType == dataType.INCIDENTSG);
		assertEquals(dataModel2.rawDataSets.get(1).interpreter.strainGauge, incidSG);
		assertTrue(dataModel2.rawDataSets.get(2).interpreter.DataType == dataType.TRANSMISSIONSG);
		assertEquals(dataModel2.rawDataSets.get(2).interpreter.strainGauge, transSG);
	}
	
	@Test
	public void testExportToDataFile(){
		
	}

}
