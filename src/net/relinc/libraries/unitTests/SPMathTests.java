package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;
import net.relinc.libraries.staticClasses.SPMath;

public class SPMathTests extends BaseTest {

	@Test
	public void testGetEngStrainFromLagrangianStrain() {
		double[] langStrain = {.1, .2, .25, .4, .5};
		double[] expected = {0.09544511501033215, 0.18321595661992318, 0.22474487139158894, 0.34164078649987384, 0.41421356237309515};
		assertArrayEquals(expected, SPMath.getEngStrainFromLagrangianStrain(langStrain), .0000000001);
	}
	
	@Test
	public void testGetEngStrainFromLagrangianStrain2() {
		double[] langStrain = {};
		double[] expected = {};
		assertArrayEquals(expected, SPMath.getEngStrainFromLagrangianStrain(langStrain), 0.0);
	}
	
	@Test
	public void testDiluteData2(){
		double[] data = {1, 2, 3, 4, 5, 6, 7, 8};
		assertArrayEquals(data, SPMath.diluteData(data, 8).get(0), 0.0);
		assertArrayEquals(new double[]{0, 1, 2, 3, 4, 5, 6, 7}, SPMath.diluteData(data, 8).get(1), 0.0);
	}
	
	@Test
	public void testDiluteData3(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(new double[]{1.0,3.0,5.0,7.0,9.0}, SPMath.diluteData(pts, 5).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData4(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(pts, SPMath.diluteData(pts, 20).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData5(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(new double[]{1,6}, SPMath.diluteData(pts, 2).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData6(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(pts, SPMath.diluteData(pts, 10).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData7(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(new double[]{1,2,3,4,5,6,7,8,9}, SPMath.diluteData(pts, 9).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData8(){
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		assertArrayEquals(new double[]{1,2,3,4,5,6,7}, SPMath.diluteData(pts, 7).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData9(){
		double[] pts = {};
		assertArrayEquals(new double[]{}, SPMath.diluteData(pts, 7).get(0), 0.0);
	}
	
	@Test
	public void testDiluteData10(){
		double[] pts = {1,2,3};
		assertArrayEquals(new double[]{}, SPMath.diluteData(pts, 0).get(0), 0.0);
	}
	
	@Test
	public void testSubtractFrom(){
		double[] data = {1.1, 2.1, 2.2, 4.5};
		assertArrayEquals(new double[]{1,  2, 2.1, 4.4}, SPMath.subtractFrom(data, .1), 0.0);
	}
	
	@Test
	public void testSubtractFrom2(){
		double[] data = {};
		assertArrayEquals(new double[]{}, SPMath.subtractFrom(data, 0), 0.0);
	}
	
	@Test
	public void testGetPicoArrowIncrease() {
		double delta = .0000001;
		assertEquals(.01, SPMath.getPicoArrowIncrease(.03, true), delta);
		assertEquals(.1, SPMath.getPicoArrowIncrease(.3, true), delta);
		assertEquals(1, SPMath.getPicoArrowIncrease(3, true), delta);
		assertEquals(1, SPMath.getPicoArrowIncrease(3., true), delta);
		assertEquals(10, SPMath.getPicoArrowIncrease(30, true), delta);
		assertEquals(10, SPMath.getPicoArrowIncrease(30., true), delta);
		assertEquals(100, SPMath.getPicoArrowIncrease(300, true), delta);
		assertEquals(1000, SPMath.getPicoArrowIncrease(3000, true), delta);
		
		assertEquals(1000, SPMath.getPicoArrowIncrease(1000, true), delta);
		assertEquals(100, SPMath.getPicoArrowIncrease(1000, false), delta);
		
		
		assertEquals(.1, SPMath.getPicoArrowIncrease(1, false), delta);
		assertEquals(.1, SPMath.getPicoArrowIncrease(.5, false), delta);
		assertEquals(.01, SPMath.getPicoArrowIncrease(.1, false), delta);
		assertEquals(.001, SPMath.getPicoArrowIncrease(.01, false), delta);
		assertEquals(.01, SPMath.getPicoArrowIncrease(.010001, false), delta);
		assertEquals(.001, SPMath.getPicoArrowIncrease(.004, false), delta);
		assertEquals(.0001, SPMath.getPicoArrowIncrease(.001, false), delta);
		assertEquals(.001, SPMath.getPicoArrowIncrease(.001000001, false), delta);
	}

}
