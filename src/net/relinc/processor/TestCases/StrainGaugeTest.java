package net.relinc.processor.TestCases;

import org.junit.Test;

import net.relinc.libraries.application.StrainGauge;

public class StrainGaugeTest {
	@Test
	public void testLoadStrainGauge(){
		StrainGauge sg = new StrainGauge("SG", 1.0, 2.0, 3.0, 4.0, 5.0);
		String fileString = sg.stringForFile();
		
	}
}
