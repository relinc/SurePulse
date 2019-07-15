package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.data.Force;
import net.relinc.libraries.staticClasses.SPOperations;

public class LoadDataTests extends BaseTest {
	// this stuff initializes javaFX so that tests can be run. Some of the classes have CheckBoxes that require javaFX
	public static class AsNonApp extends Application {
		@Override
		public void start(Stage primaryStage) throws Exception {
			// noop
		}
	}

	@BeforeClass
	public static void initJFX() {
		TestingSettings.initJFX();
	}

	@Test
	public void loadForceDataTest(){
		String rawData = "Time\tVoltage\n1.0\t2.5\n2.0\t3.4\n3.0\t4.3\n4.0\t5.6\n5.0\t4.5";
//		Time	Voltage
//		1.0	2.5
//		2.0	3.4
//		3.0	4.3
//		4.0	5.6
//		5.0	4.5
		File dataFile = new File(TestingSettings.testingOutputLocation + "/Force.txt");
		SPOperations.writeStringToFile(rawData, dataFile.getPath());
		DataModel model = new DataModel();
		model.currentFile = dataFile;
		try {
			assertTrue(model.readDataFromFile(dataFile.toPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		model.rawDataSets.get(0).interpreter.DataType = dataType.TIME;
		model.rawDataSets.get(0).interpreter.multiplier = 1.0;
		model.rawDataSets.get(1).interpreter.DataType = dataType.FORCE;
		model.rawDataSets.get(1).interpreter.multiplier = .5;
		
		DataFile df = null;
		try {
			df = model.exportToDataFile(false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Force force = (Force)df.dataSubsets.get(0);
		
		assertTrue(Arrays.equals(force.Data.getData(), new double[]{5.0,6.8,8.6,11.2,9.0}));
		assertTrue(Arrays.equals(force.Data.getTimeData(), new double[]{1,2,3,4,5}));
		
		dataFile.delete();
	}
	
	@Test
	public void testForceDataset(){
		double[] time = {1, 2,3,4,5} ;
		double[] data = {1.1, 2.2, 3.3, 4.4, 5.5};
		Force force = new Force(time, data);
		assertTrue(Arrays.equals(force.Data.getData(), data));
		assertTrue(Arrays.equals(force.Data.getTimeData(), time));
		assertTrue(force.getBegin() == 0);
		assertTrue(force.getEnd() == time.length - 1);
		force.setBeginFromTimeValue(1.1);
		force.setEndFromTimeValue(3.3);
		assertTrue(force.getBegin() == 1);
		assertTrue(force.getEnd() == 3);
	}
}
