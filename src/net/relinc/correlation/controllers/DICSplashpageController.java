package net.relinc.correlation.controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.imageio.ImageIO;

import boofcv.abst.tracker.TrackerObjectQuad;
import boofcv.alg.feature.detect.template.TemplateMatching;
import boofcv.alg.filter.binary.GThresholdImageOps;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.shapes.corner.RefineCornerLinesToImage;
import boofcv.factory.feature.detect.template.FactoryTemplateMatching;
import boofcv.factory.feature.detect.template.TemplateScoreType;
import boofcv.factory.tracker.FactoryTrackerObjectQuad;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.image.ShowImages;
import boofcv.gui.tracker.TrackerObjectQuadPanel;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.SimpleImageSequence;
import boofcv.misc.BoofMiscOps;
import boofcv.struct.feature.Match;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageUInt8;
import georegression.struct.shapes.Quadrilateral_F64;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.staticClasses.CorrSettings;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.fitter.application.FitableDataset;
import net.relinc.processor.staticClasses.Dialogs;
import sun.awt.resources.awt;

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
	
	@FXML ScrollBar scrollBarResults;
	@FXML ListView<Target> targetsListView;
	@FXML Button deleteTargetButton;
	@FXML ScrollBar targetBinarizationScrollBar;
	@FXML ScrollBar targetTrackingScrollBar;
	@FXML ScrollBar targetTrackingResultsScrollBar;
	
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
				if(t == null)
					return;
				double sizeRatio = runTargetTrackingImageView.getFitHeight() / runTargetTrackingImageView.getImage().getHeight();
				if(!tallerThanWide){
					sizeRatio = runTargetTrackingImageView.getFitWidth() / runTargetTrackingImageView.getImage().getWidth();
				}
				t.center = new Point2D(event.getX(), event.getY());
				t.center = t.center.multiply(1 / sizeRatio);
				t.vertex = null;
				t.renderRectangle();
				renderTargetTrackingTab();
			}
		});
		
		runTargetTrackingImageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Target t = getSelectedTarget();
				if(t == null)
					return;
				double sizeRatio = runTargetTrackingImageView.getFitHeight() / runTargetTrackingImageView.getImage().getHeight();
				if(!tallerThanWide){
					sizeRatio = runTargetTrackingImageView.getFitWidth() / runTargetTrackingImageView.getImage().getWidth();
				}
				t.vertex = new Point2D(event.getX(), event.getY());
				t.vertex = t.vertex.multiply(1 / sizeRatio);
				t.renderRectangle();
				renderTargetTrackingTab();
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
			
			primaryStage.showAndWait();
			if(target.getName() != null){
				targetsListView.getItems().add(target);
				targetsListView.getSelectionModel().select(target);
			}
		} catch(Exception e) {
			e.printStackTrace();
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
		//build csv.
		String csv = "Inch To Pixel Ratio:,1\n\n";
		for(Target target : targetsListView.getItems()){
			csv += target.getName() + ",,,,";
		}
		csv += "\n";
		for(Target target : targetsListView.getItems()){
			csv += "X,Y,Smoothed X,Smoothed Y";
		}
		csv += "\n";
		
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
			img = ImageIO.read(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			Graphics2D g2d = img.createGraphics();
			double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
			if(!tallerThanWide){
				sizeRatio = runDICImageView.getFitWidth() / runDICImageView.getImage().getWidth();
			}
			// Draw on the buffered image
			g2d.setColor(Color.white);
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			currentSelectedRectangle = getRectangleFromPoints(beginRectangle, endRectangle, 1/sizeRatio);
			g2d.draw(currentSelectedRectangle);
			g2d.dispose();
			runDICImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabel.setText(imagePaths.get((int)scrollBar.getValue()).getName());
		}
	}
	
	private void renderTargetTrackingTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingSetupTab.isSelected()))
			return;
		
		scrollBar.setMin(imageBeginIndex);
		scrollBar.setMax(imageEndIndex);

		BufferedImage img = null;
		BufferedImage copy = null;
		try {
			img = ImageIO.read(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
			copy = ImageIO.read(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			
			Graphics2D g2d = img.createGraphics();
			double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
			if(!tallerThanWide){
				sizeRatio = runDICImageView.getFitWidth() / runDICImageView.getImage().getWidth();
			}
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
				ImageUInt8 binary = SPTargetTracker.threshold(targetImage, (int)target.getThreshold());
				//ThresholdImageOps.threshold(targetImage,binary,(int)target.getThreshold(),false);
				
				
				//GThresholdImageOps.threshold(targetImage, binary, GThresholdImageOps.computeOtsu(targetImage, 0, 256), true);
				
				BufferedImage bufferedTargetImage = VisualizeBinaryData.renderBinary(binary, false, null);//new BufferedImage(binary.getWidth(),binary.getHeight(),BufferedImage.BITMASK);
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
			img = ImageIO.read(new File(imagePaths.get(imageIndex).getPath()));
		} catch (IOException e) {
		}

		if(img != null) {
			
			Graphics2D g2d = img.createGraphics();
			double sizeRatio = runDICImageView.getFitHeight() / runDICImageView.getImage().getHeight();
			if(!tallerThanWide){
				sizeRatio = runDICImageView.getFitWidth() / runDICImageView.getImage().getWidth();
			}
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
		if(copyImages()) {
			File dicJobFile = new File(dicBundleDirectory + "/ncorr_job_file.txt");
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
				bw.write("Strain mode:	Lagrangian"+"\n");
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
				bw.write("Output	:	Image"+"\n"+"\n");

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
		for(Target target : targetsListView.getItems()){
			target.pts = SPTargetTracker.trackTargetUnknownAlgo(imagePaths, imageBeginIndex, imageEndIndex, target);
		}
		
	}

	private void runNCorr(File dicJobFile) {
		dicResultsImagePaths.clear();
		runDICResultsImageView.setVisible(false);
		dicProgressBar.setVisible(true);
		dicStatusLabel.setVisible(true);
		dicTabPane.getSelectionModel().select(2);
		Task<Void> task = new Task<Void>() {
			@Override 
			public Void call() {
				String[] cmd = { "libs/ncorr/ncorr_CommandLine.exe", "calculate", dicJobFile.getPath() };
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
		File dir = new File(dicBundleDirectory+"/video");
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
			File roi = new File(file.getPath()+"/roi.png");
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
	

}

