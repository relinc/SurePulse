package net.relinc.viewer.GUI;

public class TrimDatasetOption {
    public enum Option {
        BOTH, LOAD, DISPLACEMNT;
    }

    private Option option;
    private String description;

    public TrimDatasetOption(Option option, String description) {
        this.option = option;
        this.description = description;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public Option getOption() {
        return this.option;
    }

    public static boolean shouldTrimLoad(Option option) {
        return option == Option.LOAD || option == Option.BOTH;
    }

    public static boolean shouldTrimDisplacement(Option option) {
        return option == Option.DISPLACEMNT || option == Option.BOTH;
    }
}
