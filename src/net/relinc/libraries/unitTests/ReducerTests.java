package net.relinc.libraries.unitTests;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.libraries.data.ModifierFolder.Reducer;
import net.relinc.libraries.staticClasses.SPSettings;

public class ReducerTests{
	
	//this stuff initializes javaFX so that tests can be run.
	public static class AsNonApp extends Application {
	    @Override
	    public void start(Stage primaryStage) throws Exception {
	        // noop
	    }
	}

	@BeforeClass
	public static void initJFX() {
	    Thread t = new Thread("JavaFX Init Thread") {
	        public void run() {
	            Application.launch(AsNonApp.class, new String[0]);
	        }
	    };
	    t.setDaemon(true);
	    t.start();
	}
	
	@Test
	public void testReducerModifier(){
		Reducer r = new Reducer();
		r.setPointsToKeep(5);
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		double[] reduced = r.applyModifierToData(pts, null);
		assertTrue(Arrays.equals(reduced, new double[]{1.0,3.0,5.0,7.0,9.0}));
	}
	
	@Test
	public void testReducerModifier2(){
		Reducer r = new Reducer();
		r.setPointsToKeep(20);
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		double[] reduced = r.applyModifierToData(pts, null);
		assertTrue(Arrays.equals(reduced, pts));
	}
	
	@Test
	public void testReducerModifier3(){
		Reducer r = new Reducer();
		r.setPointsToKeep(2);
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		double[] reduced = r.applyModifierToData(pts, null);
		assertTrue(Arrays.equals(reduced, new double[]{1,6}));
	}
	
	@Test
	public void testReducerModifier4(){
		Reducer r = new Reducer();
		r.setPointsToKeep(10);
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		double[] reduced = r.applyModifierToData(pts, null);
		assertTrue(Arrays.equals(reduced, pts));
	}
	
	@Test
	public void testReducerModifier5(){
		Reducer r = new Reducer();
		r.setPointsToKeep(9);
		double[] pts = {1,2,3,4,5,6,7,8,9,10};
		double[] reduced = r.applyModifierToData(pts, null);
		assertTrue(Arrays.equals(reduced, new double[]{1,2,3,4,5,6,7,8,9}));
	}
	
	@Test
	public void testReducerCreation(){
		Reducer r = new Reducer();
		r.setPointsToKeep(10);
		r.enabled.set(true);
		r.activated.set(true);
		String file = r.getStringForFileWriting();
		String[] lines = file.split(SPSettings.lineSeperator);
		Reducer r2 = new Reducer();
		r2.readModifierFromString(lines[0]);
		assertTrue(r2.getPointsToKeep() == r.getPointsToKeep());
	}


}
