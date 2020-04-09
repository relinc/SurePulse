package net.relinc.viewer.GUI;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.referencesample.ReferenceSample;
import net.relinc.libraries.referencesample.ReferenceSampleXY;
import net.relinc.libraries.referencesample.StressStrain;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.viewer.application.MetricMultiplier;
import net.relinc.viewer.application.RegionOfInterest;

public class CommonGUI {
	public static Stage stage;
	
	public static ListView<Sample> realCurrentSamplesListView;

	public static ListView<ReferenceSample> currentReferencesListView;
	
	//********Region for GUI for right option pane to open
	static AnchorPane optionPane = new AnchorPane();
	static TreeView<FileFX> sampleDirectoryTreeView;
	static ListView<FileFX> sessionsListView;
	static Button changeDirectoryButton;
	static Button refreshDirectoryButton;
	static Button xButton = new Button("X");
	static Button addSelectedSampleButton = new Button("Add Selected Sample(s)");
	static Button loadSessionButton = new Button("Load Selected Session");
	//*******
	public static String treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";
	
	
	//********Region for GUI for export pane to open
	static TreeItem<String> sampleGroupRoot;
	static ArrayList<SampleGroup> sampleGroups;
	static SampleGroup currentSelectedSampleGroup;
	static TextField tbSampleGroup;
	static Button buttonCreateSampleGroup;
	static TreeView<String> treeViewSampleGroups;
	static Button buttonAddSampleToGroup;
	static Button buttonExportData;
	static Button buttonExportCSV;
	static Button buttonDeleteSelectedGroup;
	static CheckBox includeSummaryPage;
	//*******
	
	//*********Video correlation Region****************
	static Button useSampleImages;
	static Button openImagesButton;
	static ScrollBar imageScrollBar;
	static Label imageShownLabel;
	static ImageView imageView;
	static LineChartWithMarkers<Number, Number> imageMatchingChart;// = new LineChart<Number, Number>();
	static Button saveVideoButton;
	
	//*******************

	public static SimpleBooleanProperty isEnglish;
	public static SimpleBooleanProperty isEngineering;
	public static SimpleBooleanProperty isLoadDisplacement;
	
	public static RegionOfInterest ROI;
	protected static MetricMultiplier timeUnits;
	
	protected int DataPointsToShow = 2000;
	
	protected static List<Color> seriesColors;
	
	public static void initCommon() {
		realCurrentSamplesListView = new ListView<Sample>();
		sampleGroups = new ArrayList<SampleGroup>();

		currentReferencesListView = new ListView<ReferenceSample>();

		//********Region for GUI for right option pane to open
		optionPane = new AnchorPane();
		sampleDirectoryTreeView = new TreeView<FileFX>();
		sessionsListView = new ListView<FileFX>();
		changeDirectoryButton = new Button("Change Directory");
		refreshDirectoryButton = new Button("", SPOperations.getIcon("/net/relinc/viewer/images/refreshIcon.png"));
		xButton = new Button("X");
		addSelectedSampleButton = new Button("Add Selected Sample(s)");
		loadSessionButton = new Button("Load Selected Session");
		//*******
		treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";
		
		
		//********Region for GUI for export pane to open
		tbSampleGroup = new TextField();
		buttonCreateSampleGroup = new Button("Create Group");
		treeViewSampleGroups = new TreeView<String>();
		buttonAddSampleToGroup = new Button("Add Samples to Group");
		buttonExportData = new Button("Export To Excel");
		buttonExportCSV = new Button("Export CSV");
		buttonDeleteSelectedGroup = new Button("Delete Group");
		includeSummaryPage = new CheckBox("Include Summary Page");
		//*******
		
		//*********Video correlation Region****************
		useSampleImages = new Button("Use sample images");
		openImagesButton = new Button("Choose Images");
		imageScrollBar = new ScrollBar();
		imageShownLabel = new Label("Image.jpg");
		imageView = new ImageView();
		saveVideoButton = new Button("Save Video");
		
		//*******************

		isEnglish = new SimpleBooleanProperty();
		isEngineering = new SimpleBooleanProperty();
		isLoadDisplacement = new SimpleBooleanProperty();
		
		ROI = new RegionOfInterest();
		timeUnits = new MetricMultiplier();
	}
	
	public List<Sample> getCheckedSamples(){
		List<Sample> samples = (List<Sample>) realCurrentSamplesListView.getItems().stream().filter(s-> s.isSelected()).collect(Collectors.toList());
		return samples;
	}

	public List<ReferenceSample> getCheckedReferenceSamples() {
		return (List<ReferenceSample>) currentReferencesListView.getItems().stream().filter(s-> s.selectedProperty().get()).collect(Collectors.toList());
	}
	
	public String getDisplayedLoadUnit(){
		if(!isLoadDisplacement.get()){
			// Stress
			return isEnglish.get() ? "ksi" : "MPa";
		}
		else{
			return isEnglish.get() ? "Lbf" : "N";
		}
	}
	
	public String getDisplayedDisplacementUnit(){
		if(!isLoadDisplacement.get()){
			// Strain
			return isEnglish.get() ? "in/in" : "mm/mm";
		}
		else{
			return isEnglish.get() ? "in" : "mm";
		}
	}
	
	public String getDisplayedStrainRateUnit(){
		if(isLoadDisplacement.get()){
			return isEnglish.get() ? "in/s" : "mm/s";
		}
		else{
			return isEnglish.get() ? "in/in/s" : "mm/mm/s";
		}
	}
	
	public String getDisplayedFaceForceUnit(){
		return isEnglish.get() ? "Lbf" : "N";
	}
	
	public int getSampleIndex(Sample s){
		return realCurrentSamplesListView.getItems().indexOf(s);
	}

	public int getReferenceSampleIndex(ReferenceSample s) {
		return currentReferencesListView.getItems().indexOf(s);
	}


}
