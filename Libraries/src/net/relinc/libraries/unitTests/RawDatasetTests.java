package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import javafx.application.Application;
import javafx.stage.Stage;
import net.relinc.libraries.data.DataInterpreter.dataType;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Displacement;
import net.relinc.libraries.data.EngineeringStrain;
import net.relinc.libraries.data.Force;
import net.relinc.libraries.data.IncidentBarStrainPulse;
import net.relinc.libraries.data.IncidentBarVoltagePulse;
import net.relinc.libraries.data.LagrangianStrain;
import net.relinc.libraries.data.LoadCell;
import net.relinc.libraries.data.RawDataset;
import net.relinc.libraries.data.ReflectedBarStrainPulse;
import net.relinc.libraries.data.ReflectedBarVoltagePulse;
import net.relinc.libraries.data.TransmissionBarStrainPulse;
import net.relinc.libraries.data.TransmissionBarVoltagePulse;
import net.relinc.libraries.data.TrueStrain;

public class RawDatasetTests extends BaseTest{
	
	HashMap<dataType, Class<? extends DataSubset>> dataTypeToClass = new HashMap<dataType, Class<? extends DataSubset>>();
	
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
	
	@Before
	public void prepareMap(){
		dataTypeToClass.put(dataType.FORCE, Force.class);
		dataTypeToClass.put(dataType.ENGINEERINGSTRAIN, EngineeringStrain.class);
		dataTypeToClass.put(dataType.TRUESTRAIN, TrueStrain.class);
		dataTypeToClass.put(dataType.LAGRANGIANSTRAIN, LagrangianStrain.class);
		dataTypeToClass.put(dataType.DISPLACEMENT, Displacement.class);
		dataTypeToClass.put(dataType.TRANSMISSIONSG, TransmissionBarVoltagePulse.class);
		dataTypeToClass.put(dataType.LOADCELL, LoadCell.class);
		dataTypeToClass.put(dataType.TRANSMISSIONBARSTRAIN, TransmissionBarStrainPulse.class);
	}
	
	@Test
	public void testDatatypes(){
		dataType[] types = dataType.values();
		types = Arrays.stream(types).filter(t -> t != dataType.TIME && t !=  dataType.NULL)
				.toArray(dataType[]::new);
		for(dataType type : types){ //Tests all the data types.
			double[] data = {1.1, 2.1, 3.2, 4.5};
			RawDataset rawDataset = new RawDataset(data);
			rawDataset.interpreter.multiplier = 2.0; // The data gets divided by the multiplier
			rawDataset.interpreter.DataType = type;
			double[] time = {.1,0.2, .3, .4};
			List<DataSubset> extractedData = null;
			try {
				extractedData = rawDataset.extractDataset(time);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(type == dataType.INCIDENTSG || type == dataType.INCIDENTBARSTRAIN){
				// Incident SG pulse generates two datasubsets, incident and reflected.
				assertTrue(extractedData.size() == 2);
				if(type == dataType.INCIDENTSG){
					assertTrue(extractedData.get(0).getClass() == IncidentBarVoltagePulse.class);
					assertTrue(extractedData.get(1).getClass() == ReflectedBarVoltagePulse.class);
				}
				else{
					assertTrue(extractedData.get(0).getClass() == IncidentBarStrainPulse.class);
					assertTrue(extractedData.get(1).getClass() == ReflectedBarStrainPulse.class);
				}
				assertArrayEquals(new double[]{1.1/2, 2.1/2, 3.2/2, 4.5/2}, extractedData.get(0).getModifiedData(), 0);
				assertArrayEquals(new double[]{1.1/2, 2.1/2, 3.2/2, 4.5/2}, extractedData.get(1).getModifiedData(), 0);
			}
			else{
				assertTrue(extractedData.size() == 1);
				assertTrue(extractedData.get(0).getClass() == (dataTypeToClass.get(type)));
				assertArrayEquals(new double[]{1.1/2, 2.1/2, 3.2/2, 4.5/2}, extractedData.get(0).getModifiedData(), 0);
			}
		}
	}
	
}
