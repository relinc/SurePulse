package net.relinc.libraries.unitTests;

import net.relinc.libraries.referencesample.ReferenceSample;
import net.relinc.libraries.referencesample.ReferenceSampleKN;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.referencesample.StressUnit;
import net.relinc.libraries.sample.CompressionSample;
import org.junit.Test;
import sun.misc.Ref;

import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReferenceSampleKNTests extends BaseTest {

	// For Al 6061-T6
	@Test
	public void testStressStrain() {
		ReferenceSampleKN sample = new ReferenceSampleKN("test", "test", 530e6, 0.14048, 68.9e9, 252e6 );
		List<Double> strain = sample.getStrain(StressStrainMode.ENGINEERING);
		List<Double> stress = sample.getStress(StressStrainMode.ENGINEERING, StressUnit.PA);

		// print this and paste to Google Sheets to inspect graph
		// IntStream.range(0, strain.size()).mapToObj(idx -> strain.get(idx) + "," + stress.get(idx)).forEach(s -> System.out.println(s));

		// snapshot test. Fail when something changes.
		assertEquals(-942667137, strain.hashCode());
		assertEquals(1781862058, stress.hashCode());
	}

}


