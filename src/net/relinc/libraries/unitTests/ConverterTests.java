package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.Test;
import net.relinc.libraries.staticClasses.Converter;

public class ConverterTests extends BaseTest {

	@Test
	public void testmmFromM() {
		assertEquals(1.0, Converter.mmFromM(1.0/1000), 0.0);
		assertEquals(0.0, Converter.mmFromM(0.0), 0.0);
	}
	
	@Test
	public void testmFromMm(){
		assertEquals(1.0, Converter.mFromMm(1000), 0.0);
		assertEquals(0.0, Converter.mFromMm(0.0), 0.0);
	}
	
	@Test
	public void testMeterFromInch(){
		assertEquals(.0254 * 12.5, Converter.MeterFromInch(12.5), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.MeterFromInch(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testInchFromMeter(){
		assertEquals(13.2 / .0254, Converter.InchFromMeter(13.2), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.InchFromMeter(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testKgM3FromLbin3(){
		assertEquals(13.2 * 27679.9047, Converter.KgM3FromLbin3(13.2), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.KgM3FromLbin3(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testLbin3FromKgM3(){
		assertEquals(13.2 / 27679.9047, Converter.Lbin3FromKgM3(13.2), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.Lbin3FromKgM3(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testgccFromKgm3(){
		assertEquals(2.3 / 1000, Converter.gccFromKgm3(2.3), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.gccFromKgm3(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testKgm3FromGcc(){
		assertEquals(2.3 * 1000, Converter.Kgm3FromGcc(2.3), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.Kgm3FromGcc(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpsiFromPa(){
		assertEquals(2.3 / 6894.75729, Converter.psiFromPa(2.3), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.psiFromPa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpaFromPsi(){
		assertEquals(2.3 * 6894.75729, Converter.paFromPsi(2.3), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.paFromPsi(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpaFromKsi(){
		assertEquals(3.4 * 6894757.29, Converter.paFromKsi(3.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.paFromKsi(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpaFromMpsi(){
		assertEquals(2.1 * 6894757290.0, Converter.paFromMpsi(2.1), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.paFromMpsi(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testksiFromPa(){
		assertEquals(2.3 / 6894757.29, Converter.ksiFromPa(2.3), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.ksiFromPa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testMpsiFromPa(){
		assertEquals(1.1 / 6894757290.0, Converter.MpsiFromPa(1.1), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.MpsiFromPa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testMpaFromPa(){
		assertEquals(1.4 / Math.pow(10, 6), Converter.MpaFromPa(1.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.MpaFromPa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpaFromMpa(){
		assertEquals(2.2 * Math.pow(10, 6), Converter.paFromMpa(2.2), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.paFromMpa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testGpaFromPa(){
		assertEquals(3.1 / Math.pow(10, 9), Converter.GpaFromPa(3.1), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.GpaFromPa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testpaFromGpa(){
		assertEquals(1.4 * Math.pow(10, 9), Converter.paFromGpa(1.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.paFromGpa(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testJoulesPerKilogramKelvinFromButanesPerPoundFarenheit(){
		assertEquals(3.4 * 4186.8, Converter.JoulesPerKilogramKelvinFromButanesPerPoundFarenheit(3.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.JoulesPerKilogramKelvinFromButanesPerPoundFarenheit(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testbutanesPerPoundFarenheitFromJoulesPerKilogramKelvin(){
		assertEquals(3.4 / 4186.8, Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(3.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testMeterFromMm(){
		assertEquals(3.4 / 1000.0, Converter.MeterFromMm(3.4), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.MeterFromMm(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testFootFromMeter(){
		assertEquals(4.5 / .0254 / 12, Converter.FootFromMeter(4.5), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.FootFromMeter(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testMeterFromFoot(){
		assertEquals(3.1 * .0254 * 12, Converter.MeterFromFoot(3.1), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.MeterFromFoot(0.0), TestingSettings.doubleTolerance);
	}
	
	@Test
	public void testLbfFromN(){
		assertEquals(1.2 * 0.224809, Converter.LbfFromN(1.2), TestingSettings.doubleTolerance);
		assertEquals(0.0, Converter.LbfFromN(0.0), TestingSettings.doubleTolerance);
	}
}
