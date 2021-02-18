package net.relinc.viewer.GUI;

import boofcv.struct.flow.ImageFlow;

import javax.swing.text.html.Option;
import java.util.Optional;

public class ChartingPreferences {
    public Optional<String> title;
    public Optional<Double> xMin;
    public Optional<Double> xMax;
    public Optional<Double> yMin;
    public Optional<Double> yMax;
    public ChartingPreferences() {
        title = null;
        xMin = null;
        xMax = null;
        yMin = null;
        yMax = null;
    }
    public void setTitle(String newTitle)
    {
        title = Optional.of(newTitle);
    }
    public void setXMin(Double newXMin) {
        xMin = Optional.of(newXMin);
    }
    public void setXMax(Double newXMax) {
        xMax = Optional.of(newXMax);
    }
    public void setYMin(Double newYMin)
    {
        yMin = Optional.of(newYMin);
    }
    public void setYMax(Double newYMax)
    {
        yMax = Optional.of(newYMax);
    }
}
