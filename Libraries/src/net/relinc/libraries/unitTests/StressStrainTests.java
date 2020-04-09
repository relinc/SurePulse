package net.relinc.libraries.unitTests;


import net.relinc.libraries.referencesample.StressStrain;
import net.relinc.libraries.referencesample.StressStrainMode;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;


public class StressStrainTests {

    static double DELTA = .00001;

    @Test
    public void testEngineeringToEngineering()
    {
        StressStrain eng = getExampleStressStrain(StressStrainMode.ENGINEERING);

        StressStrain converted = StressStrain.toEngineering(eng);

        Assert.assertArrayEquals(
                eng.getStress().stream().mapToDouble(d -> d).toArray() ,
                converted.getStress().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );

        Assert.assertArrayEquals(
                eng.getStrain().stream().mapToDouble(d -> d).toArray() ,
                converted.getStrain().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );
    }

    @Test
    public void testTrueToTrue() {
        StressStrain trueData = getExampleStressStrain(StressStrainMode.TRUE);
        StressStrain converted = StressStrain.toTrue(trueData);
        Assert.assertArrayEquals(
                trueData.getStress().stream().mapToDouble(d -> d).toArray() ,
                converted.getStress().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );
        Assert.assertArrayEquals(
                trueData.getStrain().stream().mapToDouble(d -> d).toArray() ,
                converted.getStrain().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );
    }

    @Test
    public void testTrueToEngineering() {
        StressStrain trueData = getExampleStressStrain(StressStrainMode.TRUE);
        StressStrain converted = StressStrain.toEngineering(trueData);
        Assert.assertArrayEquals(
                new double[]{0.0, 0.05127109637602412, 0.06183654654535964, 0.06289891418719518, 0.07250818125421654},
                converted.getStrain().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );

        Assert.assertArrayEquals(
                new double[]{0.0, 9.512294245007139, 47.08822667921243, 56.44939438656059, 93.23938199059482},
                converted.getStress().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );
    }

    @Test
    public void testEngineeringToTrue() {
        StressStrain engData = getExampleStressStrain(StressStrainMode.ENGINEERING);
        StressStrain converted = StressStrain.toTrue(engData);

        Assert.assertArrayEquals(
                new double[]{0.0, 0.04879016416943205, 0.058268908123975824, 0.05921185963184603, 0.06765864847381486},
                converted.getStrain().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );

        Assert.assertArrayEquals(
                new double[]{0.0, 10.5, 53.0, 63.66, 107.0},
                converted.getStress().stream().mapToDouble(d -> d).toArray(),
                DELTA
        );
    }


    private StressStrain getExampleStressStrain(StressStrainMode mode) {
        ArrayList<Double> exampleStrain = new ArrayList<>();
        exampleStrain.add(0.0);
        exampleStrain.add(0.05);
        exampleStrain.add(0.06);
        exampleStrain.add(0.061);
        exampleStrain.add(0.07);

        ArrayList<Double> exampleStress = new ArrayList<>();
        exampleStress.add(0.0);
        exampleStress.add(10.0);
        exampleStress.add(50.);
        exampleStress.add(60.0);
        exampleStress.add(100.0);


        return new StressStrain(exampleStress, exampleStrain, mode);
    }


}
