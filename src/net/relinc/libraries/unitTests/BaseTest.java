package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class BaseTest {
    @BeforeClass
    public static void setUpBaseClass() {
        // This code runs before every class of tests that is a child of this class.
    	if(!TestingSettings.testingOutputLocation.exists())
			TestingSettings.testingOutputLocation.mkdirs();
    }
}
