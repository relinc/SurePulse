package net.relinc.libraries.data.ModifierFolder;

public class ModifierResult {
    private double[] x;
    private double[] y;
    private double userIndexToOriginalIndexRatio;



    public ModifierResult(double[] x, double[] y, double userIndexToOriginalIndexRatio) {
        this.x = x;
        this.y = y;
        this.userIndexToOriginalIndexRatio = userIndexToOriginalIndexRatio;
    }

    public double[] getX() {
        return x;
    }

    public void setX(double[] x) {
        this.x = x;
    }

    public double[] getY() {
        return y;
    }

    public void setY(double[] y) {
        this.y = y;
    }

    public double getUserIndexToOriginalIndexRatio() {
        return userIndexToOriginalIndexRatio;
    }





}
