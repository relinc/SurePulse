package net.relinc.viewer.application;

public class MetricMultiplier {
	public Unit units = Unit.BASE;
	
	public enum Unit{
		BASE, MILLI, MICRO, NANO, PICO;
	}
	
	public double getMultiplier(){
		switch(units){
		case BASE:
			return 1;
		case MILLI:
			return Math.pow(10, 3);
		case MICRO:
			return Math.pow(10, 6);
		case NANO:
			return Math.pow(10, 9);
		case PICO:
			return Math.pow(10, 12);
		}
		return -1;
	}
	
	public String getString(){
		switch(units){
		case BASE:
			return "";
		case MILLI:
			return "m";
		case MICRO:
			return "u";
		case NANO:
			return "n";
		case PICO:
			return "p";
		}
		return "---";
	}
}

