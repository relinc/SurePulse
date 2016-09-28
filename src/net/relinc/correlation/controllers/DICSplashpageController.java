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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
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
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.application.TrackingAlgorithm;
import net.relinc.correlation.staticClasses.CorrSettings;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.correlation.staticClasses.SPTargetTracker.TrackingAlgo;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.libraries.application.FitableDataset;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.imgdata.ImageSize;
import net.relinc.libraries.imgdata.ResizableImage;
import net.relinc.libraries.splibraries.DICProcessorIntegrator;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.ImageOps;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import org.imgscalr.*;
//import net.relinc.libraries.splibraries.Settings;
//import net.relinc.libraries.splibraries.Operations;
//import net.relinc.libraries.splibraries.Dialogs;
//import net.relinc.processor.staticClasses.Dialogs;
//import net.relinc.processor.staticClasses.SPOperations;
//import net.relinc.processor.staticClasses.SPSettings;
//
public class DICSplashpageController {
	@FXML ImageView runDICImageView;
	@FXML ImageView runDICResultsImageView;
	@FXML ImageView targetTrackingDrawTargetsImageView;
	@FXML ImageView selectedTargetImageView;
	@FXML ImageView targetTrackingResultsImageView;
	@FXML ImageView targetTrackingUnitToPixelImageView;
	@FXML ImageView targetTrackingBeginEndImageView;
	@FXML ScrollBar dicDrawROIscrollBar;
	@FXML Label imageNameLabel;
	@FXML Label resultsImageNameLabel;
	@FXML TabPane dicTabPane;
	@FXML ProgressBar dicProgressBar;
	@FXML Label dicStatusLabel;
	@FXML VBox dicStatusVBox;
	@FXML CheckBox drawTrailsCheckBox;
	@FXML CheckBox useSmoothedPointsCheckBox;

	@FXML ScrollBar dicResultsScrollBar;
	@FXML ListView<Target> targetsListView;
	@FXML Button deleteTargetButton;
	@FXML ScrollBar targetBinarizationScrollBar;
	@FXML ScrollBar targetTrackingUnitToPixelScrollBar;
	@FXML ScrollBar targetTrackingScrollBar;
	@FXML ScrollBar targetTrackingDrawTargetsScrollBar;
	@FXML ScrollBar targetTrackingResultsScrollBar;
	@FXML ChoiceBox<TrackingAlgorithm> trackingAlgorithmChoiceBox;
	@FXML HBox topLevelControlsHBox;
	
	@FXML Tab dicTab; //the big dic tab next to the target tracking tab
	@FXML Tab dicResultsTab;

	@FXML Tab imageSetupTab;
	@FXML TabPane targetTrackingTabPane;
	@FXML Tab targetTrackingTab;
	@FXML Tab targetTrackingDrawTargetsTab;
	@FXML Tab targetTrackingResultsTab;
	@FXML Tab targetTrackingUnitToPixelTab;
	@FXML Tab targetTrackingBeginEndTab;
	
	@FXML VBox targetTrackingExportToFIleVBox;

	@FXML Label labelImageName;
	@FXML Label imageNameLabelTargetTrackingTab;
	@FXML Label meterToPixelRatioLabel;
	@FXML ComboBox<ImageSize> imageSizeChooser;
	@FXML ComboBox<ImageSize> imageSizeChooserAdv;

	@FXML VBox vboxFunctions;
	HBox hBoxFunctions = new HBox();

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
	private double meterToPixelRatio = -1;
	private double collectionRate = -1;
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
	@FXML ComboBox<String> strainModeDrop;
	@FXML ScrollBar scrollBarHome;
	@FXML ImageView loadImagesImageView;
	public DICProcessorIntegrator dicProcessorIntegrator = new DICProcessorIntegrator();

	/**
	 * Sets up the GUI, adds action listeners, sets default settings and options
	 */
	@FXML
	public void initialize(){
		runDICResultsImageView.managedProperty().bind(runDICResultsImageView.visibleProperty());
		dicProgressBar.managedProperty().bind(dicProgressBar.visibleProperty());
		dicStatusLabel.managedProperty().bind(dicStatusLabel.visibleProperty());
		
		targetTrackingScrollBar.minProperty().bindBidirectional(dicDrawROIscrollBar.minProperty());
		targetTrackingScrollBar.maxProperty().bindBidirectional(dicDrawROIscrollBar.maxProperty());
		targetTrackingScrollBar.valueProperty().bindBidirectional(dicDrawROIscrollBar.valueProperty());
		
		targetTrackingDrawTargetsScrollBar.minProperty().bindBidirectional(dicDrawROIscrollBar.minProperty());
		targetTrackingDrawTargetsScrollBar.maxProperty().bindBidirectional(dicDrawROIscrollBar.maxProperty());
		targetTrackingDrawTargetsScrollBar.valueProperty().bindBidirectional(dicDrawROIscrollBar.valueProperty());

		targetTrackingResultsScrollBar.minProperty().bindBidirectional(dicDrawROIscrollBar.minProperty());
		targetTrackingResultsScrollBar.maxProperty().bindBidirectional(dicDrawROIscrollBar.maxProperty());
		targetTrackingResultsScrollBar.valueProperty().bindBidirectional(dicDrawROIscrollBar.valueProperty());
		
		targetTrackingUnitToPixelScrollBar.minProperty().bindBidirectional(dicDrawROIscrollBar.minProperty());
		targetTrackingUnitToPixelScrollBar.maxProperty().bindBidirectional(dicDrawROIscrollBar.maxProperty());
		targetTrackingUnitToPixelScrollBar.valueProperty().bindBidirectional(dicDrawROIscrollBar.valueProperty());
		
		imageSizeChooserAdv.itemsProperty().bind(imageSizeChooser.itemsProperty());
		imageSizeChooserAdv.selectionModelProperty().bind(imageSizeChooser.selectionModelProperty());

		dicDrawROIscrollBar.setUnitIncrement(1.0);
		dicDrawROIscrollBar.setBlockIncrement(1.0);
		
		targetTrackingExportToFIleVBox.setStyle("-fx-padding: 10;" + 
                "-fx-border-style: solid inside;" + 
                "-fx-border-width: 2;" +
                "-fx-border-insets: 0;" + 
                "-fx-border-radius: 5;" + 
                "-fx-border-color: grey;");

		dicDrawROIscrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderRunROITab();
				renderTargetTrackingTab();
			}


		});

		scrollBarHome.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderHomeImages();
			}


		});

		dicResultsScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderResultsImages();
			}
		});
		
		runDICImageView.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.getScene().setCursor(Cursor.CROSSHAIR);
			}
		});

		runDICImageView.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.getScene().setCursor(Cursor.DEFAULT);
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
		strainModeDrop.getItems().add("Lagrangian");
		strainModeDrop.getItems().add("Eulerian");
		

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
		strainModeDrop.getSelectionModel().select(0);
		units.getSelectionModel().select(0);
		outsubregion.getSelectionModel().select(0);

		targetTrackingDrawTargetsImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Target t = getSelectedTarget();
				double sizeRatio = targetTrackingDrawTargetsImageView.getFitHeight() / targetTrackingDrawTargetsImageView.getImage().getHeight();
				if(!tallerThanWide){
					sizeRatio = targetTrackingDrawTargetsImageView.getFitWidth() / targetTrackingDrawTargetsImageView.getImage().getWidth();
				}
				if(t != null)
				{
					t.center = new Point2D(event.getX(), event.getY());
					t.center = t.center.multiply(1 / sizeRatio);
					t.vertex = null;
					t.renderRectangle();
				}
				renderTargetTrackingTab();
			}
		});
		
		targetTrackingDrawTargetsImageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				Target t = getSelectedTarget();
				double sizeRatio = targetTrackingDrawTargetsImageView.getFitHeight() / targetTrackingDrawTargetsImageView.getImage().getHeight();
				if (!tallerThanWide) {
					sizeRatio = targetTrackingDrawTargetsImageView.getFitWidth() / targetTrackingDrawTargetsImageView.getImage().getWidth();
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

		targetTrackingDrawTargetsImageView.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(getSelectedTarget() != null)
					stage.getScene().setCursor(Cursor.CROSSHAIR);
			}
		});
		
		targetTrackingDrawTargetsImageView.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.getScene().setCursor(Cursor.DEFAULT);
			}
		});
		
		targetTrackingUnitToPixelImageView.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double sizeRatio = getSizeRatio(targetTrackingUnitToPixelImageView);
				//draw inch to pixel ratio.
				inchToPixelPoint1 = new Point2D(event.getX(), event.getY());
				inchToPixelPoint1 = inchToPixelPoint1.multiply(1 / sizeRatio);
				inchToPixelPoint2 = null;
			}
		});
		
		targetTrackingUnitToPixelImageView.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				double sizeRatio = getSizeRatio(targetTrackingUnitToPixelImageView);
				inchToPixelPoint2 = new Point2D(event.getX(), event.getY());
				inchToPixelPoint2 = inchToPixelPoint2.multiply(1 / sizeRatio);
				renderTargetTrackingTab();
			}
		});
		
		targetTrackingUnitToPixelImageView.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				meterToPixelRatio = getMetersFromUser("Please enter the distance drawn and select the units.") / inchToPixelPoint1.distance(inchToPixelPoint2);
				meterToPixelRatioLabel.setText("Meter-to-pixel ratio: " + SPOperations.round(meterToPixelRatio, 5));
			}
		});
		
		targetTrackingUnitToPixelImageView.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.getScene().setCursor(Cursor.CROSSHAIR);
			}
		});
		
		targetTrackingUnitToPixelImageView.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				stage.getScene().setCursor(Cursor.DEFAULT);
			}
		});
		
		dicTab.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderRunROITab();
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

		targetTrackingTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
			}
		});
		
		targetTrackingDrawTargetsTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
			}
		});

		targetTrackingResultsTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
			}
		});
		
		targetTrackingBeginEndTab.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderTargetTrackingTab();
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
				renderTargetTrackingTab();
			}
		});

		for(TrackingAlgo al : TrackingAlgo.values())
			trackingAlgorithmChoiceBox.getItems().add(new TrackingAlgorithm(al));
		trackingAlgorithmChoiceBox.getSelectionModel().select(0);


		targetBinarizationScrollBar.setMin(0.0);
		targetBinarizationScrollBar.setMax(255.0);

		/*removeSelectedImages = new Button("Remove Selected Images");
		useSelectedImages = new Button("Use Selected Images");

		hBoxFunctions.setAlignment(Pos.CENTER);
		hBoxFunctions.getChildren().add(removeSelectedImages);
		hBoxFunctions.getChildren().add(useSelectedImages);
		hBoxFunctions.setSpacing(10.0);

		removeSelectedImages.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(selectedImagePaths.size() == 0) {
					return;
				} else {
					ArrayList<File> newImagePaths = new ArrayList<>();
					for(File selectedPath : selectedImagePaths) {
						for(File currentPath : imagePaths) {
							if(!selectedPath.getPath().equals(currentPath.getPath()))
								newImagePaths.add(currentPath);
						}
					}
					imagePaths = newImagePaths;
				}

				resetEverything();
				renderRunROITab();
				renderTargetTrackingTab();
				renderTargetTrackingResultsTab();
				renderImageGallery();
			}
		});

		useSelectedImages.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(selectedImagePaths.size() == 0) {
					return;
				} else {
					ArrayList<File> newImagePaths = new ArrayList<>();
					for(File selectedPath : selectedImagePaths) {
						for(File currentPath : imagePaths) {
							if(selectedPath.getPath().equals(currentPath.getPath()))
								newImagePaths.add(selectedPath);
						}
					}
					imagePaths = newImagePaths;
				}

				resetEverything();
				renderRunROITab();
				renderTargetTrackingTab();
				renderTargetTrackingResultsTab();
				renderImageGallery();
			}
		}); */
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
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("All Images", "*.*"),
				new FileChooser.ExtensionFilter("JPG", "*.jpg"),
				new FileChooser.ExtensionFilter("JPEG", "*.jpeg"),
				new FileChooser.ExtensionFilter("PNG", "*.png"),
				new FileChooser.ExtensionFilter("MP4", "*.mp4"),
				new FileChooser.ExtensionFilter("avi", "*.avi"),
				new FileChooser.ExtensionFilter("flv", "*.flv"),
				new FileChooser.ExtensionFilter("wmv", "*.mmv")
				);
		List<File> imageFiles = fileChooser.showOpenMultipleDialog(stage);
		if(imageFiles == null)
			return;
		
		meterToPixelRatio = -1;
		collectionRate = -1;
		imagePaths = null;
		
		if(imageFiles.size() == 1){
			double fr = Dialogs.getDoubleValueFromUser("You have selected a video file. Please enter the frame rate:", "frames/second");
			File videoFile = imageFiles.get(0);
			//use ffmpeg to rip to temp folder.
			File tempImagesForProcessing = new File(SPSettings.applicationSupportDirectory + "/" + "RELFX/SURE-DIC" + "/tempImagesForProcessing");
			SPOperations.deleteFolder(tempImagesForProcessing);
			tempImagesForProcessing.mkdirs();
			
			ImageOps.exportVideoToImages(videoFile.toString(), tempImagesForProcessing.getPath(), fr);
			imagePaths = Arrays.asList(tempImagesForProcessing.listFiles());
			collectionRate = fr;
		}
		else{
			imagePaths = imageFiles;
			//immediately ask for frame rate.
			collectionRate = Dialogs.getDoubleValueFromUser("Please Enter the Frame Rate:", "frames/second");
		}

		//		Stage anotherStage = new Stage();
		//		Label label = new Label("Please Enter the Frame Rate:");
		//		TextField tf = new TextField("frames/second");
		//		Button button = new Button("Done");
		//		button.setOnAction(new EventHandler<ActionEvent>() {
		//			@Override
		//			public void handle(ActionEvent event) {
		//				anotherStage.close();
		//			}
		//		});
		//		VBox box = new VBox();
		//		box.getChildren().add(label);
		//		box.getChildren().add(tf);
		//		box.getChildren().add(button);
		//		box.setSpacing(15);
		//		box.setAlignment(Pos.CENTER);
		//		box.setPadding(new Insets(10.0));
		//		AnchorPane anchor = new AnchorPane();
		//		AnchorPane.setBottomAnchor(box, 0.0);
		//		AnchorPane.setTopAnchor(box, 0.0);
		//		AnchorPane.setLeftAnchor(box, 0.0);
		//		AnchorPane.setRightAnchor(box, 0.0);
		//		anchor.getChildren().add(box);
		//		Scene scene = new Scene(anchor, 350, 200);
		//		
		//		anotherStage.setScene(scene);
		//		anotherStage.showAndWait();
		//		collectionRate = Double.parseDouble(tf.getText().replaceAll(",", ""));
		//		
		resetEverything();
		renderRunROITab();
		renderTargetTrackingTab();
		renderHomeImages();
	}

	public void advancedSettingsButtonClicked() {
		dicTabPane.getSelectionModel().select(1);
	}

	private void renderHomeImages() {
		scrollBarHome.setMin(imageBeginIndex);
		scrollBarHome.setMax(imageEndIndex);

		BufferedImage img = null;
		try {
			img = SPOperations.getRgbaImage(new File(imagePaths.get((int)scrollBarHome.getValue()).getPath()));
		} catch (IOException e) {
			e.printStackTrace();
		}


		if(img != null) {
			loadImagesImageView.setImage(SwingFXUtils.toFXImage(img,null));
			labelImageName.setText(imagePaths.get((int)scrollBarHome.getValue()).getName());
		}

		resizeImageViewToFit(loadImagesImageView);
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
			//primaryStage.getIcons().add(SPSettings.getRELLogo());
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

	public void deleteTargetButtonFired(){
		Target t = getSelectedTarget();
		if(t != null)
			targetsListView.getItems().remove(t);
		renderTargetTrackingTab();
	}

	@FXML
	private void exportTargetTrackingDisplacementToProcessorFired(){
		launchDisplacementExportWizard(true);
	}

	public void exportResultsButtonFired() {
		double[] strain = readE1Results();
		exportStrainAndLaunchProcessor(strain, true);
	}

	public void launchDisplacementExportWizard(boolean exportToProcessor) {
		// luanch the export wizard.
		Stage primaryStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(
					getClass().getResource("/net/relinc/correlation/fxml/ExportDisplacement.fxml"));
			Scene scene = new Scene(root1.load());

			primaryStage.setTitle("SURE-Pulse Image Correlation");
			primaryStage.setScene(scene);
			ExportDisplacementController c = root1.<ExportDisplacementController> getController();

			c.inchToPixelRatio = meterToPixelRatio;
			c.useSmoothedPoints = useSmoothedPointsCheckBox.isSelected();
			c.beginIndex = imageBeginIndex;
			c.imagePaths = imagePaths;
			c.fillTargetsListView(targetsListView.getItems());
			c.renderChart();
			c.stage = primaryStage;
			c.exportToProcessor = exportToProcessor;
			List<Double> displacement = new ArrayList<Double>();
			c.displacement = displacement;
			primaryStage.showAndWait();
			if(exportToProcessor && displacement != null && displacement.size() != 0){
				//export and close
				dicProcessorIntegrator.targetTrackingDisplacement = displacement;
				exportStrainAndLaunchProcessor(null, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

//		double[] s = getStrainFromImageData(false);
//		// double[] s = getTargetTrackingDisplacement()
//		exportStrainAndLaunchProcessor(s, false);
	}
	
	public double[] readE1Results() {
		String s = SPOperations.readStringFromFile(SPSettings.imageProcResulstsDir + "/data/e1.txt");
		String[] sArray = s.split("\n");
		double[] strain = new double[sArray.length];
		System.out.println(sArray);
		try {
			for(int i = 0; i < sArray.length; i++) {
				strain[i] = Double.parseDouble(sArray[i]);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return strain;
	}

	public double[] getStrainFromImageData(boolean dic) {
		if(dic) {
			return null;
		} else {
			return null;// SPTargetTracker.calculateDisplacement(targetsListView.getItems().get(0), targetsListView.getItems().get(1), inchToPixelRatio);
			//return SPTargetTracker.calculateTrueStrain(targetsListView.getItems().get(0), targetsListView.getItems().get(1), inchToPixelRatio, false, lengthOfSample);
		}
	}

	public void exportStrainAndLaunchProcessor(double[] s, boolean dic) {
		if(!dic) {
			//dicProcessorIntegrator.targetTrackingTrueStrain = s;
		} else {
			dicProcessorIntegrator.dicLagrangianStrain = s;
		}
		dicProcessorIntegrator.collectionRate = collectionRate;

		//save video sequence
		File tempDir = new File(SPSettings.applicationSupportDirectory + "/" + CorrSettings.appDataName + "/" + "ImagesForSample");
		if(tempDir.exists()) {
			SPOperations.deleteFolder(tempDir);
			System.out.println("Deleting: " + tempDir.getPath());
		}
		tempDir.mkdirs();
		if(tempDir.exists()) {
			System.out.println("Exists!");
		}
		if(!dic) {
			writeTargetTrackingImagesToFolder(tempDir);
		} else {
			System.out.println("Copy DIC");
			copyDicOutputImagesToFolder(tempDir);
		}

		dicProcessorIntegrator.imagesLocation = tempDir;

		Stage stage = (Stage) targetsListView.getScene().getWindow();
		stage.close();
	}

	private void copyDicOutputImagesToFolder(File folderLocation) {
		File[] listOfFiles = new File(SPSettings.imageProcResulstsDir + "/video").listFiles();
		for(int i = 0; i < listOfFiles.length; i++) {
			try {
				String imName = Integer.toString(i);
				while(imName.length() < 4)
					imName = "0" + imName;
				System.out.println(SPOperations.getExtension(listOfFiles[i].getPath()));
				File outputfile = new File(folderLocation.getPath() + "/" + imName + SPOperations.getExtension(listOfFiles[i].getPath())); //don't change this format, ffmpeg uses it for video making
				Files.copy(listOfFiles[i].toPath(), outputfile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void writeTargetTrackingImagesToFolder(File folderLocation){
		for(int idx = imageBeginIndex; idx <= imageEndIndex; idx++){
			BufferedImage img = null;
			try {
				img = SPOperations.getRgbaImage(new File(imagePaths.get(idx).getPath()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Graphics2D g2d = img.createGraphics();
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			for(Target targ : targetsListView.getItems())
			{
				g2d.setColor(Color.decode(targ.getColor()));
				if(targ.rectangle == null || targ.pts == null)
					continue;

				if(drawTrailsCheckBox.isSelected()){
					//					System.out.println("imageBeginIndex: " + imageBeginIndex);
					//					System.out.println("ImageIndex: " + imageIndex);
					for(int i = 0; i <= idx - imageBeginIndex; i++){
						Ellipse2D circ = new Ellipse2D.Double(targ.pts[i].getX(), targ.pts[i].getY(), 10, 10);
						g2d.draw(circ);
					}
				}
				else{
					//System.out.println("Drawing Index: " + (imageIndex - imageBeginIndex));
					Ellipse2D circ = new Ellipse2D.Double(targ.pts[idx - imageBeginIndex].getX(), targ.pts[idx - imageBeginIndex].getY(), 10, 10);
					g2d.draw(circ);
				}
			}

			String imName = Integer.toString(idx - imageBeginIndex);
			while(imName.length() < 4)
				imName = "0" + imName;
			File outputfile = new File(folderLocation.getPath() + "/" + imName + ".png"); //don't change this format, ffmpeg uses it for video making
			try {
				ImageIO.write(img, "jpg", outputfile); //jpg is much faster than png. https://blog.idrsolutions.com/2014/10/imageio-write-executorservice-io-bound-applications-java/
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
		imageBeginIndex = (int)dicDrawROIscrollBar.getValue();
		renderRunROITab();
		renderTargetTrackingTab();
	}

	/**
	 * Set End button click listener, sets the last image in the list to run DIC on
	 */
	public void setEndFired(){
		imageEndIndex = (int)dicDrawROIscrollBar.getValue();
		renderRunROITab();
		renderTargetTrackingTab();
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
			String csv = "Inch To Pixel Ratio:," + meterToPixelRatio + "\n\n";
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

			c.inchToPixelRatio = meterToPixelRatio;
			c.lengthOfSample = getMetersFromUser("Please enter the sample length");
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
		
		launchDisplacementExportWizard(false);
		
//		FileChooser fileChooser = new FileChooser();
//		fileChooser.setTitle("Save Displacement CSV");
//		File file = fileChooser.showSaveDialog(stage);
//		if (file != null) {
//			String csv = "Image";
//			for(Target target : targetsListView.getItems()){
//				csv += "," + target.getName() + " Displacement";
//			}
//			csv += "\n";
//			targetsListView.getItems().stream().forEach(t -> t.displacement = SPTargetTracker.calculateDisplacement(t, inchToPixelRatio, useSmoothedPointsCheckBox.isSelected()));
//			Target target1 = targetsListView.getItems().get(0);
//			for(int i = 0; i < target1.pts.length; i++){
//				csv += imagePaths.get(i + imageBeginIndex).getName();
//				for(Target target : targetsListView.getItems()){
//					csv += "," + target.displacement[i];
//				}
//				csv += "\n";
//			}
//			Operations.writeStringToFile(csv, file.getPath() + ".csv");
//		}
	}

	public void exportSpeedButtonFired(){
		
//		TextInputDialog dialog = new TextInputDialog("1");
//		dialog.setTitle("");
//		dialog.setHeaderText("Need Frames per second for speed");
//		dialog.setContentText("Please enter FPS:");

		// Traditional way to get the response value.
		//Optional<String> result = dialog.showAndWait();
		if (collectionRate != -1){
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Save Speed CSV");
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				String csv = "Image";
				for(Target target : targetsListView.getItems()){
					csv += "," + target.getName() + " Speed";
				}
				csv += "\n";
				targetsListView.getItems().stream().forEach(t -> t.speed = SPTargetTracker.calculateSpeed(t, meterToPixelRatio, useSmoothedPointsCheckBox.isSelected(), collectionRate));
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
		double length = Dialogs.getDoubleValueFromUser("Please Enter the desired video length (seconds):", "seconds");
		
		FileChooser choose = new FileChooser();
		choose.setInitialDirectory(imagePaths.get(0).getParentFile());
		File videoExport = choose.showSaveDialog(stage);
		if(videoExport == null)
			return;

		File tempDir = new File(SPSettings.applicationSupportDirectory + "/" + CorrSettings.appDataName + "/" + "TempExportImages");
		if(tempDir.exists())
			SPOperations.deleteFolder(tempDir);
		tempDir.mkdirs();

		for(int idx = imageBeginIndex; idx <= imageEndIndex; idx++){
			BufferedImage img = null;
			try {
				img = SPOperations.getRgbaImage(new File(imagePaths.get(idx).getPath()));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Graphics2D g2d = img.createGraphics();
			g2d.setStroke(new BasicStroke(Math.max(img.getHeight() / 200 + 1, img.getWidth()/200 + 1)));
			for(Target targ : targetsListView.getItems())
			{
				g2d.setColor(Color.decode(targ.getColor()));
				if(targ.rectangle == null || targ.pts == null)
					continue;

				if(drawTrailsCheckBox.isSelected()){
					//					System.out.println("imageBeginIndex: " + imageBeginIndex);
					//					System.out.println("ImageIndex: " + imageIndex);
					for(int i = 0; i <= idx - imageBeginIndex; i++){
						Ellipse2D circ = new Ellipse2D.Double(targ.pts[i].getX(), targ.pts[i].getY(), 10, 10);
						g2d.draw(circ);
					}
				}
				else{
					//System.out.println("Drawing Index: " + (imageIndex - imageBeginIndex));
					Ellipse2D circ = new Ellipse2D.Double(targ.pts[idx - imageBeginIndex].getX(), targ.pts[idx - imageBeginIndex].getY(), 10, 10);
					g2d.draw(circ);
				}
			}
			img = ImageOps.getImageWithEvenHeightAndWidth(img);
			String imName = Integer.toString(idx - imageBeginIndex);
			while(imName.length() < 4)
				imName = "0" + imName;
			File outputfile = new File(tempDir.getPath() + "/" + imName + ".png");
			try {
				ImageIO.write(img, "png", outputfile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
    	String videoExportString = videoExport.getPath() + ".mp4";
    	String imagesString = tempDir.getPath() + "/" + "%04d.png";
    	
    	double fr = (imageEndIndex - imageBeginIndex + 1) / length;
		
		ImageOps.exportImagesToVideo(imagesString, videoExportString, fr);
		
		
//		if(true)
//		return;
//		//ffmpeg -i %04d.png -pix_fmt yuv420p video.mp4
//		Label targetLabel = new Label();
//		Button cancelButton = new Button("Cancel");
//		ProgressBar progressBar = new ProgressBar();
//		
//		Task<Integer> task = new Task<Integer>() {
//		    @Override protected Integer call() throws Exception {
//		    		Platform.runLater(new Runnable() {
//			            @Override
//			            public void run() {
//			            	
//			            }
//			          });
//		    		return 1;
//		    }
//		};
		
//		task.setOnCancelled(new EventHandler<WorkerStateEvent>() {
//			@Override
//			public void handle(WorkerStateEvent event) {
//				SPTargetTracker.cancelled = true;
//			}
//		});
//		
//		task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
//			@Override
//			public void handle(WorkerStateEvent event) {
//			}
//		});
//		
//		cancelButton.setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent event) {
//				task.cancel();
//			}
//		});
//		new Thread(task).start();
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

		dicDrawROIscrollBar.setValue(0);
		scrollBarHome.setValue(0);
		labelImageName.setText("No Images Loaded..");
		vboxFunctions.getChildren().remove(hBoxFunctions);
	}

	/**
	 * Button click listener for reset begin, resets start image index
	 */
	public void resetBeginIndexFired() {
		imageBeginIndex = 0;
		dicDrawROIscrollBar.setMin(0);
		dicDrawROIscrollBar.setValue(0);
	}

	/**
	 * Button click listener for reset end, resets end image index
	 */
	public void resetEndIndexFired() {
		imageEndIndex = imagePaths.size() - 1;
		dicDrawROIscrollBar.setMax(imageEndIndex);
		dicDrawROIscrollBar.setValue(imageEndIndex);
	}

	private void renderRunROITab() {
		
		if(!(dicTab.isSelected()))
			return;
		
		dicDrawROIscrollBar.setMin(imageBeginIndex);
		dicDrawROIscrollBar.setMax(imageEndIndex);
		

		ResizableImage resizableImage = new ResizableImage(new File(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getPath()));
		imageSizeChooser.getItems().clear();
		imageSizeChooser.getItems().addAll(resizableImage.getAvailableImageSizes());
		
		if(imageSizeChooser.getItems().size() > 1)
			imageSizeChooser.getSelectionModel().select(imageSizeChooser.getItems().size() - 2);
		else if(imageSizeChooser.getItems().size() == 1)
			imageSizeChooser.getSelectionModel().select(0);
		
		BufferedImage img = resizableImage.getOriginalImage();

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
			imageNameLabel.setText(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getName());
		}

		resizeImageViewToFit(runDICImageView);
	}

	private void renderTargetTrackingTab(){
		if(!(targetTrackingTab.isSelected()))
			return;
		
		dicDrawROIscrollBar.setMin(imageBeginIndex);
		dicDrawROIscrollBar.setMax(imageEndIndex);

		//all of these only render if they are visible
		renderDrawUnitsToPixelRatioTab();
		renderTargetTrackingChooseBeginEndTab();
		renderTargetTrackingDrawTargetsTab();
		renderTargetTrackingResultsTab();
		
		
	}

	private void renderDrawUnitsToPixelRatioTab(){
		if(!targetTrackingUnitToPixelTab.isSelected())
			return;
		BufferedImage img = null;
		BufferedImage watermarkImage = null;
		try {
			img = SPOperations.getRgbaImage(new File(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getPath()));
			watermarkImage = ImageIO.read(ImageOps.class.getResourceAsStream("/net/relinc/libraries/images/SURE-Pulse_IC_Logo.png"));
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

			if(inchToPixelPoint1 != null && inchToPixelPoint2 != null){
				g2d.setColor(Color.decode("#FF0000"));
				g2d.drawLine((int)inchToPixelPoint1.getX(), (int)inchToPixelPoint1.getY(), (int)inchToPixelPoint2.getX(), (int)inchToPixelPoint2.getY());
			}

			g2d.dispose();
			
			try {
				img = ImageOps.watermark(img, watermarkImage, ImageOps.PlacementPosition.BOTTOMRIGHT, 35); //here's your slowness.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetTrackingUnitToPixelImageView.setImage(SwingFXUtils.toFXImage(img,null));

		}
		resizeImageViewToFit(targetTrackingUnitToPixelImageView);
	}
	
	private void renderTargetTrackingChooseBeginEndTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingBeginEndTab.isSelected()))
			return;
		BufferedImage img = null;
		BufferedImage watermarkImage = null;
		try {
			img = SPOperations.getRgbaImage(new File(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getPath()));
			watermarkImage = ImageIO.read(ImageOps.class.getResourceAsStream("/net/relinc/libraries/images/SURE-Pulse_IC_Logo.png"));
		} catch (IOException e) {
		}

		if(img != null) {

			try {
				img = ImageOps.watermark(img, watermarkImage, ImageOps.PlacementPosition.BOTTOMRIGHT, 35); //here's your slowness.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetTrackingBeginEndImageView.setImage(SwingFXUtils.toFXImage(img,null));
			//imageNameLabelTargetTrackingTab.setText(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getName());
			
		}
		resizeImageViewToFit(targetTrackingBeginEndImageView);
	}
	
	private void renderTargetTrackingDrawTargetsTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingDrawTargetsTab.isSelected()))
			return; 
		BufferedImage img = null;
		BufferedImage copy = null;
		BufferedImage watermarkImage = null;
		try {
			img = SPOperations.getRgbaImage(new File(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getPath()));
			copy = SPOperations.getRgbaImage(new File(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getPath()));
			watermarkImage = ImageIO.read(ImageOps.class.getResourceAsStream("/net/relinc/libraries/images/SURE-Pulse_IC_Logo.png"));
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

			g2d.dispose();
			
			try {
				img = ImageOps.watermark(img, watermarkImage, ImageOps.PlacementPosition.BOTTOMRIGHT, 35); //here's your slowness.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetTrackingDrawTargetsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabelTargetTrackingTab.setText(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getName());
			
			//drawing smaller image of target off to the right
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
		resizeImageViewToFit(targetTrackingDrawTargetsImageView);
	}
	
	private void renderTargetTrackingResultsTab(){
		if(!(targetTrackingTab.isSelected() && targetTrackingResultsTab.isSelected()))
			return;
		dicDrawROIscrollBar.setMin(imageBeginIndex);
		dicDrawROIscrollBar.setMax(imageEndIndex);
		int imageIndex = (int)dicDrawROIscrollBar.getValue();

		BufferedImage img = null;
		try {
			img = SPOperations.getRgbaImage(new File(imagePaths.get(imageIndex).getPath()));
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
					//					System.out.println("imageBeginIndex: " + imageBeginIndex);
					//					System.out.println("ImageIndex: " + imageIndex);
					for(int i = 0; i <= imageIndex - imageBeginIndex; i++){
						if(useSmoothedPointsCheckBox.isSelected()){
							Ellipse2D circ = new Ellipse2D.Double(targ.getSmoothedPoints()[i].getX(), targ.getSmoothedPoints()[i].getY(), 10, 10);
							g2d.draw(circ);
						}
						else{
							Ellipse2D circ = new Ellipse2D.Double(targ.pts[i].getX(), targ.pts[i].getY(), 10, 10);
							g2d.draw(circ);
						}
						
					}
				}
				else{
					//System.out.println("Drawing Index: " + (imageIndex - imageBeginIndex));
					if(useSmoothedPointsCheckBox.isSelected()){
						Ellipse2D circ = new Ellipse2D.Double(targ.getSmoothedPoints()[imageIndex - imageBeginIndex].getX(), targ.getSmoothedPoints()[imageIndex - imageBeginIndex].getY(), 10, 10);
						g2d.draw(circ);
					}
					else{
						Ellipse2D circ = new Ellipse2D.Double(targ.pts[imageIndex - imageBeginIndex].getX(), targ.pts[imageIndex - imageBeginIndex].getY(), 10, 10);
						g2d.draw(circ);
					}
					
				}


			}

			g2d.dispose();
			targetTrackingResultsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabelTargetTrackingTab.setText(imagePaths.get((int)dicDrawROIscrollBar.getValue()).getName());

		}
		resizeImageViewToFit(targetTrackingResultsImageView);
	}

	private void renderResultsImages() {
		
		dicResultsScrollBar.setMin(0);
		dicResultsScrollBar.setMax(dicResultsImagePaths.size() - 1);

		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(dicResultsImagePaths.get((int)dicResultsScrollBar.getValue()).getPath()));
		} catch (IOException e) {

		}

		if(img != null) {
			runDICResultsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			resultsImageNameLabel.setText(dicResultsImagePaths.get((int)dicResultsScrollBar.getValue()).getName());
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
		
		dicResultsImagePaths.clear();
		runDICResultsImageView.setVisible(false);
		dicProgressBar.setVisible(true);
		dicStatusLabel.setVisible(true);
		dicTabPane.getSelectionModel().select(2);
		
		File results = new File(SPSettings.imageProcResulstsDir);
		if(results.exists() && results.isDirectory()) {
			SPOperations.deleteFolder(results);
		}
		results.mkdirs();
		
		if(copyAndResizeImages(imageSizeChooser.getSelectionModel().getSelectedItem().size)) {
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
				bw.write("Strain mode:	"+strainModeDrop.getSelectionModel().getSelectedItem()+"\n");
				bw.write("CSV out:	"+csvout.getSelectionModel().getSelectedItem()+"\n");
				bw.write("Video/Img out:	"+videoimgout.getSelectionModel().getSelectedItem()+"\n");
				bw.write("units:	"+units.getSelectionModel().getSelectedItem()+"\n");
				bw.write("units per px:	.01"+"\n");
				bw.write("fps:	15"+"\n");
				bw.write("OpenCv color:	COLORMAP_JET"+"\n");
				bw.write("end delay:	2"+"\n");
				bw.write("fourcc:	M,J,P,G"+"\n");
				bw.write("colorbar:	true"+"\n");
				bw.write("axes:	false"+"\n");
				bw.write("scalebar:	false"+"\n");
				bw.write("num units:	-1"+"\n");
				bw.write("font size:	1"+"\n");
				bw.write("tick marks:	11"+"\n");
				bw.write("strain min:	0"+"\n");
				bw.write("strain max:	-1"+"\n");
				bw.write("disp min:	0"+"\n");
				bw.write("disp max:	2"+"\n");
				bw.write("strain radius:	"+strainradius.getText()+"\n");
				bw.write("Subregion:	"+outsubregion.getSelectionModel().getSelectedItem()+"\n"); //use
				bw.write("Output:	image"+"\n");
				bw.write("Output Dir:	"+SPSettings.imageProcResulstsDir+"/\n\n");

				bw.write("\u20ACResults:"+"\n");
				bw.write("DIC input:	none\n");
				bw.write("DIC output:	none\n");
				bw.write("strain input:	none\n");
				bw.write("strain output:	none\n");
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
					t.xPts = null; //resets fitable dataset data.
					t.yPts = null;
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
		String NcorrLocation = SPSettings.currentOS.contains("Win") ? "libs/ncorr_CommandLine.exe" 
				: "/Applications/SURE-Pulse.app/ncorr/ncorr_FullCmdLineTool";


		Task<Void> task = new Task<Void>() {
			
			@Override 
			public Void call() throws InterruptedException {
				String[] cmd = { NcorrLocation, "calculate", dicJobFile.getPath() };
				ProcessBuilder pb = new ProcessBuilder(cmd);
				pb.redirectError(Redirect.INHERIT);
				System.out.println("Gets Called 0");
				try {
					System.out.println("Gets Called 1");
					Process process = pb.start();
					BufferedReader reader = 
							new BufferedReader(new InputStreamReader(process.getInputStream()));
					String line = null;
					int i = 1;
					while ( (line = reader.readLine()) != null) {
						final String message = line;
						System.out.println(message);
						if(message.contains("Processing displacement field")) {
							i++;
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									String output = message.replace("Processing displacement field ","");
									dicStatusLabel.setText("DIC Status: Processing Image " + output);
								}
							}); 
							updateProgress(i, dicImageRunPaths.size() + 1);
						} else if(message.contains("Displacement field")) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									String output = message.replace("Displacement field ","");
									dicStatusLabel.setText("DIC Status: Changing perspective  " + output);
								}
							}); 
							updateProgress(i, dicImageRunPaths.size() + 1);
						} 
					}
					process.waitFor();

				} catch (IOException e) {
					dicStatusLabel.setText("DIC Failed!");
					e.printStackTrace();
					return null;
				}
				
				updateProgress(dicImageRunPaths.size() + 1, dicImageRunPaths.size() + 1);

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

	private Boolean copyAndResizeImages(int newImageSize) {

		if(imagePaths == null || imagePaths.size() == 0) {
			Dialogs.showInformationDialog("Run DIC", null, "Please Load Image Files", stage);
			return false;
		}

		dicBundleDirectory = imagePaths.get(0).getParentFile().getPath();
		//System.out.println(dicBundleDirectory);
		File file = new File(dicBundleDirectory);

		if(!file.exists())
			file.mkdir();

		dicImageRunPaths.clear();

		//System.out.println(imageBeginIndex + " " + imageEndIndex);
		for(int i = imageBeginIndex; i <= imageEndIndex; i++) {
			ResizableImage resizableImage = new ResizableImage(imagePaths.get(i));
			dicImageRunPaths.add(ResizableImage.resizeImage(resizableImage, newImageSize, SPSettings.imageProcResulstsDir + "/" + imagePaths.get(i).getName()).getPath());
		}

		BufferedImage roiImage = new BufferedImage((int)runDICImageView.getImage().getWidth(), (int)runDICImageView.getImage().getHeight(), BufferedImage.TYPE_BYTE_GRAY);
		Graphics2D g2d = roiImage.createGraphics();
		g2d.setBackground(Color.BLACK);
		g2d.setColor(Color.white);
		g2d.fill(currentSelectedRectangle);
		g2d.draw(currentSelectedRectangle);
		
		//TODO: THERE IS A BUG WHEN RUNNING THE SAME ROI TWICE, NULL POINTER
		ResizableImage resizableImage = new ResizableImage(roiImage);
		File roi = ResizableImage.resizeImage(resizableImage, newImageSize, SPSettings.imageProcResulstsDir+"/roi.png");
		roiImagePath = roi.getPath();

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
	
	private double getSizeRatio(ImageView view){
		double sizeRatio = view.getFitHeight() / view.getImage().getHeight();
		if (!tallerThanWide) {
			sizeRatio = view.getFitWidth() / view.getImage().getWidth();
		}
		return sizeRatio;
	}
	
	private double getMetersFromUser(String prompt){
		Stage anotherStage = new Stage();
		Label promptLabel = new Label(prompt);
		Label equalsLabel = new Label("= 0 meters");
		Label userInputLabel = new Label();
		NumberTextField userInputTF = new NumberTextField("", "");
		userInputLabel.textProperty().bind(userInputTF.textProperty());
		NumberTextField multiplierTF = new NumberTextField("", "");
		
		ChangeListener<String> listener = new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				equalsLabel.setText("");
				if(userInputTF.getDouble() != -1 && multiplierTF.getDouble() != -1)
					equalsLabel.setText("= " + SPOperations.round(userInputTF.getDouble() * multiplierTF.getDouble(), 7) + " meters");
			}
		};
		
		multiplierTF.textProperty().addListener(listener);
		userInputTF.textProperty().addListener(listener);
		
		multiplierTF.setText("1");
		Button button = new Button("Done");
		button.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				anotherStage.close();
			}
		});
		RadioButton inchRadio = new RadioButton("in");
		inchRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setNumberText(".0254");
				multiplierTF.setDisable(true);
			}
		});
		RadioButton mmRadio = new RadioButton("mm");
		mmRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setNumberText(".001");
				multiplierTF.setDisable(true);
			}
		});
		RadioButton mRadio = new RadioButton("m");
		mRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setNumberText("1");
				multiplierTF.setDisable(true);
			}
		});
		RadioButton customRadio = new RadioButton("custom");
		customRadio.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				multiplierTF.setNumberText("1");
				multiplierTF.setDisable(false);
			}
		});
		ToggleGroup group = new ToggleGroup();
		inchRadio.setToggleGroup(group);
		mmRadio.setToggleGroup(group);
		mRadio.setToggleGroup(group);
		customRadio.setToggleGroup(group);
		
		Button doneButton = new Button("Done");
		doneButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				anotherStage.close();
			}
		});
		
		AnchorPane anchor = new AnchorPane();
		VBox topVbox = new VBox();
		topVbox.getChildren().add(promptLabel);
		HBox inputHBox = new HBox();
		inputHBox.getChildren().add(userInputTF);
		inputHBox.getChildren().add(inchRadio);
		inputHBox.getChildren().add(mmRadio);
		inputHBox.getChildren().add(mRadio);
		inputHBox.getChildren().add(customRadio);
		inputHBox.setAlignment(Pos.CENTER);
		inputHBox.setSpacing(5);
		topVbox.getChildren().add(inputHBox);
		HBox equalsHBox = new HBox();
		equalsHBox.getChildren().add(userInputLabel);
		equalsHBox.getChildren().add(new Label("x"));
		equalsHBox.getChildren().add(multiplierTF);
		equalsHBox.getChildren().add(equalsLabel);
		equalsHBox.setAlignment(Pos.CENTER);
		equalsHBox.setSpacing(5);
		topVbox.getChildren().add(equalsHBox);
		topVbox.getChildren().add(doneButton);
		topVbox.setAlignment(Pos.CENTER);
		topVbox.setSpacing(10);
		AnchorPane.setBottomAnchor(topVbox, 0.0);
		AnchorPane.setLeftAnchor(topVbox, 0.0);
		AnchorPane.setRightAnchor(topVbox, 0.0);
		AnchorPane.setTopAnchor(topVbox, 0.0);
		
		anchor.getChildren().add(topVbox);
		
		
		Scene scene = new Scene(anchor, 400, 220);
		anotherStage.setScene(scene);
		anotherStage.initModality(Modality.WINDOW_MODAL);
		
		anotherStage.showAndWait();
		
		return userInputTF.getDouble() * multiplierTF.getDouble();
	}

}

