package net.relinc.viewer.GUI;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;
import com.sun.javafx.charts.Legend; //KEEP
import com.sun.javafx.charts.Legend.LegendItem; //KEEP

import net.relinc.libraries.application.FileFX;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.application.LineChartWithMarkers.chartDataType;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.TransmissionPulse;
import net.relinc.libraries.data.ModifierFolder.LowPass;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.data.ModifierFolder.Reducer;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.sample.CompressionSample;
import net.relinc.libraries.sample.HopkinsonBarSample;
import net.relinc.libraries.sample.LoadDisplacementSampleResults;
import net.relinc.libraries.sample.Sample;
import net.relinc.libraries.sample.SampleGroup;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.ImageOps;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.viewer.application.MetricMultiplier;
import net.relinc.viewer.application.RegionOfInterest;
import net.relinc.viewer.application.MetricMultiplier.Unit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
//import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class HomeController extends CommonGUI {
	List<Color> seriesColors;
	public List<String> parameters;
	PopOver about;

	List<String> colorString = Arrays.asList("#A0D6C8", "#D18357", "#DEAADE", "#DDD75D", "#819856", "#78ADD4",
			"#A1E17C", "#71E5B3", "#D8849C", "#5BA27E", "#5E969A", "#C29E53", "#8E89A4", "#C6DB93", "#E9A38F",
			"#E3B4C5", "#63D7DF", "#C57370", "#BFC6E4", "#AC7A9C");

	List<File> imagePaths = new ArrayList<>();

	@FXML VBox leftVBox;
	@FXML VBox middleBottomVbox;
	@FXML AnchorPane chartAnchorPane;
	@FXML VBox vboxForDisplayedChartsListView;
	@FXML VBox vBoxHoldingCharts;
	@FXML HBox globalLoadDataFilterHBox; //contains the numeric box
	@FXML VBox globalLoadDataFilterVBox;
	@FXML VBox globalDisplacementDataVBox;
	@FXML HBox globalDisplacementDataHBox;
	@FXML VBox dataModifiersVBox;

	@FXML RadioButton engineeringRadioButton;
	@FXML RadioButton trueRadioButton;
	@FXML RadioButton metricRadioButton;
	@FXML RadioButton englishRadioButton;
	@FXML RadioButton secondsRadioButton;
	@FXML RadioButton milliSecondsRadioButton;
	@FXML RadioButton microSecondsRadioButton;
	@FXML RadioButton nanoSecondsRadioButton;
	@FXML RadioButton picoSecondsRadioButton;
	@FXML CheckBox loadDisplacementCB;
	@FXML CheckBox zoomToROICB;
	@FXML SplitPane homeSplitPane;
	@FXML Button buttonOpenExportMenu;

	@FXML TextField tbAvgYValue;
	@FXML TextField tbSlopeValue;
	@FXML TextField tbKValue;
	@FXML TextField tbAvgIntegralValue;
	@FXML Label averageYValueLabel;
	@FXML RadioButton radioSetBegin;
	@FXML RadioButton radioSetEnd;
	@FXML ChoiceBox<String> choiceBoxRoi;
	@FXML ChoiceBox<Sample> roiSelectionModeChoiceBox;
	@FXML Accordion leftAccordion;
	@FXML CheckBox holdROIAnnotationsCB;
	@FXML Button showSampleDirectoryButton;
	@FXML TextField maxYValueTF;
	@FXML TextField durationTF;
	@FXML TextField averageKValueTF;
	@FXML TextField averageNValueTF;
	@FXML Label averageMaxValueLabel;
	@FXML Label xValueLabel;
	@FXML Label yValueLabel;
	@FXML TitledPane regionOfInterestTitledPane;
	@FXML TitledPane chartingTitledPane;
	@FXML TitledPane dataModifiersTitledPane;
	
	//temp trim
	@FXML RadioButton tempTrimBeginRadioButton;
	@FXML RadioButton tempTrimEndRadioButton;


	boolean mouseHoveringOverSample = false;
	boolean exportMenuOpen;

	ToggleGroup englishMetricGroup = new ToggleGroup();
	ToggleGroup engineeringTrueGroup = new ToggleGroup();
	ToggleGroup exportEnglishMetricGroup = new ToggleGroup();
	ToggleGroup exportEngineeringTrueGroup = new ToggleGroup();
	ToggleGroup roiToggleGroup = new ToggleGroup();
	ToggleGroup timeScaleToggleGroup = new ToggleGroup();
	ToggleGroup tempTrimBeginEndToggleGroup = new ToggleGroup();

	CheckListView<String> displayedChartListView = new CheckListView<String>();


	RegionOfInterest ROI = new RegionOfInterest();
	MetricMultiplier timeUnits = new MetricMultiplier();

	int DataPointsToShow = 2000;
	double tintForceAmount = 50; //the amount to tint the back face force series line
	
	boolean showROIOnChart = false;
	double widthOfLeftPanel;
	

	//*********Video correlation Region****************
	Button useSampleImages = new Button("Use sample images");
	Button openImagesButton = new Button("Choose Images");
	ScrollBar imageScrollBar = new ScrollBar();
	Label imageShownLabel = new Label("Image.jpg");
	ImageView imageView = new ImageView();
	LineChartWithMarkers<Number, Number> imageMatchingChart;// = new LineChart<Number, Number>();
	Button saveVideoButton = new Button("Save Video");
	
	//*******************

	//global filter
	NumberTextField globalLoadDataFilterTextField = new NumberTextField("KHz", "KHz");
	NumberTextField globalDisplacementFilterTextField = new NumberTextField("KHz", "KHz");

	public void initialize(){
		RightOptionPane rightOptionPane = new RightOptionPane(this);
		//rightOptionPane.initialize();
		//homeSplitPane.setStyle("-fx-box-border: transparent;");
		showSampleDirectoryButton.setGraphic(SPOperations.getIcon(SPOperations.folderImageLocation));
		
		fillColorList();
		englishRadioButton.setToggleGroup(englishMetricGroup);
		metricRadioButton.setToggleGroup(englishMetricGroup);
		engineeringRadioButton.setToggleGroup(engineeringTrueGroup);
		trueRadioButton.setToggleGroup(engineeringTrueGroup);
		
		metricRadioButton.selectedProperty().bindBidirectional(SPSettings.metricMode); //english button will be taken care of by group.

		radioSetBegin.setToggleGroup(roiToggleGroup);
		radioSetEnd.setToggleGroup(roiToggleGroup);

		secondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		milliSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		microSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		nanoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		picoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);

		tempTrimBeginRadioButton.setToggleGroup(tempTrimBeginEndToggleGroup);
		tempTrimEndRadioButton.setToggleGroup(tempTrimBeginEndToggleGroup);

		leftVBox.getChildren().add(1, realCurrentSamplesListView);
		vboxForDisplayedChartsListView.getChildren().add(0,displayedChartListView);
		fillAllSamplesTreeView();
		
		globalLoadDataFilterVBox.setStyle("-fx-border-color: #bdbdbd;\n"
                + "-fx-border-insets: -5;\n"
                + "-fx-border-width: 1;\n"
                + "-fx-border-style: solid;\n");
		globalDisplacementDataVBox.setStyle("-fx-border-color: #bdbdbd;\n"
                + "-fx-border-insets: -5;\n"
                + "-fx-border-width: 1;\n"
                + "-fx-border-style: solid;\n");

		GridPane grid = new GridPane();
		grid.add(globalLoadDataFilterTextField, 0, 0);
		grid.add(globalLoadDataFilterTextField.unitLabel, 0, 0);
		globalLoadDataFilterHBox.getChildren().add(1,grid);
		globalLoadDataFilterTextField.updateLabelPosition();
		globalLoadDataFilterTextField.setPrefWidth(80);
		
		GridPane grid2 = new GridPane();
		grid2.add(globalDisplacementFilterTextField, 0, 0);
		grid2.add(globalDisplacementFilterTextField.unitLabel, 0, 0);
		globalDisplacementDataHBox.getChildren().add(1, grid2);
		globalDisplacementFilterTextField.updateLabelPosition();
		globalDisplacementFilterTextField.setPrefWidth(80);
		
		dataModifiersVBox.setSpacing(20);
		

		setROITimeValuesToMaxRange();
		renderCharts();

		choiceBoxRoi.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				renderROIResults();
			}

		});

		roiSelectionModeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
			@Override
			public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newValue) {
				renderROIChoiceBox(); //new command
				renderCharts();
				Sample s = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
				if(s == null || s.placeHolderSample){
					roiSelectionModeChoiceBox.setStyle("");
				}
				else{
					roiSelectionModeChoiceBox.setStyle("-fx-background-color:" +  SPOperations.toHexString(getSampleChartColor(s)));
				}
			}
		});

		
		realCurrentSamplesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		addChartTypeListeners();

		realCurrentSamplesListView.setCellFactory(new Callback<ListView<Sample>, ListCell<Sample>>() {
			@Override
			public CheckBoxListCell<Sample> call(ListView<Sample> listView) {
				final CheckBoxListCell<Sample> listCell = new CheckBoxListCell<Sample>()
				{
					@Override
					public void updateItem(Sample item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
						} else {
							setText(item.getName());
						}
					}
				};
				listCell.setSelectedStateCallback(new Callback<Sample, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(Sample param) {
						return param.selectedProperty();
					}
				});

				listCell.setOnMouseEntered(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (listCell.getItem() == null)
							return;
						if (about != null)
							about.hide();

						Sample sam = listCell.getItem();
						about = new PopOver();

						VBox vbox = new VBox();
						vbox.getStyleClass().add("aboutVBox");
						Label header = new Label(sam.getName());
						header.setFont(new Font(20));
						header.getStyleClass().add("header");
						Label type = new Label(sam.getSampleType());

						String descriptors = sam.getParametersForPopover(metricRadioButton.isSelected());
						
						Label length = new Label(descriptors);
						VBox dataSubsetControlsVbox = new VBox();
						dataSubsetControlsVbox.setSpacing(5);
						//Label data = new Label(dataDescription);
						ChoiceBox<DataFile> dataFilesChoiceBox = new ChoiceBox<DataFile>();
						ChoiceBox<DataSubset> dataSubssetsChoiceBox = new ChoiceBox<DataSubset>();
						dataFilesChoiceBox.setItems(FXCollections.observableArrayList(sam.DataFiles));
						dataFilesChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataFile>() {
							@Override
							public void changed(ObservableValue<? extends DataFile> observable, DataFile oldValue,
									DataFile newValue) {
								dataSubssetsChoiceBox.setItems(FXCollections.observableArrayList(dataFilesChoiceBox.getSelectionModel().getSelectedItem().dataSubsets));
								dataSubssetsChoiceBox.getSelectionModel().select(0);
							}
						});
						dataSubssetsChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
							@Override
							public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue,
									DataSubset newValue) {
								dataSubsetControlsVbox.getChildren().clear();
								if(dataSubssetsChoiceBox.getSelectionModel().getSelectedItem() == null)
									return;
								for(Modifier mod : dataSubssetsChoiceBox.getSelectionModel().getSelectedItem().modifiers){
									dataSubsetControlsVbox.getChildren().addAll(mod.getViewerControls());
								}
							}
						});
						dataFilesChoiceBox.getSelectionModel().select(0);
						header.setTextAlignment(TextAlignment.CENTER);
						type.setTextAlignment(TextAlignment.LEFT);

						TableView<Descriptor> dictionaryTableView = new TableView<Descriptor>();

						dictionaryTableView.getColumns().clear();
						dictionaryTableView.setEditable(false);
						dictionaryTableView.setPrefHeight(300);

						TableColumn<Descriptor, String> key = new TableColumn<Descriptor, String>("Parameter");
						TableColumn<Descriptor, String> value = new TableColumn<Descriptor, String>("Value");

						key.setCellValueFactory(new PropertyValueFactory<Descriptor, String>("key"));
						key.setCellFactory(TextFieldTableCell.forTableColumn());

						value.setCellValueFactory(new PropertyValueFactory<Descriptor, String>("value"));
						value.setCellFactory(TextFieldTableCell.forTableColumn());


						value.setPrefWidth(200);
						key.setMinWidth(200);

						dictionaryTableView.getColumns().add(key);
						dictionaryTableView.getColumns().add(value);

						dictionaryTableView.setItems(sam.descriptorDictionary.descriptors);
						dictionaryTableView.setPrefHeight(0);

						Label numberOfReflectionsLabel = new Label("Number of Reflections: " + SPOperations.round(sam.results.getNumberOfReflections(), 1));
						vbox.getChildren().addAll(header, type, numberOfReflectionsLabel, length, dictionaryTableView);
						vbox.setAlignment(Pos.TOP_LEFT);
						vbox.setSpacing(5);
						vbox.setPrefHeight(400);
						vbox.setPadding(new Insets(10));
						VBox.setVgrow(dictionaryTableView, Priority.ALWAYS);
						AnchorPane.setBottomAnchor(vbox, 0.0);
						AnchorPane.setLeftAnchor(vbox, 0.0);
						AnchorPane.setTopAnchor(vbox, 0.0);
						AnchorPane.setRightAnchor(vbox, 0.0);
						about.setContentNode(vbox);
						about.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
						about.setCornerRadius(2);
						about.setArrowSize(6);
						about.setAutoHide(true);
						about.getContentNode().setOnMouseExited(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								if(about != null){
									javafx.scene.shape.Rectangle rec = new javafx.scene.shape.Rectangle(about.getX(), about.getY(), about.getWidth(), about.getHeight());
									if(!rec.contains(new Point2D(event.getScreenX(), event.getScreenY())))
										about.hide();
								}

							}
						});

						about.show(listCell);
					}
				});
				listCell.setOnMouseExited(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if(about != null)
						{
							javafx.scene.shape.Rectangle rec = new javafx.scene.shape.Rectangle(about.getX(), about.getY(), about.getWidth(), about.getHeight());
							if(!rec.contains(new Point2D(event.getScreenX(), event.getScreenY())))
								about.hide();
						}
					}
				});

				return listCell;
			}
		});


		englishRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderCharts();
			}
		});

		engineeringRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderCharts();
			}
		});

		realCurrentSamplesListView.getItems().addListener(sampleListChangedListener);

		

		buttonAddSampleToGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				addSampleToGroupButtonFired();
			}
		});

		buttonDeleteSelectedGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				int selected = treeViewSampleGroups.getSelectionModel().getSelectedIndex();
				if(selected > 0) {
					sampleGroupRoot.getChildren().remove(selected - 1);
					sampleGroups.remove(selected - 1);
				}
			}
		});

		buttonCreateSampleGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				addGroupButtonClicked();
			}
		});

		buttonExportData.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				try {
					onExportDataButtonClicked();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		buttonExportCSV.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				exportCSVButtonFired();
			}
		});

		treeViewSampleGroups.getSelectionModel().selectedItemProperty()
		.addListener(new ChangeListener<TreeItem<String>>() {

			@Override
			public void changed(ObservableValue<? extends TreeItem<String>> observable,
					TreeItem<String> old_val, TreeItem<String> new_val) {
				onTreeViewItemClicked();
			}
		});

		leftAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {

			@Override
			public void changed(ObservableValue<? extends TitledPane> observable, TitledPane oldValue,
					TitledPane newValue) {
				if(newValue != null){
				}
				if(newValue != null && newValue.getText().equals("Region Of Interest")){
					showROIOnChart = true;
					renderCharts();
				}
				else{
					if(!holdROIAnnotationsCB.isSelected()){
						if(showROIOnChart){
							showROIOnChart = false;
							renderCharts();
						}
					}
				}

			}

		});



		timeScaleToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				RadioButton button = (RadioButton)newValue;
				switch(button.getText()){
				case "s":
					timeUnits.units = Unit.BASE;
					break;
				case "ms":
					timeUnits.units = Unit.MILLI;
					break;
				case "us":
					timeUnits.units = Unit.MICRO;
					break;
				case "ns":
					timeUnits.units = Unit.NANO;
					break;
				case "ps":
					timeUnits.units = Unit.PICO;
					break;
				}
				renderCharts();
			}
		});

		loadDisplacementCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderCharts();
			}
		});

		openImagesButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				FileChooser fileChooser = new FileChooser();
				imagePaths =
						fileChooser.showOpenMultipleDialog(stage);

				Sample currentSample = getCheckedSamples().get(0);
				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);

				if(currentDisplacementDataSubset.Data.data.length != imagePaths.size()){
					Dialogs.showAlert("The number of images does not match the length of the displacement data.\n"
							+ "Number of images: " + imagePaths.size() + "\n"
									+ "Length of Displacement data: " + currentDisplacementDataSubset.Data.data.length, stage);
				}
				
				if(currentDisplacementDataSubset.getBegin() > imagePaths.size()){
					//badly off.
					imagePaths = new ArrayList<File>();
					Dialogs.showAlert("Displacement data begin Index is greater than number of images. Images not compatible.", stage);
					return;
				}

				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
				imageScrollBar.setMax(currentDisplacementDataSubset.getEnd());


				renderImageMatching();
			}

		});
		
		useSampleImages.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				//button shouldn't get clicked unless sample has images.
				Sample currentSample = getCheckedSamples().get(0);
				File tempImageLoadLocation = new File(SPSettings.applicationSupportDirectory + "/SurePulse/tempImagesForViewer");
				if(tempImageLoadLocation.exists())
					SPOperations.deleteFolder(tempImageLoadLocation);
				
				tempImageLoadLocation.mkdirs();
				
				File images = ImageOps.extractSampleImagesToDirectory(currentSample, tempImageLoadLocation);
				
				imagePaths = images == null ? null : Arrays.asList(images.listFiles());
				
				DataSubset currentDisplacementDataSubset = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);

				if(currentDisplacementDataSubset.Data.data.length != imagePaths.size()){
					Dialogs.showAlert("The number of images does not match the length of the displacement data", stage);
				}
				System.out.println("Setting minimum of scroll bar to: " + currentDisplacementDataSubset.getBegin());
				System.out.println("Setting maximum of scroll bar to: " + currentDisplacementDataSubset.getEnd());
				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
				imageScrollBar.setMax(currentDisplacementDataSubset.getEnd());


				renderImageMatching();
				
			}
		});

		saveVideoButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				double length = net.relinc.libraries.staticClasses.Dialogs.getDoubleValueFromUser("Please Enter the desired video length (seconds):", "seconds");
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Save Video");
				fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("mp4 Video(*.mp4)", "*.mp4"));
				fileChooser.setInitialFileName("*.mp4");
				File file = fileChooser.showSaveDialog(stage);


				try {
					File garbageImages = new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREANALYZE/GarbageImages");
					if(garbageImages.exists())
						SPOperations.deleteFolder(garbageImages);
					garbageImages.mkdirs();
					int begin = (int)imageScrollBar.getMin();
					int end = (int)imageScrollBar.getMax();
					imageMatchingChart.setAnimated(false);
					for(int i = begin; i <= end; i++){
						imageScrollBar.setValue(i);
						WritableImage image = chartAnchorPane.snapshot(new SnapshotParameters(), null);
						BufferedImage buf = SwingFXUtils.fromFXImage(image, null);
						buf = ImageOps.getImageWithEvenHeightAndWidth(buf);
						
						String imName = Integer.toString(i - begin);
						while(imName.length() < 4)
							imName = "0" + imName;
						
						ImageIO.write(buf, "png", new File(garbageImages.getPath() + "/" + imName + ".png"));
					}
					
					String videoExportString = file.getPath().endsWith(".mp4") ? file.getPath() : file.getPath() + ".mp4";
			    	String imagesString = garbageImages.getPath() + "/" + "%04d.png";
			    	
			    	double fr = (end - begin + 1) / length;
					
					ImageOps.exportImagesToVideo(imagesString, videoExportString, fr);
					
					imageMatchingChart.setAnimated(true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});

		imageScrollBar.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				renderImageMatching();
			}


		});


		sampleGroupRoot = new TreeItem<>("Sample Groups");
		treeViewSampleGroups.setRoot(sampleGroupRoot);

		engineeringRadioButton.setTooltip(new Tooltip("Engineering stress and strain mode"));
		trueRadioButton.setTooltip(new Tooltip("True stress and strain mode"));
		metricRadioButton.setTooltip(new Tooltip("Metric units mode"));
		englishRadioButton.setTooltip(new Tooltip("English units mode"));
		secondsRadioButton.setTooltip(new Tooltip("Graphs time data in seconds"));
		milliSecondsRadioButton.setTooltip(new Tooltip("Graphs time data in milliseconds"));
		microSecondsRadioButton.setTooltip(new Tooltip("Graphs time data in microseconds"));
		nanoSecondsRadioButton.setTooltip(new Tooltip("Graphs time data in nanoseconds"));
		picoSecondsRadioButton.setTooltip(new Tooltip("Graphs time data in picoseconds"));
		loadDisplacementCB.setTooltip(new Tooltip("Load-Displacement mode"));
		zoomToROICB.setTooltip(new Tooltip("Zooms graph to Region of Interest (ROI)"));
		buttonOpenExportMenu.setTooltip(new Tooltip("Opens a wizard for exporting to excel and .csv"));
		tbAvgYValue.setTooltip(new Tooltip("Calculates the average Y on the ROI. If multiple ROI mode (green), it's the average of the averages"));
		tbAvgIntegralValue.setTooltip(new Tooltip("Calculates the average integral of the checked samples in the ROI"));
		tempTrimBeginRadioButton.setTooltip(new Tooltip("Temporarily trims the begin of the graphed samples. Click on any graph to trim"));
		tempTrimEndRadioButton.setTooltip(new Tooltip("Temporarily trims the end of the graphed samples. Click on any graph to trim"));
		roiSelectionModeChoiceBox.setTooltip(new Tooltip("Allows independent ROI or all samples ROI. Begin and end can be set for each sample by selecting the sample here"));
		holdROIAnnotationsCB.setTooltip(new Tooltip("Holds the ROI annotations when other tabs are opend in this accordion"));
		showSampleDirectoryButton.setTooltip(new Tooltip("Shows the sample directory, where any workspace can be selected from which samples can be loaded"));
		maxYValueTF.setTooltip(new Tooltip("The maximum Y value in the ROI (Region of Interest)"));
		durationTF.setTooltip(new Tooltip("The duration of the ROI"));
		averageMaxValueLabel.setTooltip(new Tooltip("The average of the max values in the ROI"));
		xValueLabel.setTooltip(new Tooltip("The X value of the mouse position on the graph"));
		yValueLabel.setTooltip(new Tooltip("The Y value of the mouse position on the graph"));
		radioSetBegin.setTooltip(new Tooltip("Sets the begin of the ROI. Click on the graph to use"));
		radioSetEnd.setTooltip(new Tooltip("Sets the end of the ROI. Click on the graph to use"));
		choiceBoxRoi.setTooltip(new Tooltip("Select the chart that the ROI calculations should be run on"));
		
		globalLoadDataFilterTextField.setTooltip(new Tooltip("Applies a lowpass filter to all checked datasets."));
		

	}
	
	ListChangeListener<Sample> sampleListChangedListener = new ListChangeListener<Sample>(){
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends Sample> c) {
			System.out.println("Current Samples Listener Fired!");
			renderDefaultSampleResults();
			renderSampleResults();
			renderROISelectionModeChoiceBox();
			renderCharts();
		}
	};
	

	public void renderImageMatching(){
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(imagePaths.get((int)imageScrollBar.getValue()).getPath()));
		} catch (IOException e) {
			System.err.println("Failed to load image in renderImageMatching");
			e.printStackTrace();
			return;
		}

		imageView.setImage(SwingFXUtils.toFXImage(img,null));
		//runDICImageView.setFitHeight(200);
		imageShownLabel.setText(imagePaths.get((int)imageScrollBar.getValue()).getName());

		imageView.fitHeightProperty().unbind();
		imageView.fitWidthProperty().unbind();
		imageView.setFitHeight(10);
		imageView.setFitWidth(10);
		imageView.setFitHeight(-1);
		imageView.setFitWidth(-1);
		imageView.setPreserveRatio(true);

		if(imageView.getImage().getHeight() / imageView.getImage().getWidth() > ((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / 2 / ((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue()){
			imageView.setFitHeight(((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / 2);
		}
		else	
		{
			imageView.setFitWidth(((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue());
		}
		
		
		Sample currentSample = getCheckedSamples().get(0);
		DataSubset currentDisplacement = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
		int currentIndex = (int)imageScrollBar.getValue() - currentDisplacement.getBegin();
		imageMatchingChart.clearVerticalMarkers();
		if (imageMatchingChart.xDataType == chartDataType.TIME) {
			// Time is on the x axis
			imageMatchingChart.addVerticalValueMarker(
					new Data<Number, Number>(currentSample.results.time[currentIndex] * timeUnits.getMultiplier(), 0));
		} 
		else 
		{
			// Displacement/Strain is on the x axis
			if (loadDisplacementCB.isSelected()) {
				if (englishRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getDisplacement("in")[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getDisplacement("mm")[currentIndex], 0));
				}

			} 
			else 
			{
				if (engineeringRadioButton.isSelected()) {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getEngineeringStrain()[currentIndex], 0));
				} else {
					imageMatchingChart.addVerticalValueMarker(
							new Data<Number, Number>(currentSample.results.getTrueStrain()[currentIndex], 0));
				}
			}
		}
		
	}

	public void addChartTypeListeners(){
		displayedChartListView.getCheckModel().getCheckedItems().addListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().addListener(chartTypeChangeListener);
	}

	public void removeChartTypeListeners(){
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
		displayedChartListView.getCheckModel().getCheckedItems().removeListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().removeListener(chartTypeChangeListener);
	}

	ListChangeListener<String> chartTypeListener  = new ListChangeListener<String>() {
		public void onChanged(ListChangeListener.Change<? extends String> c) {
			renderROIChoiceBox();
			renderCharts();
		}
	};

	ChangeListener<String> chartTypeChangeListener = new ChangeListener<String>() {

		@Override
		public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
			renderROIChoiceBox();
			renderCharts();
		}
	};

	public void createRefreshListener(){
		Stage primaryStage = stage;
		primaryStage.focusedProperty().addListener(new ChangeListener<Boolean>()
		{
			@Override
			public void changed(ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1)
			{
				renderCharts();
			}
		});

		widthOfLeftPanel = homeSplitPane.getDividerPositions()[0] * homeSplitPane.getWidth();

		stage.getScene().widthProperty().addListener(new ChangeListener<Number>() {
			@Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
				double percentage = widthOfLeftPanel / homeSplitPane.getWidth();
				homeSplitPane.setDividerPosition(0, percentage);

				if(homeSplitPane.getDividerPositions().length > 1)
					homeSplitPane.setDividerPosition(1, 1 - percentage);
			}
		});

		if(parameters != null && parameters.size() > 0) {
			try {
				String path = parameters.get(0);
				Sample sample = SPOperations.loadSample(path);
				realCurrentSamplesListView.getItems().add(sample);
				displayedChartListView.getCheckModel().checkAll();
				renderCharts();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		stage.setMaximized(true);
	}

	@FXML
	public void selectCustomDataButtonFired(){
		Stage anotherStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/viewer/GUI/SelectCustomData.fxml"));
			Scene scene = new Scene(root1.load());
			//scene.getStylesheets().add(getClass().getResource("/application/table-column-background.css").toExternalForm());
			anotherStage.setScene(scene);
			anotherStage.initOwner(stage);
			anotherStage.initModality(Modality.WINDOW_MODAL);
			SelectCustomDataController c = root1.<SelectCustomDataController>getController();
			c.currentSamples = getCheckedSamples();
			c.render();
			anotherStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}

		renderSampleResults();
		renderCharts();
	}

	public void showSampleDirectoryButtonFired(){
		fillAllSamplesTreeView();
		//xButton.setBlendMode(BlendMode.HARD_LIGHT);
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");

		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		hBoxThatHoldsXButton.getChildren().add(new Label("All Samples in Directory"));


		VBox vbox = new VBox();
		HBox hBox = new HBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(addSelectedSampleButton);
		hBox.getChildren().add(refreshDirectoryButton);
		hBox.getChildren().add(changeDirectoryButton);
		vbox.getChildren().add(hBox);
		vbox.getChildren().add(sampleDirectoryTreeView);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		hBox.setSpacing(5);
		hBox.setAlignment(Pos.CENTER);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		vbox.prefHeightProperty().bind(stage.getScene().heightProperty());
		optionPane.getChildren().clear();
		optionPane.getChildren().add(vbox);
		AnchorPane optionPane = new AnchorPane();
		optionPane.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);

		VBox.setVgrow(sampleDirectoryTreeView, Priority.ALWAYS);

		while(homeSplitPane.getItems().size() > 2)
			homeSplitPane.getItems().remove(2);
		homeSplitPane.getItems().add(optionPane);
		homeSplitPane.setDividerPosition(1, 1 - homeSplitPane.getDividerPositions()[0]);
	}

	@FXML
	public void saveChartToImageButtonFired(){
		
		SnapshotParameters parameters = new SnapshotParameters();
		parameters.setDepthBuffer(true);
		WritableImage image = chartAnchorPane.snapshot(parameters, null);

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Image");
		fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png Image(*.png)", "*.png"));
		fileChooser.setInitialFileName("*.png");
		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void selectCustomRangeButtonFired(){

	}

	public void checkAllButtonFired(){
		realCurrentSamplesListView.getItems().stream().forEach(sample -> sample.selectedProperty().removeListener(sampleCheckedListener));
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(true));
		realCurrentSamplesListView.getItems().stream().forEach(sample -> sample.selectedProperty().addListener(sampleCheckedListener));
		sampleCheckedListener.changed(null, true, false);
	}

	public void uncheckAllButtonFired(){
		realCurrentSamplesListView.getItems().stream().forEach(sample -> sample.selectedProperty().removeListener(sampleCheckedListener));
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(false)); //
		realCurrentSamplesListView.getItems().stream().forEach(sample -> sample.selectedProperty().addListener(sampleCheckedListener));
		sampleCheckedListener.changed(null, true, false);
	}

	public void showVideoDialogButtonFired(){
		if(getCheckedSamples().size() != 1){
			Dialogs.showErrorDialog("Error", "Incorrect number of samples selected", "Please check one sample", stage);
			return;
		}
		
		Sample currentSample = getCheckedSamples().get(0);
		
		if(vBoxHoldingCharts.getChildren().size() > 1){
			vBoxHoldingCharts.getChildren().remove(1);
		}
		fillAllSamplesTreeView();
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");

		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		if(currentSample.hasImages)
			hBoxThatHoldsXButton.getChildren().add(useSampleImages);
		hBoxThatHoldsXButton.getChildren().add(openImagesButton);
		hBoxThatHoldsXButton.getChildren().add(saveVideoButton);

		VBox controlsVBox = new VBox();
		controlsVBox.setAlignment(Pos.CENTER);
		controlsVBox.setSpacing(15);
		controlsVBox.getChildren().add(imageScrollBar);
		controlsVBox.getChildren().add(imageShownLabel);


		VBox vbox = new VBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(controlsVBox);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);

		vBoxHoldingCharts.getChildren().add(vbox);
		displayedChartListView.getCheckModel().check("Stress Vs Strain");
		renderCharts();
	}

	@FXML
	private void zoomToROICBFired(){
		renderCharts();
	}

	@FXML
	private void exportCSVButtonFired(){
		if(sampleGroups == null || sampleGroups.size() == 0) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroups) {
			if(group.groupSamples == null || group.groupSamples.size() == 0)
				noData = true;
			else {
				noData = false;
				break;
			}
		}
		if(noData) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add at least one sample to a group",stage);
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Export Location");
		File file = fileChooser.showSaveDialog(stage);

		if (file != null) {
			file.mkdir();
			writeCSVFile(file);
		}
	}

	@FXML
	private void tempTrimResetButtonFired(){
		for(Sample s : getCheckedSamples()){
			DataSubset load = s.getDataSubsetAtLocation(s.results.loadDataLocation);
			DataSubset displacement = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
			load.setBeginTemp(null);
			load.setEndTemp(null);
			displacement.setBeginTemp(null);
			displacement.setEndTemp(null);
		}
		renderSampleResults();
		renderCharts();
	}

	@FXML
	private void exportWorkspaceToZipButtonFired(){
		DirectoryChooser chooser = new DirectoryChooser();
		File dir = chooser.showDialog(stage);

		if(dir == null)
			return;

		File workspace = new File(treeViewHomePath).getParentFile(); 

		SPOperations.exportWorkspaceToZipFile(workspace, dir);
	}

	@FXML
	private void applyGlobalLoadDataFilterButtonFired(){
		SPSettings.globalLoadDataLowpassFilter = new LowPass();
		SPSettings.globalLoadDataLowpassFilter.setLowPassValue(globalLoadDataFilterTextField.getDouble() * 1000);
		renderSampleResults();
		renderCharts();
	}

	@FXML
	private void removeGlobalLoadDataFilterButtonFired(){
		SPSettings.globalLoadDataLowpassFilter = null;
		renderSampleResults();
		renderCharts();
	}
	
	@FXML
	private void applyGlobalDisplacementDataFilterButtonFired(){
		SPSettings.globalDisplacementDataLowpassFilter = new LowPass();
		SPSettings.globalDisplacementDataLowpassFilter.setLowPassValue(globalDisplacementFilterTextField.getDouble() * 1000);
		renderSampleResults();
		renderCharts();
	}
	
	@FXML
	private void removeGlobalDisplacementDataFilterButtonFired(){
		SPSettings.globalDisplacementDataLowpassFilter = null;
		renderSampleResults();
		renderCharts();
	}

	private void writeCSVFile(File file){
		//file is a directory to store all the csvs. A csv for each group.

		String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedStressUnit();
		String strainUnit = getDisplayedStrainUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		String faceForceUnit = getDisplayedFaceForceUnit();

		String timeName = "Time";//always
		String stressName = loadDisplacementCB.isSelected() ? "Load" : "Stress";
		String strainName = loadDisplacementCB.isSelected() ? "Displacement" : "Strain";
		String dataset1Name = timeName + " (" + timeUnit + ")";
		String dataset2Name = stressName + " (" + stressUnit + ")";
		String dataset3Name = strainName + " (" + strainUnit + ")";
		String dataset4Name = strainName + " Rate (" + strainRateUnit + ")";
		String dataset5Name = "Front Face Force (" + faceForceUnit + ")";
		String dataset6Name = "Back Face Force (" + faceForceUnit + ")";

		// Check if face force is in all of the samples.
		boolean faceForcePresent = true;
		for(SampleGroup group : sampleGroups)
		{
			for(Sample s : group.groupSamples)
			{
				if(!(s.isFaceForceGraphable())){
					faceForcePresent = false;
				}
			}
		}
		
		for(SampleGroup group : sampleGroups){
			String csv = "";
			int longestData = 0;
			for(Sample s : group.groupSamples){
				if(faceForcePresent)
					csv += s.getName() + ",,,,,,,";
				else
					csv += s.getName() + ",,,,,";
				if(s.results.time.length > longestData)
					longestData = s.results.time.length;
			}
			csv += "\n";
			for(Sample s : group.groupSamples){ //s is unused, only using for loop as a counter.
				if(faceForcePresent){
					csv += dataset1Name + "," + dataset2Name + "," + dataset3Name + "," + dataset4Name + "," +
							dataset5Name + "," + dataset6Name + ",,";
				}
				else
				{
					csv += dataset1Name + "," + dataset2Name + "," + dataset3Name + "," + dataset4Name + ",,";
				}
			}
			
			csv += "\n";
			//now do data.
			ArrayList<double[]> timeDataList = new ArrayList<double[]>(); //double[] for each sample
			ArrayList<double[]> stressDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainRateDataList = new ArrayList<double[]>();
			ArrayList<double[]> frontFaceForceDataList = new ArrayList<double[]>();
			ArrayList<double[]> backFaceForceDataList = new ArrayList<double[]>();
 
			for(Sample sample : group.groupSamples){
				double[] timeData = {1};
				double[] stressData = {1};
				double[] strainData = {1};
				double[] strainRateData = {1};
				double[] frontFaceForceData = {1};
				double[] backFaceForceData = {1};
				
				List<double[]> data = getScaledDataArraysFromSample(sample);//, stressData, strainData, strainRateData);

				timeData = data.get(0);
				stressData = data.get(1);
				strainData = data.get(2);
				strainRateData = data.get(3);

				timeDataList.add(timeData);
				stressDataList.add(stressData);
				strainDataList.add(strainData);
				strainRateDataList.add(strainRateData);
				
				if(data.size() >= 6)
				{
					frontFaceForceData = data.get(4);
					backFaceForceData = data.get(5);
					frontFaceForceDataList.add(frontFaceForceData);
					backFaceForceDataList.add(backFaceForceData);
				}
			}
			ArrayList<String> lines = new ArrayList<String>();
			//write each line

			for(int i = 0; i < longestData; i++){
				String dataLine = "";
				for(int j = 0; j < timeDataList.size(); j++){
					if(timeDataList.get(j).length > i){
						if(faceForcePresent){
							dataLine += timeDataList.get(j)[i] + "," + stressDataList.get(j)[i] + "," + 
									strainDataList.get(j)[i] + "," + strainRateDataList.get(j)[i] + "," +
									+ frontFaceForceDataList.get(j)[i] + "," + backFaceForceDataList.get(j)[i] + ",,";
						}
						else{
							dataLine += timeDataList.get(j)[i] + "," + stressDataList.get(j)[i] + "," + 
									strainDataList.get(j)[i] + "," + strainRateDataList.get(j)[i] + ",,";
						}
						
					}
					else{
						//data isn't long enough, add space
						if(faceForcePresent)
						{
							dataLine += ",,,,,,,"; // This is insanely hard coded.
						}
						else{
							dataLine += ",,,,,";
						}
						
					}
				}
				lines.add(dataLine + "\n");
			}

			SPOperations.writeStringToFile(csv, file.getPath() + "/" + group.groupName + ".csv"); //Header
			SPOperations.writeListToFile(lines, file.getPath() + "/" + group.groupName + ".csv"); //Data
		}

	}

	private String getDisplayedStressUnit(){
		if(loadDisplacementCB.isSelected()){
			return englishRadioButton.isSelected() ? "Lbf" : "N";
		}
		else{
			return englishRadioButton.isSelected() ? "ksi" : "MPa";
		}
	}

	private String getDisplayedStrainUnit(){
		if(loadDisplacementCB.isSelected()){
			return englishRadioButton.isSelected() ? "in" : "mm";
		}
		else{
			return englishRadioButton.isSelected() ? "in/in" : "mm/mm";
		}
	}

	private String getDisplayedStrainRateUnit(){
		if(loadDisplacementCB.isSelected()){
			return englishRadioButton.isSelected() ? "in/s" : "mm/s";
		}
		else{
			return englishRadioButton.isSelected() ? "in/in/s" : "mm/mm/s";
		}
	}

	private String getDisplayedTimeUnit(){
		return ((RadioButton)timeScaleToggleGroup.getSelectedToggle()).getText();
	}
	
	private String getDisplayedFaceForceUnit(){
		return englishRadioButton.isSelected() ? "Lbf" : "N";
	}

	// All the data collection should go through this. Maybe use dictionary instead of indexes.
	private List<double[]> getScaledDataArraysFromSample(Sample s){//, double[] stress, double[] strain, double[] strainRate){
		String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedStressUnit();
		String strainUnit = getDisplayedStrainUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		double[] stress = {1};
		double[] strain;
		double[] strainRate;
		double[] frontFaceForce = {1};
		double[] backFaceForce = {1};
		if(loadDisplacementCB.isSelected()){
			stress = s.results.getLoad(stressUnit);
			//strainData = sample.results.displacement;
			strain = s.results.getDisplacement(strainUnit);
			strainRate = SPOperations.getDerivative(s.results.time, s.results.displacement);
		}
		else{
			//all hopkinson bar samples.
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)s;
			double[] load;
			load = s.results.getEngineeringStress(stressUnit); //load is scaled.

			if (trueRadioButton.isSelected()) {
				try {
					//stress = s.results.getTrueStress();
					stress = hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(load,
							s.results.getEngineeringStrain());
				} catch (Exception e) {
					e.printStackTrace();
				}
				strain = s.results.getTrueStrain();
				strainRate = SPOperations.getDerivative(s.results.time, strain);

			} else {
				stress = s.results.getEngineeringStress(stressUnit);
				strain = s.results.getEngineeringStrain();
				strainRate = SPOperations.getDerivative(s.results.time, strain);

			}
		}
		//apply time scale
		double[] time = new double[s.results.time.length];
		for(int i = 0; i < time.length; i++){
			time[i] = s.results.time[i] * timeUnits.getMultiplier();
		}
		if(s.isFaceForceGraphable())
		{
			HopkinsonBarSample hoppy = (HopkinsonBarSample)s;
			frontFaceForce = hoppy.getFrontFaceForce();
			
			
			double sign = hoppy.getTransmissionPulseSign();
			
			
			TransmissionPulse transmissionPulse = (TransmissionPulse)s.getCurrentLoadDatasubset();
			
			backFaceForce = transmissionPulse.getBackFaceForcePulse(s.barSetup.TransmissionBar, sign);
			
			if(englishRadioButton.isSelected()){
				frontFaceForce = Arrays.stream(frontFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
				backFaceForce = Arrays.stream(backFaceForce).map(d -> Converter.LbfFromN(d)).toArray();
			}
		}
		
		ArrayList<double[]> a = new ArrayList<>();
		a.add(time);
		a.add(stress);
		a.add(strain);
		a.add(strainRate);
		if(s.isFaceForceGraphable()){
			a.add(frontFaceForce);
			a.add(backFaceForce);
		}
		return a;
	}

	private void renderDefaultSampleResults(){
		boolean loadDisplacementOnly = false;
		for(Sample sample : realCurrentSamplesListView.getItems()){
			if(!(sample instanceof HopkinsonBarSample))
				loadDisplacementOnly = true; //
			if(sample.results == null){
				sample.results = new LoadDisplacementSampleResults(sample);
			}
		}
		if(loadDisplacementOnly){
			loadDisplacementCB.setSelected(false);
			loadDisplacementCB.setDisable(true);
		}
	}

	private void setROITimeValuesToMaxRange(){
		ROI.beginROITime = 0;
		ROI.endROITime = getLowestMaxTime();
		for(Sample s : realCurrentSamplesListView.getItems()){
			if(s.getBeginROITime() == -1)
				s.setBeginROITime(0);
			if(s.getEndROITime() == -1 || s.getEndROITime() > s.results.time[s.results.time.length -1])
				s.setEndROITime(s.results.time[s.results.time.length - 1]);
		}
	}
	
	private ChangeListener<Boolean> sampleCheckedListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			setROITimeValuesToMaxRange();
			renderSampleResults();
			renderROIChoiceBox(); //new command
			renderROISelectionModeChoiceBox();
			renderCharts();
		}
	};

	public void addSampleToList(String samplePath){
		try {
			Sample sampleToAdd = SPOperations.loadSample(samplePath);

			if(sampleToAdd != null){
				sampleToAdd.selectedProperty().addListener(sampleCheckedListener);
				SPTracker.track(SPTracker.surepulseViewerCategory, "Sample Analyzed");
				realCurrentSamplesListView.getItems().add(sampleToAdd);
			}
			else{
				System.out.println("Failed to load the sample.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void renderCharts(){
		if(loadDisplacementOnlySampleExists(getCheckedSamples())){
			loadDisplacementCB.setSelected(true);
			loadDisplacementCB.setDisable(true);
		}
		else{
			loadDisplacementCB.setDisable(false);
		}
		renderDisplayedChartListViewChartOptions();

		chartAnchorPane.getChildren().clear();
		renderROIResults();
		
		ArrayList<LineChart<Number, Number>> charts = new ArrayList<LineChart<Number, Number>>();
		if(vBoxHoldingCharts.getChildren().size() > 1){
			//video dialog is open.
			LineChartWithMarkers<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			imageMatchingChart = chart;
			
			VBox vBox = new VBox();
			vBox.getChildren().add(chart);
			vBox.getChildren().add(imageView);
			vBox.setAlignment(Pos.CENTER);


			VBox.setVgrow(chart, Priority.ALWAYS);
			chartAnchorPane.getChildren().add(vBox);
			AnchorPane.setTopAnchor(vBox, 0.0);
			AnchorPane.setBottomAnchor(vBox, 0.0);
			AnchorPane.setLeftAnchor(vBox, 0.0);
			AnchorPane.setRightAnchor(vBox, 0.0);
			charts.add(chart);
		}
		else if (displayedChartListView.getCheckModel().getCheckedItems().size() == 0) {
			if (displayedChartListView.getSelectionModel().getSelectedIndex() != -1) {
				LineChart<Number, Number> chart = getChart(displayedChartListView.getSelectionModel().getSelectedItem());
				if(chart == null)
					return;
				chartAnchorPane.getChildren().add(chart);
				AnchorPane.setTopAnchor(chart, 0.0);
				AnchorPane.setBottomAnchor(chart, 0.0);
				AnchorPane.setLeftAnchor(chart, 0.0);
				AnchorPane.setRightAnchor(chart, 0.0);
				charts.add(chart);
			}
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 1){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			chart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
			chartAnchorPane.getChildren().add(chart);
			AnchorPane.setTopAnchor(chart, 0.0);
			AnchorPane.setBottomAnchor(chart, 0.0);
			AnchorPane.setLeftAnchor(chart, 0.0);
			AnchorPane.setRightAnchor(chart, 0.0);
			charts.add(chart);
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 2){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			LineChart<Number, Number> chart2 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			//chart.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			//chart2.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			VBox vBox = new VBox();
			vBox.getChildren().add(chart);
			vBox.getChildren().add(chart2);
			VBox.setVgrow(chart, Priority.ALWAYS);
			VBox.setVgrow(chart2, Priority.ALWAYS);
			chartAnchorPane.getChildren().add(vBox);
			AnchorPane.setTopAnchor(vBox, 0.0);
			AnchorPane.setBottomAnchor(vBox, 0.0);
			AnchorPane.setLeftAnchor(vBox, 0.0);
			AnchorPane.setRightAnchor(vBox, 0.0);
			charts.add(chart);
			charts.add(chart2);
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 3){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			LineChart<Number, Number> chart2 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			LineChart<Number, Number> chart3 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(2));
			chart.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			chart2.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			chart3.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(2));
			VBox vBox = new VBox();
			HBox topHBox = new HBox();
			vBox.getChildren().add(chart);
			vBox.getChildren().add(topHBox);
			VBox.setVgrow(chart, Priority.ALWAYS);
			VBox.setVgrow(topHBox, Priority.ALWAYS);
			HBox.setHgrow(chart2, Priority.ALWAYS);
			HBox.setHgrow(chart3, Priority.ALWAYS);
			topHBox.getChildren().add(chart2);
			topHBox.getChildren().add(chart3);
			chartAnchorPane.getChildren().add(vBox);
			AnchorPane.setTopAnchor(vBox, 0.0);
			AnchorPane.setBottomAnchor(vBox, 0.0);
			AnchorPane.setLeftAnchor(vBox, 0.0);
			AnchorPane.setRightAnchor(vBox, 0.0);
			charts.add(chart);
			charts.add(chart2);
			charts.add(chart3);
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 4){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			LineChart<Number, Number> chart2 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			LineChart<Number, Number> chart3 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(2));
			LineChart<Number, Number> chart4 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(3));
			HBox hBox = new HBox();
			VBox leftVBox = new VBox();
			VBox rightVBox = new VBox();
			AnchorPane.setTopAnchor(hBox, 0.0);
			AnchorPane.setBottomAnchor(hBox, 0.0);
			AnchorPane.setLeftAnchor(hBox, 0.0);
			AnchorPane.setRightAnchor(hBox, 0.0);
			HBox.setHgrow(leftVBox, Priority.ALWAYS);
			HBox.setHgrow(rightVBox, Priority.ALWAYS);
			VBox.setVgrow(chart, Priority.ALWAYS);
			VBox.setVgrow(chart2, Priority.ALWAYS);
			VBox.setVgrow(chart3, Priority.ALWAYS);
			VBox.setVgrow(chart4, Priority.ALWAYS);

			hBox.getChildren().add(leftVBox);
			hBox.getChildren().add(rightVBox);
			leftVBox.getChildren().add(chart);
			leftVBox.getChildren().add(chart3);
			rightVBox.getChildren().add(chart2);
			rightVBox.getChildren().add(chart4);
			chartAnchorPane.getChildren().add(hBox);
			charts.add(chart);
			charts.add(chart2);
			charts.add(chart3);
			charts.add(chart4);
		}
		else{
			System.out.println("NONE OF THE CHARTING OPTIONS WERE VALID");
		}
		charts.stream().forEach(c -> c.setAxisSortingPolicy(LineChart.SortingPolicy.NONE));
	}


	private void renderDisplayedChartListViewChartOptions() {
		String loadVsDisplacementName = "Load Vs Displacement";
		String loadVsTimeName = "Load Vs Time";
		String displacementVsTimeName = "Displacement Vs Time";
		String displacementRateVsTimeName = "Displacement Rate Vs Time";
		
		String stressVsStrainName = "Stress Vs Strain";
		String faceForceVsTimeName = "Face Force Vs Time";
		String stressVsTimeName = "Stress Vs Time";
		String strainVsTimeName = "Strain Vs Time";
		String strainRateVsTimeName = "Strain Rate Vs Time";
		
		if(loadDisplacementCB.isSelected()){
			if(displayedChartListView.getItems().size() > 0 && displayedChartListView.getItems().get(0).equals(loadVsDisplacementName)){
				return;
			}
			removeChartTypeListeners();
			displayedChartListView.getSelectionModel().clearSelection();
			displayedChartListView.getCheckModel().clearChecks();
			displayedChartListView.getItems().clear();
			displayedChartListView.getItems().add(loadVsDisplacementName);
			displayedChartListView.getItems().add(loadVsTimeName);
			displayedChartListView.getItems().add(displacementVsTimeName);
			displayedChartListView.getItems().add(displacementRateVsTimeName);
			addChartTypeListeners();
		}
		else{
			
			if(displayedChartListView.getItems().size() > 0 && displayedChartListView.getItems().get(0).equals(stressVsStrainName)){
				removeChartTypeListeners();
				boolean forceIsApplicable = true;
				for(Sample s : getCheckedSamples()){
					if(!(s.isFaceForceGraphable())){
						forceIsApplicable = false;
					}
				}
				if(!forceIsApplicable){
					if (displayedChartListView.getItems().contains(faceForceVsTimeName)) {
						displayedChartListView.getCheckModel().clearChecks();
						displayedChartListView.getSelectionModel().clearSelection();
						displayedChartListView.getItems().clear();

						displayedChartListView.getItems().add(stressVsStrainName);
						displayedChartListView.getItems().add(stressVsTimeName);
						displayedChartListView.getItems().add(strainVsTimeName);
						displayedChartListView.getItems().add(strainRateVsTimeName);
					}
				}
				else{
					if(!displayedChartListView.getItems().contains(faceForceVsTimeName))
						displayedChartListView.getItems().add(faceForceVsTimeName);
				}
				addChartTypeListeners();
				return;
			}
			displayedChartListView.getCheckModel().clearChecks();
			displayedChartListView.getSelectionModel().clearSelection();
			displayedChartListView.getItems().clear();

			displayedChartListView.getItems().add(stressVsStrainName);
			displayedChartListView.getItems().add(stressVsTimeName);
			displayedChartListView.getItems().add(strainVsTimeName);
			displayedChartListView.getItems().add(strainRateVsTimeName);
			
			boolean forceIsApplicable = true;
			for(Sample s : getCheckedSamples()){
				if(!(s.getCurrentLoadDatasubset() instanceof TransmissionPulse && s.getCurrentDisplacementDatasubset() instanceof ReflectedPulse)){
					forceIsApplicable = false;
				}
			}
			
			if(forceIsApplicable){
				displayedChartListView.getItems().add(faceForceVsTimeName);
			}
			
			addChartTypeListeners();
		}
	}

	private boolean loadDisplacementOnlySampleExists(List<Sample> checkedSamples) {
		for(Sample s : checkedSamples){
			if(!(s instanceof HopkinsonBarSample))
				return true;
		}
		return false;
	}

	private void renderROIResults() {

		averageNValueTF.setText("");
		averageKValueTF.setText("");
		durationTF.setText("");
		tbAvgYValue.setText("");
		tbAvgIntegralValue.setText("");
		maxYValueTF.setText("");
		
		ROI.renderROIResults(getCheckedSamples(), loadDisplacementCB.isSelected(), roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem());

		//average value
		if(getCheckedSamples().size() == 0){
			System.out.println("Not rendering ROI results because there are no checked samples");
			return;
		}
		//now there's at least one checked sample

		String chartOfInterest = "";

		if(choiceBoxRoi.getSelectionModel().getSelectedItem() == null){
			//nothing selected, look at chart
			//chartOfInterest = displayedChartListView.getCheckModel().getCheckedItems().size() > 0 ? displayedChartListView.getCheckModel().getCheckedItems().get(0) : null;
		}
		else{
			chartOfInterest = choiceBoxRoi.getSelectionModel().getSelectedItem();
		}

		if(chartOfInterest == null || chartOfInterest.equals("")){
			return;
		}

		durationTF.setText(Double.toString(SPOperations.round((ROI.endROITime - ROI.beginROITime) * timeUnits.getMultiplier(),5)));

		if(chartOfInterest.equals("Stress Vs Time")){
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Stress");
				averageMaxValueLabel.setText("Average Max Stress");
			}
			else{
				averageYValueLabel.setText("Average Stress");
				averageMaxValueLabel.setText("Max Stress");
			}
			double avg = ROI.averageEngineeringStress;
			double avgMax = ROI.averageMaxEngineeringStress;
			double integral = ROI.averageEngineeringStressVsTimeIntegral;
			if (trueRadioButton.isSelected()){
				avg = ROI.averageTrueStress;
				avgMax = ROI.averageMaxTrueStress;
				integral = ROI.averageTrueStressVsTimeIntegral;
			}


			if (englishRadioButton.isSelected()){
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.ksiFromPa(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgMax),4)));
			}
			else
			{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.MpaFromPa(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avgMax),4)));
			}

		}
		else if(chartOfInterest.equals("Strain Vs Time")){
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Strain");
				averageMaxValueLabel.setText("Average Max Strain");
			}
			else{
				averageYValueLabel.setText("Average Strain");
				averageMaxValueLabel.setText("Max Strain");
			}
			double avg = ROI.averageEngineeringStrain;
			double avgMax = ROI.averageMaxEngineeringStrain;
			double integral = ROI.averageEngineeringStrainVsTimeIntegral;
			if(trueRadioButton.isSelected()){
				avg = ROI.averageTrueStrain;
				integral = ROI.averageTrueStrainVsTimeIntegral;
				avgMax = ROI.averageMaxTrueStrain;
			}

			tbAvgYValue.setText(Double.toString(SPOperations.round(avg,4)));
			tbAvgIntegralValue.setText(Double.toString(SPOperations.round(integral, 4)));
			maxYValueTF.setText(Double.toString(SPOperations.round(avgMax,4)));
		}
		else if(chartOfInterest.equals("Stress Vs Strain")){
			if(getCheckedSamples().size() >1){
				averageYValueLabel.setText("Average Stress");
				averageMaxValueLabel.setText("Average Max Stress");
			}
			else{
				averageYValueLabel.setText("Average Stress");
				averageMaxValueLabel.setText("Max Stress");
			}
			double avg = ROI.averageEngineeringStress;
			double integral = ROI.averageEngineeringStressVsStrainIntegral;
			double avgMax = ROI.averageMaxEngineeringStress;
			double avgKVal = ROI.averageEngKValue;
			double avgNVal = ROI.averageEngNValue;
			if (trueRadioButton.isSelected()){
				avg = ROI.averageTrueStress;
				integral = ROI.averageTrueStressVsStrainIntegral;
				avgMax = ROI.averageMaxTrueStress;
				avgKVal = ROI.averageTrueKValue;
				avgNVal = ROI.averageTrueNValue;
			}

			if (englishRadioButton.isSelected()){
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.ksiFromPa(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgMax),4)));
				
				averageKValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgKVal), 4)));
				averageNValueTF.setText(Double.toString(SPOperations.round(avgNVal, 4)));
			}
			else{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.MpaFromPa(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avgMax),4)));
				
				averageKValueTF.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avgKVal), 4)));
				averageNValueTF.setText(Double.toString(SPOperations.round(avgNVal, 4)));
			}
		}
		else if(chartOfInterest.equals("Strain Rate Vs Time")){
			//String avg = getCheckedSamples().size() > 1 ? "Average" : "";//cool code
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Strain Rate");
				averageMaxValueLabel.setText("Average Max Strain Rate");
			}
			else{
				averageYValueLabel.setText("Average Strain Rate");
				averageMaxValueLabel.setText("Max Strain Rate");
			}
			double avg = ROI.averageEngineeringStrainRate;
			double integral = ROI.averageEngineeringStrainRateVsTimeIntegral;
			double avgMax = ROI.averageMaxEngineeringStrainRate;
			if(trueRadioButton.isSelected()){
				avg = ROI.averageTrueStrainRate;
				integral = ROI.averageTrueStrainRateVsTimeIntegral;
				avgMax = ROI.averageMaxTrueStrainRate;
			}


			tbAvgYValue.setText(Double.toString(SPOperations.round(avg, 4)));
			tbAvgIntegralValue.setText(Double.toString(SPOperations.round(integral, 4)));
			maxYValueTF.setText(Double.toString(SPOperations.round(avgMax, 4)));
		}
		else if(chartOfInterest.equals("Displacement Vs Time")){
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Displacement");
				averageMaxValueLabel.setText("Average Max Displacement");
			}
			else{
				averageYValueLabel.setText("Average Displacement");
				averageMaxValueLabel.setText("Max Displacement");
			}
			double avg = ROI.averageDisplacement;
			double avgMax = ROI.averageMaxDisplacement;
			double integral = ROI.averageDisplacementVsTimeIntegral;

			tbAvgYValue.setText(Double.toString(SPOperations.round(avg,4)));
			tbAvgIntegralValue.setText(Double.toString(SPOperations.round(integral, 4)));
			maxYValueTF.setText(Double.toString(SPOperations.round(avgMax,4)));
		}
		else if(chartOfInterest.equals("Displacement Rate Vs Time")){
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Displacement Rate");
				averageMaxValueLabel.setText("Average Max Displacement Rate");
			}
			else{
				averageYValueLabel.setText("Average Displacement Rate");
				averageMaxValueLabel.setText("Max Displacement Rate");
			}
			double avg = ROI.averageDisplacementRate;
			double avgMax = ROI.averageMaxDisplacementRate;
			double integral = ROI.averageDisplacementRateVsTimeIntegral;

			tbAvgYValue.setText(Double.toString(SPOperations.round(avg,4)));
			tbAvgIntegralValue.setText(Double.toString(SPOperations.round(integral, 4)));
			maxYValueTF.setText(Double.toString(SPOperations.round(avgMax,4)));
		}
		else if(chartOfInterest.equals("Load Vs Time")){
			if(getCheckedSamples().size() > 1){
				averageYValueLabel.setText("Average Load");
				averageMaxValueLabel.setText("Average Max Load");
			}
			else{
				averageYValueLabel.setText("Average Load");
				averageMaxValueLabel.setText("Max Load");
			}
			double avg = ROI.averageLoad;
			double avgMax = ROI.averageMaxLoad;
			double integral = ROI.averageLoadVsTimeIntegral;

			if (englishRadioButton.isSelected()){
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.LbfFromN(avg),4)));
				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.LbfFromN(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.LbfFromN(avgMax),4)));
			}
			else
			{
				tbAvgYValue.setText(Double.toString(SPOperations.round(avg,4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(integral, 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(avgMax,4)));
			}
		}
		else if(chartOfInterest.equals("Load Vs Displacement")){
			if(getCheckedSamples().size() >1){
				averageYValueLabel.setText("Average Load");
				averageMaxValueLabel.setText("Average Max Load");
			}
			else{
				averageYValueLabel.setText("Average Load");
				averageMaxValueLabel.setText("Max Load");
			}
			double avg = ROI.averageLoad;
			double integral = ROI.averageLoadVsDisplacementIntegral;
			double avgMax = ROI.averageMaxLoad;
			double avgKVal = ROI.averageLoadKValue;
			double avgNVal = ROI.averageLoadNValue;

			if (englishRadioButton.isSelected()){
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.LbfFromN(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.LbfFromN(integral), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.LbfFromN(avgMax),4)));
				
				averageKValueTF.setText(Double.toString(SPOperations.round(Converter.LbfFromN(avgKVal), 4)));
				averageNValueTF.setText(Double.toString(SPOperations.round(avgNVal, 4)));
			}
			else{
				tbAvgYValue.setText(Double.toString(SPOperations.round(avg,4)));

				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(integral, 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(avgMax,4)));
				
				averageKValueTF.setText(Double.toString(SPOperations.round(avgKVal, 4)));
				averageNValueTF.setText(Double.toString(SPOperations.round(avgNVal, 4)));
			}
		}
	}

	public List<Sample> getCheckedSamples(){
		List<Sample> samples = (List<Sample>) realCurrentSamplesListView.getItems().stream().filter(s-> s.isSelected()).collect(Collectors.toList());
		return samples;
	}
	
	public int getSampleIndex(Sample s){
		return realCurrentSamplesListView.getItems().indexOf(s);

	}
	
	public int getSampleIndexByName(String sampleName){
		for(int i = 0; i < realCurrentSamplesListView.getItems().size(); i++){
					if(sampleName.equals(realCurrentSamplesListView.getItems().get(i).getName()))
						return i;
				}
				return -1;
	}

	private LineChartWithMarkers<Number, Number> getChart(String selectedItem) {
		LineChartWithMarkers<Number, Number> chart = null;
		switch (selectedItem){
		case "Stress Vs Strain":
			chart = getStressStrainChart();
			break;
		case "Stress Vs Time":
			chart = getStressTimeChart();
			break;
		case "Strain Vs Time":
			chart = getStrainTimeChart();
			break;
		case "Strain Rate Vs Time":
			chart = getStrainRateTimeChart();
			break;
		case "Face Force Vs Time":
			chart = getFaceForceTimeChart();
			break;
		case "Energy Vs Time":
			chart = getEnergyTimeChart();
			break;
		case "Load Vs Displacement":
			chart = getLoadDisplacementChart();
			break;
		case "Load Vs Time":
			chart = getLoadTimeChart();
			break;
		case "Displacement Vs Time":
			chart = getDisplacementTimeChart();
			break;
		case "Displacement Rate Vs Time":
			chart = getDisplacementRateTimeChart();

		}
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getDisplacementRateTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Displacement Rate";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/s)";

		if(metricRadioButton.isSelected()){
			yUnits = "(mm/s)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<Number, Number>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENTRATE);
		
		chart.setCreateSymbols(false);

		chart.setTitle("Displacement Rate Vs Time");

		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);

		double maxPlottedVal = Double.MIN_VALUE;
		for(Sample s : getCheckedSamples()){
			double[] displacement = null;
			if(englishRadioButton.isSelected())
				displacement = s.results.getDisplacement("in");
			else
				displacement = s.results.getDisplacement("mm");

			if(displacement == null)
				continue;

			double[] strainRate = null;
			try {
				strainRate = SPOperations.getDerivative(s.results.time, displacement);
			} catch (Exception e) {
				e.printStackTrace();
			}


			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = displacement.length;

			for(int i = 0; i < displacement.length; i++){
				if(strainRate[i] > maxPlottedVal)
					maxPlottedVal = strainRate[i];
				dataPoints.add(new Data<Number, Number>(s.results.time[i] * timeUnits.getMultiplier(), strainRate[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);

			chart.getData().addAll(series1);
			Color seriesColor = seriesColors.get(getSampleIndex(s) % seriesColors.size());
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart, series1, seriesColor, 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getDisplacementTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Displacement";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in)";

		if(metricRadioButton.isSelected()){
			yUnits = "(mm)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.DISPLACEMENT);
		chart.setCreateSymbols(false);
		chart.setTitle("Displacement Vs Time");

		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);


		for(Sample s : getCheckedSamples()){
			double[] strain = s.results.getDisplacement("in");
			if(metricRadioButton.isSelected())
				strain = s.results.getDisplacement("mm");
			if(strain == null)
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = strain.length;

			for(int i = 0; i < strain.length; i++){
				dataPoints.add(new Data<Number, Number>(s.results.time[i] * timeUnits.getMultiplier(), strain[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);
			chart.getData().addAll(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getLoadTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Load";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(Lbf)";

		if(metricRadioButton.isSelected()){
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Load Vs Time");

		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			double[] strain = s.results.getLoad("Lbf");
			if(metricRadioButton.isSelected())
				strain = s.results.load; //Newtons
			if(strain == null)
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = strain.length;

			for(int i = 0; i < strain.length; i++){
				dataPoints.add(new Data<Number, Number>(s.results.time[i] * timeUnits.getMultiplier(), strain[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);
			chart.getData().add(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getLoadDisplacementChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Displacement";
		String yLabel = "Load";
		String xUnits = "(in)";
		String yUnits = "(Lbf)";

		if (metricRadioButton.isSelected()) {
			xUnits = "(mm)";
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.DISPLACEMENT, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Load Vs Displacement");

		if(showROIOnChart){
			chart.clearVerticalMarkers();
			Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
			if (roiSample == null || roiSample.placeHolderSample) {
				if (getCheckedSamples().size() == 1) {
					Sample s = getCheckedSamples().get(0);
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, ROI.beginROITime);
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, ROI.endROITime);

					if (englishRadioButton.isSelected()) {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(s.results.getDisplacement("in")[beginIndex],
										s.results.getDisplacement("in")[endIndex]),
								Color.GREEN);
					} else {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(s.results.getDisplacement("mm")[beginIndex],
										s.results.getDisplacement("mm")[endIndex]),
								Color.GREEN);
					}

				}
			} 
			else {
				for(Sample s : getCheckedSamples()){
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, s.getBeginROITime());
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, s.getEndROITime());

					if (englishRadioButton.isSelected()) {
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getDisplacement("in")[beginIndex],
										0),
								getSampleChartColor(s));
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getDisplacement("in")[endIndex],
										0),
								getSampleChartColor(s));
					} else {
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getDisplacement("mm")[beginIndex],
										0),
								getSampleChartColor(s));
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getDisplacement("mm")[endIndex],
										0),
								getSampleChartColor(s));
					}
				}
			}

		}

		//set click listener
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				
				if (dataModifiersTitledPane.isExpanded()) {
					
					double displacementValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
					int index = 0;
					Sample sam = getCheckedSamples().get(0);
					if (englishRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getDisplacement("in"),
								displacementValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getDisplacement("mm"),
								displacementValue);
					double timeValue = sam.results.time[index];
					if (tempTrimBeginRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setBeginTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setBeginTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					} else if (tempTrimEndRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setEndTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setEndTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					}
					renderSampleResults();
				} else if (regionOfInterestTitledPane.isExpanded()) {

					Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiSample == null || roiSample.placeHolderSample) {
						if (getCheckedSamples().size() != 1)
							return;
						Sample sam = getCheckedSamples().get(0);
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (englishRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getDisplacement("in"),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getDisplacement("mm"),
									strainValue);

						double timeValue = sam.results.time[index];

						if (radioSetBegin.isSelected()) {
							if (timeValue > 0 && timeValue < ROI.endROITime) {
								ROI.beginROITime = timeValue;
							}
						} else if (radioSetEnd.isSelected()) {
							if (timeValue < getLowestMaxTime() && timeValue > ROI.beginROITime) {
								ROI.endROITime = timeValue;
							}
						}
					} else {
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (englishRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									roiSample.results.getDisplacement("in"), strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									roiSample.results.getDisplacement("mm"), strainValue);

						if (index != -1) {
							double timeValue = roiSample.results.time[index];
							if (radioSetBegin.isSelected()) {
								if (timeValue > 0 && timeValue < roiSample.getEndROITime()) {
									roiSample.setBeginROITime(timeValue);
								}
							} else if (radioSetEnd.isSelected()) {
								if (timeValue < roiSample.results.time[roiSample.results.time.length - 1]
										&& timeValue > roiSample.getBeginROITime()) {
									roiSample.setEndROITime(timeValue);
								}
							}
						}
					}
				}

				renderCharts();
			}
		});

		chart.lookup(".chart-plot-background").setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				double xValue = (double) chart.getXAxis().getValueForDisplay(event.getX());
				double yValue = (double) chart.getYAxis().getValueForDisplay(event.getY());
				xValueLabel.setText("X: " + SPOperations.round(xValue, 4));
				yValueLabel.setText("Y: " + SPOperations.round(yValue,4));
			}

		});



		for(Sample s : getCheckedSamples()){
			double[] load = null;
			double[] displacement = null;//s.results.getEngineeringStrain();


			if(englishRadioButton.isSelected()){
				load = s.results.getLoad("Lbf");
				displacement = s.results.getDisplacement("in");
			}
			else{
				load = s.results.load;
				displacement = s.results.getDisplacement("mm");
			}

			if(load == null || displacement == null) //failed to find the stress data
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());



			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = load.length;

			for(int i = 0; i < load.length; i++){
				dataPoints.add(new Data<Number, Number>(displacement[i], load[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);
			chart.getData().add(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart ,series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	@SuppressWarnings("restriction")
	private void createChartLegend(List<Sample> checkedSamples, LineChartWithMarkers<Number, Number> chart, boolean addTintedLegends) {
		ArrayList<LegendItem> items = new ArrayList<>();
		for(Sample s : getCheckedSamples()){
			if(addTintedLegends){
				items.add(new Legend.LegendItem(s.getName() + " Front Face", new javafx.scene.shape.Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
				items.add(new Legend.LegendItem(s.getName() + " Back Face", new javafx.scene.shape.Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()).darker())));
			}
			else{
				items.add(new Legend.LegendItem(s.getName(), new javafx.scene.shape.Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
			}
		}
		
		Legend legend = (Legend)chart.lookup(".chart-legend");
		legend.getItems().setAll(items);
	}

	private LineChartWithMarkers<Number, Number> getEnergyTimeChart() {
		// TODO Auto-generated method stub
		return null;
	}

	private LineChartWithMarkers<Number, Number> getFaceForceTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Force";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(Lbf)";


		if(metricRadioButton.isSelected()){
			yUnits = "(N)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.LOAD);
		chart.setCreateSymbols(false);
		chart.setTitle("Face Force Vs Time");

		if(zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)s; // Only hbar samples are checked if face force is graphable.
			double[] frontFaceForce = null;
			double[] backFaceForce = null;

			double sign = hopkinsonBarSample.getTransmissionPulseSign();
			
			frontFaceForce = hopkinsonBarSample.getFrontFaceForce();
			
			TransmissionPulse transmissionPulse = (TransmissionPulse)s.getCurrentLoadDatasubset();
			
			ReflectedPulse reflectedPulse = (ReflectedPulse)s.getCurrentDisplacementDatasubset();
			
			backFaceForce = transmissionPulse.getBackFaceForcePulse(s.barSetup.TransmissionBar, sign);

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName() + " Front Face Force");
			XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
			series2.setName(s.getName() + " Back Face Force");

			ArrayList<Data<Number, Number>> frontFaceForceDatapoints = new ArrayList<Data<Number, Number>>();
			ArrayList<Data<Number, Number>> backFaceForceDatapoints = new ArrayList<Data<Number, Number>>();

			double[] frontTime = reflectedPulse.getTrimmedTime();
			frontTime = Arrays.copyOfRange(frontTime, 0, frontFaceForce.length);
			
			int totalDataPoints = frontFaceForce.length;
			for(int i = 0; i < frontFaceForce.length; i++){
				if(metricRadioButton.isSelected())
					frontFaceForceDatapoints.add(new Data<Number, Number>(frontTime[i] * timeUnits.getMultiplier(), frontFaceForce[i]));
				else
					frontFaceForceDatapoints.add(new Data<Number, Number>(frontTime[i] * timeUnits.getMultiplier(), Converter.LbfFromN(frontFaceForce[i])));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(frontFaceForceDatapoints);
			
			totalDataPoints = backFaceForce.length;
			for(int i = 0; i < backFaceForce.length; i++){
				if(metricRadioButton.isSelected())
					backFaceForceDatapoints.add(new Data<Number, Number>(transmissionPulse.getTrimmedTime()[i] * timeUnits.getMultiplier(), backFaceForce[i]));
				else
					backFaceForceDatapoints.add(new Data<Number, Number>(transmissionPulse.getTrimmedTime()[i] * timeUnits.getMultiplier(), Converter.LbfFromN(backFaceForce[i])));
				i += totalDataPoints / DataPointsToShow;
			}
			series2.getData().addAll(backFaceForceDatapoints);
			
			chart.getData().add(series1);
			chart.getData().add(series2);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
			setSeriesColor(chart, series2, seriesColors.get(getSampleIndex(s) % seriesColors.size()).darker(), 0); //makes it a bit darker
		}

		createChartLegend(getCheckedSamples(), chart, true);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getStrainRateTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Strain Rate";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/in/s)";

		if(trueRadioButton.isSelected()){
			yLabel = "True Strain Rate";
		}
		if(metricRadioButton.isSelected()){
			yUnits = "(mm/mm/s)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAINRATE);

		chart.setCreateSymbols(false);

		chart.setTitle("Strain Rate Vs Time");

		if(zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		addROIFunctionalityToTimeChart(chart);

		addXYListenerToChart(chart);

		 double maxPlottedVal = Double.MIN_VALUE;

		for(Sample s : getCheckedSamples()){
			double[] strain = null;
			if(engineeringRadioButton.isSelected()){
				strain = s.results.getEngineeringStrain();//engineeringStrain;
			}
			else{
				strain = s.getTrueStrainFromEngineeringStrain(s.results.getEngineeringStrain());
			}
			if(strain == null)
				continue;

			double[] strainRate = null;
			try {
				strainRate = SPOperations.getDerivative(s.results.time, strain);
			} catch (Exception e) {
				e.printStackTrace();
			}


			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = strain.length;

			for(int i = 0; i < strain.length; i++){
				if(strainRate[i] > maxPlottedVal)
					maxPlottedVal = strainRate[i];
				dataPoints.add(new Data<Number, Number>(s.results.time[i] * timeUnits.getMultiplier(), strainRate[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);

			chart.getData().add(series1);
			Color seriesColor = seriesColors.get(getSampleIndex(s) % seriesColors.size());
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart, series1, seriesColor, 0);

		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}


	private LineChartWithMarkers<Number, Number> getStrainTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Strain";
		String xUnits = "(" + timeUnits.getString() + "s)";
		String yUnits = "(in/in)";

		if(trueRadioButton.isSelected()){
			yLabel = "True Strain";
		}
		if(metricRadioButton.isSelected()){
			yUnits = "(mm/mm)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRAIN);
		chart.setCreateSymbols(false);
		chart.setTitle("Strain Vs Time");

		if(zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			double[] strain = null;
			if(engineeringRadioButton.isSelected()){
				strain = s.results.getEngineeringStrain();
			}
			else{
				strain = s.getTrueStrainFromEngineeringStrain(s.results.getEngineeringStrain());
			}
			if(strain == null)
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = strain.length;

			for(int i = 0; i < strain.length; i++){
				dataPoints.add(new Data<Number, Number>(s.results.time[i] * timeUnits.getMultiplier(), strain[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);
			chart.getData().add(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	private void renderSampleResults(){
		//renders the result object for each sample
		for(Sample sample : getCheckedSamples()){
			sample.results.render();
		}
		setROITimeValuesToMaxRange();
	}

	private LineChartWithMarkers<Number, Number> getStressTimeChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Time";
		String yLabel = "Engineering Stress";
		String xUnits = "(" + timeUnits.getString() + "s)";

		String yUnits = "(ksi)";


		if(trueRadioButton.isSelected())
			yLabel = "True Stress";
		if(metricRadioButton.isSelected())
			yUnits = "(MPa)";

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.TIME, chartDataType.STRESS);
		chart.setCreateSymbols(false);
		chart.setTitle("Stress Vs Time");

		if(zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}

		addROIFunctionalityToTimeChart(chart);

		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			double[] load = null;
			double[] time = s.results.time;
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)s;

			if(englishRadioButton.isSelected()){
				load = s.results.getEngineeringStress("ksi");
			}
			else{
				load = s.results.getEngineeringStress("MPa");
			}

			if(engineeringRadioButton.isSelected()){
				//no adjustments
			}
			else{
				try {
					load = hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(load, s.results.getEngineeringStrain());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}


			if(load == null) //failed to find the stress data
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = load.length;

			for(int i = 0; i < load.length; i++){
				dataPoints.add(new Data<Number, Number>(time[i] * timeUnits.getMultiplier(), load[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);
			chart.getData().add(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart, series1, getSampleChartColor(s), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getStressStrainChart() {
		NumberAxis XAxis = new NumberAxis();
		NumberAxis YAxis = new NumberAxis();

		String xlabel = "Engineering Strain";
		String yLabel = "Engineering Stress";
		String xUnits = "(in/in)";
		String yUnits = "(ksi)";


		if(trueRadioButton.isSelected()){
			xlabel = "True Strain";
			yLabel = "True Stress";
		}
		if(metricRadioButton.isSelected()){
			xUnits = "(mm/mm)";
			yUnits = "(MPa)";
		}

		XAxis.setLabel(xlabel + " " + xUnits);
		YAxis.setLabel(yLabel + " " + yUnits);

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis, chartDataType.STRAIN, chartDataType.STRESS);
		chart.setCreateSymbols(false);
		chart.setTitle("Stress Vs Strain");

		if(showROIOnChart){
			chart.clearVerticalMarkers();
			Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
			if (roiSample == null || roiSample.placeHolderSample) {
				if (getCheckedSamples().size() == 1) {
					Sample s = getCheckedSamples().get(0);
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, ROI.beginROITime);
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, ROI.endROITime);

					if (engineeringRadioButton.isSelected()) {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(s.results.getEngineeringStrain()[beginIndex],
										s.results.getEngineeringStrain()[endIndex]),
								Color.GREEN);
					} else {
						chart.addVerticalRangeMarker(new Data<Number, Number>(s.results.getTrueStrain()[beginIndex],
								s.results.getTrueStrain()[endIndex]), Color.GREEN);
					}

				}
			}
			else{
				for(Sample s : getCheckedSamples()){
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, s.getBeginROITime());
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(s.results.time, s.getEndROITime());
					if (engineeringRadioButton.isSelected()) {
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getEngineeringStrain()[beginIndex],
										0),
								getSampleChartColor(s));
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getEngineeringStrain()[endIndex],
										0),
								getSampleChartColor(s));
					} else {
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getTrueStrain()[beginIndex],
										0),
								getSampleChartColor(s));
						chart.addVerticalValueMarker(
								new Data<Number, Number>(s.results.getTrueStrain()[endIndex],
										0),
								getSampleChartColor(s));
					}
				}
			}

		}

		//set click listener
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				
				if (dataModifiersTitledPane.isExpanded()) {
					double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
					int index = 0;
					Sample sam = getCheckedSamples().get(0);
					if (trueRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getTrueStrain(),
								strainValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getEngineeringStrain(),
								strainValue);

					double timeValue = sam.results.time[index];
					
					if (tempTrimBeginRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setBeginTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setBeginTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					} else if (tempTrimEndRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setEndTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setEndTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					}
					renderSampleResults();
				} else if (regionOfInterestTitledPane.isExpanded()) {

					Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiSample == null || roiSample.placeHolderSample) {
						if (getCheckedSamples().size() != 1)
							return;
						Sample sam = getCheckedSamples().get(0);
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (trueRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getTrueStrain(),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(sam.results.getEngineeringStrain(),
									strainValue);

						double timeValue = sam.results.time[index];
						if (radioSetBegin.isSelected()) {
							if (timeValue > 0 && timeValue < ROI.endROITime) {
								ROI.beginROITime = timeValue;
							}
						} else if (radioSetEnd.isSelected()) {
							if (timeValue < getLowestMaxTime() && timeValue > ROI.beginROITime) {
								ROI.endROITime = timeValue;
							}
						}
					} else {
						// individual sample mode
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (trueRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(roiSample.results.getTrueStrain(),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									roiSample.results.getEngineeringStrain(), strainValue);
						if (index != -1) {
							double timeValue = roiSample.results.time[index];
							if (radioSetBegin.isSelected()) {
								if (timeValue > 0 && timeValue < roiSample.getEndROITime()) {
									roiSample.setBeginROITime(timeValue);
								}
							} else if (radioSetEnd.isSelected()) {
								if (timeValue < roiSample.results.time[roiSample.results.time.length - 1]
										&& timeValue > roiSample.getBeginROITime()) {
									roiSample.setEndROITime(timeValue);
								}
							}
						}
					}
				}

				renderCharts();
			}
		});

		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			double[] load = null;
			double[] displacement = null;
			HopkinsonBarSample hopkinsonBarSample = (HopkinsonBarSample)s;

			if(englishRadioButton.isSelected()){
				load = s.results.getEngineeringStress("ksi");
			}
			else{
				load = s.results.getEngineeringStress("MPa");
			}
			displacement = s.results.getEngineeringStrain();
			if(engineeringRadioButton.isSelected()){

			}
			else{
				try {
					load = hopkinsonBarSample.getTrueStressFromEngStressAndEngStrain(load, displacement);
					displacement = s.getTrueStrainFromEngineeringStrain(displacement);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}




			if(load == null || displacement == null) //failed to find the stress data
				continue;

			XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
			series1.setName(s.getName());

			ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

			int totalDataPoints = load.length;

			for(int i = 0; i < load.length; i++){
				dataPoints.add(new Data<Number, Number>(displacement[i], load[i]));
				i += totalDataPoints / DataPointsToShow;
			}
			series1.getData().addAll(dataPoints);

			chart.getData().add(series1);
			series1.nodeProperty().get().setMouseTransparent(true);
			setSeriesColor(chart ,series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()), 0);
		}

		createChartLegend(getCheckedSamples(), chart, false);

		return chart;
	}

	public void addROIFunctionalityToTimeChart(LineChartWithMarkers<Number, Number> chart){
		if(showROIOnChart){
			chart.clearVerticalMarkers();
			Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
			if(roiMode == null || roiMode.placeHolderSample){
				//all samples ROI mode
				chart.addVerticalRangeMarker(new Data<Number, Number>(ROI.beginROITime * timeUnits.getMultiplier(), 
						ROI.endROITime * timeUnits.getMultiplier()), Color.GREEN);
			}
			else{
				//individual samples ROI mode
				for(Sample s : getCheckedSamples()){
					chart.addVerticalValueMarker(new Data<Number, Number>(s.getBeginROITime() * timeUnits.getMultiplier(),
							0), getSampleChartColor(s));

					chart.addVerticalValueMarker(new Data<Number, Number>(s.getEndROITime() * timeUnits.getMultiplier(),
							0), getSampleChartColor(s));

				}
			}
		}

		//set click listener
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				
				if (dataModifiersTitledPane.isExpanded()) {
					double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX())
							/ timeUnits.getMultiplier();
					if (tempTrimBeginRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setBeginTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setBeginTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					} else if (tempTrimEndRadioButton.isSelected()) {
						for (Sample s : getCheckedSamples()) {
							DataSubset displacementData = s.getDataSubsetAtLocation(s.results.displacementDataLocation);
							DataSubset loadData = s.getDataSubsetAtLocation(s.results.loadDataLocation);
							displacementData.setEndTempFromTimeValue(timeValue + displacementData.Data.timeData[displacementData.getBegin()]);
							loadData.setEndTempFromTimeValue(timeValue + loadData.Data.timeData[loadData.getBegin()]);
						}
					}
					renderSampleResults();
					renderCharts();
				}

				else if (regionOfInterestTitledPane.isExpanded()) {
					double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX())
							/ timeUnits.getMultiplier();
					if (radioSetBegin.isSelected()) {
						Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
						if (roiMode == null || roiMode.placeHolderSample) {
							if (timeValue > 0 && timeValue < ROI.endROITime) {
								ROI.beginROITime = timeValue;
							}
						} else {
							if (timeValue > 0 && timeValue < roiMode.getEndROITime())
								roiMode.setBeginROITime(timeValue);
						}
					} else if (radioSetEnd.isSelected()) {
						Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
						if (roiMode == null || roiMode.placeHolderSample) {
							if (timeValue < getLowestMaxTime() && timeValue > ROI.beginROITime) {
								ROI.endROITime = timeValue;
							}
						} else {
							if (timeValue < roiMode.results.time[roiMode.results.time.length - 1]
									&& timeValue > roiMode.getBeginROITime())
								roiMode.setEndROITime(timeValue);
						}
					}

					renderCharts();
				}
			}


		});
	}

	public void addXYListenerToChart(LineChartWithMarkers<Number, Number> chart){
		chart.lookup(".chart-plot-background").setOnMouseMoved(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				double xValue = (double) chart.getXAxis().getValueForDisplay(event.getX());
				double yValue = (double) chart.getYAxis().getValueForDisplay(event.getY());
				xValueLabel.setText("X: " + SPOperations.round(xValue, 4));
				yValueLabel.setText("Y: " + SPOperations.round(yValue,4));
			}

		});
	}

	public void fillAllSamplesTreeView(){
		findFiles(new File(treeViewHomePath), null);
		sampleDirectoryTreeView.setShowRoot(false);
	}

	public void removeSelectedSampleFromList() {
		Sample currentSelectedSample = realCurrentSamplesListView.getSelectionModel().selectedItemProperty().getValue();
		if(currentSelectedSample == null) {
			Dialogs.showInformationDialog("Error removing sample", null, "Please select sample you wish to remove",stage);
			return;
		}
		
		realCurrentSamplesListView.getItems().removeListener(sampleListChangedListener);
		
		ArrayList<Sample> keep = new ArrayList<Sample>();
		for(Sample s : realCurrentSamplesListView.getSelectionModel().getSelectedItems()){
			keep.add(s);
		}
		
		for(Sample s : keep){
			realCurrentSamplesListView.getItems().remove(s);
		}
		sampleListChangedListener.onChanged(null);
		realCurrentSamplesListView.getItems().addListener(sampleListChangedListener);
		renderROIResults();
		renderCharts();
	}

	private void findFiles(File dir, TreeItem<FileFX> parent) {
		TreeItem<FileFX> root = new TreeItem<>(new FileFX(dir), SPOperations.getIcon(SPOperations.folderImageLocation));
		root.setExpanded(true);
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				findFiles(file,root);
			} else {
				if(file.getName().endsWith(SPSettings.compressionExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));

				if(file.getName().endsWith(SPSettings.shearCompressionExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.compressionImageLocation)));

				if(file.getName().endsWith(SPSettings.tensionRectangularExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRectImageLocation)));

				if(file.getName().endsWith(SPSettings.tensionRoundExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.tensionRoundImageLocation)));
				if(file.getName().endsWith(SPSettings.loadDisplacementExtension))
					root.getChildren().add(new TreeItem<>(new FileFX(file),SPOperations.getIcon(SPOperations.loadDisplacementImageLocation)));

			}
		}
		if(parent==null){
			sampleDirectoryTreeView.setRoot(root);
		} else {

			parent.getChildren().add(root);
		}
	} 

	public void addGroupButtonClicked() {

		if(tbSampleGroup.getText().trim().equals("")) {
			Dialogs.showInformationDialog("Error Creating Group", null, "Group Must Have a Name",stage);
			return;
		}

		if(!SPOperations.specialCharactersAreNotInTextField(tbSampleGroup)) {
			Dialogs.showInformationDialog("Add Sample Group","Invalid Character In Group Name", "Only a-z, A-Z, 0-9, dash, space, and parenthesis are allowed",stage);
			return;
		}
		
		if(tbSampleGroup.getText().contains("-")){
			Dialogs.showAlert("Dashes (-) are not allowed in group names", stage);
			return;
		}

		if(findStringInSampleGroups(tbSampleGroup.getText()) > -1) {
			Dialogs.showInformationDialog("Error Creating Group", null, "Group Name Already Exists!",stage);
			return;
		}

		SampleGroup sampleGroup = new SampleGroup(tbSampleGroup.getText());
		sampleGroups.add(sampleGroup);	
		sampleGroupRoot.setExpanded(true);
		refreshSampleGroupTreeView();
	}

	private void refreshSampleGroupTreeView() {
		sampleGroupRoot.getChildren().clear();
		for(SampleGroup sampleGroup : sampleGroups) {
			TreeItem<String> treeItemSampleGroup = new TreeItem<>(sampleGroup.groupName);
			sampleGroupRoot.getChildren().add(treeItemSampleGroup);
			for(Sample sample : sampleGroup.groupSamples) {
				TreeItem<String> treeItemSample = new TreeItem<>(sample.getName());
				treeItemSampleGroup.getChildren().add(treeItemSample);
			}
		}

	}

	public void openExportMenuButtonFired() {
		leftAccordion.setExpandedPane((TitledPane)leftAccordion.getChildrenUnmodifiable().get(0));
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");
		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		hBoxThatHoldsXButton.getChildren().add(new Label("Create Groups to Export"));

		VBox vbox = new VBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(tbSampleGroup);
		vbox.getChildren().add(buttonCreateSampleGroup);
		vbox.getChildren().add(treeViewSampleGroups);
		vbox.getChildren().add(buttonAddSampleToGroup);
		vbox.getChildren().add(buttonDeleteSelectedGroup);

		tbSampleGroup.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if(newValue.length() > 31)
					tbSampleGroup.setText(oldValue);
			}
		});

		includeSummaryPage.setSelected(true);
		includeSummaryPage.setPadding(new Insets(10, 10, 10, 10));
		vbox.getChildren().add(includeSummaryPage);

		vbox.getChildren().add(buttonExportData);
		if(SPSettings.currentOS.contains("Mac")){
			buttonExportData.setDisable(true);
			buttonExportData.setText(buttonExportData.getText() + " (Windows Only)");
		}
		vbox.getChildren().add(buttonExportCSV);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		vbox.prefHeightProperty().bind(stage.getScene().heightProperty());
		AnchorPane anchor = new AnchorPane();
		anchor.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		if(homeSplitPane.getItems().size() > 2)
			homeSplitPane.getItems().remove(2);
		homeSplitPane.getItems().add(anchor);
		homeSplitPane.setDividerPosition(1, 1 - homeSplitPane.getDividerPositions()[0]);
	}

	public void addSampleToGroupButtonFired() {
		if(currentSelectedSampleGroup != null) {
			for(Sample s : getCheckedSamples()) {
				if(currentSelectedSampleGroup.groupSamples.indexOf(s) < 0) {
					// This adds the reference to the sample. So if the selected data is later changed, it changes as well
					// It would be nice to add by copy, but we do not have the means to successfully copy a sample
//					Sample newSample = null;
//
//					if(s instanceof CompressionSample){
//						newSample = new CompressionSample();
//						((CompressionSample)newSample).setDiameter(((CompressionSample)s).getDiameter());
//						
//					}
//					else if(s instanceof TensionRoundSample){
//						newSample = new TensionRoundSample();
//						((TensionRoundSample)newSample).setDiameter(((TensionRoundSample)s).getDiameter());
//					}
//					else if(s instanceof TensionRectangularSample){
//						newSample = new TensionRectangularSample();
//						((TensionRectangularSample)newSample).setWidth(((TensionRectangularSample)s).getWidth());
//						((TensionRectangularSample)newSample).setHeight(((TensionRectangularSample)s).getHeight());
//					}
//					else if(s instanceof ShearCompressionSample){
//						newSample = new ShearCompressionSample();
//						((ShearCompressionSample)newSample).setGaugeWidth(((ShearCompressionSample)s).getGaugeWidth());
//						((ShearCompressionSample)newSample).setGaugeHeight(((ShearCompressionSample)s).getGaugeWidth());
//					} else if(s instanceof LoadDisplacementSample) {
//						newSample = new LoadDisplacementSample();
//					}
//					
//					if(s instanceof HopkinsonBarSample){
//						((HopkinsonBarSample)newSample).setLength(((HopkinsonBarSample)s).getLength());
//					}
//
//
//					LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(newSample);
//
//					results.displacement = Arrays.copyOf(s.results.displacement, s.results.displacement.length);
//					results.load = Arrays.copyOf(s.results.load, s.results.load.length);
//
//					//results.engineeringStrain = Arrays.copyOf(s.results.getEngineeringStrain(), s.results.getEngineeringStrain().length);
//					results.time = Arrays.copyOf(s.results.time, s.results.time.length);
//					//results.engineeringStress = Arrays.copyOf(s.results.getEngineeringStress(), s.results.getEngineeringStress().length);
//
//					newSample.results = results;
//					newSample.setName(s.getName());
					

					//currentSelectedSampleGroup.groupSamples.add(newSample);
					currentSelectedSampleGroup.groupSamples.add(s);
				}
			}
		} else {
			Dialogs.showInformationDialog("Error Adding Sample to Group", null, "Please select a group!",stage);
		}
		refreshSampleGroupTreeView();
	}

	public void onTreeViewItemClicked() {
		TreeItem<String> selectedSampleGroup = treeViewSampleGroups.getSelectionModel().getSelectedItem();
		//github
		if(selectedSampleGroup == sampleGroupRoot) {
			currentSelectedSampleGroup = null;
			return;
		}

		if(findStringInSampleGroups(selectedSampleGroup.getValue()) != -1) {
			currentSelectedSampleGroup = sampleGroups.get(findStringInSampleGroups(selectedSampleGroup.getValue()));
			return;
		}

		if(currentSelectedSampleGroup == null) {
			currentSelectedSampleGroup = new SampleGroup(selectedSampleGroup.getValue());
		}
	}

	public void onExportDataButtonClicked() throws Exception {

		if(sampleGroups == null || sampleGroups.size() == 0) {

			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			return;
		}

		boolean noData = false;
		for(SampleGroup group : sampleGroups) {
			if(group.groupSamples == null || group.groupSamples.size() == 0)
				noData = true;
			else {
				noData = false;
				break;
			}
		}
		if(noData) {
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add at least one sample to a group",stage);
			return;
		}
		
		int pointsToKeep = getPointsToKeepForExcelFileFromUser();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Export Location");
		ExtensionFilter extensionFilter = new ExtensionFilter("Microsoft Excel Worksheet (*.xlsx)","*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		File file = fileChooser.showSaveDialog(stage);

		if (file != null) {
			File jobFile = writeConsoleExcelFileMakerJobFile(file.getPath(), pointsToKeep);

			if(jobFile.exists()) {
				if(SPOperations.writeExcelFileUsingEpPlus(jobFile.getPath())) {
					Dialogs.showInformationDialog("Excel Export", "Job File Created", "EPPlus is creating an excel file at "+file.getAbsolutePath(), stage);
				} else {
					Dialogs.showErrorDialog("Excel Export", "Excel Export Failed", "There was an error exporting your excel file, this usually means the installation of SURE-Pulse Viewer is broken or some files have been removed", stage);
				}
			}
		}
	}

	private int getPointsToKeepForExcelFileFromUser() {
		Stage anotherStage = new Stage();
		Label promptLabel = new Label("If you'd like to reduce the data quantity,\nplease enter the number of points you'd "
				+ "like to keep below.");
		CheckBox reduceDataCheckBox = new CheckBox("Reduce Data Quantity");
		
		Label pointsToKeepLabel = new Label("Points To Keep:");
		NumberTextField userInputTF = new NumberTextField("", "");
		userInputTF.disableProperty().bind(reduceDataCheckBox.selectedProperty().not());
		pointsToKeepLabel.disableProperty().bind(reduceDataCheckBox.selectedProperty().not());
		
		
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
		topVbox.getChildren().add(reduceDataCheckBox);
		HBox inputHBox = new HBox();
		inputHBox.getChildren().add(pointsToKeepLabel);
		inputHBox.getChildren().add(userInputTF);
		inputHBox.setAlignment(Pos.CENTER);
		inputHBox.setSpacing(5);
		topVbox.getChildren().add(inputHBox);
		topVbox.getChildren().add(doneButton);
		topVbox.setAlignment(Pos.CENTER);
		topVbox.setSpacing(15);
		AnchorPane.setBottomAnchor(topVbox, 0.0);
		AnchorPane.setLeftAnchor(topVbox, 0.0);
		AnchorPane.setRightAnchor(topVbox, 0.0);
		AnchorPane.setTopAnchor(topVbox, 0.0);
		
		anchor.getChildren().add(topVbox);
		
		Scene scene = new Scene(anchor, 400, 220);
		anotherStage.setScene(scene);
		anotherStage.initModality(Modality.WINDOW_MODAL);
		
		anotherStage.showAndWait();
		
		int val = userInputTF.getDouble().intValue();
		if(reduceDataCheckBox.isSelected())
			val = -1;
		
		return val;
	}

	private File writeConsoleExcelFileMakerJobFile(String path, int pointsToKeep) {
		File jobFile =new File(SPSettings.applicationSupportDirectory + "/RELFX/SUREPulse/JobFile");
		if(jobFile.exists())
			SPOperations.deleteFolder(jobFile);
		jobFile.mkdir();

		String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedStressUnit();
		String strainUnit = getDisplayedStrainUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();

		String timeName = "Time";//always
		String stressName = loadDisplacementCB.isSelected() ? "Load" : "Stress";
		String strainName = loadDisplacementCB.isSelected() ? "Displacement" : "Strain";
		String trueEng = loadDisplacementCB.isSelected() ? "" : (trueRadioButton.isSelected() ? "True" : "Engineering");

		String parametersString = "Version$1\n";
		parametersString += "Export Location$" + path + "\n";
		parametersString += "Summary Page$" + includeSummaryPage.isSelected() + "\n";
		parametersString += "Dataset1$" + timeName + "$" + "(" + timeUnit + ")" + "$" + "\n";
		parametersString += "Dataset2$" + stressName + "$(" + stressUnit + ")" + "$" + trueEng + "\n";
		parametersString += "Dataset3$" + strainName + "$(" + strainUnit + ")"  + "$" + trueEng + "\n";
		parametersString += "Dataset4$" + strainName + " Rate$(" + strainRateUnit + ")" + "$" + trueEng + "\n";

		SPOperations.writeStringToFile(parametersString, jobFile.getPath() + "/Parameters.txt");

		for(net.relinc.libraries.sample.SampleGroup group : sampleGroups){
			File groupDir = new File(jobFile.getPath() + "/" + group.groupName);
			groupDir.mkdir();
			for(Sample sample : group.groupSamples){
				File sampleDir = new File(groupDir.getPath() + "/" + sample.getName());
				sampleDir.mkdir();
				double[] timeData;
				double[] stressData;
				double[] strainData;
				double[] strainRateData;

				ArrayList<String> sampleData = new ArrayList<String>();

				List<double[]> data = getScaledDataArraysFromSample(sample);
				timeData = data.get(0);
				stressData = data.get(1);
				strainData = data.get(2);
				strainRateData = data.get(3);
				
				Reducer r = new Reducer();
				r.enabled.set(true);
				r.activated.set(true);
				r.setPointsToKeep(pointsToKeep);
				
				timeData = r.applyModifierToData(timeData, null);
				stressData = r.applyModifierToData(stressData, null);
				strainData = r.applyModifierToData(strainData, null);
				strainRateData = r.applyModifierToData(strainRateData, null);
				
				for(int i = 0; i < timeData.length; i++){
					sampleData.add(timeData[i] + "," + stressData[i] + "," + strainData[i] + "," + strainRateData[i] + "\n");
				}
				SPOperations.writeListToFile(sampleData, sampleDir.getPath() + "/Data.txt");
				String parameters = "Color$" + colorString.get(getSampleIndexByName(sample.getName()) % colorString.size()).substring(1) + "\n";
				SPOperations.writeStringToFile(parameters, sampleDir.getPath() + "/Parameters.txt");
			}

		}
		return jobFile;
	}
	private int findStringInSampleGroups(String find) {

		if(sampleGroups == null || sampleGroups.size() == 0)
			return -1;

		int i = 0;

		for(SampleGroup group : sampleGroups) {
			if (group.groupName.equals(find)) {
				return i;
			}
			i++;
		}

		return -1;
	}

	private double getLowestMaxTime() {
		double minTime = Double.MAX_VALUE;
		for(Sample s : getCheckedSamples()){
			if(s.results.time[s.results.time.length-1] < minTime){
				minTime = s.results.time[s.results.time.length-1];
			}
		}
		return minTime;
	}

	private void renderROIChoiceBox(){
		String prevChoice = choiceBoxRoi.getSelectionModel().getSelectedItem();
		choiceBoxRoi.getItems().clear();
		Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
		for(String s : displayedChartListView.getCheckModel().getCheckedItems()) {
			if(!(getCheckedSamples().size() > 1 && s.equals("Stress Vs Strain") && (roiMode == null || roiMode.placeHolderSample == true)))
				choiceBoxRoi.getItems().add(s);
		}
		
		if(choiceBoxRoi.getItems().size() == 0){
			String item = displayedChartListView.getSelectionModel().getSelectedItem();
			if(item != null && item != "")
				choiceBoxRoi.getItems().add(item);
		}
		
		if(prevChoice != null && prevChoice != "" && choiceBoxRoi.getItems().contains(prevChoice)){
			choiceBoxRoi.getSelectionModel().select(prevChoice);
		}
		else{
			if(choiceBoxRoi.getItems().size() > 0)
				choiceBoxRoi.getSelectionModel().select(0);
		}
	}

	private void renderROISelectionModeChoiceBox(){
		roiSelectionModeChoiceBox.getItems().clear();
		CompressionSample allSample = new CompressionSample();
		allSample.placeHolderSample = true;
		allSample.setName("All Samples");

		roiSelectionModeChoiceBox.getItems().add(allSample);
		for(Sample s : getCheckedSamples()){
			roiSelectionModeChoiceBox.getItems().add(s);
		}
	}

	private void setSeriesColor(LineChartWithMarkers<Number, Number> chart, Series<Number, Number> series, Color color, double tint){

		String rgb = String.format("%d, %d, %d",
				(int) (color.getRed() * 255 - tint),
				(int) (color.getGreen() * 255- tint),
				(int) (color.getBlue() * 255- tint));

		series.nodeProperty().get().setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
	}

	private void fillColorList(){
		seriesColors = new ArrayList<Color>();
		for(String s : colorString)
			seriesColors.add(Color.valueOf(s));
	}

	public void showInitialOptions() {
		showSampleDirectoryButtonFired();
		leftAccordion.setExpandedPane((TitledPane)leftAccordion.getChildrenUnmodifiable().get(0));
	}

	private Color getSampleChartColor(Sample s){
		return seriesColors.get(getSampleIndex(s) % seriesColors.size());
	}

}
