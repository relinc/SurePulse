package net.relinc.libraries.application;

import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.SPOperations;

public class SGProp {
	

	private String genericname;
	private String specificname;
	private double distance;
	
	public String getGenericname() {
		return genericname;
	}
	public void setGenericname(String genericname) {
		this.genericname = genericname;
	}
	public String getSpecificname() {
		return specificname;
	}
	public void setSpecificname(String specificname) {
		this.specificname = specificname;
	}
	public double getDistance() {
		return distance;
	}
	public double getInchdistance(){
		return SPOperations.round(Converter.InchFromMeter(distance), 4);
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	
	public SGProp(String genericname, String specificname, double distance) {
		super();
		this.genericname = genericname;
		this.specificname = specificname;
		this.distance = distance;
	}
	
	
	
	
	
//	public SGProp(String genName, String specName, String d){
//		setGenericname(genName);
//		setSpecificName(specName);
//		setDistance(d);
//	}
//	
//	
//	private StringProperty genericname;
//    public void setGenericname(String value) { genericnameProperty().set(value); }
//    public String getGenericname() { return genericnameProperty().get(); }
//    
//    public StringProperty genericnameProperty() { 
//        if (genericname == null) genericname = new SimpleStringProperty(this, "name");
//        return genericname; 
//    }
//    
//    private StringProperty specificName;
//    public void setSpecificName(String value) { specificNameProperty().set(value); }
//    public String getSpecificName() { return specificNameProperty().get(); }
//    
//    public StringProperty specificNameProperty() { 
//        if (specificName == null) specificName = new SimpleStringProperty(this, "name");
//        return specificName; 
//    }
//
//    private StringProperty distance;
//    public void setDistance(String value) { distanceProperty().set(value); }
//    public String getDistance() { return distanceProperty().get(); }
//    public StringProperty distanceProperty() { 
//        if (distance == null) distance = new SimpleStringProperty(this, "distance");
//        return distance; 
//    } 
	
}
