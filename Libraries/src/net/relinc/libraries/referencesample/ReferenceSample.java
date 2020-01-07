package net.relinc.libraries.referencesample;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mark on 1/6/20.
 */
public abstract class ReferenceSample {
    private String name;
    private File fileLocation;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ReferenceSample(String name) {
        this.setName(name);
    }



    @Override
    public String toString() {
        return this.getName();
    }

    public abstract String getJson();

    public static ReferenceSample createFromJson(String json) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject object = (JSONObject) parser.parse(json);

            if (object.get("type").equals("xy")) {
                return ReferenceSampleXY.fromJson(object);
            } else {
                System.err.println("Tried to parse with xy parser but json file is not type=xy !!");
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
