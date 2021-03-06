package net.relinc.libraries.unitTests;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import net.relinc.libraries.data.ModifierFolder.Resampler;
import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Force;
import net.relinc.libraries.staticClasses.SPSettings;

public class ResamplerTests extends BaseTest{
	
	//this stuff initializes javaFX so that tests can be run.
	public static class AsNonApp extends Application {
	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        // noop
	    }
	}

	@BeforeClass
	public static void initJFX() {
		TestingSettings.initJFX();
	}
	
	private Resampler createResampler(){
		Resampler r = new Resampler();
		r.enabled.set(true);
		r.activated.set(true);
		return r;
	}
	
	private DataSubset getDataSubset(){
		return new Force(new double[]{1, 2,3}, new double[]{1,2,3});
	}
	
//	@Test
//	public void testReducerModifier(){
//		Resampler r = createResampler();
//		r.setUserDataPoints(5);
//		double[] pts = {1,2,3,4,5,6,7,8,9,10};
//		double[] reduced = r.applyModifierToData(pts, getDataSubset());
//		assertTrue(Arrays.equals(reduced, new double[]{1.0,3.25, 5.5, 7.75, 10.0}));
//	}
	
//	@Test
//	public void testReducerModifier2(){
//		Resampler r = createResampler();
//		r.setUserDataPoints(20);
//		double[] pts = {1,2,3,4,5,6,7,8,9,10};
//		double[] reduced = r.applyModifierToData(pts, getDataSubset());
//		assertTrue(reduced.length == 20);
//	}

	
	@Test
	public void testResamplerCreation(){
		Resampler r = createResampler();
		r.setUserDataPoints(10);
		String file = r.getStringForFileWriting();
		String[] lines = file.split(SPSettings.lineSeperator);
		Resampler r2 = new Resampler();
		r2.readModifierFromString(lines[0]);
		assertTrue(r2.getUserDataPoints() == r.getUserDataPoints());
	}


}
