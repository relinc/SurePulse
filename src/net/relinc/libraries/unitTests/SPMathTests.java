package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;
import org.omg.Messaging.SyncScopeHelper;

import net.relinc.libraries.staticClasses.SPMath;

public class SPMathTests extends BaseTest {

	@Test
	public void testGetEngStrainFromLagrangianStrain() {
		double[] langStrain = {.1, .2, .25, .4, .5};
		double[] expected = {0.09544511501033215, 0.18321595661992318, 0.22474487139158894, 0.34164078649987384, 0.41421356237309515};
		assertArrayEquals(expected, SPMath.getEngStrainFromLagrangianStrain(langStrain), .0000000001);
	}

}
