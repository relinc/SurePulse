package net.relinc.viewer.GUI;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.xml.ws.spi.http.HttpHandler;

import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import net.relinc.libraries.referencesample.ReferenceSample;
import net.relinc.libraries.sample.*;
import net.relinc.viewer.application.SampleGroupSession;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.PopOver;
import net.relinc.libraries.application.LineChartWithMarkers;
import net.relinc.libraries.data.DataFile;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.data.Descriptor;
import net.relinc.libraries.data.ReflectedPulse;
import net.relinc.libraries.data.TransmissionPulse;
import net.relinc.libraries.data.ModifierFolder.LowPass;
import net.relinc.libraries.data.ModifierFolder.Modifier;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Converter;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPLogger;
import net.relinc.libraries.staticClasses.SPMath;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;
import net.relinc.libraries.staticClasses.SPTracker;
import net.relinc.viewer.application.SampleSession;
import net.relinc.viewer.application.Session;
import net.relinc.viewer.application.MetricMultiplier.Unit;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

public class HomeController extends CommonGUI {



	public List<String> parameters;
	List<String> colorString = Arrays.asList("#A0D6C8", "#D18357", "#DEAADE", "#DDD75D", "#819856", "#78ADD4",
			"#A1E17C", "#71E5B3", "#D8849C", "#5BA27E", "#5E969A", "#C29E53", "#8E89A4", "#C6DB93", "#E9A38F",
			"#E3B4C5", "#63D7DF", "#C57370", "#BFC6E4", "#AC7A9C");


	@FXML VBox leftVBox;
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
	@FXML public CheckBox zoomToROICB;
	@FXML SplitPane homeSplitPane;
	@FXML Button buttonOpenExportMenu;

	@FXML TextField tbAvgYValue;
	@FXML TextField tbAvgIntegralValue;
	@FXML Label averageYValueLabel;
	@FXML RadioButton radioSetBegin;
	@FXML RadioButton radioSetEnd;
	@FXML public ChoiceBox<String> choiceBoxRoi;
	@FXML public ChoiceBox<Sample> roiSelectionModeChoiceBox;
	@FXML Accordion leftAccordion;
	@FXML public CheckBox holdROIAnnotationsCB;
	@FXML Button showSampleDirectoryButton;
	@FXML TextField maxYValueTF;
	@FXML TextField durationTF;
	@FXML TextField averageKValueTF;
	@FXML TextField averageNValueTF;
	@FXML Label averageMaxValueLabel;
	@FXML Label roiIntegralLabel;

	@FXML Label xValueLabel;
	@FXML Label yValueLabel;
	@FXML TitledPane regionOfInterestTitledPane;
	@FXML TitledPane chartingTitledPane;
	@FXML TitledPane dataModifiersTitledPane;


	@FXML ComboBox<Sample> trimSampleComboBox;
	@FXML ChoiceBox<TrimDatasetOption> trimDatasetChoiceBox;
	@FXML RadioButton trimBeginRadioButton;
	@FXML RadioButton trimEndRadioButton;

	@FXML VBox referencesVBox;

	@FXML Button refreshReferencesButton;
	@FXML Button newReferenceButton;
	@FXML Button importReferenceButton;
	@FXML Button exportReferenceButton;
	@FXML Button deleteReferenceButton;

	@FXML public VBox sampleGroupsVBox;


	ToggleGroup englishMetricGroup = new ToggleGroup();
	ToggleGroup engineeringTrueGroup = new ToggleGroup();
	ToggleGroup roiToggleGroup = new ToggleGroup();
	ToggleGroup timeScaleToggleGroup = new ToggleGroup();
	ToggleGroup trimBeginEndToggleGroup = new ToggleGroup();

	CheckListView<String> displayedChartListView = new CheckListView<String>();


	boolean showROIOnChart = false;
	double widthOfLeftPanel;
	double trimStep = .01;
	
	//global filter
	NumberTextField globalLoadDataFilterTextField = new NumberTextField("KHz", "KHz");
	NumberTextField globalDisplacementFilterTextField = new NumberTextField("KHz", "KHz");

	ExportGUI rightOptionPane = new ExportGUI(this);;
	SampleDirectoryGUI sampleDirectoryGUI = new SampleDirectoryGUI(this);
	VideoCorrelationGUI videoCorrelationGUI = new VideoCorrelationGUI(this);
	ChartsGUI chartsGUI = new ChartsGUI(this);
	
	private boolean renderBlock = false;
	
	public void initialize(){

		SPLogger.logger.info("HomeController is initializing");
		
		// Attaching the radio button values to the parent CommonGUI class.
		isEnglish.bindBidirectional(englishRadioButton.selectedProperty());
		isEngineering.bindBidirectional(engineeringRadioButton.selectedProperty());
		isLoadDisplacement.bindBidirectional(loadDisplacementCB.selectedProperty());
		
		showSampleDirectoryButton.setGraphic(SPOperations.getIcon(SPOperations.folderImageLocation));
		
		fillColorList();
		englishRadioButton.setToggleGroup(englishMetricGroup);
		metricRadioButton.setToggleGroup(englishMetricGroup);
		engineeringRadioButton.setToggleGroup(engineeringTrueGroup);
		trueRadioButton.setToggleGroup(engineeringTrueGroup);
		
		metricRadioButton.selectedProperty().bindBidirectional(SPSettings.metricMode); //english button will be taken care of by group.

		radioSetBegin.setToggleGroup(roiToggleGroup);
		radioSetEnd.setToggleGroup(roiToggleGroup);
		radioSetBegin.setFocusTraversable(false);
		radioSetEnd.setFocusTraversable(false);

		secondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		milliSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		microSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		nanoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		picoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);

		trimBeginRadioButton.setToggleGroup(trimBeginEndToggleGroup);
		trimEndRadioButton.setToggleGroup(trimBeginEndToggleGroup);
		trimBeginRadioButton.setFocusTraversable(false);
		trimEndRadioButton.setFocusTraversable(false);

		leftVBox.getChildren().add(1, realCurrentSamplesListView);
		vboxForDisplayedChartsListView.getChildren().add(0,displayedChartListView);

		referencesVBox.getChildren().add(0, currentReferencesListView);
		
		
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
							Optional<SampleGroup> g = getCheckedSampleGroups().stream().filter(g1 -> g1.groupSamples.contains(item)).findFirst();
							if(g.isPresent()) {
								setText(item.getName() + " (" + g.get().groupName + ")");
							} else {
								setText(item.getName());
							}
						}
					}
				};
				listCell.setSelectedStateCallback(new Callback<Sample, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(Sample param) {
						return param.selectedProperty();
					}
				});

				listCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if (listCell.getItem() == null)
							return;

						if(event.getClickCount() != 2) {
							return;
						}



						StackPane secondaryLayout = new StackPane();

						Scene secondScene = new Scene(secondaryLayout, 500, 600);

						// New window (Stage)
						Stage newWindow = new Stage();
						newWindow.setAlwaysOnTop(true);
						newWindow.setScene(secondScene);


						newWindow.show();


						Sample sam = listCell.getItem();

						newWindow.setTitle(sam.getName());



						VBox vbox = new VBox();

						secondaryLayout.getChildren().add(vbox);


						vbox.getStyleClass().add("aboutVBox");
						Label header = new Label(sam.getName());
						header.setFont(new Font(20));
						header.getStyleClass().add("header");
						header.setTextFill(getSampleChartColor(sam));
						Label type = new Label(sam.getSampleType());

						ChoiceBox<SampleGroup> groupsChoiceBox = new ChoiceBox<>();
						groupsChoiceBox.getItems().addAll(sampleGroupsList.getItems());

						sampleGroupsList.getItems().stream().filter(gr -> gr.groupSamples.contains(sam)).findFirst().ifPresent(gr -> {
							groupsChoiceBox.getSelectionModel().select(gr);
						});


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
								for(Modifier mod : dataSubssetsChoiceBox.getSelectionModel().getSelectedItem().getModifiers()){
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

						vbox.getChildren().addAll(header, type);

						HBox hb2 = new HBox();
						hb2.setSpacing(10);
						Button clearGroupButton = new Button();
						clearGroupButton.setText("Clear");
						clearGroupButton.setTextFill(Paint.valueOf("red"));
						clearGroupButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								groupsChoiceBox.getSelectionModel().clearSelection();
							}
						});

						hb2.getChildren().add(groupsChoiceBox);
						hb2.getChildren().add(clearGroupButton);
						vbox.getChildren().add(hb2);


						if(sam.getResults().size() == 1) {
							Label numberOfReflectionsLabel = new Label("Number of Reflections: " + SPOperations.round(sam.getResults().get(0).getNumberOfReflections(), 1));
							vbox.getChildren().addAll(numberOfReflectionsLabel);
						}

						vbox.getChildren().addAll(length, dictionaryTableView);

						Button saveButton = new Button();
						saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
							@Override
							public void handle(MouseEvent event) {
								// handle sample group stuff.

								// remove sample from any other groups
								sampleGroupsList.getItems().forEach(g -> {
									g.groupSamples.remove(sam);
								});

								// Add sample to group if group selected.
								if(groupsChoiceBox.getSelectionModel().getSelectedItem() != null) {
									sampleGroupsList.getItems().stream()
											.filter(el -> el == groupsChoiceBox.getSelectionModel().getSelectedItem())
											.findFirst()
											.ifPresent(g -> g.groupSamples.add(sam)
											);
								}

								newWindow.close();
								renderCharts();
								realCurrentSamplesListView.refresh();
							}
						});
						saveButton.setText("Save");

						vbox.getChildren().add(saveButton);

						vbox.setAlignment(Pos.TOP_LEFT);
						vbox.setSpacing(5);
						vbox.setPrefHeight(400);
						vbox.setPadding(new Insets(10));
						VBox.setVgrow(dictionaryTableView, Priority.ALWAYS);
						AnchorPane.setBottomAnchor(vbox, 0.0);
						AnchorPane.setLeftAnchor(vbox, 0.0);
						AnchorPane.setTopAnchor(vbox, 0.0);
						AnchorPane.setRightAnchor(vbox, 0.0);

					}
				});


				return listCell;
			}
		});




		sampleGroupsList.setCellFactory(new Callback<ListView<SampleGroup>, ListCell<SampleGroup>>() {
			@Override
			public CheckBoxListCell<SampleGroup> call(ListView<SampleGroup> listView) {
				final CheckBoxListCell<SampleGroup> listCell = new CheckBoxListCell<SampleGroup>()
				{
					@Override
					public void updateItem(SampleGroup item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
						} else {
							setText(item.groupName);
						}
					}
				};

				listCell.setSelectedStateCallback(new Callback<SampleGroup, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(SampleGroup param) {
						 return param.selectedProperty();
					}
				});



				return listCell;
			}
		});



		currentReferencesListView.setCellFactory(new Callback<ListView<ReferenceSample>, ListCell<ReferenceSample>>() {
			@Override
			public CheckBoxListCell<ReferenceSample> call(ListView<ReferenceSample> listView) {
				final CheckBoxListCell<ReferenceSample> listCell = new CheckBoxListCell<ReferenceSample>()
				{
					@Override
					public void updateItem(ReferenceSample item, boolean empty) {
						super.updateItem(item, empty);
						if (empty) {
							setText(null);
						} else {
							setText(item.getName());
						}
					}
				};
				listCell.setSelectedStateCallback(new Callback<ReferenceSample, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(ReferenceSample param) {
						return param.selectedProperty();
					}
				});

				listCell.setOnMouseClicked(new EventHandler<MouseEvent>() {
					@Override
					public void handle(MouseEvent event) {
						if(event.getClickCount() == 2) {
							showReferencePage(Optional.of(currentReferencesListView.getSelectionModel().getSelectedItem()));
						}
					}
				});


				return listCell;
			}
		});


		sampleGroupsVBox.getChildren().add(1, sampleGroupsList);
		sampleGroupsVBox.getChildren().add(3, disableGroupsCheckBox);



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

		leftAccordion.expandedPaneProperty().addListener(new ChangeListener<TitledPane>() {

			@Override
			public void changed(ObservableValue<? extends TitledPane> observable, TitledPane oldValue,
					TitledPane newValue) {
				if(newValue != null && newValue.getText().equals("Region Of Interest")){
					showROIOnChart = true;
					renderCharts();
				}
				else if(newValue != null && newValue.getText().equals("References")) {

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
				renderTrimDatasetChoiceBox();
				renderCharts();
			}
		});
		
		globalLoadDataFilterTextField.textProperty().addListener((a,b,c) -> {
			globalLoadDataFilterTextField.updateLabelPosition();
		});

		globalDisplacementFilterTextField.textProperty().addListener((a,b,c) -> {
			globalDisplacementFilterTextField.updateLabelPosition();
		});

		trimSampleComboBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
			@Override
			public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newValue) {
				Sample s = trimSampleComboBox.getSelectionModel().getSelectedItem();
				if(s == null || s.placeHolderSample){
					trimSampleComboBox.setStyle("");
				}
				else{
					trimSampleComboBox.setStyle("-fx-background-color:" +  SPOperations.toHexString(getSampleChartColor(s)));
				}
				renderCharts();
			}
		});

		trimSampleComboBox.setCellFactory(new Callback<ListView<Sample>, ListCell<Sample>>() {
			@Override
			public ListCell<Sample> call(ListView<Sample> param) {
				return new ListCell<Sample>() {
					@Override
					protected void updateItem(Sample item, boolean empty) {
						super.updateItem(item, empty);
						if(item != null) {
							setText(item.getName());
							if(getSampleIndex(item) != -1) {
								setBackground(new Background(new BackgroundFill(getSampleChartColor(item), CornerRadii.EMPTY, Insets.EMPTY)));

							}
						}

					}
				};
			}
		});

		sampleGroupsList.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent click) {

				if (click.getClickCount() == 2) {
					//Use ListView's getSelected Item
					SampleGroup currentItemSelected = sampleGroupsList.getSelectionModel()
							.getSelectedItem();
					//use this to do whatever you want to. Open Link etc.
					createEditSampleGroup(Optional.of(currentItemSelected));

				}
			}
		});

		disableGroupsCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				realCurrentSamplesListView.refresh();
				renderROIChoiceBox();
				renderROIResults();
				renderCharts();
			}
		});

		CommonGUI.trimSampleComboBox = trimSampleComboBox;


		renderTrimDatasetChoiceBox();

		refreshReferencesButton.setGraphic(SPOperations.getIcon("/net/relinc/viewer/images/refreshIcon.png"));
		newReferenceButton.setGraphic(SPOperations.getIcon("/net/relinc/viewer/images/plus_icon5.png"));
		newReferenceButton.setContentDisplay(ContentDisplay.LEFT);
		importReferenceButton.setGraphic(SPOperations.getIcon("/net/relinc/viewer/images/import.png"));
		importReferenceButton.setContentDisplay(ContentDisplay.LEFT);
		exportReferenceButton.setGraphic(SPOperations.getIcon("/net/relinc/viewer/images/export.png"));
		exportReferenceButton.setContentDisplay(ContentDisplay.LEFT);
		deleteReferenceButton.setStyle("-fx-text-fill: red");

		refreshReferencesFromDisk();

		
		engineeringRadioButton.setTooltip(new Tooltip("Engineering rawStressData and rawStrainData mode"));
		trueRadioButton.setTooltip(new Tooltip("True rawStressData and rawStrainData mode"));
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
		choiceBoxRoi.setTooltip(new Tooltip("Select the xyChart that the ROI calculations should be run on"));
		trimSampleComboBox.setTooltip(new Tooltip("Select a sample to trim"));
		trimDatasetChoiceBox.setTooltip(new Tooltip("Select which dataset to trim"));
		
		globalLoadDataFilterTextField.setTooltip(new Tooltip("Applies a lowpass filter to all checked datasets."));
		

	}


	
	ListChangeListener<Sample> sampleListChangedListener = new ListChangeListener<Sample>(){
		@Override
		public void onChanged(javafx.collections.ListChangeListener.Change<? extends Sample> c) {
			renderDefaultSampleResults();
			renderSampleResults();
			renderROISelectionModeChoiceBox();
			renderTrimSampleChoiceBox();
			renderCharts();
		}
	};

	private void refreshReferencesFromDisk() {
		currentReferencesListView.getItems().clear();
		File folder = new File(SPSettings.referencesLocation);

		File[] files = folder.listFiles();

		Arrays.sort(files, Comparator.comparingLong(f -> -f.lastModified()));

		for (final File fileEntry : files) {
			if(fileEntry.isFile() && fileEntry.getPath().endsWith(".json")) {
				String json = SPOperations.readStringFromFile(fileEntry.getPath());
				ReferenceSample r = ReferenceSample.createFromJson(json, fileEntry.getPath());
				if(r != null) {
					r.selectedProperty().addListener(referenceCheckedListener);
					currentReferencesListView.getItems().add(r);
				} else {
					System.err.println("Failed to read reference file!");
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

	private void bumpTrim(boolean left, boolean begin) {
		// left: left or right
		// begin: begin or end
		Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();
		List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

		samples.forEach(sample -> {
			sample.getResults().forEach(result -> {
				DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());

				int displacementChange = (int)((displacementData.getEnd() - displacementData.getBegin()) * trimStep);
				if(displacementChange == 0) {
					displacementChange = 1;
				}

				DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
				int loadChange = (int)((loadData.getEnd() - loadData.getBegin()) * trimStep);
				if(loadChange == 0) {
					loadChange = 1;
				}

				Runnable displacementOperation = () -> {};
				Runnable loadOperation = () -> {};
				final int displacementChangeFinal = displacementChange;
				final int loadChangeFinal = loadChange;

				if(left && begin) {
					// move begin left
					displacementOperation = () -> displacementData.setBeginTemp(displacementData.getBegin() - displacementChangeFinal);
					loadOperation = () -> loadData.setBeginTemp(loadData.getBegin() - loadChangeFinal);
				} else if(begin) {
					// move begin right
					displacementOperation = () -> displacementData.setBeginTemp(displacementData.getBegin() + displacementChangeFinal);
					loadOperation = () -> loadData.setBeginTemp(loadData.getBegin() + loadChangeFinal);
				} else if(left) {
					// move end left
					displacementOperation = () -> displacementData.setEndTemp(displacementData.getEnd() - displacementChangeFinal);
					loadOperation = () -> loadData.setEndTemp(loadData.getEnd() - loadChangeFinal);
				} else {
					// move end right
					displacementOperation = () -> displacementData.setEndTemp(displacementData.getEnd() + displacementChangeFinal);
					loadOperation = () -> loadData.setEndTemp(loadData.getEnd() + loadChangeFinal);
				}

				TrimDatasetOption.Option selectedOption = trimDatasetChoiceBox.getSelectionModel().getSelectedItem().getOption();
				if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
					loadOperation.run();
				}
				if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
					displacementOperation.run();
				}

			});
		});
		renderSampleResults();
		renderCharts();
	}

	public void createArrowKeyListener() {
		stage.getScene().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(leftAccordion.expandedPaneProperty() == null || !leftAccordion.expandedPaneProperty().get().getText().equals("Data Modifiers")) {
					return;
				}
				switch (event.getCode()) {
					case LEFT:
						if(trimBeginRadioButton.isSelected()) {
							bumpTrim(true, true);

						} else if(trimEndRadioButton.isSelected()) {
							bumpTrim(true, false);
						}
						break;
					case RIGHT:
						if(trimBeginRadioButton.isSelected()) {
							bumpTrim(false, true);
						} else if(trimEndRadioButton.isSelected()) {
							bumpTrim(false, false);
						}
						break;
				}
			}
		});
	}

	public void createRefreshListener(){
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

			for(Sample sample : getCheckedSamples()) {

				LoadDisplacementSampleResults previousResults = sample.getResults().get(0);
				sample.getResults().clear();
				sample.getResults().addAll(LoadDisplacementSampleResults.createResults(
						sample,
						previousResults.getLoadDataLocation(),
						previousResults.getDisplacementDataLocation()
				));
			}

			renderSampleResults();
			renderCharts();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void showSampleDirectoryButtonFired(){
		sampleDirectoryGUI.showSampleDirectoryPane();
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
	
	@FXML
	public void citeButtonFired(){
		System.out.println("Fired");
		Dialogs.showCitationDialog();
}
  public void loadFilterUpArrowFired(){
		double currentVal = getValueAfterArrowClick(globalLoadDataFilterTextField);
		globalLoadDataFilterTextField.setText(new DecimalFormat(".#####").format(currentVal + SPMath.getPicoArrowIncrease(currentVal, true)));
		applyGlobalLoadDataFilterButtonFired();
	}
	
	@FXML
	public void loadFilterDownArrowFired(){
		double currentVal = getValueAfterArrowClick(globalLoadDataFilterTextField);
		globalLoadDataFilterTextField.setText(new DecimalFormat(".#####").format(currentVal - SPMath.getPicoArrowIncrease(currentVal, false)));
		applyGlobalLoadDataFilterButtonFired();
	}
	
	@FXML
	public void displacementFilterUpArrowFired(){
		double currentVal = getValueAfterArrowClick(globalDisplacementFilterTextField);
		globalDisplacementFilterTextField.setText(new DecimalFormat(".#####").format(currentVal + SPMath.getPicoArrowIncrease(currentVal, true)));
		applyGlobalDisplacementDataFilterButtonFired();
	}
	
	@FXML
	public void displacementFilterDownArrowFired(){
		double currentVal = getValueAfterArrowClick(globalDisplacementFilterTextField);
		globalDisplacementFilterTextField.setText(new DecimalFormat(".#####").format(currentVal - SPMath.getPicoArrowIncrease(currentVal, false)));
		applyGlobalDisplacementDataFilterButtonFired();
	}
	
	private static double getValueAfterArrowClick(NumberTextField tf)
	{
		double currentVal = 1.;
		try{
			currentVal = Double.parseDouble(tf.getText());	
		}
		catch(NumberFormatException e)
		{
			// Failed to parse a double, stick with 1
		}
		return currentVal;
	}

	@FXML
	public void reduceDataSizeButtonFired() {
		Map<String, Number> reduceParams = DataReducerDialog.showDataReducerDialog();
		getCheckedSamples().stream().forEach(sample -> {
			sample.DataFiles.stream().forEach(df -> {
				df.dataSubsets.stream().forEach(subset -> {
					if (reduceParams.containsKey("pointsToKeep")) {
						subset.reduceData(reduceParams.get("pointsToKeep").intValue());
					} else {
						subset.reduceDataByFrequency(reduceParams.get("frequency").doubleValue());
					}

				});
			});
		});
		renderSampleResults();
		renderCharts();
	}
	
	@FXML
	public void checkAllButtonFired(){
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(true));
	}

	@FXML
	public void uncheckAllButtonFired(){
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(false)); //
	}

	@FXML
	public void showVideoDialogButtonFired(){
		videoCorrelationGUI.showVideoDialog();
	}

	@FXML
	private void zoomToROICBFired(){
		renderCharts();
	}

	@FXML
	private void exportCSVButtonFired(){
		rightOptionPane.exportCSVButtonFired();
	}

	@FXML
	private void resetTrimmedDataClicked(){
		for(Sample s : getCheckedSamples()){
			for(LoadDisplacementSampleResults result : s.getResults()) {
				DataSubset load = s.getDataSubsetAtLocation(result.getLoadDataLocation());
				DataSubset displacement = s.getDataSubsetAtLocation(result.getDisplacementDataLocation());
				load.setBeginTemp(null);
				load.setEndTemp(null);
				displacement.setBeginTemp(null);
				displacement.setEndTemp(null);
			}

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
		if(globalLoadDataFilterTextField.getText().equals(""))
		{
			SPSettings.globalLoadDataLowpassFilter = null;
		}
		else
		{
			SPSettings.globalLoadDataLowpassFilter = new LowPass(globalLoadDataFilterTextField.getDouble() * 1000);
		}
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
		if(globalDisplacementFilterTextField.getText().equals(""))
		{
			SPSettings.globalDisplacementDataLowpassFilter = null;
		}
		else
		{
			SPSettings.globalDisplacementDataLowpassFilter = new LowPass(globalDisplacementFilterTextField.getDouble() * 1000);
		}
		
		renderSampleResults();
		renderCharts();
	}
	
	@FXML
	private void removeGlobalDisplacementDataFilterButtonFired(){
		SPSettings.globalDisplacementDataLowpassFilter = null;
		renderSampleResults();
		renderCharts();
	}
	
	@FXML
	private void saveSessionButtonFired()
	{
		sampleDirectoryGUI.showSampleDirectoryPane();
		sampleDirectoryGUI.fireSessionsToggleButton();
		String name = Dialogs.getStringValueFromUser("Please provide a session name");
		if(name.equals(""))
		{
			return;
		}
		
		File sessionFile = new File((new File(treeViewHomePath)).getParent(), "Sessions/" + name + ".session");
		if(sessionFile.exists())
		{
			Dialogs.showAlert("Session name already used!", stage);
			return;
		}
		
		if(!sessionFile.getParentFile().exists())
			sessionFile.getParentFile().mkdir();
		Session session = new Session();
		SPOperations.writeStringToFile(session.getJSONString(this), sessionFile.getPath());
		sampleDirectoryGUI.fillSessionsListView();
	}

	@FXML
	private void saveTrimmedDataClicked() {
		if(Dialogs.showConfirmationDialog("Confirm", "Please confirm", "Are you sure you want to save this trim to disk?", stage)) {
			getCheckedSamples().forEach(sample -> {
				sample.DataFiles.forEach(df -> {
					df.dataSubsets.forEach(ds -> {
						if(ds.getEndTemp() != null) {
							ds.setEnd(ds.getEndTemp());
							// ds.setEnd(ds.getEndTemp()  * (ds.Data.getOriginalDataPoints() - 1) / (ds.Data.getUserDataPoints() - 1));
						}
						if(ds.getBeginTemp() != null) {
							ds.setBegin(ds.getBeginTemp());
							// ds.setBegin(ds.getBeginTemp() * (ds.Data.getOriginalDataPoints() - 1) / (ds.Data.getUserDataPoints() - 1));
						}
					});
				});
				sample.writeSampleToFile(sample.loadedFromLocation.getAbsolutePath());
			});
		}

	}

	@FXML
	private void setTrimStepButtonClicked() {
		double newTrimStep = Dialogs.getDoubleValueFromUser(String.format("Please enter a new step percentage (current percentage is %.2f%%)", trimStep * 100), "%");
		if(newTrimStep > 0 && newTrimStep < 100) {
			trimStep = newTrimStep / 100.0;
		} else {
			Dialogs.showErrorDialog("Invalid trim step. Must be between 0 and 100", stage);
		}
	}

	@FXML
	private void newReferenceClicked() {
		showReferencePage(Optional.empty());
	}

	private void showReferencePage(Optional<ReferenceSample> sample) {
		Stage anotherStage = new Stage();
		try {
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/viewer/GUI/NewReference.fxml"));
			Scene scene = new Scene(root1.load());
			anotherStage.setScene(scene);
			anotherStage.initOwner(stage);
			anotherStage.getIcons().add(SPSettings.getRELLogo());
			anotherStage.setTitle("New Stress-Strain Reference");
			anotherStage.initModality(Modality.WINDOW_MODAL);
			NewReferenceController c = root1.<NewReferenceController>getController();
			c.stage = anotherStage;
			System.out.println("setting sample");
			c.clickedReferenceSample = sample;
			if(trueRadioButton.isSelected()) {
				c.xyDatas = chartsGUI.getStressStrainSerie(Optional.empty());
			}

			c.renderFromProps();


			anotherStage.showAndWait();

			refreshReferencesFromDisk();
			renderCharts();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	private void refreshReferenceSamplesClicked() {
		refreshReferencesFromDisk();
		renderCharts();
	}

	@FXML
	private void importReferenceButtonClicked() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select Reference To Import");
		FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("JSON File (*.json)","*.json");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		File file = fileChooser.showOpenDialog(stage);

		if (file != null) {
			ReferenceSample s = ReferenceSample.createFromJson(SPOperations.readStringFromFile(file.getPath()), file.getPath());
			if(s == null) {
				Dialogs.showErrorDialog("Reference failed to read!", stage);
			} else {
				// save to app data.
				NewReferenceController.saveReference(s, s.getName());
			}

			refreshReferencesFromDisk();
			renderCharts();
		}
	}

	@FXML
	private void exportReferenceButtonClicked() {
		if(currentReferencesListView.getSelectionModel().getSelectedItem() == null) {
			Dialogs.showErrorDialog("No reference selected for export", stage);
		} else {
			ReferenceSample s = currentReferencesListView.getSelectionModel().getSelectedItem();
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Export Reference");
			fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json (*.json)", "*.json"));
			fileChooser.setInitialFileName(s.getName() + ".json");
			File file = fileChooser.showSaveDialog(stage);
			if (file != null) {
				SPOperations.writeStringToFile(s.getJson(), file.getPath());
			}
		}
	}

	@FXML
	private void deleteReferenceButtonClicked() {
		if(currentReferencesListView.getSelectionModel().getSelectedItem() == null) {
			Dialogs.showErrorDialog("No reference selected to delete", stage);
		} else {
			boolean result = Dialogs.showConfirmationDialog("Warning", "Confirm Delete", "Are you sure you want to delete this reference?", stage);
			if(result) {
				File f = new File(currentReferencesListView.getSelectionModel().getSelectedItem().getLoadedPath());
				if(f.exists()) {
					f.delete();
					refreshReferencesFromDisk();
					renderCharts();
				} else {
					Dialogs.showErrorDialog("Failed to delete, file not found!", stage);
				}
			} else {
				// no-op
				System.out.println("Delete cancelled.");
			}
		}
	}

	@FXML
	public void newGroupButtonClicked() {
		createEditSampleGroup(Optional.empty());
	}

	private ChangeListener<Boolean> createSampleGroupChangeListener() {
		return new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//							renderROIChoiceBox(); //new command
//							renderROISelectionModeChoiceBox();
				realCurrentSamplesListView.refresh();
				renderCharts();
			}
		};
	}


	private void createEditSampleGroup(Optional<SampleGroup> sg) {
		StackPane secondaryLayout = new StackPane();

		Scene secondScene = new Scene(secondaryLayout, 500, 600);

		// New window (Stage)
		Stage newWindow = new Stage();
		// newWindow.setAlwaysOnTop(true);
		newWindow.setScene(secondScene);


		newWindow.show();


		newWindow.setTitle(sg.isPresent() ? "Edit" : "New" + " Group");

		GridPane grid = new GridPane();

		secondaryLayout.getChildren().add(grid);



		TextField nameField = new TextField();
		sg.ifPresent(g -> nameField.setText(g.groupName));

		grid.add(new Label("Name"), 0, 0);
		grid.add(nameField, 1, 0);

		grid.add(new Label("Color"), 0, 1);
		ColorPicker picker = new ColorPicker();
		if(sg.isPresent()) {
			picker.setValue(sg.get().color);
		} else {
			picker.setValue(Color.valueOf("#4d66cc"));
		}
		grid.add(picker, 1, 1);

		Button saveButton = new Button();
		saveButton.setText("Save");
		saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(sg.isPresent()) {
					sg.get().groupName = nameField.getText();
					sg.get().color = picker.getValue();
					sampleGroupsList.refresh();
					newWindow.close();
					realCurrentSamplesListView.refresh();
					renderCharts();
				} else {
					SampleGroup g = new SampleGroup(nameField.getText(), picker.getValue(), createSampleGroupChangeListener());

					//g.selectedProperty().addListener();

					sampleGroupsList.getItems().add(g);
					newWindow.close();
				}

			}
		});

		HBox hb2 = new HBox();
		hb2.setSpacing(10);

		// grid.add(saveButton, 1, 2);

		sg.ifPresent(g -> {
			Button deleteButton = new Button("Delete");
			deleteButton.setTextFill(Paint.valueOf("red"));
			deleteButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					sampleGroupsList.getItems().remove(g);
					newWindow.close();
					realCurrentSamplesListView.refresh();
					renderCharts();
				}
			});
			hb2.getChildren().add(deleteButton);
			// grid.add(deleteButton, 2, 2);
		});

		hb2.getChildren().add(saveButton);

		grid.add(hb2, 1, 2);

		grid.setAlignment(Pos.TOP_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPrefHeight(400);
		grid.setPadding(new Insets(10));
		AnchorPane.setBottomAnchor(grid, 0.0);
		AnchorPane.setLeftAnchor(grid, 0.0);
		AnchorPane.setTopAnchor(grid, 0.0);
		AnchorPane.setRightAnchor(grid, 0.0);
	}

	String separatorsToSystem(String res) {
		if (res==null) return null;
		if (File.separatorChar=='\\') {
			// From Windows to Linux/Mac
			return res.replace('/', File.separatorChar);
		} else {
			// From Linux/Mac to Windows
			return res.replace('\\', File.separatorChar);
		}
	}
	
	public void applySession(File sessionFile)
	{
		renderBlock = true;
		Session session = Session.getSessionFromJSONString(SPOperations.readStringFromFile(sessionFile.getPath()));
		removeChartTypeListeners();
		realCurrentSamplesListView.getItems().removeListener(sampleListChangedListener);
		realCurrentSamplesListView.getItems().clear();
		
		if(session.globalDisplacementLowpassValue == null)
		{
			SPSettings.globalDisplacementDataLowpassFilter = null;
		}
		else{
			SPSettings.globalDisplacementDataLowpassFilter = new LowPass(session.globalDisplacementLowpassValue);
		}
		
		if(session.globalLoadLowpassValue == null)
		{
			SPSettings.globalLoadDataLowpassFilter = null;
		}
		else{
			SPSettings.globalLoadDataLowpassFilter = new LowPass(session.globalLoadLowpassValue);
		}
		
		
		
		for(SampleSession sampleSession : session.samplePaths)
		{
			Optional<Sample> sampleOptional = Optional.empty();
			try {
				sampleOptional = Optional.of(SPOperations.loadSample(treeViewHomePath + separatorsToSystem(sampleSession.path)));
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			if(!sampleOptional.isPresent())
				continue;
			
			Sample sample = sampleOptional.get();
			addSampleToList(sample);
			// this re-computes for every sample load... could optimize this into different for loop...
			renderDefaultSampleResults(); //Need to initialize sample.results
			if(sample != null)
			{
				LoadDisplacementSampleResults result = sample.getResults().get(0);

				sample.getResults().clear();
				sample.getResults().addAll(LoadDisplacementSampleResults.createResults(sample, sampleSession.loadLocation, sampleSession.displacementLocation));

				sample.setSelected(sampleSession.checked);
				result.getCurrentDisplacementDatasubset().setBeginTemp(sampleSession.displacementTempTrimBeginIndex);
				result.getCurrentDisplacementDatasubset().setEndTemp(sampleSession.displacementTempTrimEndIndex);
				result.getCurrentLoadDatasubset().setBeginTemp(sampleSession.loadTempTrimBeginIndex);
				result.getCurrentLoadDatasubset().setEndTemp(sampleSession.loadTempTrimEndIndex);
				sample.setBeginROITime(sampleSession.beginROITime);
				sample.setEndROITime(sampleSession.endROITime);
			}
			
		}
		renderSampleResults();

		for(SampleGroupSession groupSession : session.sampleGroups) {
			SampleGroup group = new SampleGroup(groupSession.name, Color.valueOf(groupSession.color), createSampleGroupChangeListener());
			group.groupSamples = groupSession.samplePaths.stream().map(path -> {
				return realCurrentSamplesListView.getItems().stream().filter(sam -> Session.getSamplePathForId(sam).equals(path)).findFirst();
			}).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
			sampleGroupsList.getItems().add(group);
		}
		
		

		loadDisplacementCB.setSelected(session.chartingAreaSession.loadDisplacementSelected);		
		
		if(session.chartingAreaSession.isEnglish)
			englishRadioButton.setSelected(true);
		else
			metricRadioButton.setSelected(true);
		
		if(session.chartingAreaSession.isEngineering)
			engineeringRadioButton.setSelected(true);
		else
			trueRadioButton.setSelected(true);
		
		timeScaleToggleGroup.getToggles().stream().filter(t -> ((RadioButton)t).getText().equals(session.chartingAreaSession.timeUnit)).findFirst().get().setSelected(true);
		
		sampleListChangedListener.onChanged(null); //This resets all the checked charts if load displacement sample detected
		realCurrentSamplesListView.getItems().addListener(sampleListChangedListener);
		addChartTypeListeners();
		
		session.chartingAreaSession.checkedCharts.stream().forEach(s -> displayedChartListView.getCheckModel().check(s));
		
		renderBlock = false;
		
		ROI.beginROITime = session.roiSession.beginTime;
		ROI.endROITime = session.roiSession.endTime;
		if(session.roiSession.selectedROISample != null){
			Sample s = realCurrentSamplesListView.getItems().get(getSampleIndexByName(session.roiSession.selectedROISample));
			roiSelectionModeChoiceBox.getSelectionModel().select(s);
		}
		choiceBoxRoi.getSelectionModel().select(session.roiSession.selectedData);
		holdROIAnnotationsCB.setSelected(session.roiSession.holdROIAnnotations);
		zoomToROICB.setSelected(session.roiSession.zoomToROI);
		renderCharts();
	}

	public String getDisplayedTimeUnit(){
		return ((RadioButton)timeScaleToggleGroup.getSelectedToggle()).getText();
	}
	
	private void renderDefaultSampleResults(){
		boolean loadDisplacementOnly = false;
		for(Sample sample : realCurrentSamplesListView.getItems()){
			if(!(sample instanceof HopkinsonBarSample))
				loadDisplacementOnly = true; //
			if(sample.getResults().isEmpty()){
				sample.getResults().clear();
				sample.getResults().addAll(LoadDisplacementSampleResults.createResults(
						sample,
						sample.getDefaultStressLocation(),
						sample.getDefaultStrainLocation()
						)
				);
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
		// I beleive this should be getCheckedSamples instead of all the loaded samples.
		for(Sample s : getCheckedSamples()){
			for(LoadDisplacementSampleResults result: s.getResults())
			{
				if(s.getBeginROITime() == -1)
					s.setBeginROITime(0);
				if(s.getEndROITime() == -1 || s.getEndROITime() > result.time[result.time.length -1])
					s.setEndROITime(result.time[result.time.length - 1]);
			}

		}
	}


	private ChangeListener<Boolean> referenceCheckedListener = new ChangeListener<Boolean>() {
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
			renderCharts();
		}
	};

	public void addSampleToList(Sample sampleToAdd){
		if(sampleToAdd != null){
			sampleToAdd.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					//setROITimeValuesToMaxRange();
					System.out.println("Checked changed!");
					if(newValue) {
						renderSampleResults(Arrays.asList(sampleToAdd));
					}
					renderROIChoiceBox(); //new command
					renderROISelectionModeChoiceBox();
					renderCharts();
				}
			});
			SPTracker.track(SPTracker.surepulseViewerCategory, "Sample Analyzed");
			realCurrentSamplesListView.getItems().add(sampleToAdd);
		}
		else{
			System.out.println("Failed to load the sample.");
		}
	}
	
	public List<String> getCheckedCharts()
	{
		return displayedChartListView.getCheckModel().getCheckedItems();
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

		if(renderBlock)
			return;
		
		chartAnchorPane.getChildren().clear();
		chartAnchorPane.setStyle("-fx-background-color: white;");
		renderROIResults();
		ArrayList<LineChart<Number, Number>> charts = new ArrayList<LineChart<Number, Number>>();
		if(vBoxHoldingCharts.getChildren().size() > 1){
			//vBoxHoldingCharts holds the xyChart pane and optionally the video dialog.
			if(displayedChartListView.getCheckModel().getCheckedItems().size() == 0)
			{
				// All the samples have been unchecked. The video/images need to be cleared.
				videoCorrelationGUI.removeVideoControls();
				videoCorrelationGUI.imagePaths = new ArrayList<>();
				imageView.setImage(null);
				return;
			}
			
			if(getCheckedSamples().size() != 1)
			{
				// Video only supports one sample. Clear and remove if not 1
				videoCorrelationGUI.removeVideoControls();
				videoCorrelationGUI.imagePaths = new ArrayList<>();
				imageView.setImage(null);
				return;
			}
			

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
					for(LoadDisplacementSampleResults results: s.getResults())
					{
						if(!(results.isFaceForceGraphable())){
							forceIsApplicable = false;
						}
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
			
			removeChartTypeListeners();
			displayedChartListView.getCheckModel().clearChecks();
			displayedChartListView.getSelectionModel().clearSelection();
			displayedChartListView.getItems().clear();

			displayedChartListView.getItems().add(stressVsStrainName);
			displayedChartListView.getItems().add(stressVsTimeName);
			displayedChartListView.getItems().add(strainVsTimeName);
			displayedChartListView.getItems().add(strainRateVsTimeName);
			addChartTypeListeners();
			
			boolean forceIsApplicable = true;
			for(Sample s : getCheckedSamples()){
				for(LoadDisplacementSampleResults results : s.getResults()) {
					if(!(results.getCurrentLoadDatasubset() instanceof TransmissionPulse && results.getCurrentDisplacementDatasubset() instanceof ReflectedPulse)){
						forceIsApplicable = false;
					}
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
			if(!(s instanceof HopkinsonBarSample || s instanceof TorsionSample || s instanceof BrazilianTensileSample))
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
			return;
		}
		//now there's at least one checked sample

		String chartOfInterest = "";

		if(choiceBoxRoi.getSelectionModel().getSelectedItem() == null){
			//nothing selected, look at xyChart
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
						SPOperations.round(Converter.psiFromKsi(Converter.ksiFromPa(integral)), 4)));

				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgMax),4)));
				
				averageKValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgKVal), 4)));
				averageNValueTF.setText(Double.toString(SPOperations.round(avgNVal, 4)));
			}
			else{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avg),4)));

				tbAvgIntegralValue.setText(Double.toString(SPOperations.round(Converter.MjFromJ(integral), 4)));

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

			if(englishRadioButton.isSelected())
			{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(avg),4)));
				tbAvgIntegralValue.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(integral), 4)));
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(avgMax),4)));
			} else {
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.mmFromM(avg),4)));
				tbAvgIntegralValue.setText(Double.toString(SPOperations.round(Converter.mmFromM(integral), 4)));
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.mmFromM(avgMax),4)));
			}
			
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

			if(englishRadioButton.isSelected())
			{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(avg),4)));
				tbAvgIntegralValue.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(integral), 4)));
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.InchFromMeter(avgMax),4)));
			} else {
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.mmFromM(avg),4)));
				tbAvgIntegralValue.setText(Double.toString(SPOperations.round(Converter.mmFromM(integral), 4)));
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.mmFromM(avgMax),4)));
			}
			
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

		if(chartOfInterest.equals("Stress Vs Strain")) {
			if(isEnglish.get()) {
				roiIntegralLabel.setText("Toughness (Lbf/inch^2)");
			} else {
				roiIntegralLabel.setText("Toughness (MJ/m^3)");
			}
		} else {
			roiIntegralLabel.setText("Average Integral");
		}
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
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getDisplacementRateTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getDisplacementTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getDisplacementTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getLoadTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getLoadTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getLoadDisplacementChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getLoadDisplacementChart();
		
		if(showROIOnChart){
			chart.clearVerticalMarkers();
			Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
			if (roiSample == null || roiSample.placeHolderSample) {
				if (getCheckedSamples().size() == 1) {
					Sample s = getCheckedSamples().get(0);
					LoadDisplacementSampleResults result = s.getResults().get(0);
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, ROI.beginROITime);
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, ROI.endROITime);

					if (englishRadioButton.isSelected()) {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(result.getDisplacement("in")[beginIndex],
										result.getDisplacement("in")[endIndex]),
								Color.GREEN);
					} else {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(result.getDisplacement("mm")[beginIndex],
										result.getDisplacement("mm")[endIndex]),
								Color.GREEN);
					}

				}
			} 
			else {
				for(Sample s : getCheckedSamples()){
					for(LoadDisplacementSampleResults result : s.getResults()) {
						int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, s.getBeginROITime());
						int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, s.getEndROITime());

						if (englishRadioButton.isSelected()) {
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getDisplacement("in")[beginIndex],
											0),
									getSampleChartColor(s));
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getDisplacement("in")[endIndex],
											0),
									getSampleChartColor(s));
						} else {
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getDisplacement("mm")[beginIndex],
											0),
									getSampleChartColor(s));
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getDisplacement("mm")[endIndex],
											0),
									getSampleChartColor(s));
						}
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
					
					double longestStrain = -Double.MAX_VALUE;
					LoadDisplacementSampleResults resultWithLongestStrain = null;
					for(Sample s : getCheckedSamples())
					{
						for(LoadDisplacementSampleResults result : s.getResults())
						{
							if(result.getCurrentDisplacementDatasubset().getTrimmedData()[result.getCurrentDisplacementDatasubset().getTrimmedData().length - 1] > longestStrain)
							{
								longestStrain = result.getCurrentDisplacementDatasubset().getTrimmedData()[result.getCurrentDisplacementDatasubset().getTrimmedData().length - 1];
								resultWithLongestStrain = result;
							}
						}

					}
					
					if (englishRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(resultWithLongestStrain.getDisplacement("in"),
								displacementValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(resultWithLongestStrain.getDisplacement("mm"),
								displacementValue);
					double timeValue = resultWithLongestStrain.time[index];

					TrimDatasetOption.Option selectedOption = trimDatasetChoiceBox.getSelectionModel().getSelectedItem().getOption();
					if (trimBeginRadioButton.isSelected()) {
						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();
						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setBeginTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}
								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setBeginTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}
							});
						});

					} else if (trimEndRadioButton.isSelected()) {

						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();

						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setEndTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}
								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setEndTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}
							});
						});

					}

					renderSampleResults();
				} else if (regionOfInterestTitledPane.isExpanded()) {

					Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiSample == null || roiSample.placeHolderSample) {
						// placeHolderSample is not a real sample, just a placeholder to signify all samples are in ROI.
						if (getCheckedSamples().size() != 1)
							return;
						Sample sam = getCheckedSamples().get(0);
						LoadDisplacementSampleResults result = sam.getResults().get(0);
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (englishRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(result.getDisplacement("in"),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(result.getDisplacement("mm"),
									strainValue);

						double timeValue = result.time[index];

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
						LoadDisplacementSampleResults result = roiSample.getResults().get(0);
						if (englishRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									result.getDisplacement("in"), strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									result.getDisplacement("mm"), strainValue);

						if (index != -1) {
							double timeValue = result.time[index];
							if (radioSetBegin.isSelected()) {
								if (timeValue > 0 && timeValue < roiSample.getEndROITime()) {
									roiSample.setBeginROITime(timeValue);
								}
							} else if (radioSetEnd.isSelected()) {
								if (timeValue < result.time[result.time.length - 1]
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

		addChartEditingFeatures(chart);

		return chart;
	}

	private LineChartWithMarkers<Number, Number> getEnergyTimeChart() {
		// TODO Auto-generated method stub
		return null;
	}

	private LineChartWithMarkers<Number, Number> getFaceForceTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getFaceForceTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getStrainRateTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getStrainRateTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getStrainTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getStrainTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private void renderSampleResults(List<Sample> samples) {
		//renders the result object for each sample
		samples.stream().forEach(sample -> sample.DataFiles.getAllDatasets().forEach(ds -> ds.invalidateResult()));

		samples.stream()
				.parallel() // empirically provides 3-4X speedup.
				.forEach(s -> s.getResults().stream().forEach(LoadDisplacementSampleResults::render));
		setROITimeValuesToMaxRange();
	}

	private void renderSampleResults(){
		renderSampleResults(getCheckedSamples());
	}

	private LineChartWithMarkers<Number, Number> getStressTimeChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getStressTimeChart();
		addROIFunctionalityToTimeChart(chart);
		addXYListenerToChart(chart);
		addChartEditingFeatures(chart);
		return chart;
	}

	private LineChartWithMarkers<Number, Number> getStressStrainChart() {
		LineChartWithMarkers<Number, Number> chart = chartsGUI.getStressStrainChart();
		
		// The ROI and click listener are different for Stress/Strain because Strain is on the x axis instead of time.
		if(showROIOnChart){
			chart.clearVerticalMarkers();
			Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
			if (roiSample == null || roiSample.placeHolderSample) {
				if (getCheckedSamples().size() == 1) {
					Sample s = getCheckedSamples().get(0);
					LoadDisplacementSampleResults results = s.getResults().get(0);
					int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(results.time, ROI.beginROITime);
					int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(results.time, ROI.endROITime);

					if (engineeringRadioButton.isSelected()) {
						chart.addVerticalRangeMarker(
								new Data<Number, Number>(results.getEngineeringStrain()[beginIndex],
										results.getEngineeringStrain()[endIndex]),
								Color.GREEN);
					} else {
						chart.addVerticalRangeMarker(new Data<Number, Number>(results.getTrueStrain()[beginIndex],
								results.getTrueStrain()[endIndex]), Color.GREEN);
					}

				}
			}
			else{
				for(Sample s : getCheckedSamples()){
					for(LoadDisplacementSampleResults result : s.getResults())
					{
						int beginIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, s.getBeginROITime());
						int endIndex = SPOperations.findFirstIndexGreaterorEqualToValue(result.time, s.getEndROITime());
						if (engineeringRadioButton.isSelected()) {
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getEngineeringStrain()[beginIndex],
											0),
									getSampleChartColor(s));
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getEngineeringStrain()[endIndex],
											0),
									getSampleChartColor(s));
						} else {
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getTrueStrain()[beginIndex],
											0),
									getSampleChartColor(s));
							chart.addVerticalValueMarker(
									new Data<Number, Number>(result.getTrueStrain()[endIndex],
											0),
									getSampleChartColor(s));
						}
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
					
					//Get the sample with the largest rawStrainData value.
					double longestStrain = -Double.MAX_VALUE;
					LoadDisplacementSampleResults resultsWithLongestStrain = null;
					for(Sample s : getCheckedSamples())
					{
						for(LoadDisplacementSampleResults results : s.getResults()) {
							if(results.getCurrentDisplacementDatasubset().getTrimmedData()[results.getCurrentDisplacementDatasubset().getTrimmedData().length - 1] > longestStrain)
							{
								longestStrain = results.getCurrentDisplacementDatasubset().getTrimmedData()[results.getCurrentDisplacementDatasubset().getTrimmedData().length - 1];
								resultsWithLongestStrain = results;
							}
						}

					}
					
					if (trueRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(resultsWithLongestStrain.getTrueStrain(),
								strainValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(resultsWithLongestStrain.getEngineeringStrain(),
								strainValue);

					double timeValue = resultsWithLongestStrain.time[index];
					TrimDatasetOption.Option selectedOption = trimDatasetChoiceBox.getSelectionModel().getSelectedItem().getOption();
					if (trimBeginRadioButton.isSelected()) {
						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();
						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setBeginTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}
								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setBeginTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}
							});
						});

					} else if (trimEndRadioButton.isSelected()) {

						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();

						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);


						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setEndTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}
								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setEndTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}
							});
						});

					}
					renderSampleResults();
				} else if (regionOfInterestTitledPane.isExpanded()) {

					Sample roiSample = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiSample == null || roiSample.placeHolderSample) {
						if (getCheckedSamples().size() != 1)
							return;
						Sample sam = getCheckedSamples().get(0);
						LoadDisplacementSampleResults result = sam.getResults().get(0);
						double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
						int index = 0;

						if (trueRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(result.getTrueStrain(),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(result.getEngineeringStrain(),
									strainValue);

						double timeValue = result.time[index];
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

						LoadDisplacementSampleResults results = roiSample.getResults().get(0);
						if (trueRadioButton.isSelected())
							index = SPOperations.findFirstIndexGreaterorEqualToValue(results.getTrueStrain(),
									strainValue);
						else
							index = SPOperations.findFirstIndexGreaterorEqualToValue(
									results.getEngineeringStrain(), strainValue);
						if (index != -1) {
							double timeValue = results.time[index];
							if (radioSetBegin.isSelected()) {
								if (timeValue > 0 && timeValue < roiSample.getEndROITime()) {
									roiSample.setBeginROITime(timeValue);
								}
							} else if (radioSetEnd.isSelected()) {
								if (timeValue < results.time[results.time.length - 1]
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
		addChartEditingFeatures(chart);

		return chart;
	}

	private void addChartEditingFeatures(LineChartWithMarkers<Number, Number> chart) {
		chart.lookup(".chart-title").setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(event.getClickCount() == 2) {
					String newTitle = Dialogs.getStringValueFromUser("Please Enter New Chart Title");
					if(!newTitle.equals("")) {
						chart.setTitle(newTitle);
					}
				}
			}
		});

		chart.getXAxis().setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(event.getClickCount() == 2) {
					try {
						Double d = Dialogs.getDoubleValueFromUser("Please Enter New Axis Min Value", "");
						chart.getXAxis().setAutoRanging(false);

						((NumberAxis)chart.getXAxis()).setLowerBound(d);
					} catch(NumberFormatException e) {

					}

					try {
						Double d = Dialogs.getDoubleValueFromUser("Please Enter New Axis Max Value", "");
						chart.getXAxis().setAutoRanging(false);

						((NumberAxis)chart.getXAxis()).setUpperBound(d);
					} catch(NumberFormatException e) {

					}
				}
			}
		});

		chart.getYAxis().setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(event.getClickCount() == 2) {
					try {
						Double d = Dialogs.getDoubleValueFromUser("Please Enter New Axis Min Value", "");
						chart.getYAxis().setAutoRanging(false);
						((NumberAxis)chart.getYAxis()).setLowerBound(d);

					} catch(NumberFormatException e) {

					}

					try {
						Double d = Dialogs.getDoubleValueFromUser("Please Enter New Axis Max Value", "");
						chart.getYAxis().setAutoRanging(false);

						((NumberAxis)chart.getYAxis()).setUpperBound(d);
					} catch(NumberFormatException e) {

					}
				}
			}
		});
	}

	private void addROIFunctionalityToTimeChart(LineChartWithMarkers<Number, Number> chart){
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
					TrimDatasetOption.Option selectedOption = trimDatasetChoiceBox.getSelectionModel().getSelectedItem().getOption();
					if (trimBeginRadioButton.isSelected()) {
						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();
						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								// I'm not certain that it only should be adjusting the load and displacement arrays, and not all the DataSubsets.
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setBeginTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}

								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setBeginTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}

							});
						});

					} else if (trimEndRadioButton.isSelected()) {
						Sample selectedSample = trimSampleComboBox.getSelectionModel().getSelectedItem();

						List<Sample> samples = selectedSample.placeHolderSample ? getCheckedSamples() : Arrays.asList(selectedSample);

						samples.forEach(sample -> {
							sample.getResults().forEach(result -> {
								DataSubset displacementData = sample.getDataSubsetAtLocation(result.getDisplacementDataLocation());
								DataSubset loadData = sample.getDataSubsetAtLocation(result.getLoadDataLocation());
								if(TrimDatasetOption.shouldTrimDisplacement(selectedOption)) {
									displacementData.setEndTempFromTimeValue(timeValue + displacementData.getModifiedTime()[displacementData.getBegin()]);
								}
								if(TrimDatasetOption.shouldTrimLoad(selectedOption)) {
									loadData.setEndTempFromTimeValue(timeValue + loadData.getModifiedTime()[loadData.getBegin()]);
								}
							});
						});

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
							LoadDisplacementSampleResults results = roiMode.getResults().get(0);
							if (timeValue < results.time[results.time.length - 1]
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
				xValueLabel.setText("X: " + SPOperations.round(xValue, 8));
				yValueLabel.setText("Y: " + SPOperations.round(yValue,8));
			}

		});
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



	@FXML
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


	private double getLowestMaxTime() {
		double minTime = Double.MAX_VALUE;
		for(Sample s : getCheckedSamples()){
			for(LoadDisplacementSampleResults results: s.getResults()) {
				if(results.time[results.time.length-1] < minTime){
					minTime = results.time[results.time.length-1];
				}
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

	private void renderTrimSampleChoiceBox() {
		trimSampleComboBox.getItems().clear();
		CompressionSample allSample = new CompressionSample();
		allSample.placeHolderSample = true;
		allSample.setName("All Samples");

		trimSampleComboBox.getItems().add(allSample);
		trimSampleComboBox.getItems().addAll(getCheckedSamples());

		trimSampleComboBox.getSelectionModel().select(allSample);
	}

	private void renderTrimDatasetChoiceBox() {
		trimDatasetChoiceBox.getItems().clear();
		TrimDatasetOption both = new TrimDatasetOption(TrimDatasetOption.Option.BOTH, "Both");
		if(loadDisplacementCB.isSelected()) {
			trimDatasetChoiceBox.getItems().addAll(
					both,
					new TrimDatasetOption(TrimDatasetOption.Option.LOAD, "Load"),
					new TrimDatasetOption(TrimDatasetOption.Option.DISPLACEMNT, "Displacement")
			);
		} else {
			trimDatasetChoiceBox.getItems().addAll(
					both,
					new TrimDatasetOption(TrimDatasetOption.Option.LOAD, "Stress"),
					new TrimDatasetOption(TrimDatasetOption.Option.DISPLACEMNT, "Strain")
			);
		}
		trimDatasetChoiceBox.getSelectionModel().select(both);
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

	public Color getSampleChartColor(Sample s){
		int index = getSampleIndex(s);
		if(index != -1) {
			Sample selectedTrimSample = trimSampleComboBox.getSelectionModel().getSelectedItem();
			if(selectedTrimSample == null || selectedTrimSample.placeHolderSample || selectedTrimSample == s) {
				return ChartsGUI.getColor(getSampleIndex(s), 0, 1, false,1 );
			} else {
				System.out.println("setting low opacity!");
				return ChartsGUI.getColor(getSampleIndex(s), 0, 1, false, .5); // this opacity value is duped.
			}
		} else {
			return Color.color(0, 0, 0);
		}
	}

}
