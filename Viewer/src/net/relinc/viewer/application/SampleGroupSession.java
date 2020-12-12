package net.relinc.viewer.application;

import javafx.scene.paint.Color;
import net.relinc.viewer.GUI.ChartsGUI;

import java.util.List;

public class SampleGroupSession {

    // path is how we ID samples.
    public List<String> samplePaths;
    public String color;
    public String name;

    public SampleGroupSession(List<String> samplePaths, Color color, String name) {
        this.samplePaths = samplePaths;
        this.color = ChartsGUI.getColorAsString(color);
        this.name = name;
    }
}
