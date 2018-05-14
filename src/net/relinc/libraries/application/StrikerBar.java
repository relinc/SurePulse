package net.relinc.libraries.application;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class StrikerBar {
	private double length;
	private double diameter;
	private double density;
	private double speed;
	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public boolean isValid(){
		return !(speed <= 0.0 && density <= 0.0 && length <= 0.0 && diameter <= 0.0);
	}

	public String getStringForFile() {
		Gson gson = new GsonBuilder().create();
		return gson.toJson(this);
	}

	public double getDiameter() {
		return diameter;
	}

	public void setDiameter(double diameter) {
		this.diameter = diameter;
	}

	public double getEnergy(){
		return .5 * length * Math.pow(diameter / 2, 2) * Math.PI * density * Math.pow(speed, 2);
	}
}
