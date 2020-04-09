package net.relinc.libraries.unitTests;

import net.relinc.libraries.referencesample.ReferenceSampleJohnsonCook;
import net.relinc.libraries.referencesample.ReferenceSampleKN;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.referencesample.StressUnit;
import org.junit.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class ReferenceSampleJohnsonCookTests extends BaseTest {

	// For Al 6061-T6
	@Test
	public void testStressStrain() {

		ReferenceSampleJohnsonCook sample = new ReferenceSampleJohnsonCook(
				"test",
				"test",
				68.9e9,
				252e6,
				1.0,

				1.0,
				294.26,
				925.37,
				300.0,
				252e6,
				203.4e6,
				0.011,
				0.35,
				1.34
		);

		List<Double> strain = sample.getStrain(StressStrainMode.ENGINEERING);
		List<Double> stress = sample.getStress(StressStrainMode.ENGINEERING, StressUnit.PA);

		// print this and paste to Google Sheets to inspect graph
		// IntStream.range(0, strain.size()).mapToObj(idx -> strain.get(idx) + "," + stress.get(idx)).forEach(s -> System.out.println(s));

		// snapshot test. Fail when something changes.
		assertEquals(-942667137, strain.hashCode());
		assertEquals(-141452253, stress.hashCode());
	}

}


