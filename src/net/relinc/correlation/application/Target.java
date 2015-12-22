package net.relinc.correlation.application;

import java.awt.Rectangle;

public class Target {

	public Rectangle rectangle;
	private String name;

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
	
}
