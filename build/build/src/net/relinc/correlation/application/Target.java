package net.relinc.correlation.application;

import java.awt.Rectangle;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Point2D;
import net.relinc.libraries.application.FitableDataset;

public class Target {

	public Rectangle rectangle;
	private String name;
	private String color;
	private double threshold;
	public Point2D center;
	public Point2D vertex;
	public Point2D[] pts;
	public FitableDataset xPts;
	public FitableDataset yPts;
	public double[] displacement;
	private BooleanProperty selected = new SimpleBooleanProperty(false);
	public double[] speed;
	public BooleanProperty selectedProperty() {
        return selected;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString(){
		return getName();
	}

	public void renderRectangle() {
		if(center == null || vertex == null)
			return;
		double diffX = Math.abs(center.getX() - vertex.getX());
		double diffY = Math.abs(center.getY() - vertex.getY());
		int x = (int)(vertex.getX() < center.getX() ? vertex.getX() : center.getX() - diffX);
		int y = (int)(vertex.getY() < center.getY() ? vertex.getY() : center.getY() - diffY);
		rectangle = new Rectangle(x, y, (int)(diffX * 2), (int)(diffY * 2));
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public double getThreshold() {
		return threshold;
	}

	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	public Point2D[] getSmoothedPoints(){
		Point2D[] smoothed = new Point2D[pts.length];
		for(int i = 0; i < smoothed.length; i++){
			smoothed[i] = new Point2D(xPts.fittedY.get(i), yPts.fittedY.get(i));
		}
		return smoothed;
	}
	
}
