package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.libraries.data.ModifierFolder.ZeroOffset;

public class ZeroOffsetTests extends BaseTest{
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
			
			
	@Test
	public void testZeroOffset(){
		ZeroOffset offset = new ZeroOffset();
		offset.setZero(1.1);
		double[] data = new double[]{1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 1.1, 5.6, 5.7, 5.8};
		double[] dataCopy = data.clone();
		offset.enabled.set(true);
		offset.activated.set(true);
		double[] mod = offset.applyModifierToData(data, null);
		assertTrue(Arrays.equals(mod, Arrays.stream(dataCopy).map(x -> x - 1.1).toArray()));
		//here
	}
}
