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
import net.relinc.correlation.application.Target;
import net.relinc.correlation.application.TrackingAlgorithm;
import net.relinc.correlation.staticClasses.CorrSettings;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.correlation.staticClasses.SPTargetTracker.TrackingAlgo;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.libraries.application.FitableDataset;
import net.relinc.libraries.splibraries.DICProcessorIntegrator;
import net.relinc.libraries.splibraries.Dialogs;
import net.relinc.libraries.splibraries.Operations;
import net.relinc.libraries.splibraries.Settings;
import net.relinc.libraries.staticClasses.ImageOps;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
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

	@FXML Label labelImageName;
	@FXML Label imageNameLabelTargetTrackingTab;

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
	private double inchToPixelRatio = -1;
	private double lengthOfSample = -1;
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

		scrollBarHome.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderHomeImages();
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
					inchToPixelRatio = Dialogs.getDoubleValueFromUser("Please Enter the Distance Drawn", "") / inchToPixelPoint1.distance(inchToPixelPoint2);
					//					Stage anotherStage = new Stage();
					//					Label label = new Label("Please Enter the Distance Drawn");
					//					NumberTextField tf = new NumberTextField("", "", true);
					//					Button button = new Button("Done");
					//					button.setOnAction(new EventHandler<ActionEvent>() {
					//						@Override
					//						public void handle(ActionEvent event) {
					//							// TODO Auto-generated method stub
					//						}
					//					});
					//					
					//					TextInputDialog dialog = new TextInputDialog("distance");
					//					dialog.setTitle("Input Required");
					//					dialog.setHeaderText("Configure inch to pixel ratio");
					//					dialog.setContentText("Please enter the distance drawn:");
					//					dialog.initOwner(stage.getOwner());
					//					// Traditional way to get the response value.
					//					Optional<String> result = dialog.showAndWait();
					//					if (result.isPresent()) {
					//						inchToPixelRatio = Double.parseDouble(result.get())
					//								/ inchToPixelPoint1.distance(inchToPixelPoint2);
					//					}
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
		
		inchToPixelRatio = -1;
		collectionRate = -1;
		lengthOfSample = -1;
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
		renderTargetTrackingResultsTab();
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
			img = getRgbaImage(new File(imagePaths.get((int)scrollBarHome.getValue()).getPath()));
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
			primaryStage.getIcons().add(Settings.getRELLogo());
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

	public void drawKnownLengthButtonFired(){
		targetsListView.getSelectionModel().clearSelection();
	}

	public void enterSampleLengthButtonFired(){
		lengthOfSample = Dialogs.getDoubleValueFromUser("Please Enter the Sample Length:", "");
	}

	public void deleteTargetButtonFired(){
		Target t = getSelectedTarget();
		if(t != null)
			targetsListView.getItems().remove(t);
		renderTargetTrackingTab();
	}

	@FXML
	private void exportTargetTrackingDisplacementToProcessorFired(){
		System.out.println("here");
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

			c.inchToPixelRatio = inchToPixelRatio;
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
			if(displacement != null && displacement.size() != 0){
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
		String s = SPOperations.readStringFromFile(Settings.imageProcResulstsDir + "/data/e1.txt");
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
			dicProcessorIntegrator.dicTrueStrain = s;
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
		File[] listOfFiles = new File(Settings.imageProcResulstsDir + "/video").listFiles();
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
				img = getRgbaImage(new File(imagePaths.get(idx).getPath()));
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
			Operations.writeStringToFile(csv, file.getPath() + ".csv");
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
				Operations.writeStringToFile(csv, file.getPath() + ".csv");
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
				img = getRgbaImage(new File(imagePaths.get(idx).getPath()));
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

		scrollBar.setValue(0);
		scrollBarHome.setValue(0);
		labelImageName.setText("No Images Loaded..");
		vboxFunctions.getChildren().remove(hBoxFunctions);
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

		resizeImageViewToFit(runDICImageView);
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
		BufferedImage watermarkImage = null;
		try {
			img = getRgbaImage(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
			copy = getRgbaImage(new File(imagePaths.get((int)scrollBar.getValue()).getPath()));
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

			if(inchToPixelPoint1 != null && inchToPixelPoint2 != null){
				g2d.setColor(Color.decode("#a8a8a8"));
				g2d.drawLine((int)inchToPixelPoint1.getX(), (int)inchToPixelPoint1.getY(), (int)inchToPixelPoint2.getX(), (int)inchToPixelPoint2.getY());
			}


			g2d.dispose();
			
			try {
				img = ImageOps.watermark(img, watermarkImage, ImageOps.PlacementPosition.BOTTOMRIGHT, 35); //here's your slowness.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			runTargetTrackingImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabelTargetTrackingTab.setText(imagePaths.get((int)scrollBar.getValue()).getName());

			
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
					//					System.out.println("imageBeginIndex: " + imageBeginIndex);
					//					System.out.println("ImageIndex: " + imageIndex);
					for(int i = 0; i <= imageIndex - imageBeginIndex; i++){
						Ellipse2D circ = new Ellipse2D.Double(targ.pts[i].getX(), targ.pts[i].getY(), 10, 10);
						g2d.draw(circ);
					}
				}
				else{
					//System.out.println("Drawing Index: " + (imageIndex - imageBeginIndex));
					Ellipse2D circ = new Ellipse2D.Double(targ.pts[imageIndex - imageBeginIndex].getX(), targ.pts[imageIndex - imageBeginIndex].getY(), 10, 10);
					g2d.draw(circ);
				}


			}

			g2d.dispose();

			targetTrackingResultsImageView.setImage(SwingFXUtils.toFXImage(img,null));
			imageNameLabelTargetTrackingTab.setText(imagePaths.get((int)scrollBar.getValue()).getName());

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
		File results = new File(Settings.imageProcResulstsDir);
		if(results.exists() && results.isDirectory()) {
			Operations.deleteFolder(results);
		}
		results.mkdirs();
		if(copyImages()) {
			File dicJobFile = new File(Settings.imageProcResulstsDir + "/ncorr_job_file.txt");
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
				bw.write("Output Dir:	"+Settings.imageProcResulstsDir+"/\n\n");

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
		dicResultsImagePaths.clear();
		runDICResultsImageView.setVisible(false);
		dicProgressBar.setVisible(true);
		dicStatusLabel.setVisible(true);
		dicTabPane.getSelectionModel().select(2);
		String NcorrLocation = Settings.currentOS.contains("Win") ? "libs/ncorr_CommandLine.exe" 
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
		File dir = new File(Settings.imageProcResulstsDir+"/video");
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
		//System.out.println(dicBundleDirectory);
		File file = new File(dicBundleDirectory);

		if(!file.exists())
			file.mkdir();

		dicImageRunPaths.clear();

		//System.out.println(imageBeginIndex + " " + imageEndIndex);
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
			File roi = new File(Settings.imageProcResulstsDir+"/roi.png");
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

