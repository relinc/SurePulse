package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.junit.Test;

import net.relinc.libraries.staticClasses.SPOperations;

public class SPOperationsTests extends BaseTest {

	@Test
	public void testWriteStringToFile(){
		File file = new File(TestingSettings.testingOutputLocation + "/testWrite.txt");
		String myString = "String";
		SPOperations.writeStringToFile(myString, file.getPath());
		assertTrue(file.exists());
		assertEquals(SPOperations.readStringFromFile(file.getPath()).trim(), myString);
	}
	
	@Test
	public void testWriteListToFile(){
		ArrayList<String> list = new ArrayList<String>();
		list.add("s1");
		list.add("s2");
		File file = new File(TestingSettings.testingOutputLocation + "/testListWrite.txt");
		if(file.exists())
			file.delete();
		SPOperations.writeListToFile(list, file.getPath());
		SPOperations.writeListToFile(list, file.getPath()); // writeListToFile appends to the file. 
		assertEquals("s1s2s1s2", SPOperations.readStringFromFile(file.getPath()).trim());
	}
	
	@Test
	public void testDeleteFolder(){
		File testDir = new File(TestingSettings.testingOutputLocation + "/TestFolder");
		SPOperations.deleteFolder(testDir);
		assertTrue(testDir.mkdir());
		File dir1 = new File(testDir, "another");
		assertTrue(dir1.mkdir());
		File f1 = new File(dir1, "file.txt");
		SPOperations.writeStringToFile("Hello", f1.getPath());
		File f2 = new File(testDir, "j.txt");
		SPOperations.writeStringToFile("afaf", f2.getPath());
		SPOperations.deleteFolder(testDir);
		assertFalse(testDir.exists());
	}
	
	@Test
	public void testCopyFolder(){
		File testDir = new File(TestingSettings.testingOutputLocation + "/TestFolder");
		SPOperations.deleteFolder(testDir);
		assertTrue(testDir.mkdir());
		File dir1 = new File(testDir, "another");
		assertTrue(dir1.mkdir());
		File f1 = new File(dir1, "file.txt");
		SPOperations.writeStringToFile("Hello", f1.getPath());
		File f2 = new File(testDir, "j.txt");
		SPOperations.writeStringToFile("afaf", f2.getPath());
		
		File dest = new File(TestingSettings.testingOutputLocation + "/testCopy");
		if(dest.exists())
			SPOperations.deleteFolder(dest);
		assertTrue(dest.mkdir());
		try {
			SPOperations.copyFolder(testDir, dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		assertTrue(dest.exists());
		assertTrue(new File(dest, "j.txt").exists());
	}
	
	@Test
	public void testGetSampleType(){
		
	}
	
	@Test
	public void testLoadSampleParametersOnly(){
		
	}
	
	@Test
	public void testLoadSample(){
		
	}
	
	@Test
	public void testGetSampleTypeFromSampleParametersString(){
		
	}
	
	@Test
	public void testGetDerivative(){
		double[] time = {1, 2, 3, 4, 5, 6};
		double[] data = {1, 2, 3, 4, 5, 6};
		double[] expected = {0, 1, 1, 1, 1, 1};
		assertArrayEquals(expected, SPOperations.getDerivative(time, data), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testGetDerivative2(){
		double[] time = {1, 2, 3, 4, 5, 6};
		double[] data = {1, 4, 9, 16, 25, 36};
		double[] expected = {0, 3, 5, 7, 9, 11};
		assertArrayEquals(expected, SPOperations.getDerivative(time, data), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testGetDerivative3(){
		double[] time = {};
		double[] data = {};
		assertNull(SPOperations.getDerivative(time, data));
	}
	
	@Test
	public void testRound(){
		double d1 = 1.1236;
		assertEquals(1.1, SPOperations.round(d1, 1), TestingSettings.doubleTolerance);
		assertEquals(1.12, SPOperations.round(d1, 2), TestingSettings.doubleTolerance);
		assertEquals(1.124, SPOperations.round(d1, 3), TestingSettings.doubleTolerance);
		assertEquals(112341.1231, SPOperations.round(112341.1231, 20), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testFindFirstIndexGreaterorEqualToValue(){
		double[] data = {1, 2, 3, 4, 5, 6};
		assertEquals(3, SPOperations.findFirstIndexGreaterorEqualToValue(data, 4));
		assertEquals(-1, SPOperations.findFirstIndexGreaterorEqualToValue(data, 10));
		assertEquals(0, SPOperations.findFirstIndexGreaterorEqualToValue(data, -1));
		assertEquals(-1, SPOperations.findFirstIndexGreaterorEqualToValue(new double[]{}, -1));
	}
	
	@Test
	public void testFindFirstIndexGreaterThanValue(){
		double[] data = {1, 2, 3, 4, 5, 6};
		assertEquals(4, SPOperations.findFirstIndexGreaterThanValue(data, 4));
		assertEquals(-1, SPOperations.findFirstIndexGreaterThanValue(data, 10));
		assertEquals(0, SPOperations.findFirstIndexGreaterThanValue(data, -1));
		assertEquals(-1, SPOperations.findFirstIndexGreaterThanValue(new double[]{}, -1));
	}
	
	@Test
	public void testIntegrate(){
		double[] x = {1, 2, 3, 4};
		double[] y = {1, 2, 3, 4};
		double[] r = SPOperations.integrate(x, y, 1, 2);
		assertArrayEquals(new double[]{0, 3}, r, TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testIntegrate2(){
		double[] x = {};
		double[] y = {};
		double[] r = SPOperations.integrate(x, y, 1, 2);
		assertArrayEquals(new double[]{0, 0}, r, TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testCountContentsInFolder(){
		File dir = new File(TestingSettings.testingOutputLocation + "/TestCountContents");
		if(dir.exists())
			SPOperations.deleteFolder(dir);
		assertTrue(dir.mkdir());
		File dir1 = new File(dir, "dir1");
		assertTrue(dir1.mkdir());
		
		File textFile = new File(dir1, "here.txt");
		SPOperations.writeStringToFile("jja", textFile.getPath());
		assertEquals(1, SPOperations.countContentsInFolder(dir).get(0).intValue());
		assertEquals(1, SPOperations.countContentsInFolder(dir).get(1).intValue());
	}
	
	@Test
	public void testStripExtension(){
		assertEquals("file", SPOperations.stripExtension("file.txt"));
		assertEquals("", SPOperations.stripExtension(""));
		assertEquals("file.txt", SPOperations.stripExtension("file.txt.txt"));
	}
	
	@Test
	public void testGetExtension(){
		assertEquals(".txt", SPOperations.getExtension("file.txt"));
		assertEquals("", SPOperations.getExtension(""));
	}
	
	@Test
	public void testGetLatestDataProcessorVersionAvailable(){
		assertEquals("SUREPulse-1.10.9.exe", SPOperations.getLatestDataProcessorVersionAvailable());
	}
	
	@Test
	public void testGetLatestDataProcessorVersionNumber(){
		assertEquals("1.10.9", SPOperations.getLatestDataProcessorVersionNumber());
	}
}
