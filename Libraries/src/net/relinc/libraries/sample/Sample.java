package net.relinc.libraries.sample;

import java.io.File;

import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.google.gson.Gson;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import net.relinc.libraries.application.BarSetup;
import net.relinc.libraries.application.JsonReader;
import net.relinc.libraries.application.StrikerBar;
import net.relinc.libraries.data.*;
import net.relinc.libraries.staticClasses.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	public DataFileListWrapper DataFiles = new DataFileListWrapper();
	public BarSetup barSetup;
	private String name;//, sampleType;
	public String delimiter = ":";
	private double  density, youngsModulus, heatCapacity;
	private long dateSaved;
	private int sampleVersion = 1;
	//public LoadDisplacementSampleResults results;
	private List<LoadDisplacementSampleResults> results = new ArrayList<>();
	public DescriptorDictionary descriptorDictionary = new DescriptorDictionary();
	public boolean placeHolderSample = false;
	public File savedImagesLocation;
	public File loadedFromLocation;
	public boolean hasImages = false;
	public StrikerBar strikerBar = new StrikerBar();

	public abstract JSONObject getSpecificJSON();
	public abstract void setSpecificParametersJSON(JSONObject jsonObject);
	public abstract void setSpecificParameters(String des, String val);
	public abstract int addSpecificParametersToDecriptorDictionary(DescriptorDictionary d, int i); //need to add some from HopkinsonBarSample and then some from each individual.
	
	public Sample() {
		if (SampleTypes.getSampleConstants(this.getClass()) == null) {
			throw new RuntimeException(this.getClass() + " needs to be implemented in SampleTypes!");
		}
	}
	
	public DescriptorDictionary createAllParametersDecriptorDictionary(){
		DescriptorDictionary d = descriptorDictionary;
		d.setName(getName());
		int i = 0;
		d.descriptors.add(i++, new Descriptor("Sample Name", getName()));
		d.descriptors.add(i++, new Descriptor("Type", getSampleType()));
		
		if(dateSaved > 0){
			d.descriptors.add(i++, new Descriptor("Date Saved", Converter.getFormattedDate(new Date(dateSaved))));
		}
		

		
		i = addSpecificParametersToDecriptorDictionary(d, i); //width, height etc.
		
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
		
		addStrikerBarParametersToDescriptionDictionary(d, i);
		
		return d;
	}
	
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
			File sampleParameters = new File(sampleDir + "/Parameters.json");
			File sampleDesciptors = new File(sampleDir + "/Descriptors.json");

			SPOperations.writeStringToFile(getStringForFileWriting(), sampleParameters.getPath());
			SPOperations.writeStringToFile(getDescriptorsStringForFileWriting(), sampleDesciptors.getPath());
			zipFile.addFile(sampleParameters, parameters);
			zipFile.addFile(sampleDesciptors, parameters);
			
			//this copies the saved files a temp /Data from a different temp /Data folder, including interpreter files.
			for(DataFile d : DataFiles){
					File specificDataFolder = new File(sampleDataDir.getPath() + "/" + d.tempDataFolder.getName());
					SPOperations.copyFolder(d.tempDataFolder, specificDataFolder);
					d.savedSampleFolder = zipFile.getFile();
			}
			
			zipFile.addFolder(sampleDataDir, parameters);
			
			//this goes back in and writes the modifiers. Probably should be done in a single step.
			DataFiles.stream().forEach(df -> df.WriteModifier());
			
			SPOperations.deleteFolder(sampleDir);
			
			if(barSetup != null)
				writeBarSetupToSampleFile(path);
			
			if(savedImagesLocation != null)
				ImageOps.copyImagesToSampleFile(savedImagesLocation, path);
			
			String description = "Unknown";
			Optional<SampleConstants> sampleConstants = SampleTypes.getSampleConstantsMap().values().stream()
					.filter(constants -> path.endsWith(constants.getExtension()))
					.findFirst();
			
			if(sampleConstants.isPresent()) {
				description = sampleConstants.get().getShortName();
			}
			
			SPTracker.track(SPTracker.surepulseProcessorCategory, description + " Saved");
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private String getDescriptorsStringForFileWriting() {
		JSONObject jsonObject = new JSONObject();

		for(Descriptor d : descriptorDictionary.descriptors) {
			if(!d.getKey().equals("") || !d.getValue().equals("")) {
				jsonObject.put(d.getKey(), d.getValue());
			}
		}

		jsonObject.put("Title", "Descriptors File");
		jsonObject.put("Version", 1);

		return jsonObject.toString();
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
		JSONObject jsonObject = getCommonJSON();

		if(strikerBar.isValid()) {
			jsonObject.put("StrikerBar", strikerBar.getStringForFile());
		}
		jsonObject.putAll(getSpecificJSON());

		return jsonObject.toString();
	}

	private JSONObject getCommonJSON() {
		JSONObject jsonObject = new JSONObject();

		jsonObject.put("Sample Version", sampleVersion);
		jsonObject.put("Sample Type", getSampleType());
		jsonObject.put("Name", getName());
		jsonObject.put("Date Saved", (new Date().getTime()));
		if(getDensity() > 0) {
			jsonObject.put("Density", getDensity());
		}
		if(getYoungsModulus() > 0) {
			jsonObject.put("Young's Modulus", getYoungsModulus());
		}
		if(getHeatCapacity() > 0) {
			jsonObject.put("Heat Capacity", getHeatCapacity());
		}

		return jsonObject;
	}
	
	private void setCommonParameters(String line) {
		if(line.split(delimiter).length < 2)
			return;
		String des = line.split(delimiter)[0];
		String val = line.split(delimiter)[1];
		String restOfLine = line.substring(des.length() + 1);
		//if(des.equals("Sample Type"))
			//setSampleType(val);
		if(des.equals("Name"))
			setName(val);
		
		if(des.equals("Density"))
			setDensity(Double.parseDouble(val));
		if(des.equals("Young's Modulus"))
			setYoungsModulus(Double.parseDouble(val));
		if(des.equals("Heat Capacity"))
			setHeatCapacity(Double.parseDouble(val));
		if(des.equals("StrikerBar")){
			strikerBar = new Gson().fromJson(restOfLine, StrikerBar.class);
		}
		if(des.equals("Date Saved"))
			setDateSaved(Long.parseLong(val));
		setSpecificParameters(des, val);
	}
	
	//this gets overridden. This is for reading legacy Sample files.
	public void setParametersFromString(String input){
		for(String line : input.split(SPSettings.lineSeperator)){
			setCommonParameters(line);
		}
	}

	private void setCommonParametersJSON(JSONObject jsonObject) {
		JsonReader json = new JsonReader(jsonObject);

		json.get("Name").ifPresent(ob -> setName((String)ob));
		json.get("Density").ifPresent(ob -> setDensity((Double)ob));
		json.get("Young's Modulus").ifPresent(ob -> setYoungsModulus((Double)ob));
		json.get("Heat Capacity").ifPresent(ob -> setHeatCapacity((Double)ob));
		json.get("StrikerBar").ifPresent(ob -> setStrikerBar((String)ob) );

		setSpecificParametersJSON(jsonObject);
	}
	private void setStrikerBar(String input){
		JSONParser jsonParser = new JSONParser();
		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(input);
			strikerBar = new StrikerBar(jsonObject);
		} catch (org.json.simple.parser.ParseException e) {
			e.printStackTrace();
		}
	}
	public void setParametersFromJSONString(String input) {
		JSONParser jsonParser = new JSONParser();

		try {
			JSONObject jsonObject = (JSONObject) jsonParser.parse(input);
			setCommonParametersJSON(jsonObject);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public long getDateSaved() {
		return dateSaved;
	}
	public void setDateSaved(long dateSaved) {
		this.dateSaved = dateSaved;
	}
	public String getSampleType() {
		String name = SampleTypes.getSampleConstantsMap().get(this.getClass()).getName();
		if(name == null) {
			throw new RuntimeException("Sample Type not supported! : " + this.getClass());
		}
		return name;
	}

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
				if(dataSet instanceof TrueStrain || dataSet instanceof EngineeringStrain || dataSet instanceof LagrangianStrain)
					return new DataLocation(i,j);
			}
		}
		
		// then displacement
		for (int i = 0; i < DataFiles.size(); i++) {
			DataFile file = DataFiles.get(i);
			for (int j = 0; j < file.dataSubsets.size(); j++) {
				DataSubset dataSet = file.dataSubsets.get(j);
				if (dataSet instanceof Displacement)
					return new DataLocation(i, j);
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
		
		
		return null;
	}
	
	public DataSubset getDataSubsetAtLocation(DataLocation loc) {
		if(loc == null)
			return null;
		return DataFiles.get(loc.dataFileIndex).dataSubsets.get(loc.dataSubsetIndex);
	}
	
	public static double[] getEngineeringStrainFromTrueStrain(double[] trueStrain) {
		double[] engineeringStrain = new double[trueStrain.length];
		for(int i = 0; i < engineeringStrain.length; i++){
			engineeringStrain[i] = Math.pow(Math.E, trueStrain[i]) - 1;
		}
		return engineeringStrain;
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

	
	public Sample clone(){  
	    try{  
	        return (Sample) super.clone();  
	    }catch(Exception e){ 
	        return null; 
	    }
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

	public void setDescriptorsFromJSONString(String descriptors) {
		try {
			JSONObject json = (JSONObject) new JSONParser().parse(descriptors);
			json.keySet().stream().forEach(key ->
					descriptorDictionary.descriptors.add(new Descriptor(key.toString(),  json.get(key).toString()))
			);
		} catch(ParseException e) {
			e.printStackTrace();
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
		d.setName(getName());
		d.descriptors.add(i++, new Descriptor("Type", getSampleType()));
		
		if(dateSaved > 0){ //dates started to get saved 2016.04.11
			SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd.hh:mm a zzz");
			d.descriptors.add(i++, new Descriptor("Date Saved", ft.format(new Date(dateSaved))));
		}
		
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
	
	public int addStrikerBarParametersToDescriptionDictionary(DescriptorDictionary d, int i){
		double strikerBarDensity = 0;
		double strikerBarLength = 0;
		double strikerBarDiameter = 0;
		double strikerBarSpeed = 0; 
		
		strikerBarDensity = Converter.Lbin3FromKgM3(strikerBar.getDensity());
		strikerBarLength = Converter.InchFromMeter(strikerBar.getLength());
		strikerBarDiameter = Converter.InchFromMeter(strikerBar.getDiameter());
		strikerBarSpeed = Converter.FootFromMeter(strikerBar.getSpeed());
		if(SPSettings.metricMode.get()){
			strikerBarDensity = Converter.gccFromKgm3(strikerBar.getDensity());
			strikerBarLength = Converter.mmFromM(strikerBar.getLength());
			strikerBarDiameter = Converter.mmFromM(strikerBar.getDiameter());
			strikerBarSpeed = strikerBar.getSpeed();
		}
		if(strikerBar.getDensity()>0) 
			d.descriptors.add(i++, new Descriptor("Striker Bar Density", Double.toString(SPOperations.round(strikerBarDensity, 3))));
	
		if(strikerBar.getLength()>0) 
			d.descriptors.add(i++, new Descriptor("Striker Bar Length", Double.toString(SPOperations.round(strikerBarLength, 3))));

		if(strikerBar.getDiameter()>0) 
			d.descriptors.add(i++, new Descriptor("Striker Bar Diameter", Double.toString(SPOperations.round(strikerBarDiameter, 3))));

		if(strikerBar.getSpeed()>0) 
			d.descriptors.add(i++, new Descriptor("Striker Bar Speed", Double.toString(SPOperations.round(strikerBarSpeed, 3))));

		return i;
	}
	
	public abstract String getParametersForPopover(boolean selected2); 
	
	public String getCommonParametersForPopover(boolean metric){
		String des = "";
		if(dateSaved > 0){
			des += "Date Saved: " + Converter.getFormattedDate(new Date(dateSaved)) + SPSettings.lineSeperator;
		}
		if(metric){
			des += "Density: " + SPOperations.round(Converter.gccFromKgm3(density), 3) + " g/cc\n";
			des += "Heat Capacity: " + SPOperations.round(heatCapacity, 3) + " J/KgK\n";
			des += "Young's Modulus: " + SPOperations.round(Converter.GpaFromPa(youngsModulus), 3) + " GPA\n";
			if(strikerBar.getDensity()>0) {
				des += "Striker Bar Density: " + SPOperations.round(Converter.gccFromKgm3(strikerBar.getDensity()), 3) + " g/cc\n";

			}
			if(strikerBar.getLength()>0) {
				des += "Striker Bar Length: " + SPOperations.round(Converter.mmFromM(strikerBar.getLength()), 3) + " mm\n";

			}
			if(strikerBar.getDiameter()>0) {
				des += "Striker Bar Diameter: " + SPOperations.round(Converter.mmFromM(strikerBar.getDiameter()), 3) + " mm\n";

			}
			if(strikerBar.getSpeed()>0) {
				des += "Striker Bar Speed: " + SPOperations.round(strikerBar.getSpeed(), 3) + " m/s\n";
			}	
		}
		else{
			des += "Density: " + SPOperations.round(Converter.Lbin3FromKgM3(density), 3) + " Lb/in^3\n";
			des += "Heat Capacity: " + SPOperations.round(Converter.butanesPerPoundFarenheitFromJoulesPerKilogramKelvin(heatCapacity), 3) + " BTU/LbF\n";
			des += "Young's Modulus: " + SPOperations.round(Converter.psiFromPa(youngsModulus / Math.pow(10, 6)), 3) + " psi*10^6\n";
			if(strikerBar.getDensity()>0) {
				des += "Striker Bar Density: " + SPOperations.round(Converter.Lbin3FromKgM3(strikerBar.getDensity()), 3) + " Lb/in^3\n";

			}
			if(strikerBar.getLength()>0) {
				des += "Striker Bar Length: " + SPOperations.round(Converter.InchFromMeter(strikerBar.getLength()), 3) + " in\n";

			}
			if(strikerBar.getDiameter()>0) {
				des += "Striker Bar Diameter: " + SPOperations.round(Converter.InchFromMeter(strikerBar.getDiameter()), 3) + " in\n";

			}
			if(strikerBar.getSpeed()>0) {
				des += "Striker Bar Speed: " + SPOperations.round(Converter.FootFromMeter(strikerBar.getSpeed()), 3) + " ft/s\n";
			}
		}
		return des;
	}
	public double getWavespeed() {
		if(density == 0.0 || youngsModulus == 0.0)
			return 0;
		return Math.pow(youngsModulus / density, .5);
	}



	public abstract String getFileExtension();

	public List<LoadDisplacementSampleResults> getResults() {
		return this.results;
	}

	public void addResult(LoadDisplacementSampleResults result) {
		this.results.add(result);
	}
}
