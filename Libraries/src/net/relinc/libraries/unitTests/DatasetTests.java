package net.relinc.libraries.unitTests;

import static org.junit.Assert.*;

import net.relinc.libraries.data.Dataset;
import org.junit.Test;
import net.relinc.libraries.staticClasses.SPMath;

import javax.xml.crypto.Data;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

public class DatasetTests extends BaseTest {

    private double DELTA = .000001;

    @Test
    public void testBasic() {
        double[] time = new double[]{1,2,3,4,5,6,7,8,9,10};
        double[] y = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.10};
        Dataset d = new Dataset(time, y);
        assertArrayEquals(d.getTimeData(), time, DELTA);
        assertArrayEquals(d.getData(), y, DELTA);
    }

    @Test
    public void testUpsample() {
        double[] time = new double[]{1,2,3,4,5,6,7,8,9,10,11};
        double[] y = new double[]{1.1, 2.2, 3.3, 4.4, 5.5, 6.6, 7.7, 8.8, 9.9, 10.10, 11.11};
        Dataset d = new Dataset(time, y);
        d.setUserDataPoints(101);
        assertEquals(d.getTimeData().length, 101);
        assertArrayEquals(IntStream.range(0, 101).mapToDouble(i -> 1 + i / 10.0).toArray(), d.getTimeData() , DELTA);
        assertEquals(d.getData().length, 101);
    }

    @Test
    public void testDownsample() {
        double[] time = IntStream.range(0, 100).mapToDouble(i -> i).toArray();
        double[] y = IntStream.range(500, 600).mapToDouble(i -> i).toArray();
        Dataset d = new Dataset(time, y);
        d.setUserDataPoints(10);

        assertEquals(10, d.getTimeData().length);
        assertEquals(10, d.getData().length);

        assertArrayEquals(IntStream.range(0, 10).mapToDouble(i -> i * 11).toArray(), d.getTimeData(), DELTA);

    }

    @Test
    public void testNoop() {
        double[] time = new double[]{1,2,3};
        double[] y = new double[]{3.3, 4.5, 3.2};
        Dataset d = new Dataset(time, y);
        d.setUserDataPoints(time.length);
        assertArrayEquals(d.getTimeData(), time, DELTA);
        assertArrayEquals(d.getData(), y, DELTA);
    }

}
