package net.relinc.libraries.unitTests;

import java.io.File;

import javafx.application.Application;
import net.relinc.libraries.unitTests.RawDatasetTests.AsNonApp;

public class TestingSettings extends BaseTest{
	public static File testingOutputLocation = new File("UnitTestingOutputs");
	public static File testingInputLocation = new File("UnitTestingInputs");
	private static boolean javaFXRunning = false;
	public static void initJFX() {
		if(!javaFXRunning){
			javaFXRunning = true;
			Thread t = new Thread("JavaFX Init Thread") {
				public void run() {
					Application.launch(AsNonApp.class, new String[0]);
				}
			};
			t.setDaemon(true);
			t.start();
		}
		
	}
}
