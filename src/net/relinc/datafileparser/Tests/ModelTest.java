package net.relinc.datafileparser.Tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import net.relinc.datafileparser.application.Model;
import net.relinc.datafileparser.application.ParseCandidate;

public class ModelTest {
	
	String testFile1 = "pad,pad,pad,pad\npad,1,2,pad\npad,1,2,pad\npad,1,2,pad\npad,pad,pad,pad,\n";

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
	
	//------------------------------------------------
	// Start auto parameter setting tests.
	//------------------------------------------------
	@Test
	public void testCSV6(){
		String csv = "1,2,3\n4,5,6\n7,8,9\n10,11,12";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setParsingParametersAutomatically();
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
	public void testCSV7(){
		String csv = "GarbageRow\n1,2,3\n4,5,6\n7,8,9\n10,11,12";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setParsingParametersAutomatically();
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
	public void testCSV8(){
		String csv = "GarbageRow\n1,2,3\n4,5,6\n7,8,9\n10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setParsingParametersAutomatically();
		m.printParsingParameters();
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
	public void testCSV9(){
		String csv = "GarbageRow\nhi,1,2,3\nhi,4,5,6\nhi,7,8,9\nhi,10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setParsingParametersAutomatically();
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
	public void testCSV10(){
		String csv = "GarbageRow\nhi,1,2,3,4\nhi,4,5,6\nhi,7,8,9\nhi,10,11,12\nGarbageRow";
		Model m = new Model("\n", ",");
		m.setDataFile(csv);
		m.setParsingParametersAutomatically();
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
	public void testCSV11(){
		Model m = new Model("\n", ",");
		String file = "";
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically() == false);
	}
	
	@Test
	public void testCSV12(){
		Model m = new Model("\n", ",");
		String file = "asdasdada";
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically() == false);
	}
	
	@Test
	public void testCSV13(){
		Model m = new Model("\n", ",");
		String file = "\n\n\n\n\n";
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically() == false);
	}
	
	@Test
	public void testCSV14(){
		Model m = new Model("\n", ",");
		String file = "pad,pad,pad,pad\npad,1,2,pad\npad,1,2,pad\npad,1,2,pad\npad,pad,pad,pad,\n";
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically());
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("1");
		col1.add("1");
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("2");
		col2.add("2");
		List<List<String>> data = m.parse();
		assertTrue(data.get(0).equals(col1));
		assertTrue(data.get(1).equals(col2));
	}
	
	@Test
	public void testCSV15(){
		Model m = new Model("\n", ",");
		String file = "pad,pad,pad,pad\npad,1,2,pad\npad,1,2,pad\npad,1,2,pad\npad,pad,pad,pad,\n";
		file = file.replaceAll(",", "\t");
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically());
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("1");
		col1.add("1");
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("2");
		col2.add("2");
		List<List<String>> data = m.parse();
		assertTrue(data.get(0).equals(col1));
		assertTrue(data.get(1).equals(col2));
	}

	@Test
	public void testGetParseCandidates(){
		Model m = new Model("\n", ",");
		String file = "pad,pad,pad,pad\npad,1,2,pad\npad,1,2,pad\npad,1,2,pad\npad,pad,pad,pad,\n";
		m.setDataFile(file);
		assertTrue(m.setParsingParametersAutomatically());
		ArrayList<String> col1 = new ArrayList<String>();
		col1.add("1");
		col1.add("1");
		col1.add("1");
		ArrayList<String> col2 = new ArrayList<String>();
		col2.add("2");
		col2.add("2");
		col2.add("2");
		List<List<String>> data = m.parse();
		assertTrue(data.get(0).equals(col1));
		assertTrue(data.get(1).equals(col2));
		
		List<List<ParseCandidate>> list = m.getParseCandidates();
		int row = 0;
		int col = 0;
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		col = 0;
		row++;
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		col = 0;
		row++;
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		col = 0;
		row++;
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == true);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		col = 0;
		row++;
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		assertTrue(list.get(row).get(col++).isParsable() == false);
		col = 0;
		row++;
	}
}
