package net.relinc.libraries.unitTests;
import static org.junit.Assert.*;
import org.junit.Test;

import net.relinc.libraries.staticClasses.TriangularLowPassFilter;

public class TriangularFilterTest extends BaseTest {
	@Test
	public void runFilter(){
		double[] testArray= new double[20];
		for(int idx=0; idx<20;idx++)
			testArray[idx]=Math.sin(idx/4);
		double[] result=TriangularLowPassFilter.triangularLowPass(testArray,.2);
		assertTrue(Math.abs(result[0])<.01);
		assertTrue(Math.abs(result[10]-.909)<.01);
		assertTrue(Math.abs(result[19]+.756)<.01);
	}
}
