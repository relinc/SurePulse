package net.relinc.processor.sample;

import java.io.File;

import java.io.FileNotFoundException;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.relinc.processor.application.BarSetup;
import net.relinc.processor.data.DataFile;
import net.relinc.processor.data.DataFileListWrapper;
import net.relinc.processor.data.DataLocation;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.Descriptor;
import net.relinc.processor.data.DescriptorDictionary;
import net.relinc.processor.data.Displacement;
import net.relinc.processor.data.EngineeringStrain;
import net.relinc.processor.data.Force;
import net.relinc.processor.data.IncidentPulse;
import net.relinc.processor.data.LoadCell;
import net.relinc.processor.data.ReflectedPulse;
import net.relinc.processor.data.TransmissionPulse;
import net.relinc.processor.data.TrueStrain;
import net.relinc.processor.staticClasses.Converter;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;
import net.relinc.processor.staticClasses.SPTracker;



public abstract class Sample {

	private double beginROITime = -1; //initially set to -1
	private double endROITime = -1;
	private BooleanProperty selected = new SimpleBooleanProperty(true);
	public BooleanProperty selectedProperty() {
        return selected;
    }
	public boolean isSelected() {
        return selected.get();
    }
    public void setSelected(boolean selected) {
        this.selected.set(selected);
    }
	public boolean checked = true;
	public DataFileListWrapper DataFiles = new DataFileListWrapper();
	public BarSetup barSetup;
	private String name;//, sampleType;
	public String delimiter = ":";
	private double length, density, youngsModulus, heatCapacity;
	private int sampleVersion = 1;
	public LoadDisplacementSampleResults results;
	public DescriptorDictionary descriptorDictionary = new DescriptorDictionary();
	
	//public abstract double getArea();
	public abstract String getSpecificString();
	public abstract void setSpecificParameters(String des, String val);
	public abstract DescriptorDictionary createAllParametersDecriptorDictionary();
	
	public boolean writeSampleToFile(String path) {
		try {
			ZipFile zipFile = new ZipFile(path);
			ZipParameters parameters = new ZipParameters();
			parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			File sampleDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/");
			File sampleDataDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/Data");
			SPOperations.deleteFolder(sampleDir); //recursive
			sampleDir.mkdir();
			sampleDataDir.mkdir();
			File sampleParameters = new File(sampleDir + "/Parameters.txt");
			File sampleDesciptors = new File(sampleDir + "/Descriptors.txt");
			SPOperations.writeStringToFile(getStringForFileWriting(), sampleParameters.getPath());
			SPOperations.writeStringToFile(getDescriptorsStringForFileWriting(), sampleDesciptors.getPath());
			zipFile.addFile(sampleParameters, parameters);
			zipFile.addFile(sampleDesciptors, parameters);
			
			
			//this copies the saved files a temp /Data from a different temp /Data folder, including interpreter files.
			for(DataFile d : DataFiles){
					File specificDataFolder = new File(sampleDataDir.getPath() + "/" + d.tempDataFolder.getName());
					System.out.println("Copying from: " + d.tempDataFolder + " To : " + specificDataFolder);
					//Dialogs.showAlert("STOP");
					SPOperations.copyFolder(d.tempDataFolder, specificDataFolder);
					d.savedSampleFolder = zipFile.getFile();
			}
			
			zipFile.addFolder(sampleDataDir, parameters);
			
			DataFiles.stream().forEach(df -> df.WriteModifier());
			
			SPOperations.deleteFolder(sampleDir);
			
			if(barSetup != null)
				writeBarSetupToSampleFile(path);
			
			//do some tracking.
			String description = "Compression";
			if(path.endsWith(SPSettings.tensionRectangularExtension))
				description = "Tension Rectangular";
			if(path.endsWith(SPSettings.tensionRoundExtension))
				description = "Tension Round";
			if(path.endsWith(SPSettings.shearCompressionExtension))
				description = "Shear Compression";
			if(path.endsWith(SPSettings.loadDisplacementExtension))
				description = "Load Displacement";
			
			SPTracker.track(SPTracker.surepulseProcessorCategory, description + " Saved");
			//SPTracker.track(new FocusPoint(description));
			
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String getDescriptorsStringForFileWriting() {
		String file = "Descriptors File\nVersion~1\n";
		for(Descriptor d : descriptorDictionary.descriptors){
			if(!d.getKey().equals("") || !d.getValue().equals(""))
				file += d.getKey() + ":" + d.getValue() + SPSettings.lineSeperator;
		}
		return file;
	}
	
	public boolean writeBarSetupToSampleFile(String sampleZipPath) {
		
		File tempDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/");
		SPOperations.deleteFolder(tempDir);
		tempDir.mkdirs();
		
		//zipFile = new ZipFile(tempDir + "/" + barSetup.barSetupName + ".zip");
		
		ZipParameters parameters = new ZipParameters();

		// set compression method to store compression
		parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);

		// Set the compression level
		parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
		
		
		ZipFile sampleZip;
		try {
			sampleZip = new ZipFile(sampleZipPath);
			if(barSetup.IncidentBar != null && barSetup.TransmissionBar != null)
				sampleZip.addFile(barSetup.createZipFile(tempDir + "/" + barSetup.name).getFile(), parameters);
		} catch (ZipException e) {
			e.printStackTrace();
			return false;
		}
		return true;

		
	}
	
	private String getStringForFileWriting() {
		return getCommonString() + getSpecificString();
	}
	
	private String getCommonString() {
		String commonString = "Sample Version"+delimiter+sampleVersion+SPSettings.lineSeperator;
		commonString += "Sample Type"+delimiter+getSampleType()+SPSettings.lineSeperator;
		commonString+="Name"+delimiter+getName()+SPSettings.lineSeperator;
		if(getLength() > 0)
			commonString+="Length"+delimiter+getLength()+SPSettings.lineSeperator;
		if(getDensity() > 0)
			commonString+="Density"+delimiter+getDensity()+SPSettings.lineSeperator;
		if(getYoungsModulus() > 0)
			commonString+="Young's Modulus"+delimiter+getYoungsModulus()+SPSettings.lineSeperator;
		if(getHeatCapacity() > 0)
			commonString+="Heat Capacity"+delimiter+getHeatCapacity()+SPSettings.lineSeperator;
		return commonString;
	}
	
	public boolean readSampleFromFile(String path) {
		try {
		File sampleFile = extractSampleFromFile(path);
		String parameters = SPOperations.readStringFromFile(sampleFile.getPath() + "/Parameters.txt");
		setParametersFromString(parameters);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private File extractSampleFromFile(String path) {
		try {
			File sampleDataDir = new File(path);
			if(!sampleDataDir.exists()){
				throw new FileNotFoundException();
			}
			ZipFile zipFile = new ZipFile(path);
			zipFile.extractAll(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/Sample/"+zipFile.getFile().getName());
			File sampleDir = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/Temp/Sample/"+zipFile.getFile().getName());
			return sampleDir;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void setCommonParameters(String line) {
		if(line.split(delimiter).length < 2)
			return;
		String des = line.split(delimiter)[0];
		String val = line.split(delimiter)[1];
		//if(des.equals("Sample Type"))
			//setSampleType(val);
		if(des.equals("Name"))
			setName(val);
		if(des.equals("Length"))
			setLength(Double.parseDouble(val));
		if(des.equals("Density"))
			setDensity(Double.parseDouble(val));
		if(des.equals("Young's Modulus"))
			setYoungsModulus(Double.parseDouble(val));
		if(des.equals("Heat Capacity"))
			setHeatCapacity(Double.parseDouble(val));
		setSpecificParameters(des, val);
		
	}
	
	public void setParametersFromString(String input){
		for(String line : input.split(SPSettings.lineSeperator)){
			setCommonParameters(line);
		}
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
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
	public double getYoungsModulus() {
		return youngsModulus;
	}
	public void setYoungsModulus(double youngsModulus) {
		this.youngsModulus = youngsModulus;
	}
	public double getHeatCapacity() {
		return heatCapacity;
	}
	public void setHeatCapacity(double heatCapacity) {
		this.heatCapacity = heatCapacity;
	}
	public String getSampleType() {
		if(this instanceof CompressionSample)
			return "Compression Sample";
		if(this instanceof TensionRoundSample)
			return "Tension Round Sample";
		if(this instanceof TensionRectangularSample)
			return "Tension Rectangular Sample";
		if(this instanceof ShearCompressionSample)
			return "Shear Compression Sample";
		if(this instanceof LoadDisplacementSample)
			return "Load Displacement Sample";
		return null;
	}
//	public void setSampleType(String sampleType) {
//		this.sampleType = sampleType;
//	}
	public void populateSampleDataFromDataFolder(String string, BarSetup bar) throws Exception {
		//string = /Data Folder.
		File dataFolder = new File(string);
		if(!dataFolder.exists())
			throw new Exception("Data folder not found.");
		
		for(File f : dataFolder.listFiles()){
			DataFiles.addDataFile(f, bar);
		}
		
		for(TransmissionPulse tran : DataFiles.getTransmissionStrainGaugeDatasets()){
			tran.strainGauge = barSetup.TransmissionBar.getStrainGauge(tran.strainGaugeName);
			if(tran.strainGauge == null)
				System.out.println("Failed to set transmission bar strain gauge!" + tran.strainGaugeName);
		}
		for(ReflectedPulse ref : DataFiles.getReflectedStrainGaugeDatasets()){
			ref.strainGauge = barSetup.IncidentBar.getStrainGauge(ref.strainGaugeName);
			if(ref.strainGauge == null)
				System.out.println("Failed to set reflected strain gauge!" + ref.strainGaugeName);
		}
		for(IncidentPulse incid : DataFiles.getIncidentStrainGaugeDatasets()){
			incid.strainGauge = barSetup.IncidentBar.getStrainGauge(incid.strainGaugeName);
			if(incid.strainGauge == null)
				System.out.println("Failed to set incident strain gauge. Name: " + incid.strainGaugeName);
		}
	}

	public abstract double[] getEngineeringStressFromForce(double[] force);
	
	


	public DataSubset getTrueStress() {
		//gets the first instance of eng strain and stress, matches them for time, and then gets the
		return null;
	}

	
	
	public abstract double[] getTrueStressFromEngStressAndEngStrain(double[] engStress, double[] engStrain);
	
	
	
	public DataLocation getDefaultStressLocation() {
		//explicit force data first
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof Force)
					return new DataLocation(i,j);
			}
		}
		//then load cell
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof Force)
					return new DataLocation(i,j);
			}
		}
		//then transmission gauge
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof TransmissionPulse)
					return new DataLocation(i,j);
			}
		}
		return null;
	}
	public DataLocation getDefaultStrainLocation() {
		//first explicit strain data
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof TrueStrain || dataSet instanceof EngineeringStrain)
					return new DataLocation(i,j);
			}
		}
		//then incident strain gauge
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof ReflectedPulse)
					return new DataLocation(i,j);
			}
		}
		
		//then displacement
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile file = DataFiles.get(i);
			for(int j = 0; j < file.dataSubsets.size(); j++){
				DataSubset dataSet = file.dataSubsets.get(j);
				if(dataSet instanceof Displacement)
					return new DataLocation(i,j);
			}
		}
		return null;
	}
	public DataSubset getDataSubsetAtLocation(DataLocation loc) {
		System.out.println(loc);
		return DataFiles.get(loc.dataFileIndex).dataSubsets.get(loc.dataSubsetIndex);
	}
	public double[] getEngineeringStrainFromTrueStrain(double[] trueStrain) {
		double[] engineeringStrain = new double[trueStrain.length];
		for(int i = 0; i < engineeringStrain.length; i++){
			engineeringStrain[i] = Math.pow(Math.E, trueStrain[i]) - 1;
		}
		return engineeringStrain;
	}
	public double[] getTrueStrainFromEngineeringStrain(double[] engineeringStrain){
		double[] trueStrain = new double[engineeringStrain.length];
		for(int i = 0; i < trueStrain.length; i++){
			trueStrain[i] = Math.log(1 + engineeringStrain[i]);
		}
		return trueStrain;
	}
	public double[] getForceFromStrain(double[] barStrain) {
		double[] force = new double[barStrain.length];
		for(int i = 0; i < barStrain.length; i++){
			force[i] = barStrain[i] * barSetup.TransmissionBar.youngsModulus * barSetup.TransmissionBar.getArea();
		}
		return force;
	}
	public DataLocation getLocationOfDataSubset(DataSubset d) {
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile dataFile = DataFiles.get(i);
			for(int j = 0; j < dataFile.dataSubsets.size(); j++){
				DataSubset subset = dataFile.dataSubsets.get(j);
				if(subset == d)
					return new DataLocation(i, j);
			}
		}
		return null;
	}
	public DataLocation getLocationOfDataSubset(String dataSetName){
		for(int i = 0; i < DataFiles.size(); i++){
			DataFile dataFile = DataFiles.get(i);
			for(int j = 0; j < dataFile.dataSubsets.size(); j++){
				DataSubset subset = dataFile.dataSubsets.get(j);
				if(subset.name.equals(dataSetName))
					return new DataLocation(i, j);
			}
		}
		System.out.println("HERE IS THE ERROR");
		return null;
	}
	public double[] getEngineeringStrainFromReflectedPulseStrain(double[] time, double[] reflectedStrain) {
		double[] strainRate = new double[reflectedStrain.length];
		double strainRateMultiplier = 2 * barSetup.IncidentBar.getWaveSpeed() / (length);
		for(int i = 0; i < strainRate.length; i++){
			strainRate[i] = strainRateMultiplier * reflectedStrain[i];
		}
		//UnivariateFunction uf = (UnivariateFunction)new PolynomialFunction(strainRate);
		//TrapezoidIntegrator trapezoid = new TrapezoidIntegrator();
		//double j = trapezoid.integrate(0, uf, 0, 1);
		//trapezoid.integrate(0, f, lower, upper)
		double[] strain = new double[strainRate.length];
		for(int i = 0; i < strain.length; i++){

			if(i==0){
				strain[0]=0;
			}
			else{
				strain[i] = strain[i - 1] + strainRate[i] * (time[i] - time[i - 1]);
			}


		}
		return strain;
	}
		
	public boolean datasubsetIsValidForStress(DataSubset data){
		return data instanceof TransmissionPulse || data instanceof LoadCell || data instanceof Force;
	}
	
	public boolean datasubsetIsValidForStrain(DataSubset data){
		return data instanceof TrueStrain || data instanceof EngineeringStrain || data instanceof ReflectedPulse;
	}
	
	public Sample clone(){  
	    try{  
	        return (Sample) super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
	}
	public double[] getDisplacementFromEngineeringStrain(double[] engStrain) {
		if(length <= 0)
			System.out.println("THIS SHOUDN'T HAPPEN!!!!!!!");
		double[] displacement = new double[engStrain.length];
		for(int i = 0; i < displacement.length; i++){
			displacement[i] = engStrain[i] * length;
		}
		return displacement;
	}
	public void setDescriptorsFromString(String descriptors) {
		String[] lines = descriptors.split("\n");
		for(String line : lines){
			if(line.split(":").length < 2)
				continue;
			String des = line.split(":")[0];
			String val = line.split(":")[1];
			descriptorDictionary.descriptors.add(new Descriptor(des,  val));
		}
	}
	
	@Override
	public String toString(){
		return name;
	}
	public double getBeginROITime() {
		return beginROITime;
	}
	public void setBeginROITime(double beginROITime) {
		this.beginROITime = beginROITime;
	}
	public double getEndROITime() {
		return endROITime;
	}
	public void setEndROITime(double endROITime) {
		this.endROITime = endROITime;
	}
	
	public int addCommonRequiredSampleParametersToDescriptionDictionary(DescriptorDictionary d){
		int i = 0;
		d.descriptors.add(i++, new Descriptor("Sample Name", getName()));
		d.descriptors.add(i++, new Descriptor("Type", getSampleType()));
		double density = Converter.Lbin3FromKgM3(getDensity());
		double youngsModulus = Converter.MpsiFromPa(getYoungsModulus());
		double heatCapacity = Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(getHeatCapacity());
		if(SPSettings.metricMode.get()){
			density = Converter.gccFromKgm3(getDensity());
			youngsModulus = Converter.GpaFromPa(getYoungsModulus());
			heatCapacity = getHeatCapacity();
		}
		d.descriptors.add(i++, new Descriptor("Density", Double.toString(SPOperations.round(density, 3))));
		d.descriptors.add(i++, new Descriptor("Young's Modulus", Double.toString(SPOperations.round(youngsModulus, 3))));
		d.descriptors.add(i++, new Descriptor("Heat Capacity", Double.toString(SPOperations.round(heatCapacity, 3))));
		return i;
	}
	
}
