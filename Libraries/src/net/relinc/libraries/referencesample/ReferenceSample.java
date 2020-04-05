package net.relinc.libraries.referencesample;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.relinc.libraries.sample.Sample;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mark on 1/6/20.
 */
public abstract class ReferenceSample {
    private String name;
    private String loadedPath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ReferenceSample(String name, String loadedPath) {
        this.setName(name);
        this.loadedPath = loadedPath;
    }

    public String getLoadedPath() {
        return this.loadedPath;
    }


    private BooleanProperty selected = new SimpleBooleanProperty(false);

    public BooleanProperty selectedProperty() {
        return selected;
    }


    @Override
    public String toString() {
        return this.getName();
    }

    public abstract String getJson();

    public static ReferenceSample createFromJson(String json, String loadedPath) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(json);

            if (object.get("type").equals("xy")) {
                return ReferenceSampleXY.fromJson(object, loadedPath);
            } else if(object.get("type").equals("kn")) {
                return ReferenceSampleKN.fromJson(object, loadedPath);
            } else if(object.get("type").equals("johnsonCook")) {
                return ReferenceSampleJohnsonCook.fromJson(object, loadedPath);
            } else  if(object.get("type").equals("ludwig")) {
                return ReferenceSampleLudwig.fromJson(object, loadedPath);
            }
            else {
                System.err.println("Tried to parse with xy parser but json file is not type=xy !!");
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public abstract List<Double> getStress(StressStrainMode mode, StressUnit unit);
    public abstract List<Double> getStrain(StressStrainMode mode);
}
