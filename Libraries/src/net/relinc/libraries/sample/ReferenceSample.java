package net.relinc.libraries.sample;

/**
 * Created by mark on 1/6/20.
 */
public class ReferenceSample {
    private String name;


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
}
