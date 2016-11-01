package net.relinc.libraries.unitTests;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.Ignore;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

@Ignore
public class BaseTest {
    @BeforeClass
    public static void setUpBaseClass() {
        // This code runs before every class of tests that is a child of this class.
    	if(!TestingSettings.testingOutputLocation.exists())
			TestingSettings.testingOutputLocation.mkdirs();
    	
    	//Prepare AppData.
    	SPSettings.applicationSupportDirectory = "APPDATA";
    	File appData = new File(SPSettings.applicationSupportDirectory);
    	if(!appData.exists())
    		System.out.println("Created local app data: " + appData.mkdir());
    	SPOperations.prepareAppDataDirectory();
    }
}
