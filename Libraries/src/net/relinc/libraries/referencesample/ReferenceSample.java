package net.relinc.libraries.referencesample;

import java.io.File;

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

    public abstract ReferenceSample fromJson(String json);

}
