package net.relinc.datafileparser.application.Tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.relinc.datafileparser.application.Model;

public class ModelTest {

	@Test
	public void initTest(){
		Model m = new Model("\n", ",");
		assertTrue(m.getFrameDelimiter().equals("\n"));
		assertTrue(m.getDatapointDelimiter().equals(","));
	}
	
	@Test
	public void testCSV(){
		String csv = "1,2,3\n4,5,6\n7,8,9\n10,11,12";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setStartFrameDelimiter(0);
		m.setEndFrameDelimiter(3);
		m.setStartDatapointDelimiter(0);
		m.setEndDatapointDelimiter(2);
		List<List<String>> list = m.parse();
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("4");
		col1.add("7");
		col1.add("10");
		
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("5");
		col2.add("8");
		col2.add("11");
		
		ArrayList<String> col3 = new ArrayList<String>();
		col3.add("3");
		col3.add("6");
		col3.add("9");
		col3.add("12");
		
		assertTrue(list.get(0).equals(col1));
		assertTrue(list.get(1).equals(col2));
		assertTrue(list.get(2).equals(col3));
	}
	
	@Test
	public void testCSV2(){
		String csv = "GarbageRow\n1,2,3\n4,5,6\n7,8,9\n10,11,12";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setStartFrameDelimiter(1);
		m.setEndFrameDelimiter(4);
		m.setStartDatapointDelimiter(0);
		m.setEndDatapointDelimiter(2);
		List<List<String>> list = m.parse();
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("4");
		col1.add("7");
		col1.add("10");
		
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("5");
		col2.add("8");
		col2.add("11");
		
		ArrayList<String> col3 = new ArrayList<String>();
		col3.add("3");
		col3.add("6");
		col3.add("9");
		col3.add("12");
		
		assertTrue(list.get(0).equals(col1));
		assertTrue(list.get(1).equals(col2));
		assertTrue(list.get(2).equals(col3));
	}
	
	@Test
	public void testCSV3(){
		String csv = "GarbageRow\n1,2,3\n4,5,6\n7,8,9\n10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setStartFrameDelimiter(1);
		m.setEndFrameDelimiter(4);
		m.setStartDatapointDelimiter(0);
		m.setEndDatapointDelimiter(2);
		List<List<String>> list = m.parse();
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("4");
		col1.add("7");
		col1.add("10");
		
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("5");
		col2.add("8");
		col2.add("11");
		
		ArrayList<String> col3 = new ArrayList<String>();
		col3.add("3");
		col3.add("6");
		col3.add("9");
		col3.add("12");
		
		assertTrue(list.get(0).equals(col1));
		assertTrue(list.get(1).equals(col2));
		assertTrue(list.get(2).equals(col3));
	}
	
	@Test
	public void testCSV4(){
		String csv = "GarbageRow\nhi,1,2,3\nhi,4,5,6\nhi,7,8,9\nhi,10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setStartFrameDelimiter(1);
		m.setEndFrameDelimiter(4);
		m.setStartDatapointDelimiter(1);
		m.setEndDatapointDelimiter(3);
		List<List<String>> list = m.parse();
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("4");
		col1.add("7");
		col1.add("10");
		
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("5");
		col2.add("8");
		col2.add("11");
		
		ArrayList<String> col3 = new ArrayList<String>();
		col3.add("3");
		col3.add("6");
		col3.add("9");
		col3.add("12");
		
		assertTrue(list.get(0).equals(col1));
		assertTrue(list.get(1).equals(col2));
		assertTrue(list.get(2).equals(col3));
	}
	
	@Test
	public void testCSV5(){
		String csv = "GarbageRow\nhi,1,2,3,4\nhi,4,5,6\nhi,7,8,9\nhi,10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setStartFrameDelimiter(1);
		m.setEndFrameDelimiter(4);
		m.setStartDatapointDelimiter(1);
		m.setEndDatapointDelimiter(3);
		List<List<String>> list = m.parse();
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("4");
		col1.add("7");
		col1.add("10");
		
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("5");
		col2.add("8");
		col2.add("11");
		
		ArrayList<String> col3 = new ArrayList<String>();
		col3.add("3");
		col3.add("6");
		col3.add("9");
		col3.add("12");
		
		assertTrue(list.get(0).equals(col1));
		assertTrue(list.get(1).equals(col2));
		assertTrue(list.get(2).equals(col3));
	}

}
