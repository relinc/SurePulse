package net.relinc.libraries.unitTests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import com.sun.javafx.binding.StringFormatter;

import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataModel;
import net.relinc.libraries.data.Force;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.staticClasses.SPOperations;

public final class TestingUtilities {
	// This provides utilities for testing SPLibraries.
	// A Tension Rectangular example data is in TestFiles/052
	private static DataFile loadCompressionSampleDataFile(){
		String rawData = "Time\tVoltage\tVoltage\n"
				+ "1.0\t2.5\t1.1\n"
				+ "2.0\t3.4\t2.2\n"
				+ "3.0\t4.3\t3.3\n"
				+ "4.0\t5.6\t4.4\n"
				+ "5.0\t4.5\t5.0\n";
		File dataFile = new File(TestingSettings.testingOutputLocation + "/Compression.txt");
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
		model.rawDataSets.get(1).interpreter.DataType = dataType.INCIDENTSG;
		model.rawDataSets.get(1).interpreter.multiplier = .5;
		model.rawDataSets.get(2).interpreter.DataType = dataType.TRANSMISSIONSG;
		model.rawDataSets.get(2).interpreter.multiplier = .5;
		
		DataFile df = null;
		try {
			df = model.exportToDataFile(false, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return df;
	}
	
	
//	String rawData = "Time\tVoltage\n1.0\t2.5\n2.0\t3.4\n3.0\t4.3\n4.0\t5.6\n5.0\t4.5";
////	Time	Voltage
////	1.0	2.5
////	2.0	3.4
////	3.0	4.3
////	4.0	5.6
////	5.0	4.5
//	File dataFile = new File(TestingSettings.testingLocation + "/Force.txt");
//	SPOperations.writeStringToFile(rawData, dataFile.getPath());
//	DataModel model = new DataModel();
//	model.currentFile = dataFile;
//	try {
//		assertTrue(model.readDataFromFile(dataFile.toPath()));
//	} catch (IOException e) {
//		e.printStackTrace();
//	}
//	
//	model.rawDataSets.get(0).interpreter.DataType = dataType.TIME;
//	model.rawDataSets.get(0).interpreter.multiplier = 1.0;
//	model.rawDataSets.get(1).interpreter.DataType = dataType.FORCE;
//	model.rawDataSets.get(1).interpreter.multiplier = .5;
//	
//	DataFile df = null;
//	try {
//		df = model.exportToDataFile(false, false);
//	} catch (Exception e) {
//		e.printStackTrace();
//	}
//	
//	Force force = (Force)df.dataSubsets.get(0);
//	
//	assertTrue(Arrays.equals(force.Data.data, new double[]{5.0,6.8,8.6,11.2,9.0}));
//	assertTrue(Arrays.equals(force.Data.timeData, new double[]{1,2,3,4,5}));
//	
//	dataFile.delete();
}
