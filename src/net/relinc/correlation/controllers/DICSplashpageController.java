package net.relinc.correlation.controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import javax.imageio.ImageIO;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.ImageUInt8;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.application.TrackingAlgorithm;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.correlation.staticClasses.SPTargetTracker.TrackingAlgo;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.fitter.application.FitableDataset;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.processor.staticClasses.SPOperations;
import net.relinc.processor.staticClasses.SPSettings;

public class DICSplashpageController {
	@FXML ImageView runDICImageView;
	@FXML ImageView runDICResultsImageView;
	@FXML ImageView runTargetTrackingImageView;
	@FXML ImageView selectedTargetImageView;
	@FXML ImageView targetTrackingResultsImageView;
	@FXML ScrollBar scrollBar;
	@FXML Label imageNameLabel;
	@FXML Label resultsImageNameLabel;
	@FXML TabPane dicTabPane;
	@FXML ProgressBar dicProgressBar;
	@FXML Label dicStatusLabel;
	@FXML VBox dicStatusVBox;
	@FXML CheckBox drawTrailsCheckBox;
	@FXML CheckBox useSmoothedPointsCheckBox;
	
	@FXML ScrollBar scrollBarResults;
	@FXML ListView<Target> targetsListView;
	@FXML Button deleteTargetButton;
	@FXML ScrollBar targetBinarizationScrollBar;
	@FXML ScrollBar targetTrackingScrollBar;
	@FXML ScrollBar targetTrackingResultsScrollBar;
	@FXML ChoiceBox<TrackingAlgorithm> trackingAlgorithmChoiceBox;
	@FXML HBox topLevelControlsHBox;
	
	@FXML Tab imageSetupTab;
	@FXML Tab targetTrackingTab;
	@FXML Tab targetTrackingSetupTab;
	@FXML Tab targetTrackingResultsTab;
	public Stage stage;
	public int imageBeginIndex;
	public int imageEndIndex;
	public Point2D beginRectangle = new Point2D(0, 0);
	public Point2D endRectangle = new Point2D(0, 0);
	private List<File> imagePaths;
	public double displayImageToRealImageSizeRatio = 1;
	private Rectangle currentSelectedRectangle;
	private ArrayList<String> dicImageRunPaths = new ArrayList<>();
	private ArrayList<File> dicResultsImagePaths = new ArrayList<>();
	private String roiImagePath;
	private String dicBundleDirectory;
	private boolean tallerThanWide = true;
	private String[] targetColors = {  "#7ECC4F", "#CF5235", "#9D66D0", "#8ECBA7", "#4E5A34", "#CCB04E",
			"#9AA5C4", "#CA5093", "#9F5A52","#4E3959" };
	private double inchToPixelRatio = 1;
	private double lengthOfSample = 1;
	protected Point2D inchToPixelPoint1;
	protected Point2D inchToPixelPoint2;
	//Settings
	@FXML TextField scalefactor;
	@FXML TextField threads;
	@FXML TextField radiussub;
	@FXML TextField strainradius;
	@FXML ComboBox<String> interpolation;
	@FXML ComboBox<String> subregion;
	@FXML ComboBox<String> dicconfig;
	@FXML ComboBox<String> csvout;
	@FXML ComboBox<String> videoimgout;
	@FXML ComboBox<String> units;
	@FXML ComboBox<String> outsubregion;
	
	


	/**
	 * Sets up the GUI, adds action listeners, sets default settings and options
	 */
	@FXML
	public void initialize(){
		runDICResultsImageView.managedProperty().bind(runDICResultsImageView.visibleProperty());
		dicProgressBar.managedProperty().bind(dicProgressBar.visibleProperty());
		dicStatusLabel.managedProperty().bind(dicStatusLabel.visibleProperty());
		targetTrackingScrollBar.minProperty().bindBidirectional(scrollBar.minProperty());
		targetTrackingScrollBar.maxProperty().bindBidirectional(scrollBar.maxProperty());
		targetTrackingScrollBar.valueProperty().bindBidirectional(scrollBar.valueProperty());
		
		targetTrackingResultsScrollBar.minProperty().bindBidirectional(scrollBar.minProperty());
		targetTrackingResultsScrollBar.maxProperty().bindBidirectional(scrollBar.maxProperty());
		targetTrackingResultsScrollBar.valueProperty().bindBidirectional(scrollBar.valueProperty());

		scrollBar.setUnitIncrement(1.0);
		scrollBar.setBlockIncrement(1.0);
		
		scrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderRunROITab();
				renderTargetTrackingTab();
			}


		});
		
		scrollBarResults.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderResultsImages();
			}
		});
		
		//Fill setting values
		interpolation.getItems().add("Cubic Keys Precompute");
		interpolation.getItems().add("Quintic B-spline Precompute");
		subregion.getItems().add("Circle");
		dicconfig.getItems().add("NO_UPDATE");
		dicconfig.getItems().add("KEEP_MOST_POINTS");
		dicconfig.getItems().add("REMOVE_BAD_POINTS");
		csvout.getItems().add("mean e1,mean e2,mean exx,mean eyy,mean exy");
		csvout.getItems().add("mean e1");
		csvout.getItems().add("mean e2");
		csvout.getItems().add("mean exx");
		csvout.getItems().add("mean eyy");
		csvout.getItems().add("mean exy");
		videoimgout.getItems().add("e1");
		videoimgout.getItems().add("e2");
		videoimgout.getItems().add("exx");
		videoimgout.getItems().add("eyy");
		videoimgout.getItems().add("exy");
		units.getItems().add("inch");
		units.getItems().add("mm");
		outsubregion.getItems().add("Circle");
		outsubregion.getItems().add("Square");
		
		//Set default setting values
		scalefactor.setText("3");
		threads.setText("8");
		radiussub.setText("20");
		strainradius.setText("20");
		interpolation.getSelectionModel().select(0);
		subregion.getSelectionModel().select(0);
		dicconfig.getSelectionModel().select(0);
		csvout.getSelectionModel().select(0);
		videoimgout.getSelectionModel().select(0);
		units.getSelectionModel().select(0);
		outsubregion.getSelectionModel().select(0);

		runTargetTrackingImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Target t = getSelectedTarget();
				double sizeRatio = runTargetTrackingImageView.getFitHeight() / runTargetTrackingImageView.getImage().getHeight();
				if(!tallerThanWide){
					sizeRatio = runTargetTrackingImageView.getFitWidth() / runTargetTrackingImageView.getImage().getWidth();
				}
				if(t == null)
				{
					//draw inch to pixel ratio.
					inchToPixelPoint1 = new Point2D(event.getX(), event.getY());
					inchToPixelPoint1 = inchToPixelPoint1.multiply(1 / sizeRatio);
					inchToPixelPoint2 = null;
				}
				else{
					
					t.center = new Point2D(event.getX(), event.getY());
					t.center = t.center.multiply(1 / sizeRatio);
					t.vertex = null;
					t.renderRectangle();
				}
				
				renderTargetTrackingTab();
			}
		});
		
		runTargetTrackingImageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Target t = getSelectedTarget();
				double sizeRatio = runTargetTrackingImageView.getFitHeight() / runTargetTrackingImageView.getImage().getHeight();
				if (!tallerThanWide) {
					sizeRatio = runTargetTrackingImageView.getFitWidth() / runTargetTrackingImageView.getImage().getWidth();
				}
				if (t == null) {
					inchToPixelPoint2 = new Point2D(event.getX(), event.getY());
					inchToPixelPoint2 = inchToPixelPoint2.multiply(1 / sizeRatio);
				}
				else 
				{
					t.vertex = new Point2D(event.getX(), event.getY());
					t.vertex = t.vertex.multiply(1 / sizeRatio);
					t.renderRectangle();
					
				}
				renderTargetTrackingTab();
				
			}
		});
		
		runTargetTrackingImageView.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (getSelectedTarget() == null) {
					TextInputDialog dialog = new TextInputDialog("distance");
					dialog.setTitle("Input Required");
					dialog.setHeaderText("Configure inch to pixel ratio");
					dialog.setContentText("Please enter the distance drawn:");

					// Traditional way to get the response value.
					Optional<String> result = dialog.showAndWait();
					if (result.isPresent()) {
						inchToPixelRatio = Double.parseDouble(result.get())
								/ inchToPixelPoint1.distance(inchToPixelPoint2);
					}
				}

			}
		});
		
		targetsListView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Target>() {
			@Override
			public void changed(ObservableValue<? extends Target> observable, Target oldValue, Target newValue) {
				Target t = getSelectedTarget();
				if(t != null)
					targetBinarizationScrollBar.setValue(t.getThreshold());
				renderTargetTrackingTab();
			}
		});
		
		targetBinarizationScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				Target t = getSelectedTarget();
				if(t != null){
					t.setThreshold(targetBinarizationScrollBar.getValue());
					renderTargetTrackingTab();
				}
			}
		});
		
		targetTrackingResultsScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			@Override
			public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
				renderTargetTrackingResultsTab();
			}
		});
		
		targetTrackingTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
				renderTargetTrackingResultsTab();
			}
		});
		
		targetTrackingResultsTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
				renderTargetTrackingResultsTab();
			}
		});
		
		imageSetupTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderRunROITab();
			}
		});
		
		drawTrailsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingResultsTab();
			}
		});
		
		for(TrackingAlgo al : TrackingAlgo.values())
			trackingAlgorithmChoiceBox.getItems().add(new TrackingAlgorithm(al));
		trackingAlgorithmChoiceBox.getSelectionModel().select(0);
		
		
		targetBinarizationScrollBar.setMin(0.0);
		targetBinarizationScrollBar.setMax(255.0);
		
	}
	
	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
		  @Override
		  public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
		  {
//			  Target tar = new Target();
//			  targetsListView.getItems().add(tar);
//			  targetsListView.getItems().remove(tar);
//			  System.out.println(targetsListView.getItems().toString());
		  }
		});
	}

	/**
	 * Load Images button click listener
	 */
	public void loadImagesFired(){
		FileChooser fileChooser = new FileChooser();
		imagePaths =
				fileChooser.showOpenMultipleDialog(stage);
		resetEverything();
		renderRunROITab();
		renderTargetTrackingTab();
		renderTargetTrackingResultsTab();
	}
	
	/**
	 * Draw ROI button click listener
	 */
	public void drawROIFired() {
		resizeImageViewToFit(runDICImageView);
		
		
	}
	
	public void newTargetButtonFired(){
		Stage primaryStage = new Stage();
		try {
			//prepare app data directory. 
			
			//BorderPane root = new BorderPane();
			FXMLLoader root = new FXMLLoader(getClass().getResource("/net/relinc/correlation/fxml/GetTargetName.fxml"));
			
			Scene scene = new Scene(root.load());
			//scene.getStylesheets().add(getClass().getResource("dicapplication.css").toExternalForm());
			primaryStage.setScene(scene);
			GetTargetNameController cont = root.getController();
			cont.stage = primaryStage;
			cont.targetNameTextField.setText("Target " + (targetsListView.getItems().size() + 1));
			Target target = new Target();
			target.setColor(targetColors[targetsListView.getItems().size() % targetColors.length]);
			cont.target = target;
			
			//Alert alert = new Alert(AlertType.INFORMATION);
			//Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
			primaryStage.getIcons().add(SPSettings.getRELLogo());
			primaryStage.initOwner(stage);
			primaryStage.initModality(Modality.WINDOW_MODAL);
			
			primaryStage.showAndWait();
			if(target.getName() != null){
				targetsListView.getItems().add(target);
				targetsListView.getSelectionModel().select(target);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void drawKnownLengthButtonFired(){
		targetsListView.getSelectionModel().clearSelection();
	}
	
	public void enterSampleLengthButtonFired(){
		TextInputDialog dialog = new TextInputDialog("sample length");
		dialog.setTitle("Input Required");
		dialog.setHeaderText("Sample length");
		dialog.setContentText("Please enter the sample length:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			lengthOfSample = Double.parseDouble(result.get());
		}
	}
	
	public void deleteTargetButtonFired(){
		Target t = getSelectedTarget();
		if(t != null)
			targetsListView.getItems().remove(t);
		renderTargetTrackingTab();
	}
	
	
	
	public Target getSelectedTarget(){
		return targetsListView.getSelectionModel().getSelectedItem();
	}
	
	/**
	 * Resizes an image view to fit the bounds of its parent
	 * 
	 * @param imageView The image view to be resized
	 */
	public void resizeImageViewToFit(ImageView imageView){
		imageView.fitHeightProperty().unbind();
		imageView.fitWidthProperty().unbind();
		imageView.setFitHeight(10);
		imageView.setFitWidth(10);
		imageView.setFitHeight(-1);
		imageView.setFitWidth(-1);
 
		if(imageView.getImage().getHeight() / imageView.getImage().getWidth() > ((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / ((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue()){
			tallerThanWide = true;
			imageView.fitHeightProperty().bind(((AnchorPane)imageView.getParent().getParent()).heightProperty());
		}
		else	
		{
			tallerThanWide = false;
			imageView.fitWidthProperty().bind(((AnchorPane)imageView.getParent().getParent()).widthProperty());
		}

	}
	
	/**
	 * Set Begin button click listener, sets the first image in the list to run DIC on
	 */
	public void setBeginFired(){
		imageBeginIndex = (int)scrollBar.getValue();
		renderRunROITab();
	}
	
	/**
	 * Set End button click listener, sets the last image in the list to run DIC on
	 */
	public void setEndFired(){
		imageEndIndex = (int)scrollBar.getValue();
		renderRunROITab();
	}
	
	/**
	 * Starts the ROI rectangle drawing
	 * 
	 * @param e MouseEvent object from mouse press
	 */
	@FXML
	public void imageViewMousePressedFired(MouseEvent e){
		beginRectangle = new Point2D(e.getX(), e.getY());
		endRectangle = new Point2D(e.getX(), e.getY());
		renderRunROITab();
	}
	
	/**
	 * Draws rectangle for ROI on mouse drag
	 * 
	 * @param e MouseEvent object from mouse drag
	 */
	public void imageViewMouseDraggedFired(MouseEvent e){
		endRectangle = new Point2D(e.getX(), e.getY());
		renderRunROITab();
	}
	

	
	public void smoothResultsButtonFired(){
		Stage primaryStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/fitter/GUI/Home.fxml"));
			Scene scene = new Scene(root1.load());
			
	        primaryStage.setTitle("SURE-Pulse Fitter");
			primaryStage.setScene(scene);
			HomeController c = root1.<HomeController>getController();
			c.renderGUI();
			
			for(Target target : targetsListView.getItems()){
				if(target.xPts == null){
					target.xPts = createXFitableDataset(target.pts, target.getName() + " X");
					target.yPts = createYFitableDataset(target.pts, target.getName() + " Y");
				}
				c.datasetsListView.getItems().add(target.xPts);
				c.datasetsListView.getItems().add(target.yPts);
			}
			
			c.datasetsListView.getSelectionModel().select(0);
			c.renderGUI();
			
			//c.stage = primaryStage;
			primaryStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exportRawDataButtonFired(){
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Raw Data CSV");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
        	String csv = "Inch To Pixel Ratio:," + inchToPixelRatio + "\n\n";
    		for(Target target : targetsListView.getItems()){
    			csv += "," + target.getName() + ",,,";
    		}
    		csv += "\n";
    		for(Target target : targetsListView.getItems()){
    			csv += ",X,Y,Smoothed X,Smoothed Y";
    		}
    		csv += "\n";
    		Target target1 = targetsListView.getItems().get(0);
    		for(int i = 0; i < target1.pts.length; i++){
    			csv += imagePaths.get(i + imageBeginIndex).getName();
    			for(Target target : targetsListView.getItems()){
    				csv += "," + target.pts[i].getX() + "," + target.pts[i].getY() + "," +
    						target.xPts.fittedY.get(i) + "," + target.yPts.fittedY.get(i);
    			}
    			csv += "\n";
    		}
    		SPOperations.writeStringToFile(csv, file.getPath() + ".csv");
        }
		
	}
	
	public void exportStrainButtonFired(){
		Stage primaryStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/correlation/fxml/ExportStrain.fxml"));
			Scene scene = new Scene(root1.load());
			
	        primaryStage.setTitle("SURE-Pulse Image Correlation");
			primaryStage.setScene(scene);
			ExportStrainController c = root1.<ExportStrainController>getController();
			
			c.inchToPixelRatio = inchToPixelRatio;
			c.lengthOfSample = lengthOfSample;
			c.useSmoothedPoints = useSmoothedPointsCheckBox.isSelected();
			c.beginIndex = imageBeginIndex;
			c.imagePaths = imagePaths;
			c.fillTargetsListView(targetsListView.getItems());
			c.renderGUI();
			c.stage = primaryStage;
			primaryStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void exportDisplacementButtonFired(){
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Displacement CSV");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
        	String csv = "Image";
    		for(Target target : targetsListView.getItems()){
    			csv += "," + target.getName() + " Displacement";
    		}
    		csv += "\n";
    		targetsListView.getItems().stream().forEach(t -> t.displacement = SPTargetTracker.calculateDisplacement(t, inchToPixelRatio, useSmoothedPointsCheckBox.isSelected(), lengthOfSample));
    		Target target1 = targetsListView.getItems().get(0);
    		for(int i = 0; i < target1.pts.length; i++){
    			csv += imagePaths.get(i + imageBeginIndex).getName();
    			for(Target target : targetsListView.getItems()){
    				csv += "," + target.displacement[i];
    			}
    			csv += "\n";
    		}
    		SPOperations.writeStringToFile(csv, file.getPath() + ".csv");
        }
	}
	
	public void exportSpeedButtonFired(){
		TextInputDialog dialog = new TextInputDialog("1");
		dialog.setTitle("");
		dialog.setHeaderText("Need Frames per second for speed");
		dialog.setContentText("Please enter FPS:");

		// Traditional way to get the response value.
		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			FileChooser fileChooser = new FileChooser();
	        fileChooser.setTitle("Save Speed CSV");
	        File file = fileChooser.showSaveDialog(stage);
	        if (file != null) {
	        	String csv = "Image";
	    		for(Target target : targetsListView.getItems()){
	    			csv += "," + target.getName() + " Speed";
	    		}
	    		csv += "\n";
	    		targetsListView.getItems().stream().forEach(t -> t.speed = SPTargetTracker.calculateSpeed(t, inchToPixelRatio, useSmoothedPointsCheckBox.isSelected(), lengthOfSample,Double.parseDouble(result.get())));
	    		Target target1 = targetsListView.getItems().get(0);
	    		for(int i = 0; i < target1.pts.length; i++){
	    			csv += imagePaths.get(i + imageBeginIndex).getName();
	    			for(Target target : targetsListView.getItems()){
	    				csv += "," + target.speed[i];
	    			}
	    			csv += "\n";
	    		}
	    		SPOperations.writeStringToFile(csv, file.getPath() + ".csv");
	        }
		}
	}
	
	public void exportVideoButtonFired(){
		Dialogs.showAlert("Not Implemented", stage);
	}
	
	private FitableDataset createXFitableDataset(Point2D[] x, String name){
		ArrayList<Double> xList = new ArrayList<Double>(x.length);
		ArrayList<Double> yList = new ArrayList<Double>(x.length);
		for(int i = 0; i < x.length; i++){
			xList.add(new Double(i));
			yList.add(new Double(x[i].getX()));
		}
		return new FitableDataset(xList, yList, name);
	}
	
	private FitableDataset createYFitableDataset(Point2D[] y, String name){
		ArrayList<Double> xList = new ArrayList<Double>(y.length);
		ArrayList<Double> yList = new ArrayList<Double>(y.length);
		for(int i = 0; i < y.length; i++){
			xList.add(new Double(i));
			yList.add(new Double(y[i].getY()));
		}
		return new FitableDataset(xList, yList, name);
	}

	/**
	 * Reset beginning and end images, reset scroll bar to start
	 */
	private void resetEverything() {
		imageBeginIndex = 0;

		if(imagePaths != null)
			imageEndIndex = imagePaths.size() - 1;

		scrollBar.setValue(0);
	}

	/**
	 * Button click listener for reset begin, resets start image index
	 */
	public void resetBeginIndexFired() {
		imageBeginIndex = 0;
		scrollBar.setMin(0);
		scrollBar.setValue(0);
	}

	/**
	 * Button click listener for reset end, resets end image index
	 */
	public void resetEndIndexFired() {
		imageEndIndex = imagePaths.size() - 1;
		scrollBar.setMax(imageEndIndex);
		scrollBar.setValue(imageEndIndex);
	}

	private void renderRunROITab() {
		if(!imageSetupTab.isSelected())
			return;
		
		scrollBar.setMin(imageBeginIndex);
		scrollBar.setMax(imageEndIndex);

		BufferedImage img = null;
		try {
			img = getRgbaImage(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	

		if(img != null) {
			Graphics2D g2d = img.createGraphics();
			double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
			if(!tallerThanWide){
				sizeRatio = runDICImageView.getFitWidth() / runDICImageView.getImage().getWidth();
			}
			// Draw on the buffered image
			g2d.setColor(Color.green);
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			currentSelectedRectangle = getRectangleFromPoints(beginRectangle, endRectangle, 1/sizeRatio);
			g2d.draw(currentSelectedRectangle);
			g2d.dispose();
			runDICImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabel.setText(imagePaths.get((int)scrollBar.getValue()).getName());
		}
	}
	
	private BufferedImage getRgbaImage(File imageFile) throws IOException {
		BufferedImage img = ImageIO.read(imageFile);
		BufferedImage rgbImg = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_ARGB);
		rgbImg.getGraphics().drawImage(img,0,0,null);
		return rgbImg;
	}
	
	private void renderTargetTrackingTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingSetupTab.isSelected()))
			return;
		
		scrollBar.setMin(imageBeginIndex);
		scrollBar.setMax(imageEndIndex);

		BufferedImage img = null;
		BufferedImage copy = null;
		try {
			img = getRgbaImage(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
			copy = getRgbaImage(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			
			Graphics2D g2d = img.createGraphics();
//			double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
//			if(!tallerThanWide){
//				sizeRatio = runDICImageView.getFitWidth() / runDICImageView.getImage().getWidth();
//			}
			// Draw on the buffered image
			
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			for(Target targ : targetsListView.getItems())
			{
				g2d.setColor(Color.decode(targ.getColor()));
				if(targ.rectangle == null)
					continue;
				currentSelectedRectangle =  targ.rectangle;//getRectangleFromPoints(beginRectangle, endRectangle, 1/sizeRatio);
				g2d.draw(currentSelectedRectangle);
			}
			
			if(inchToPixelPoint1 != null && inchToPixelPoint2 != null){
				g2d.setColor(Color.decode("#a8a8a8"));
				g2d.drawLine((int)inchToPixelPoint1.getX(), (int)inchToPixelPoint1.getY(), (int)inchToPixelPoint2.getX(), (int)inchToPixelPoint2.getY());
			}
			
			
			g2d.dispose();
			
			runTargetTrackingImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabel.setText(imagePaths.get((int)scrollBar.getValue()).getName());
			
			img = copy;
			Target target = getSelectedTarget();
			if(target != null && target.rectangle != null && target.rectangle.getWidth() > 0 && target.rectangle.getHeight() > 0)
			{
				ImageUInt8 image = new ImageUInt8(img.getWidth(), img.getHeight());
				ConvertBufferedImage.convertFrom(img, image);
				ImageUInt8 targetImage = image.subimage((int)target.rectangle.getX(), (int)target.rectangle.getY(), 
						(int)target.rectangle.getWidth() + (int)target.rectangle.getX(),
						(int)target.rectangle.getHeight() + (int)target.rectangle.getY());
				//ThresholdImageOps.threshold(targetImage, targetImage, 20, false);
				//ImageUInt8 binary = new ImageUInt8(targetImage.getWidth(), targetImage.getHeight());
				
				if(target.getThreshold() == 0.0){
					target.setThreshold(GThresholdImageOps.computeOtsu(targetImage, 0, 255));
					targetBinarizationScrollBar.setValue(target.getThreshold());
				}
				
				// Apply the threshold to create a binary image
				ImageUInt8 binary = targetImage;
				BufferedImage bufferedTargetImage = getBufferedImageFromBoofCVImage(binary);//VisualizeBinaryData.renderBinary(binary, false, null);
				if(trackingAlgorithmChoiceBox.getSelectionModel().getSelectedItem().algo == TrackingAlgo.SIMPLECORRELATE)
				{
					targetBinarizationScrollBar.setVisible(true);
					binary = SPTargetTracker.threshold(targetImage, (int)target.getThreshold());
					bufferedTargetImage = VisualizeBinaryData.renderBinary(binary, false, null);
				}
				else{
					targetBinarizationScrollBar.setVisible(false);
				}
				
				//new BufferedImage(binary.getWidth(),binary.getHeight(),BufferedImage.BITMASK);
				//ConvertBufferedImage.convertTo(binary, bufferedTargetImage);
				selectedTargetImageView.setImage(SwingFXUtils.toFXImage(bufferedTargetImage, null));
				deleteTargetButton.setStyle(" -fx-base: " + getSelectedTarget().getColor() + ";");
			}
			
		}
		resizeImageViewToFit(runTargetTrackingImageView);
	}
	
	private void renderTargetTrackingResultsTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingResultsTab.isSelected()))
			return;
		scrollBar.setMin(imageBeginIndex);
		scrollBar.setMax(imageEndIndex);
		int imageIndex = (int)scrollBar.getValue();

		BufferedImage img = null;
		try {
			img = getRgbaImage(new File(imagePaths.get(imageIndex).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			
			Graphics2D g2d = img.createGraphics();
			// Draw on the buffered image
			
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			for(Target targ : targetsListView.getItems())
			{
				g2d.setColor(Color.decode(targ.getColor()));
				if(targ.rectangle == null || targ.pts == null)
					continue;
				
				if(drawTrailsCheckBox.isSelected()){
					for(int i = imageBeginIndex; i <= imageIndex; i++){
						Ellipse2D circ = new Ellipse2D.Double(targ.pts[i].getX(), targ.pts[i].getY(), 10, 10);
						g2d.draw(circ);
					}
				}
				else{
					Ellipse2D circ = new Ellipse2D.Double(targ.pts[imageIndex].getX(), targ.pts[imageIndex].getY(), 10, 10);
					g2d.draw(circ);
				}
				
				
			}
			
			g2d.dispose();
			
			targetTrackingResultsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabel.setText(imagePaths.get((int)scrollBar.getValue()).getName());
			
		}
		resizeImageViewToFit(targetTrackingResultsImageView);
	}
	
	private void renderResultsImages() {
		scrollBarResults.setMin(0);
		scrollBarResults.setMax(dicResultsImagePaths.size() - 1);

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(dicResultsImagePaths.get((int)scrollBarResults.getValue()).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			runDICResultsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			resultsImageNameLabel.setText(dicResultsImagePaths.get((int)scrollBarResults.getValue()).getName());
		}
	}

	private Rectangle getRectangleFromPoints(Point2D p1, Point2D p2, double sizeRatio) {
		p1 = p1.multiply(sizeRatio);
		p2 = p2.multiply(sizeRatio);

		int height = (int)Math.abs(p1.getY() - p2.getY());
		int width = (int)Math.abs(p1.getX() - p2.getX());

		int startX = (int)Math.min(p1.getX(), p2.getX());
		int startY = (int)Math.min(p1.getY(), p2.getY());

		return new Rectangle(startX, startY, width, height);
	}


	public void runDicButtonFired() {
		if(imagePaths.size() < 2) {
			Dialogs.showInformationDialog("Run DIC", "Please Select More Images", "DIC Requires at least 2 images to run", stage);
			return;
		}
		File results = new File(SPSettings.imageProcResulstsDir);
		if(results.exists() && results.isDirectory()) {
			SPOperations.deleteFolder(results);
		}
		results.mkdirs();
		if(copyImages()) {
			File dicJobFile = new File(SPSettings.imageProcResulstsDir + "/ncorr_job_file.txt");
			try {
				if(dicJobFile.exists())
					dicJobFile.delete();
				
				BufferedWriter bw = new BufferedWriter(new FileWriter(dicJobFile, true));
				bw.write("Version 1.0	SURE-DIC"+"\n");
				bw.write("\u20ACSample Name:	Trial Sample"+"\n"+"\n");

				bw.write("\u20ACImages:"+"\n");

				System.out.println(dicImageRunPaths);
				for(String imagePath : dicImageRunPaths) {
					bw.write(imagePath+""+"\n");
				}

				bw.write("\n"+ "\u20ACROI:"+"\n");
				bw.write(roiImagePath+""+"\n"+"\n");

				bw.write("\u20ACDIC Settings:"+"\n");
				bw.write("Scale factor:	"+scalefactor.getText()+"\n"); 
				bw.write("Interpolation:	"+interpolation.getSelectionModel().getSelectedItem()+"\n"); 
				bw.write("threads:	"+threads.getText()+"\n"); 
				bw.write("Subregion:	"+subregion.getSelectionModel().getSelectedItem()+"\n"); 
				bw.write("Radius sub:	"+radiussub.getText()+"\n");
				bw.write("DIC config:	"+dicconfig.getSelectionModel().getSelectedItem()+"\n"+"\n"); 

				bw.write("\u20ACOutput Settings:"+"\n");
				bw.write("Strain mode:	Eulerian"+"\n");
				bw.write("CSV out:	"+csvout.getSelectionModel().getSelectedItem()+"\n");
				bw.write("Video/Img out:	"+videoimgout.getSelectionModel().getSelectedItem()+"\n");
				bw.write("units	:	"+units.getSelectionModel().getSelectedItem()+"\n");
				bw.write("units per px:	.01"+"\n");
				bw.write("fps	:	15"+"\n");
				bw.write("OpenCv color:	COLORMAP_JET"+"\n");
				bw.write("end delay:	2"+"\n");
				bw.write("fourcc	:	M,J,P,G"+"\n");
				bw.write("colorbar:	true"+"\n");
				bw.write("axes	:	false"+"\n");
				bw.write("scalebar:	false"+"\n");
				bw.write("num units:	-1"+"\n");
				bw.write("font size:	1"+"\n");
				bw.write("tick marks:	11"+"\n");
				bw.write("strain min:	0"+"\n");
				bw.write("strain max:	.1"+"\n");
				bw.write("disp min:	0"+"\n");
				bw.write("disp max:	2"+"\n");
				bw.write("strain radius:	"+strainradius.getText()+"\n");
				bw.write("Subregion:	"+outsubregion.getSelectionModel().getSelectedItem()+"\n"); //use
				bw.write("Output	:	Image"+"\n");
				bw.write("Output Dir:	"+SPSettings.imageProcResulstsDir+"/\n\n");

				bw.write("\u20ACResults:"+"\n");
				bw.write("DIC input:	None"+"\n");
				bw.write("DIC output:	None"+"\n");
				bw.write("strain input:	None"+"\n");
				bw.write("strain output:	None"+"\n");
				bw.flush();
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			runNCorr(dicJobFile);

		}
	}
	
	@FXML
	private void runTargetTrackingButtonFired(){
//		SwingWorker<Integer, Integer> worker = new SwingWorker<Integer, Integer>() {
//		    @Override
//		    public Integer doInBackground() {
//		    	for(Target t : targetsListView.getItems())
//		    		t.pts = SPTargetTracker.trackTargetUnknownAlgo(imagePaths, imageBeginIndex, imageEndIndex, t, trackingAlgorithmChoiceBox.getSelectionModel().getSelectedItem().algo);
//		    	return 1;
//		    }
//
//		    @Override
//		    public void done() {
//		        topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
//		    }
//		};
		Label targetLabel = new Label();
		Button cancelButton = new Button("Cancel");
		ProgressBar progressBar = new ProgressBar();
		Task<Integer> task = new Task<Integer>() {
		    @Override protected Integer call() throws Exception {
		    	SPTargetTracker.cancelled = false;
		    	for(Target t : targetsListView.getItems()){
		    		Platform.runLater(new Runnable() {
			            @Override
			            public void run() {
			            	//String output = message.replace("Processing displacement field ","");
							targetLabel.setText(t.getName());
							progressBar.setStyle("-fx-accent: " + t.getColor() + ";");
			            }
			          });
		    		t.pts = SPTargetTracker.trackTargetUnknownAlgo(imagePaths, imageBeginIndex, imageEndIndex, t, trackingAlgorithmChoiceBox.getSelectionModel().getSelectedItem().algo);
		    	}
		    		return 1;
		    }
		};
		
		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				SPTargetTracker.cancelled = true;
				System.out.println("On cancelled");
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
			}
		});
		
		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
				topLevelControlsHBox.getChildren().remove(topLevelControlsHBox.getChildren().size() - 1);
			}
		});
		
		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				System.out.println("Canceling");
				task.cancel();
			}
		});
		
		//targetLabel.textProperty().bind(SPTargetTracker.targetName);
		
		//progressBar.setStyle("-fx-accent: green");
		SPTargetTracker.progress.set(0.0);
		progressBar.progressProperty().bind(SPTargetTracker.progress);
		
		//progressBar.progressProperty().bind(
		cancelButton.setStyle("-fx-base: #ff6666;");
		topLevelControlsHBox.getChildren().add(cancelButton);
		topLevelControlsHBox.getChildren().add(targetLabel);
		topLevelControlsHBox.getChildren().add(progressBar);
		
		
		new Thread(task).start();
//		for(Target target : targetsListView.getItems()){
//			target.pts = SPTargetTracker.trackTargetUnknownAlgo(imagePaths, imageBeginIndex, imageEndIndex, target, trackingAlgorithmChoiceBox.getSelectionModel().getSelectedItem().algo);
//		}
	}

	private void runNCorr(File dicJobFile) {
		dicResultsImagePaths.clear();
		runDICResultsImageView.setVisible(false);
		dicProgressBar.setVisible(true);
		dicStatusLabel.setVisible(true);
		dicTabPane.getSelectionModel().select(3);
		Task<Void> task = new Task<Void>() {
			@Override 
			public Void call() {
				
				String NcorrLocation = SPSettings.currentOS.contains("Win") ? "libs/ncorr/ncorr_CommandLine.exe" 
						: "/Applications/SURE-Pulse.app/ncorr/ncorr_FullCmdLineTool";
				String[] cmd = { NcorrLocation, "calculate", dicJobFile.getPath() };
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.redirectError(Redirect.INHERIT);
				int i = 1;
				try {
					Process p = pb.start();
					BufferedReader reader = 
							new BufferedReader(new InputStreamReader(p.getInputStream()));
					String line = null;
					while ( (line = reader.readLine()) != null) {
						final String message = line;
						if(message.contains("Processing displacement field")) {
							i++;
							Platform.runLater(new Runnable() {
					            @Override
					            public void run() {
					            	String output = message.replace("Processing displacement field ","");
									dicStatusLabel.setText("DIC Status: Processing Image " + output);
					            }
					          }); 
							updateProgress(i, dicImageRunPaths.size());
						}
					}

				} catch (IOException e) {
					dicStatusLabel.setText("DIC Failed!");
					e.printStackTrace();
					return null;
				}
				Platform.runLater(new Runnable() {
		            @Override
		            public void run() {
		            	notifyGuiDicFinished();
		            }
		          });
				return null;
			}
		};
		dicProgressBar.progressProperty().bind(task.progressProperty());
		new Thread(task).start();
	}
	
	public void notifyGuiDicFinished() {
		runDICResultsImageView.setVisible(true);
		dicProgressBar.setVisible(false);
		dicStatusLabel.setVisible(false);
		File dir = new File(SPSettings.imageProcResulstsDir+"/video");
		  File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	dicResultsImagePaths.add(child);
		    }
		  } 
		  
		  renderResultsImages();
		  resizeImageViewToFit(runDICResultsImageView);
	}

	private Boolean copyImages() {

		if(imagePaths == null || imagePaths.size() == 0) {
			Dialogs.showInformationDialog("Run DIC", null, "Please Load Image Files", stage);
			return false;
		}

		dicBundleDirectory = imagePaths.get(0).getParentFile().getPath();
		System.out.println(dicBundleDirectory);
		File file = new File(dicBundleDirectory);

		if(!file.exists())
			file.mkdir();

		dicImageRunPaths.clear();

		 System.out.println(imageBeginIndex + " " + imageEndIndex);
		 for(int i = imageBeginIndex; i <= imageEndIndex; i++) {
			 dicImageRunPaths.add(imagePaths.get(i).getPath());
		}

		BufferedImage roiImage = new BufferedImage((int)runDICImageView.getImage().getWidth(), (int)runDICImageView.getImage().getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = roiImage.createGraphics();
		g2d.setBackground(Color.BLACK);
		g2d.setColor(Color.white);
		g2d.fill(currentSelectedRectangle);
		g2d.draw(currentSelectedRectangle);
		try {
			//TODO: THERE IS A BUG WHEN RUNNING THE SAME ROI TWICE, NULL POINTER
			File roi = new File(SPSettings.imageProcResulstsDir+"/roi.png");
			ImageIO.write(roiImage, "PNG", roi);
			roiImagePath = roi.getPath();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private String getTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy_HH-mm-ss");
		return sdf.format(cal.getTime());
	}
	
	private javafx.scene.image.Image getFXImageFromBoofCVImage(ImageUInt8 im){
		BufferedImage buf = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		ConvertBufferedImage.convertTo(im, buf);
		return SwingFXUtils.toFXImage(buf, null);
	}
	
	private BufferedImage getBufferedImageFromBoofCVImage(ImageUInt8 im){
		BufferedImage buf = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
		ConvertBufferedImage.convertTo(im, buf);
		return buf;
	}
	


}

