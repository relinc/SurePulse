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
import org.jcodec.api.awt.SequenceEncoder;
import com.sun.javafx.charts.Legend; //KEEP
import com.sun.javafx.charts.Legend.LegendItem; //KEEP

import net.relinc.processor.application.FileFX;
import net.relinc.processor.application.LineChartWithMarkers;
import net.relinc.processor.data.DataFile;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.Descriptor;
import net.relinc.processor.data.ModifierFolder.Modifier;
import net.relinc.processor.sample.CompressionSample;
import net.relinc.processor.sample.HopkinsonBarSample;
import net.relinc.processor.sample.LoadDisplacementSample;
import net.relinc.processor.sample.LoadDisplacementSampleResults;
import net.relinc.processor.sample.Sample;
import net.relinc.processor.sample.SampleGroup;
import net.relinc.processor.sample.ShearCompressionSample;
import net.relinc.processor.sample.TensionRectangularSample;
import net.relinc.processor.sample.TensionRoundSample;
import net.relinc.processor.staticClasses.*;
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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import net.lingala.zip4j.exception.ZipException;



public class HomeController {
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
	@FXML CheckBox applyFiltersCB;
	@FXML CheckBox zoomToROICB;
	@FXML CheckBox applyDataFittersCB;
	@FXML CheckBox applyZeroCheckBox;
	
	
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
	@FXML Label averageMaxValueLabel;
	@FXML Label xValueLabel;
	@FXML Label yValueLabel;
	
	
	boolean mouseHoveringOverSample = false;
	boolean exportMenuOpen;


	public Stage stage;

	ToggleGroup englishMetricGroup = new ToggleGroup();
	ToggleGroup engineeringTrueGroup = new ToggleGroup();
	ToggleGroup exportEnglishMetricGroup = new ToggleGroup();
	ToggleGroup exportEngineeringTrueGroup = new ToggleGroup();
	ToggleGroup roiToggleGroup = new ToggleGroup();
	ToggleGroup timeScaleToggleGroup = new ToggleGroup();
	


	//CheckListView<String> currentSamplesListView = new CheckListView<String>();
	CheckListView<String> displayedChartListView = new CheckListView<String>();
	
	ListView<Sample> realCurrentSamplesListView = new ListView<Sample>();
	
	//ObservableList<Sample> currentSamples = FXCollections.observableList(new ArrayList<Sample>());

	RegionOfInterest ROI = new RegionOfInterest();
	MetricMultiplier timeUnits = new MetricMultiplier();
	
	int DataPointsToShow = 2000;
	boolean showROIOnChart = false;
	double widthOfLeftPanel;
	
	//*********Video correlation Region****************
	Button openImagesButton = new Button("Choose Images");
	ScrollBar imageScrollBar = new ScrollBar();
	Label imageShownLabel = new Label("Image.jpg");
	ImageView imageView = new ImageView();
	LineChartWithMarkers<Number, Number> imageMatchingChart;// = new LineChart<Number, Number>();
	Button saveVideoButton = new Button("Save Video");
	//*******************

	//********Region for GUI for right option pane to open
	AnchorPane optionPane = new AnchorPane();
	TreeView<FileFX> sampleDirectoryTreeView = new TreeView<FileFX>();
	Button changeDirectoryButton = new Button("Change Directory");
	Button refreshDirectoryButton = new Button("", SPOperations.getIcon("/net/relinc/viewer/images/refreshIcon.png"));
	Button xButton = new Button("X");
	Button addSelectedSampleButton = new Button("Add Selected Sample(s)");
	//*******
	private String treeViewHomePath = SPSettings.Workspace.getPath() + "/Sample Data";

	//********Region for GUI for export pane to open
	private TreeItem<String> sampleGroupRoot;
	private ArrayList<SampleGroup> sampleGroups = new ArrayList<SampleGroup>();
	private SampleGroup currentSelectedSampleGroup;
	private TextField tbSampleGroup = new TextField();
	private Button buttonCreateSampleGroup = new Button("Create Group");
	private TreeView<String> treeViewSampleGroups = new TreeView<String>();
	private Button buttonAddSampleToGroup = new Button("Add Samples to Group");
	private Button buttonExportData = new Button("Export To Excel");
	private Button buttonExportCSV = new Button("Export CSV");
//	private RadioButton exportEngineeringRadioButton = new RadioButton("Engineering");
//	private RadioButton exportTrueRadioButton = new RadioButton("True");
//	private RadioButton exportMetricRadioButton = new RadioButton("Metric");
//	private RadioButton exportEnglishRadioButton = new RadioButton("English");
	private CheckBox includeSummaryPage = new CheckBox("Include Summary Page");
	//*******
	
	public void initialize(){
		//homeSplitPane.setStyle("-fx-box-border: transparent;");
		showSampleDirectoryButton.setGraphic(SPOperations.getIcon(SPOperations.folderImageLocation));
		changeDirectoryButton.setGraphic(SPOperations.getIcon(SPOperations.folderImageLocation));
		fillColorList();
		englishRadioButton.setToggleGroup(englishMetricGroup);
		metricRadioButton.setToggleGroup(englishMetricGroup);
		engineeringRadioButton.setToggleGroup(engineeringTrueGroup);
		trueRadioButton.setToggleGroup(engineeringTrueGroup);

//		exportEnglishRadioButton.setToggleGroup(exportEnglishMetricGroup);
//		exportMetricRadioButton.setToggleGroup(exportEnglishMetricGroup);
//		exportEngineeringRadioButton.setToggleGroup(exportEngineeringTrueGroup);
//		exportTrueRadioButton.setToggleGroup(exportEngineeringTrueGroup);
		
		radioSetBegin.setToggleGroup(roiToggleGroup);
		radioSetEnd.setToggleGroup(roiToggleGroup);
		
		secondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		milliSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		microSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		nanoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);
		picoSecondsRadioButton.setToggleGroup(timeScaleToggleGroup);

		//leftVBox.getChildren().add(1, currentSamplesListView);
		leftVBox.getChildren().add(1, realCurrentSamplesListView);
		//middleBottomVbox.getChildren().add(0,displayedChartListView);
		vboxForDisplayedChartsListView.getChildren().add(0,displayedChartListView);
		fillAllSamplesTreeView();
		//fillCurrentSamplesListView();

		setROITimeValuesToMaxRange();
		renderCharts();
		//and listen to the relevant events (e.g. when the selected indices or 
		//selected items change).
		//displayedChartListView.getCheckModel()

		//currentSamplesListView.getCheckModel().getCheckedItems().addListener(checkListener);
		
		choiceBoxRoi.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				renderROIResults();
			}
			
		});
		
		roiSelectionModeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Sample>() {
			@Override
			public void changed(ObservableValue<? extends Sample> observable, Sample oldValue, Sample newValue) {
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

		sampleDirectoryTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		//currentSamplesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		realCurrentSamplesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		addChartTypeListeners();
		
		//realCurrentSamplesListView.setCellFactory(CheckBoxListCell.forListView(Sample::selectedProperty));
		
//		realCurrentSamplesListView.setCellFactory(CheckBoxListCell.forListView(Sample::selectedProperty, new StringConverter<Sample>() {
//            @Override
//            public String toString(Sample object) {
//                return object.getName();
//            }
//
//            @Override
//            public Sample fromString(String string) {
//                return null;
//            }
//        }));
		
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
//						String dataDescription = "";
//						for(DataFile d : sam.DataFiles){
//							dataDescription += d.getName() + "\n";
//							for(DataSubset sub : d.dataSubsets){
//								dataDescription += "\t" + sub.name + "\n";
//							}
//						}
						String len = "Length: ";
						len += metricRadioButton.isSelected() ? Double.toString(SPOperations.round(Converter.mmFromM(sam.getLength()), 3)) + " mm" 
								: Double.toString(SPOperations.round(Converter.InchFromMeter(sam.getLength()), 3)) + " in";
						Label length = new Label(len);
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
						//Label filter = new Label(sam.DataFiles.getAllDatasets().get(0).))
						header.setTextAlignment(TextAlignment.CENTER);
						type.setTextAlignment(TextAlignment.LEFT);
						//data.setTextAlignment(TextAlignment.LEFT);
						
						TableView<Descriptor> dictionaryTableView = new TableView<Descriptor>();
						
						dictionaryTableView.getColumns().clear();
						dictionaryTableView.setEditable(false);
						dictionaryTableView.setPrefHeight(300);

						//sam.descriptorDictionary.updateDictionary();
						//descriptorDictionary.updateDictionary();
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
						
						vbox.getChildren().addAll(header, type,length, dataFilesChoiceBox, dataSubssetsChoiceBox, dataSubsetControlsVbox, dictionaryTableView);
						vbox.setAlignment(Pos.TOP_LEFT);
						//vbox.setPrefWidth(500);
						vbox.setSpacing(5);
						vbox.setPrefHeight(300);
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
									Rectangle rec = new Rectangle(about.getX(), about.getY(), about.getWidth(), about.getHeight());
				            		if(!rec.contains(new Point2D(event.getScreenX(), event.getScreenY())))
				            			about.hide();
								}
									
							}
						});
						
						about.setOnHidden(new EventHandler<WindowEvent>() {
							@Override
							public void handle(WindowEvent event) {
								renderSampleResults();
								renderCharts();
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
		            		Rectangle rec = new Rectangle(about.getX(), about.getY(), about.getWidth(), about.getHeight());
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

		realCurrentSamplesListView.getItems().addListener(new ListChangeListener<Sample>(){
			@Override
			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Sample> c) {
				renderDefaultSampleResults();
				renderSampleResults();
				renderROISelectionModeChoiceBox();
			}
		});
		
//		currentSamples.addListener(new ListChangeListener<Sample>(){
//			@Override
//			public void onChanged(javafx.collections.ListChangeListener.Change<? extends Sample> c) {
//				renderDefaultSampleResults();
//				renderSampleResults();
//			}
//		});

		xButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//if(HboxHoldingCharts.getChildren().size() > 1)//this should always be true, double check
					//HboxHoldingCharts.getChildren().remove(1);
					if(homeSplitPane.getItems().size() > 2)
						homeSplitPane.getItems().remove(2);
					if(vBoxHoldingCharts.getChildren().size() > 1)
						vBoxHoldingCharts.getChildren().remove(1);
			}
		});

		addSelectedSampleButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				//addSelectedSampleButton.getScene().setCursor(Cursor.WAIT); //dont know why this doesnt work
				for(TreeItem<FileFX> item : sampleDirectoryTreeView.getSelectionModel().getSelectedItems()){
					if(item.getValue().file.isDirectory()){
						for(File samFile : item.getValue().file.listFiles()){
							if(realCurrentSamplesListView.getItems().stream().filter(sample -> sample.getName().equals(SPOperations.stripExtension(samFile.getName()))).count() > 0){
								Dialogs.showErrorDialog("Sample already added", "Cannot add sample twice", "Sample was not added",stage);
								return;
							}
							addSampleToList(samFile.getPath());
						}
					}
					else{
						if(realCurrentSamplesListView.getItems().stream().filter(sample -> sample.getName().equals(item.getValue().toString())).count() > 0){
							Dialogs.showErrorDialog("Sample already added", "Cannot add sample twice", "Sample was not added",stage);
							return;
						}
						addSampleToList(item.getValue().file.getPath());
					}
				}
				//addSelectedSampleButton.getScene().setCursor(Cursor.DEFAULT);
			}
		});

		changeDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override public void handle(ActionEvent e) {
				DirectoryChooser fileChooser = new DirectoryChooser();
				fileChooser.setTitle("Change Working Directory");
				File dir = fileChooser.showDialog(stage);
				if (dir != null) {
					//if(Arrays.asList(dir.listFiles()).stream().filter(f -> f.getName().equals("Sample Data")).collect(Collectors.toList()).size() > 0)
					File sampleDataDir = dir;
					File[] files = dir.listFiles();
					for(int i = 0; i < files.length; i++){
						if(files[i].isDirectory() && files[i].getName().equals("Sample Data"))
							sampleDataDir = files[i];
					}
					treeViewHomePath = sampleDataDir.getPath();
					fillAllSamplesTreeView();
				}
			}
		});
		
		refreshDirectoryButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				fillAllSamplesTreeView();
			}
		});

		buttonAddSampleToGroup.setOnAction(new EventHandler<ActionEvent>() {
			@Override 
			public void handle(ActionEvent e) {
				addSampleToGroupButtonFired();
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
				System.out.println("Opened pane changed");
				if(newValue != null){
					System.out.println(newValue.getText());
				}
				if(newValue != null && newValue.getText().equals("Region Of Interest")){
					System.out.println("HERE");
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
				//System.out.println(timeUnits.units);
				renderCharts();
			}
		});
		
		loadDisplacementCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				
				renderCharts();
			}
		});
		
		applyFiltersCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderSampleResults();
				renderCharts();
			}
		});
		
		applyDataFittersCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderSampleResults();
				renderCharts();
			}
		});
		
		applyZeroCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderSampleResults();
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
					Dialogs.showAlert("The number of images does not match the length of the displacement data", stage);
				}
				
				imageScrollBar.setMin(currentDisplacementDataSubset.getBegin());
				imageScrollBar.setMax(currentDisplacementDataSubset.getEnd());
				
				
				renderImageMatching();
			}
			
		});
		
		saveVideoButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				
				//WritableImage image = chartAnchorPane.snapshot(new SnapshotParameters(), null);
				//imageView.setImage(SwingFXUtils.toFXImage(SwingFXUtils.fromFXImage(image, null),null));
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
					
					SequenceEncoder enc = new SequenceEncoder(file);
					imageMatchingChart.setAnimated(false);
					imageScrollBar.setValue(imageScrollBar.getMin() + 1);
					imageScrollBar.setValue(imageScrollBar.getMin());
					for(int i = (int)imageScrollBar.getMin(); i <= imageScrollBar.getMax(); i++){
						//SnapshotParameters a = new SnapshotParameters();
						WritableImage image = chartAnchorPane.snapshot(new SnapshotParameters(), null);
						BufferedImage buf = SwingFXUtils.fromFXImage(image, null);
						//ImageIO.write(buf, "png", new File(garbageImages.getPath() + "/" + i + ".png"));
						ImageIO.write(buf, "png", new File(file.getParent() + "/" + i + ".png"));
						//Thread.sleep(100);
						enc.encodeImage(buf);
						imageScrollBar.setValue(i);
						//Thread.sleep(100);
					}
					enc.finish();
					imageMatchingChart.setAnimated(true);
				} catch (IOException e) {
					// TODO Auto-generated catch block
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
		

	}
	

	
//	public ListChangeListener<String> checkListener = new ListChangeListener<String>() {
//		public void onChanged(ListChangeListener.Change<? extends String> c) {
//			setROITimeValuesToMaxRange();
//			renderCharts();
//		}
//	};
	
	public void renderImageMatching(){
		BufferedImage img = null;
		try {
			img = ImageIO.read(new File(imagePaths.get((int)imageScrollBar.getValue()).getPath()));
		} catch (IOException e) {
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
			//tallerThanWide = true;
			System.out.println("Fitting height");
			imageView.setFitHeight(((AnchorPane)imageView.getParent().getParent()).heightProperty().doubleValue() / 2);
			//imageView.fitHeightProperty().bind(((AnchorPane)imageView.getParent().getParent()).heightProperty());
		}
		else	
		{
			//tallerThanWide = false;
			System.out.println("Fitting width");
			imageView.setFitWidth(((AnchorPane)imageView.getParent().getParent()).widthProperty().doubleValue());
			//imageView.fitWidthProperty().bind(((AnchorPane)imageView.getParent().getParent()).widthProperty());
		}
		
		int currentIndex = (int)imageScrollBar.getValue() - (int)imageScrollBar.getMin();
		Sample currentSample = getCheckedSamples().get(0);
		DataSubset currentDisplacement = currentSample.getDataSubsetAtLocation(currentSample.results.displacementDataLocation);
		imageMatchingChart.clearVerticalMarkers();
		imageMatchingChart.addVerticalValueMarker(new Data<Number, Number>(currentDisplacement.getUsefulTrimmedData()[currentIndex], 0));
		
		
//		XYChart.Series<Number, Number> point = imageMatchingChart.getData().get(0);
//		Data<Number, Number> data = point.getData().get(currentIndex);
//		XYChart.Series<Number, Number> pt = new XYChart.Series<Number, Number>();
//		pt.setName("At Image");
//
//		ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();
//
//		int totalDataPoints = load.length;
//
//		dataPoints.add(new Data<Number, Number>(currentDisplacement.getUse[currentIndex], currentLoad.getUsefulTrimmedData()[currentIndex]));
//		point.getData().add(dataPoints);
//	
//		chart.getData().add(series1);
//		
		
		
//		.addVerticalRangeMarker(
//				new Data<Number, Number>(s.results.getEngineeringStrain()[beginIndex],
//						s.results.getEngineeringStrain()[endIndex]),
//				Color.GREEN);
		
	}
	
	public void addChartTypeListeners(){
		displayedChartListView.getCheckModel().getCheckedItems().addListener(chartTypeListener);
		displayedChartListView.getSelectionModel().selectedItemProperty().addListener(chartTypeChangeListener);
	}
	
	public void removeChartTypeListeners(){
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
				//renderSampleResults();
				renderCharts();
			}
		});
		
		widthOfLeftPanel = homeSplitPane.getDividerPositions()[0] * homeSplitPane.getWidth();
		
		stage.getScene().widthProperty().addListener(new ChangeListener<Number>() {
		    @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
		        //System.out.println("Width: " + newSceneWidth);
		    	//on resize, keep the panel on the left from getting frikin huge
		    	//p*width = pixels
		    	double percentage = widthOfLeftPanel / homeSplitPane.getWidth();
		    	homeSplitPane.setDividerPosition(0, percentage);
		    	
		    	if(homeSplitPane.getDividerPositions().length > 1)
		    		homeSplitPane.setDividerPosition(1, 1 - percentage);
		    }
		});
		
		if(parameters.size() > 0) {
			try {
				String path = parameters.get(0);
				Sample sample = SPOperations.loadSample(path);
				realCurrentSamplesListView.getItems().add(sample);
				displayedChartListView.getCheckModel().checkAll();
				//fillCurrentSamplesListView();
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
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/viewer/GUI/SelectCustomData.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
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
//		if(HboxHoldingCharts.getChildren().size() > 1){
//			//hide the option panel
//			HboxHoldingCharts.getChildren().remove(1);
//		}
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
		//vbox.getChildren().add(new Label("All Samples in Directory"));
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
		//HboxHoldingCharts.getChildren().add(optionPane);
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

	public void saveChartToImageButtonFired(){
		SnapshotParameters parameters = new SnapshotParameters();
//		parameters.setDepthBuffer(true);
//		parameters.setFill(Color.CORNSILK);
		WritableImage image = chartAnchorPane.snapshot(parameters, null);

	    // TODO: probably use a file chooser here
		FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("png Image(*.png)", "*.png"));
        fileChooser.setInitialFileName("*.png");
        File file = fileChooser.showSaveDialog(stage);
//        if (file != null) {
//            try {
//                ImageIO.write(SwingFXUtils.fromFXImage(pic.getImage(),
//                    null), "png", file);
//            } catch (IOException ex) {
//                System.out.println(ex.getMessage());
//            }
//        }
//		
//	    File file = new File("chart.png");
		if (file != null) {
			try {
				ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
			} catch (IOException e) {
				// TODO: handle exception here
			}
		}
	}
	
	public void selectCustomRangeButtonFired(){
		
	}
	
	public void checkAllButtonFired(){
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(true));
	}
	
	public void uncheckAllButtonFired(){
		
		//currentSamplesListView.getCheckModel().clearChecks();
		realCurrentSamplesListView.getItems().forEach(s -> s.setSelected(false));
	}
	
	public void showVideoDialogButtonFired(){
//		if(HboxHoldingCharts.getChildren().size() > 1){
//			//hide the option panel
//			HboxHoldingCharts.getChildren().remove(1);
//		}
		if(vBoxHoldingCharts.getChildren().size() > 1){
			vBoxHoldingCharts.getChildren().remove(1);
		}
		fillAllSamplesTreeView();
		//xButton.setBlendMode(BlendMode.HARD_LIGHT);
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");
		
		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		hBoxThatHoldsXButton.getChildren().add(openImagesButton);
		hBoxThatHoldsXButton.getChildren().add(saveVideoButton);
		
		VBox controlsVBox = new VBox();
		controlsVBox.setAlignment(Pos.CENTER);
		controlsVBox.setSpacing(15);
		//controlsVBox.getChildren().add(openImagesButton);
		controlsVBox.getChildren().add(imageScrollBar);
		controlsVBox.getChildren().add(imageShownLabel);
		

		VBox vbox = new VBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(controlsVBox);
		//vbox.getChildren().add(new Label("All Samples in Directory"));
		//vbox.getChildren().add(sampleDirectoryTreeView);
		//vbox.getChildren().add(addSelectedSampleButton);
		//vbox.getChildren().add(changeDirectoryButton);
		vbox.setPadding(new Insets(10, 10, 10, 10));
		vbox.setSpacing(10);
		vbox.setAlignment(Pos.TOP_CENTER);
		vbox.getStyleClass().add("right-vbox");
		//vbox.prefHeightProperty().bind(stage.getScene().heightProperty());
		//optionPane.getChildren().clear();
		//optionPane.getChildren().add(vbox);
		//HboxHoldingCharts.getChildren().add(optionPane);
		//AnchorPane optionPane = new AnchorPane();
		//optionPane.getChildren().add(vbox);
		AnchorPane.setBottomAnchor(vbox, 0.0);
		AnchorPane.setLeftAnchor(vbox, 0.0);
		AnchorPane.setRightAnchor(vbox, 0.0);
		AnchorPane.setTopAnchor(vbox, 0.0);
		
		vBoxHoldingCharts.getChildren().add(vbox);
		
		renderCharts();
//		while(homeSplitPane.getItems().size() > 2)
//			homeSplitPane.getItems().remove(2);
//		homeSplitPane.getItems().add(optionPane);
//		homeSplitPane.setDividerPosition(1, 1 - homeSplitPane.getDividerPositions()[0]);
	}
	
	@FXML
	private void zoomToROICBFired(){
		renderCharts();
	}
	
	@FXML
	private void exportCSVButtonFired(){
		if(sampleGroups == null || sampleGroups.size() == 0) {
			
			Dialogs.showInformationDialog("Export Data", "Not able to export data", "Please add a group to export",stage);
			//alert.showAndWait();

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
	private void exportWorkspaceToZipButtonFired(){
		
		DirectoryChooser chooser = new DirectoryChooser();
		File dir = chooser.showDialog(stage);
		
		if(dir == null)
			return;
		
		File workspace = new File(treeViewHomePath).getParentFile(); 
		
		SPOperations.exportWorkspaceToZipFile(workspace, dir);
	}
	
	private void writeCSVFile(File file){
		//file is a directory to store all the csvs. A csv for each group.

		String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedStressUnit();
		String strainUnit = getDisplayedStrainUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		
		String timeName = "Time";//always
		String stressName = loadDisplacementCB.isSelected() ? "Load" : "Stress";
		String strainName = loadDisplacementCB.isSelected() ? "Displacement" : "Strain";

		
		String dataset1Name = timeName + " (" + timeUnit + ")";
		String dataset2Name = stressName + " (" + stressUnit + ")";
		String dataset3Name = strainName + " (" + strainUnit + ")";
		String dataset4Name = strainName + " Rate (" + strainRateUnit + ")";
		
		for(SampleGroup group : sampleGroups){
			String csv = "";
			int longestData = 0;
			for(Sample s : group.groupSamples){
				csv += s.getName() + ",,,,,";
				if(s.results.time.length > longestData)
					longestData = s.results.time.length;
			}
			csv += "\n";
			for(Sample s : group.groupSamples){
				csv += dataset1Name + "," + dataset2Name + "," + dataset3Name + "," + dataset4Name + ",,";
			}
			csv += "\n";
			//now do data.
			ArrayList<double[]> timeDataList = new ArrayList<double[]>(); //double[] for each sample
			ArrayList<double[]> stressDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainDataList = new ArrayList<double[]>();
			ArrayList<double[]> strainRateDataList = new ArrayList<double[]>();
			
			
			
			for(Sample sample : group.groupSamples){
				double[] timeData = sample.results.time;
				double[] stressData = {1};// = sample.results.load;
				double[] strainData = {1};// = sample.results.displacement;
				double[] strainRateData = {1};// = SPOperations.getDerivative(sample.results.time, sample.results.displacement);
				
				List<double[]> data = getScaledDataArraysFromSample(sample, timeData);//, stressData, strainData, strainRateData);
				
				timeData = data.get(0);
				stressData = data.get(1);
				strainData = data.get(2);
				strainRateData = data.get(3);
				
//				if(loadDisplacementCB.isSelected()){
//					stressData = sample.results.getLoad(stressUnit);
//					//strainData = sample.results.displacement;
//					strainData = sample.results.getDisplacement(strainUnit);
//					strainRateData = SPOperations.getDerivative(sample.results.time, sample.results.displacement);
//				}
//				else{
//					double[] load;
//					load = sample.results.getEngineeringStress(stressUnit);
//					
//					if (trueRadioButton.isSelected()) {
//						try {
//							stressData = sample.getTrueStressFromEngStressAndEngStrain(load,
//									sample.results.getEngineeringStrain());
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						strainData = sample.results.getTrueStrain();
//						strainRateData = SPOperations.getDerivative(sample.results.time, strainData);
//
//					} else {
//						stressData = sample.results.getEngineeringStress(stressUnit);
//						strainData = sample.results.getEngineeringStrain();
//						strainRateData = SPOperations.getDerivative(sample.results.time, strainData);
//
//					}
//				}
//				//apply time scale
//				for(int i = 0; i < timeData.length; i++){
//					timeData[i] = timeData[i] * timeUnits.getMultiplier();
//				}
				
				timeDataList.add(timeData);
				stressDataList.add(stressData);
				strainDataList.add(strainData);
				strainRateDataList.add(strainRateData);
			}
			ArrayList<String> lines = new ArrayList<String>();
			//write each line
			
			for(int i = 0; i < longestData; i++){
				String dataLine = "";
				for(int j = 0; j < timeDataList.size(); j++){
					if(timeDataList.get(j).length > i){
						dataLine += timeDataList.get(j)[i] + "," + stressDataList.get(j)[i] + "," + 
								strainDataList.get(j)[i] + "," + strainRateDataList.get(j)[i] + ",,";
					}
					else{
						//data isn't long enough, add space
						dataLine += ",,,,,";
					}
				}
				lines.add(dataLine + "\n");
			}
			
			SPOperations.writeStringToFile(csv, file.getPath() + "/" + group.groupName + ".csv");
			SPOperations.writeListToFile(lines, file.getPath() + "/" + group.groupName + ".csv");
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
	
	private List<double[]> getScaledDataArraysFromSample(Sample s, double[] time){//, double[] stress, double[] strain, double[] strainRate){
		String timeUnit = getDisplayedTimeUnit();
		String stressUnit = getDisplayedStressUnit();
		String strainUnit = getDisplayedStrainUnit();
		String strainRateUnit = getDisplayedStrainRateUnit();
		double[] stress = {1};
		double[] strain;
		double[] strainRate;
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
					// TODO Auto-generated catch block
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
		for(int i = 0; i < time.length; i++){
			time[i] = time[i] * timeUnits.getMultiplier();
		}
		ArrayList<double[]> a = new ArrayList<>();
		a.add(time);
		a.add(stress);
		a.add(strain);
		a.add(strainRate);
		return a;
	}
	
	private void renderDefaultSampleResults(){
		boolean loadDisplacementOnly = false;
		for(Sample sample : realCurrentSamplesListView.getItems()){
			if(sample.getLength() <= 0)
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
			if(s.getEndROITime() == -1)
				s.setEndROITime(s.results.time[s.results.time.length - 1]);
		}
	}
	
	public void addSampleToList(String samplePath){
		try {
			Sample sampleToAdd = SPOperations.loadSample(samplePath);
			
			if(sampleToAdd != null){
				sampleToAdd.selectedProperty().addListener(new ChangeListener<Boolean>() {
					@Override
					public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
						setROITimeValuesToMaxRange();
						renderCharts();
					}
				});
				SPTracker.track(SPTracker.surepulseViewerCategory, "Sample Analyzed");
				realCurrentSamplesListView.getItems().add(sampleToAdd);
			}
			else{
				System.out.println("Failed to load the sample.");
			}
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		renderDefaultSampleResults();
		setROITimeValuesToMaxRange();
		renderCharts();
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
		if(vBoxHoldingCharts.getChildren().size() > 1){
			//video dialog is open.
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			imageMatchingChart = (LineChartWithMarkers<Number, Number>) chart;

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

			}
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 1){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			//chart.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			chartAnchorPane.getChildren().add(chart);
			AnchorPane.setTopAnchor(chart, 0.0);
			AnchorPane.setBottomAnchor(chart, 0.0);
			AnchorPane.setLeftAnchor(chart, 0.0);
			AnchorPane.setRightAnchor(chart, 0.0);

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
		}
		else if(displayedChartListView.getCheckModel().getCheckedItems().size() == 4){
			LineChart<Number, Number> chart = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			LineChart<Number, Number> chart2 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			LineChart<Number, Number> chart3 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(2));
			LineChart<Number, Number> chart4 = getChart(displayedChartListView.getCheckModel().getCheckedItems().get(3));
			//			chart.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(0));
			//			chart2.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(1));
			//			chart3.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(2));
			//			chart4.setTitle(displayedChartListView.getCheckModel().getCheckedItems().get(3));
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
		}

	}

	private void setFilterActivations() {
		for(Sample s : getCheckedSamples())
			s.DataFiles.getAllDatasets().stream().forEach(ds -> ds.modifiers.getLowPassModifier().activated.set(applyFiltersCB.isSelected()));
	}
	
	private void setDataFitterActivations(){
		getCheckedSamples().stream().forEach(s -> s.DataFiles.getAllDatasets().stream().forEach(ds -> ds.fittedDatasetActive = applyDataFittersCB.isSelected()));
	}
	
	private void setZeroActivations(){
		getCheckedSamples().stream().forEach(s -> s.DataFiles.getAllDatasets().stream().forEach(ds -> ds.modifiers.getZeroModifier().activated.set(applyZeroCheckBox.isSelected())));
	}

	private void renderDisplayedChartListViewChartOptions() {
		if(loadDisplacementCB.isSelected()){
			if(displayedChartListView.getItems().size() > 0 && displayedChartListView.getItems().get(0).equals("Load Vs Displacement"))
				return;
			removeChartTypeListeners();
			displayedChartListView.getSelectionModel().clearSelection();
			displayedChartListView.getCheckModel().clearChecks();
			displayedChartListView.getItems().clear();
			displayedChartListView.getItems().add("Load Vs Displacement");
			displayedChartListView.getItems().add("Load Vs Time");
			displayedChartListView.getItems().add("Displacement Vs Time");
			displayedChartListView.getItems().add("Displacement Rate Vs Time");
			addChartTypeListeners();
		}
		else{
			if(displayedChartListView.getItems().size() > 0 && displayedChartListView.getItems().get(0).equals("Stress Vs Strain"))
				return;
			removeChartTypeListeners();
			displayedChartListView.getCheckModel().clearChecks();
			displayedChartListView.getSelectionModel().clearSelection();
			displayedChartListView.getItems().clear();
			
			displayedChartListView.getItems().add("Stress Vs Strain");
			displayedChartListView.getItems().add("Stress Vs Time");
			displayedChartListView.getItems().add("Strain Vs Time");
			displayedChartListView.getItems().add("Strain Rate Vs Time");
			addChartTypeListeners();
		}
	}

	private boolean loadDisplacementOnlySampleExists(List<Sample> checkedSamples) {
		for(Sample s : checkedSamples){
			if(s.getLength() == 0)
				return true;
		}
		return false;
	}

	private void renderROIResults() {
		
		if(loadDisplacementCB.isSelected()){
			System.out.println("Load Displacement ROI is not implemented.");
			return;
		}
		ROI.renderROIResults(getCheckedSamples(), loadDisplacementCB.isSelected(), applyFiltersCB.isSelected(), roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem());
		
		//average value
		if(getCheckedSamples().size() == 0)
			return;
		
		//now there's at least one checked sample
		
		String chartOfInterest = "";
		
		if(choiceBoxRoi.getSelectionModel().getSelectedItem() == null){
			//nothing selected, look at chart
			chartOfInterest = displayedChartListView.getSelectionModel().getSelectedItem();
		}
		else{
			chartOfInterest = choiceBoxRoi.getSelectionModel().getSelectedItem();
		}
		
		if(chartOfInterest == null || chartOfInterest.equals(""))
			return;
		
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
			if (trueRadioButton.isSelected()){
				avg = ROI.averageTrueStress;
				integral = ROI.averageTrueStressVsStrainIntegral;
				avgMax = ROI.averageMaxTrueStress;
			}
			
				

			if (englishRadioButton.isSelected()){
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avg),4)));
				
				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.ksiFromPa(integral), 4)));
				
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.ksiFromPa(avgMax),4)));
			}
			else{
				tbAvgYValue.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avg),4)));
				
				tbAvgIntegralValue.setText(Double.toString(
						SPOperations.round(Converter.MpaFromPa(integral), 4)));
				
				maxYValueTF.setText(Double.toString(SPOperations.round(Converter.MpaFromPa(avgMax),4)));
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
	}

	public List<Sample> getCheckedSamples(){
		List<Sample> samples = (List<Sample>) realCurrentSamplesListView.getItems().stream().filter(s-> s.isSelected()).collect(Collectors.toList());
		return samples;
		
//		ArrayList<Sample> samples = new ArrayList<Sample>();
//		for(int i = 0; i < currentSamples.size(); i++){
//			if(currentSamplesListView.getCheckModel().isChecked(i))
//				samples.add(currentSamples.get(i));
//		}
//		return samples;
	}
	public int getSampleIndex(Sample s){
		return realCurrentSamplesListView.getItems().indexOf(s);
//		for(int i = 0; i < currentSamples.size(); i++){
//			if(s.getName().equals(currentSamples.get(i).getName()))
//				return i;
//		}
//		return -1;
	}

	private LineChart<Number, Number> getChart(String selectedItem) {
		LineChart<Number, Number> chart = null;
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

	private LineChart<Number, Number> getDisplacementRateTimeChart() {
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
		

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
		
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
			setSeriesColor(chart, series1, seriesColor);
			
		}
		
		createChartLegend(getCheckedSamples(), chart);
		
//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		//legend.getItems().set(legend.getItems().size() - 1, new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColor)));
	    //Legend.LegendItem newItem = new Legend.LegendItem(series.getName(), new Rectangle(10,4,color));
	    //if(legend.getItems().size() > 0)
	    //	legend.getItems().remove(legend.getItems().size() - 1);
//	    Legend.LegendItem li1=new Legend.LegendItem("Over 8", new Rectangle(10,4,Color.NAVY));
//	    Legend.LegendItem li2=new Legend.LegendItem("Over 5 up to 8", new Rectangle(10,4,Color.FIREBRICK));
//	    Legend.LegendItem li3=new Legend.LegendItem("Below 5", new Rectangle(10,4,Color.ORANGE));
	    //legend.getItems().setAll(legend.getItems().get(0), newItem);
		//legend.getItems().set
		
		//double upper = YAxis.getUpperBound();
//		YAxis.setAutoRanging(false);
//		YAxis.setLowerBound(0);
//		YAxis.setUpperBound(maxPlottedVal * 1.1);
//		XAxis.setAutoRanging(false);
		//testing slack
		return chart;
	}

	private LineChart<Number, Number> getDisplacementTimeChart() {
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

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()));
		}
		
		createChartLegend(getCheckedSamples(), chart);

//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		
		return chart;
	}

	private LineChart<Number, Number> getLoadTimeChart() {
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

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
			chart.getData().addAll(series1);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()));
		}
		
		createChartLegend(getCheckedSamples(), chart);

//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		
		return chart;
	}

	private LineChart<Number, Number> getLoadDisplacementChart() {
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


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
				} 
				else {
					double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
					int index = 0;

					if (englishRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(roiSample.results.getDisplacement("in"),
								strainValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(roiSample.results.getDisplacement("mm"),
								strainValue);
					
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
			chart.getData().addAll(series1);
			setSeriesColor(chart ,series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()));
		}
		
		createChartLegend(getCheckedSamples(), chart);
		
		//css approach
//		String chartCSS = "";
//		for(int i = 0; i < getCheckedSamples().size(); i++){
//			Sample s = getCheckedSamples().get(i);
//			String color = colorString.get(getSampleIndex(s) % colorString.size());
//			chartCSS += ".default-color" + i + ".chart-series-line { -fx-stroke: " + color + "; }";
//			chartCSS += ".default-color" + i + ".chart-line-symbol { -fx-background-color: " + color + ", white; }";
//		}
//		stage.getScene().getStylesheets().add(chartCSS);

		return chart;
	}

	private void createChartLegend(List<Sample> checkedSamples, LineChartWithMarkers<Number, Number> chart) {
		// TODO Auto-generated method stub
		ArrayList<LegendItem> items = new ArrayList<>();
		for(Sample s : getCheckedSamples()){
			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
		}
		Legend legend = (Legend)chart.lookup(".chart-legend");
		legend.getItems().setAll(items);
	}

	private LineChart<Number, Number> getEnergyTimeChart() {
		// TODO Auto-generated method stub
		return null;
	}

	private LineChart<Number, Number> getFaceForceTimeChart() {
		// TODO Auto-generated method stub
		return null;
	}

	private LineChart<Number, Number> getStrainRateTimeChart() {
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
		

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
		
		chart.setCreateSymbols(false);

		chart.setTitle("Strain Rate Vs Time");
		
		if(zoomToROICB.isSelected()){
			XAxis.setLowerBound(ROI.beginROITime * timeUnits.getMultiplier());
			XAxis.setUpperBound(ROI.endROITime * timeUnits.getMultiplier());
			XAxis.setAutoRanging(false);
		}
		
		addROIFunctionalityToTimeChart(chart);
		
		addXYListenerToChart(chart);
		
//		if(showROIOnChart){
//			chart.clearVerticalMarkers();
//			chart.addVerticalRangeMarker(new Data<Number, Number>(ROI.beginROITime * timeUnits.getMultiplier(), 
//        		ROI.endROITime * timeUnits.getMultiplier()), Color.GREEN);
//		}
//		
//		//set click listener
//		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
//			@Override
//			public void handle(MouseEvent mouseEvent) {
//				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX()) / timeUnits.getMultiplier();
//				if(radioSetBegin.isSelected()){
//					if(timeValue > 0 && timeValue < ROI.endROITime){
//						ROI.beginROITime = timeValue;
//					}
//				}
//				else if(radioSetEnd.isSelected()){
//					if(timeValue < getLowestMaxTime() && timeValue > ROI.beginROITime){
//						ROI.endROITime = timeValue;
//					}
//				}
//				
//		        renderCharts();
//			}
//
//			
//		});
//		
//		chart.lookup(".chart-plot-background").setOnMouseMoved(new EventHandler<MouseEvent>() {
//
//			@Override
//			public void handle(MouseEvent event) {
//				double xValue = (double) chart.getXAxis().getValueForDisplay(event.getX());
//				double yValue = (double) chart.getYAxis().getValueForDisplay(event.getY());
//				xValueLabel.setText("X: " + SPOperations.round(xValue, 4));
//				yValueLabel.setText("Y: " + SPOperations.round(yValue,4));
//			}
//			
//		});
		
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
			
			chart.getData().addAll(series1);
			Color seriesColor = seriesColors.get(getSampleIndex(s) % seriesColors.size());
			setSeriesColor(chart, series1, seriesColor);
			
		}
		
		createChartLegend(getCheckedSamples(), chart);
		
//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		
		
		//legend.getItems().set(legend.getItems().size() - 1, new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColor)));
	    //Legend.LegendItem newItem = new Legend.LegendItem(series.getName(), new Rectangle(10,4,color));
	    //if(legend.getItems().size() > 0)
	    //	legend.getItems().remove(legend.getItems().size() - 1);
//	    Legend.LegendItem li1=new Legend.LegendItem("Over 8", new Rectangle(10,4,Color.NAVY));
//	    Legend.LegendItem li2=new Legend.LegendItem("Over 5 up to 8", new Rectangle(10,4,Color.FIREBRICK));
//	    Legend.LegendItem li3=new Legend.LegendItem("Below 5", new Rectangle(10,4,Color.ORANGE));
	    //legend.getItems().setAll(legend.getItems().get(0), newItem);
		//legend.getItems().set
		
		//double upper = YAxis.getUpperBound();
//		YAxis.setAutoRanging(false);
//		YAxis.setLowerBound(0);
//		YAxis.setUpperBound(maxPlottedVal * 1.1);
//		XAxis.setAutoRanging(false);
		//testing slack
		return chart;
	}


	private LineChart<Number, Number> getStrainTimeChart() {
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

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
			chart.getData().addAll(series1);
			setSeriesColor(chart , series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()));
		}
		
		createChartLegend(getCheckedSamples(), chart);

//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		
		return chart;
	}

	private void renderSampleResults(){
		//renders the result object for each sample
		//setFilterActivations();
		//setDataFitterActivations();
		//setZeroActivations();
		for(Sample sample : realCurrentSamplesListView.getItems()){
			sample.results.render();
		}
		setROITimeValuesToMaxRange();
	}

	private void setSampleResultsDataLocationToDefault(){
		for(Sample sample : realCurrentSamplesListView.getItems()){
			sample.results.loadDataLocation = sample.getDefaultStressLocation();
			sample.results.displacementDataLocation = sample.getDefaultStrainLocation();
		}
	}


	private LineChart<Number, Number> getStressTimeChart() {
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

		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
		//LineChart<Number, Number> chart = new LineChart<>(XAxis, YAxis);
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
						// TODO Auto-generated catch block
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
			chart.getData().addAll(series1);
			setSeriesColor(chart, series1, getSampleChartColor(s));
		}
		
		createChartLegend(getCheckedSamples(), chart);
//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);

		return chart;
	}

	private LineChart<Number, Number> getStressStrainChart() {
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


		LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<>(XAxis, YAxis);
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
				}
				else{
					//individual sample mode
					double strainValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
					int index = 0;

					if (trueRadioButton.isSelected())
						index = SPOperations.findFirstIndexGreaterorEqualToValue(roiSample.results.getTrueStrain(),
								strainValue);
					else
						index = SPOperations.findFirstIndexGreaterorEqualToValue(roiSample.results.getEngineeringStrain(),
								strainValue);
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
				
		        renderCharts();
			}
		});
		
		addXYListenerToChart(chart);

		for(Sample s : getCheckedSamples()){
			double[] load = null;
			double[] displacement = null;//s.results.getEngineeringStrain();
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
						// TODO Auto-generated catch block
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
			setSeriesColor(chart ,series1, seriesColors.get(getSampleIndex(s) % seriesColors.size()));
		}
		
		createChartLegend(getCheckedSamples(), chart);
		
//		ArrayList<LegendItem> items = new ArrayList<>();
//		for(Sample s : getCheckedSamples()){
//			items.add(new Legend.LegendItem(s.getName(), new Rectangle(10,4,seriesColors.get(getSampleIndex(s) % seriesColors.size()))));
//		}
//		
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//		legend.getItems().setAll(items);
		
		//css approach
//		String chartCSS = "";
//		for(int i = 0; i < getCheckedSamples().size(); i++){
//			Sample s = getCheckedSamples().get(i);
//			String color = colorString.get(getSampleIndex(s) % colorString.size());
//			chartCSS += ".default-color" + i + ".chart-series-line { -fx-stroke: " + color + "; }";
//			chartCSS += ".default-color" + i + ".chart-line-symbol { -fx-background-color: " + color + ", white; }";
//		}
//		stage.getScene().getStylesheets().add(chartCSS);

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
				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX()) / timeUnits.getMultiplier();
				if(radioSetBegin.isSelected()){
					Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiMode == null || roiMode.placeHolderSample) {
						if (timeValue > 0 && timeValue < ROI.endROITime) {
							ROI.beginROITime = timeValue;
						}
					}
					else{
						if(timeValue > 0 && timeValue < roiMode.getEndROITime())
							roiMode.setBeginROITime(timeValue);
					}
				}
				else if(radioSetEnd.isSelected()){
					Sample roiMode = roiSelectionModeChoiceBox.getSelectionModel().getSelectedItem();
					if (roiMode == null || roiMode.placeHolderSample) {
						if (timeValue < getLowestMaxTime() && timeValue > ROI.beginROITime) {
							ROI.endROITime = timeValue;
						}
					}
					else{
						if(timeValue < roiMode.results.time[roiMode.results.time.length - 1] && timeValue > roiMode.getBeginROITime())
							roiMode.setEndROITime(timeValue);
					}
				}
				
		        renderCharts();
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
		
		realCurrentSamplesListView.getItems().remove(currentSelectedSample);
		renderROIResults();
		renderCharts();
	}

	private void findFiles(File dir, TreeItem<FileFX> parent) {
		TreeItem<FileFX> root = new TreeItem<>(new FileFX(dir), SPOperations.getIcon(SPOperations.folderImageLocation));
		root.setExpanded(true);
		File[] files = dir.listFiles();
		//System.out.println(Arrays.toString(files));

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
//		if(HboxHoldingCharts.getChildren().size() > 1){
//			HboxHoldingCharts.getChildren().remove(1);
//		}
		xButton.setStyle("-fx-background-color: #ddd;-fx-text-fill:#FF0000;");
		HBox hBoxThatHoldsXButton = new HBox();
		hBoxThatHoldsXButton.setAlignment(Pos.CENTER_LEFT);
		hBoxThatHoldsXButton.setSpacing(15);
		hBoxThatHoldsXButton.getChildren().add(xButton);
		hBoxThatHoldsXButton.getChildren().add(new Label("Create Groups to Export"));
		

//		exportEngineeringRadioButton.setSelected(true);
//		exportEnglishRadioButton.setSelected(true);

//		HBox dataTypeGroup = new HBox();
//		dataTypeGroup.setAlignment(Pos.TOP_CENTER);
////		exportEngineeringRadioButton.setPadding(new Insets(0, 10, 10, 0));
////		exportTrueRadioButton.setPadding(new Insets(0, 10, 0, 10));
////		dataTypeGroup.getChildren().add(exportEngineeringRadioButton);
////		dataTypeGroup.getChildren().add(exportTrueRadioButton);
//
//		HBox unitsGroup = new HBox();
//		unitsGroup.setAlignment(Pos.TOP_CENTER);
//		exportEnglishRadioButton.setPadding(new Insets(0, 10, 10, 0));
//		exportMetricRadioButton.setPadding(new Insets(0, 10, 0, 10));
//		unitsGroup.getChildren().add(exportEnglishRadioButton);
//		unitsGroup.getChildren().add(exportMetricRadioButton);

		VBox vbox = new VBox();
		vbox.getChildren().add(hBoxThatHoldsXButton);
		vbox.getChildren().add(tbSampleGroup);
		vbox.getChildren().add(buttonCreateSampleGroup);
		vbox.getChildren().add(treeViewSampleGroups);
		vbox.getChildren().add(buttonAddSampleToGroup);

//		Label selectDataTypeLabel = new Label("Select Data Type");
//		selectDataTypeLabel.setPadding(new Insets(10, 0, 0, 0));
//		vbox.getChildren().add(selectDataTypeLabel);
//		//vbox.getChildren().add(dataTypeGroup);
//
//		Label selectUnitsLabel = new Label("Select Data Type");
//		selectUnitsLabel.setPadding(new Insets(10, 0, 0, 0));
//		vbox.getChildren().add(selectUnitsLabel);
//		vbox.getChildren().add(unitsGroup);

		includeSummaryPage.setSelected(true);
		includeSummaryPage.setPadding(new Insets(10, 10, 10, 10));
		vbox.getChildren().add(includeSummaryPage);

		vbox.getChildren().add(buttonExportData);
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
		//optionPane.getChildren().clear();
		//optionPane.getChildren().add(vbox);
		//HboxHoldingCharts.getChildren().add(optionPane);
	}

	public void addSampleToGroupButtonFired() {
		if(currentSelectedSampleGroup != null) {
			for(Sample s : getCheckedSamples()) {
				if(currentSelectedSampleGroup.groupSamples.indexOf(s) < 0) {
					Sample newSample = null;
					
					if(s instanceof CompressionSample){
						newSample = new CompressionSample();
						((CompressionSample)newSample).setDiameter(((CompressionSample)s).getDiameter());
					}
					else if(s instanceof TensionRoundSample){
						newSample = new TensionRoundSample();
						((TensionRoundSample)newSample).setDiameter(((TensionRoundSample)s).getDiameter());
					}
					else if(s instanceof TensionRectangularSample){
						newSample = new TensionRectangularSample();
						((TensionRectangularSample)newSample).setWidth(((TensionRectangularSample)s).getWidth());
						((TensionRectangularSample)newSample).setHeight(((TensionRectangularSample)s).getWidth());
					}
					else if(s instanceof ShearCompressionSample){
						newSample = new ShearCompressionSample();
						((ShearCompressionSample)newSample).setGaugeWidth(((ShearCompressionSample)s).getGaugeWidth());
						((ShearCompressionSample)newSample).setGaugeHeight(((ShearCompressionSample)s).getGaugeWidth());
					} else if(s instanceof LoadDisplacementSample) {
						newSample = new LoadDisplacementSample();
					}
				
					
					LoadDisplacementSampleResults results = new LoadDisplacementSampleResults(newSample);
					
					results.displacement = Arrays.copyOf(s.results.displacement, s.results.displacement.length);
					results.load = Arrays.copyOf(s.results.load, s.results.load.length);
					
					//results.engineeringStrain = Arrays.copyOf(s.results.getEngineeringStrain(), s.results.getEngineeringStrain().length);
					results.time = Arrays.copyOf(s.results.time, s.results.time.length);
					//results.engineeringStress = Arrays.copyOf(s.results.getEngineeringStress(), s.results.getEngineeringStress().length);
					
					newSample.results = results;
					newSample.setName(s.getName());
					newSample.setLength(s.getLength());
					
					currentSelectedSampleGroup.groupSamples.add(newSample);
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
			//alert.showAndWait();

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
		ExtensionFilter extensionFilter = new ExtensionFilter("Microsoft Excel Worksheet (*.xlsx)","*.xlsx");
		fileChooser.getExtensionFilters().add(extensionFilter);
		fileChooser.setSelectedExtensionFilter(extensionFilter);
		File file = fileChooser.showSaveDialog(stage);
		
		if (file != null) {
			File jobFile = writeConsoleExcelFileMakerJobFile(file.getPath());
			
			if(jobFile.exists()) {
				if(SPOperations.writeExcelFileUsingEpPlus(jobFile.getPath())) {
					Dialogs.showInformationDialog("Excel Export", "Export Success", "Successfully exported excel file to "+file.getAbsolutePath(), stage);
				} else {
					Dialogs.showErrorDialog("Excel Export", "Excel Export Failed", "There was an error exporting your excel file, this usually means the installation of SURE-Pulse Viewer is broken or some files have been removed", stage);
				}
			}

			writeConsoleExcelFileMakerJobFile(file.getPath());
		}
	}

	private File writeConsoleExcelFileMakerJobFile(String path) {
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
		
		String parametersString = "Version$1\n";
		parametersString += "Export Location$" + path + "\n";
		parametersString += "Summary Page$" + includeSummaryPage.isSelected() + "\n";
		parametersString += "Dataset1$" + timeName + "$" + "(" + timeUnit + ")\n";
		parametersString += "Dataset2$" + stressName + "$(" + stressUnit + ")\n";
		parametersString += "Dataset3$" + strainName + "$(" + strainUnit + ")\n";
		parametersString += "Dataset4$" + strainName + " Rate$(" + strainRateUnit + ")\n";
		
		SPOperations.writeStringToFile(parametersString, jobFile.getPath() + "/Parameters.txt");
		
		for(net.relinc.processor.sample.SampleGroup group : sampleGroups){
			File groupDir = new File(jobFile.getPath() + "/" + group.groupName);
			groupDir.mkdir();
			for(Sample sample : group.groupSamples){
				File sampleDir = new File(groupDir.getPath() + "/" + sample.getName());
				sampleDir.mkdir();
				double[] timeData = sample.results.time;
				double[] stressData = {1};// = sample.results.load;
				double[] strainData;// = sample.results.displacement;
				double[] strainRateData;// = SPOperations.getDerivative(sample.results.time, sample.results.displacement);
				
//				if(loadDisplacementCB.isSelected()){
//					stressData = sample.results.getLoad(stressUnit);
//					strainData = sample.results.displacement;
//					strainRateData = SPOperations.getDerivative(sample.results.time, sample.results.displacement);
//				}
//				else{
//					double[] load;
//					if(englishRadioButton.isSelected()){
//						load = sample.results.getEngineeringStress("ksi");
//					}
//					else{
//						load = sample.results.getEngineeringStress("MPa");
//					}
//					
//					if (trueRadioButton.isSelected()) {
//						try {
//							stressData = sample.getTrueStressFromEngStressAndEngStrain(load,
//									sample.results.getEngineeringStrain());
//						} catch (Exception e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						strainData = sample.results.getTrueStrain();
//						strainRateData = SPOperations.getDerivative(sample.results.time, strainData);
//
//					} else {
//						stressData = sample.results.getEngineeringStress(stressUnit);
//						strainData = sample.results.getEngineeringStrain();
//						strainRateData = SPOperations.getDerivative(sample.results.time, strainData);
//
//					}
//				}
//				//apply time scale
//				for(int i = 0; i < timeData.length; i++){
//					timeData[i] = timeData[i] * timeUnits.getMultiplier();
//				}
				ArrayList<String> sampleData = new ArrayList<String>();
				
				List<double[]> data = getScaledDataArraysFromSample(sample, timeData);//, stressData, strainData, strainRateData);
				timeData = data.get(0);
				stressData = data.get(1);
				strainData = data.get(2);
				strainRateData = data.get(3);
				for(int i = 0; i < timeData.length; i++){
					sampleData.add(timeData[i] + "," + stressData[i] + "," + strainData[i] + "," + strainRateData[i] + "\n");
				}
				SPOperations.writeListToFile(sampleData, sampleDir.getPath() + "/Data.txt");
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
		choiceBoxRoi.getItems().clear();
		for(String s : displayedChartListView.getCheckModel().getCheckedItems()) {
			if(!(getCheckedSamples().size() > 1 && s.equals("Stress Vs Strain")))
				choiceBoxRoi.getItems().add(s);
		}
		if(choiceBoxRoi.getItems().size() > 0)
			choiceBoxRoi.getSelectionModel().select(0);
	}
	
	private void renderROISelectionModeChoiceBox(){
		roiSelectionModeChoiceBox.getItems().clear();
		CompressionSample allSample = new CompressionSample();//not actually a sample, just an "All" placeholder
		//so a sample named "All" might cause problems
		allSample.placeHolderSample = true;
		allSample.setName("All Samples");
		
		roiSelectionModeChoiceBox.getItems().add(allSample);
		for(Sample s : realCurrentSamplesListView.getItems()){
			roiSelectionModeChoiceBox.getItems().add(s);
			//roiSelectionModeChoiceBox.setItems(value);
		}
		//roiSelectionModeChoiceBox.getSelectionModel().select(0);
	}
	
	private void setSeriesColor(LineChartWithMarkers<Number, Number> chart, Series<Number, Number> series, Color color){

		String rgb = String.format("%d, %d, %d",
		        (int) (color.getRed() * 255),
		        (int) (color.getGreen() * 255),
		        (int) (color.getBlue() * 255));

		//fill.setStyle("-fx-fill: rgba(" + rgb + ", 0.15);");
		//series.nodeProperty().get().setStyle("-fx-stroke-width: 1px;");
		series.nodeProperty().get().setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
		//series.nodeProperty().get().setStyle("-fx-background-color: rgba(" + rgb + ", 1.0);");
//		Legend legend = (Legend)chart.lookup(".chart-legend");
//	    Legend.LegendItem newItem = new Legend.LegendItem(series.getName(), new Rectangle(10,4,color));
	    //if(legend.getItems().size() > 0)
	    //	legend.getItems().remove(legend.getItems().size() - 1);
//	    Legend.LegendItem li1=new Legend.LegendItem("Over 8", new Rectangle(10,4,Color.NAVY));
//	    Legend.LegendItem li2=new Legend.LegendItem("Over 5 up to 8", new Rectangle(10,4,Color.FIREBRICK));
//	    Legend.LegendItem li3=new Legend.LegendItem("Below 5", new Rectangle(10,4,Color.ORANGE));
	    //legend.getItems().setAll(legend.getItems().get(0), newItem);
		//line.setStyle("-fx-stroke: rgba(" + rgb + ", 1.0);");
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
