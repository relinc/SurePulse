package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import net.relinc.libraries.data.ModifierFolder.LowPass;

public class LowPassTests {

	@Test
	public void testGetIncrease() {
		double delta = .0000001;
		assertEquals(.01, LowPass.getIncrease(.03, true), delta);
		assertEquals(.1, LowPass.getIncrease(.3, true), delta);
		assertEquals(1, LowPass.getIncrease(3, true), delta);
		assertEquals(1, LowPass.getIncrease(3., true), delta);
		assertEquals(10, LowPass.getIncrease(30, true), delta);
		assertEquals(10, LowPass.getIncrease(30., true), delta);
		assertEquals(100, LowPass.getIncrease(300, true), delta);
		assertEquals(1000, LowPass.getIncrease(3000, true), delta);
		
		assertEquals(1000, LowPass.getIncrease(1000, true), delta);
		assertEquals(100, LowPass.getIncrease(1000, false), delta);
		
		
		assertEquals(.1, LowPass.getIncrease(1, false), delta);
		assertEquals(.1, LowPass.getIncrease(.5, false), delta);
		assertEquals(.01, LowPass.getIncrease(.1, false), delta);
		assertEquals(.001, LowPass.getIncrease(.01, false), delta);
		assertEquals(.01, LowPass.getIncrease(.010001, false), delta);
		assertEquals(.001, LowPass.getIncrease(.004, false), delta);
		assertEquals(.0001, LowPass.getIncrease(.001, false), delta);
		assertEquals(.001, LowPass.getIncrease(.001000001, false), delta);
	}
}
