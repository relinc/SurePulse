package net.relinc.libraries.unitTests;

import net.relinc.libraries.referencesample.ReferenceSampleKN;
import net.relinc.libraries.referencesample.ReferenceSampleLudwig;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.referencesample.StressUnit;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class ReferenceSampleLudwigTests extends BaseTest {

	// For Al 6061-T6
	@Test
	public void testStressStrain() {
		ReferenceSampleLudwig sample = new ReferenceSampleLudwig(
				"test",
				"test",

				68.9e9,
				252e6,

				208.8e6,
				0.28
		);

		List<Double> strain = sample.getStrain(StressStrainMode.ENGINEERING);
		List<Double> stress = sample.getStress(StressStrainMode.ENGINEERING, StressUnit.PA);

		// print this and paste to Google Sheets to inspect graph
		 // IntStream.range(0, strain.size()).mapToObj(idx -> strain.get(idx) + "," + stress.get(idx)).forEach(s -> System.out.println(s));

		// snapshot test. Fail when something changes.
		assertEquals(487190670, strain.hashCode());
		assertEquals(723382373, stress.hashCode());
	}

}


