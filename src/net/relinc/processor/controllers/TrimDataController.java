package net.relinc.processor.controllers;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.math3.util.CombinatoricsUtils;
import org.apache.commons.math3.util.MathArrays;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import net.relinc.fitter.GUI.HomeController;
import net.relinc.fitter.application.FitableDataset;
import net.relinc.processor.application.BarSetup;
import net.relinc.processor.application.LineChartWithMarkers;
import net.relinc.processor.data.DataFile;
import net.relinc.processor.data.DataFileListWrapper;
import net.relinc.processor.data.DataSubset;
import net.relinc.processor.data.HopkinsonBarPulse;
import net.relinc.processor.data.IncidentPulse;
import net.relinc.processor.data.ReflectedPulse;
import net.relinc.processor.data.ModifierFolder.Modifier;
import net.relinc.processor.data.ModifierFolder.ZeroOffset;
import net.relinc.processor.data.ModifierFolder.Modifier.ModifierEnum;
import net.relinc.processor.fxControls.NumberTextField;
import net.relinc.processor.staticClasses.Dialogs;
import net.relinc.processor.staticClasses.PochammerChreeDispersion;
import net.relinc.processor.staticClasses.SPMath;
import net.relinc.processor.staticClasses.SPOperations;


public class TrimDataController {

	//@FXML LineChart<Number, Number> chart;
	@FXML AnchorPane chartAnchorPane;
	@FXML RadioButton beginRadio;
	@FXML RadioButton endRadio;
	@FXML ListView<DataSubset> listView;
	@FXML RadioButton drawZoomRadio;
	@FXML CheckBox logCB;
	//@FXML CheckBox applyFilterCB;
	@FXML HBox bottomHBox;
	@FXML HBox filterHBox;
	@FXML HBox beginEndHBox;
	@FXML HBox modifierControlsHBox;
	@FXML ChoiceBox<Modifier> modifierChoiceBox;
	//@FXML TextField filterTF;
	//@FXML TextField filterValue2TF;
	//NumberTextField filterTF;
	AnchorPane tfHolder = new AnchorPane();
	
	int dataPointsToShow = 1000;
	//double lowPassFilterValue = -1;

	
	NumberAxis xAxis = new NumberAxis();
	NumberAxis yAxis = new NumberAxis();
	LineChartWithMarkers<Number, Number> chart = new LineChartWithMarkers<Number, Number>(xAxis, yAxis);
	final ToggleGroup group = new ToggleGroup();
	Button getReflectedBeginFromIncidentButton = new Button("Set Begin From Incident and Bar Setup");
	
	Point2D beginRectangle = new Point2D(0, 0);
	Point2D endRectangle = new Point2D(0, 0);
	Point2D beginDrawnRectangle = new Point2D(0, 0);
	Point2D endDrawnRectangle = new Point2D(0, 0);
	Rectangle DrawnRectangle = new Rectangle(0,0,Color.RED);
	double greyLineVal = 0.0;
	public DataFileListWrapper DataFiles;
	public Stage stage;
	public BarSetup barSetup;
	VBox holdGrid = new VBox();
	
	
	public void initialize(){
		chartAnchorPane.getChildren().add(chart);
		chartAnchorPane.getChildren().add(DrawnRectangle);
		AnchorPane.setTopAnchor(chart, 0.0);
		AnchorPane.setBottomAnchor(chart, 0.0);
		AnchorPane.setLeftAnchor(chart, 0.0);
		AnchorPane.setRightAnchor(chart, 0.0);
		
		
		DrawnRectangle.setFill(null);
		DrawnRectangle.setStroke(Color.RED);
		
		
//		GridPane grid = new GridPane();
//		
//
////		filterTF = new NumberTextField("KHz", "KHz");
////		filterTF.setText("1000");
////		filterTF.updateLabelPosition();
//		grid.add(filterTF, 0, 0);
//		grid.add(filterTF.unitLabel, 0, 0);
//		
//		
//		holdGrid.getChildren().add(grid);
//		holdGrid.setAlignment(Pos.CENTER);

		//filterHBox.getChildren().add(1, holdGrid);
		//bottomHBox.getChildren().add(0,holdGrid);

		
		beginRadio.setToggleGroup(group);
		endRadio.setToggleGroup(group);
		drawZoomRadio.setToggleGroup(group);
		
		logCB.selectedProperty().addListener(new ChangeListener<Boolean>() {
	        public void changed(ObservableValue<? extends Boolean> ov,
	                Boolean old_val, Boolean new_val) {
	        	
	                    updateChart();
	                    
	            }
	        });
		
		listView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataSubset>() {
		    @Override
		    public void changed(ObservableValue<? extends DataSubset> observable, DataSubset oldValue, DataSubset newValue) {
		        xAxis.setAutoRanging(true); //zooms out
		        yAxis.setAutoRanging(true); //zooms out
		        updateControls();
		        updateChart();
		    }
		});
		
		chart.lookup(".chart-plot-background").setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				double timeValue = (double) chart.getXAxis().getValueForDisplay(mouseEvent.getX());
				if(beginRadio.isSelected()){
					getActivatedData().setBeginFromTimeValue(timeValue);
				}
				else if(endRadio.isSelected()){
					getActivatedData().setEndFromTimeValue(timeValue);
				}
				else if(drawZoomRadio.isSelected()){
					beginRectangle = new Point2D((double)chart.getXAxis().getValueForDisplay(mouseEvent.getX()), (double)chart.getYAxis().getValueForDisplay(mouseEvent.getY()));
				}
				updateAnnotations();
			}
		});
		
		chart.lookup(".chart-plot-background").setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				endRectangle = new Point2D((double)chart.getXAxis().getValueForDisplay(mouseEvent.getX()), (double)chart.getYAxis().getValueForDisplay(mouseEvent.getY()));
			}
		
		
		});
		chart.lookup(".chart-plot-background").setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(drawZoomRadio.isSelected()){
				
				xAxis.setLowerBound(Math.min(beginRectangle.getX(), endRectangle.getX()));
				xAxis.setUpperBound(Math.max(beginRectangle.getX(), endRectangle.getX()));
				
				
				
				yAxis.setLowerBound(Math.min(beginRectangle.getY(), endRectangle.getY()));
				yAxis.setUpperBound(Math.max(beginRectangle.getY(), endRectangle.getY()));
				
				xAxis.setAutoRanging(false);
				yAxis.setAutoRanging(false);
				
				DrawnRectangle.setWidth(0);
				DrawnRectangle.setHeight(0);
				
				updateChart();
				}
			}
		
		
		});
		
		chart.lookup(".chart-plot-background").setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(beginRadio.isSelected()){
					if(getActivatedData().getIndexFromTimeValue((double) xAxis.getValueForDisplay(mouseEvent.getX()))
							< getActivatedData().getEnd()){
						greyLineVal = (double) xAxis.getValueForDisplay(mouseEvent.getX());
					}
					else{
						greyLineVal = Double.MAX_VALUE;
					}
					updateAnnotations();
					
				}
				else if(endRadio.isSelected()){
					if(getActivatedData().getIndexFromTimeValue((double) xAxis.getValueForDisplay(mouseEvent.getX()))
							> getActivatedData().getBegin()){
						greyLineVal = (double) xAxis.getValueForDisplay(mouseEvent.getX());
					}
					else{
						greyLineVal = Double.MAX_VALUE;
					}
					updateAnnotations();
					
				}
			}
		
		
		});
		
		chart.lookup(".chart-plot-background").setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				greyLineVal = Double.MAX_VALUE;
				updateAnnotations();
			}
		});
		
		
		
		chartAnchorPane.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				
				if(drawZoomRadio.isSelected()){
					beginDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
				}
				updateAnnotations();
			}
		});
		
		chartAnchorPane.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent){
				if(drawZoomRadio.isSelected()){
					endDrawnRectangle = new Point2D(mouseEvent.getX(), mouseEvent.getY());
					Rectangle r = getRectangleFromPoints(beginDrawnRectangle, endDrawnRectangle);
					DrawnRectangle.relocate(r.getX(), r.getY());
					DrawnRectangle.setWidth(r.getWidth());
					DrawnRectangle.setHeight(r.getHeight());
				}
			}

			
		
		
		});
		

		
		chartAnchorPane.setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if(group.getSelectedToggle() == null)
					chart.getScene().setCursor(Cursor.DEFAULT);
				else if(group.getSelectedToggle() == drawZoomRadio)
					chart.getScene().setCursor(Cursor.CROSSHAIR);
			}
		});
		chartAnchorPane.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				chart.getScene().setCursor(Cursor.DEFAULT);
				
			}
		});
		
		getReflectedBeginFromIncidentButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				setReflectedBeginFromIncidentAndBarSetup();
			}
		});
		
//		for(ModifierEnum en : ModifierEnum.values())
//			modifierChoiceBox.getItems().add(Modifier.getNewModifier(en));
		
		modifierChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Modifier>() {
			@Override
			public void changed(ObservableValue<? extends Modifier> observable, Modifier oldValue, Modifier newValue) {
				updateModifierControls();
				updateChart();
			}
		});
		
		
	}
	
	@FXML
	public void runAutoselectButtonFired(){
		int theta = 2;
		
		double[] testX = getActivatedData().getTrimmedData();
		ArrayList<double[]> diluted = SPMath.diluteData(testX, 1000);
		testX = diluted.get(0);
		double[] oldIndices = diluted.get(1);
		
//		double[] a = {-0.052797, -0.0863674, -0.142827, 0., 0.00824, -0.0488296, 
//				-0.0546281, 0.0625629, 0.144353, 0.0860622, 0.00091556, 0.0402844, 
//				0.00183111, -0.0292978, 0.0476089, 0.0421155, -0.00854518, 
//				-0.0827052, -0.0900296, 0.00915555, 0.14893, 0.130314, 0.0579852, 
//				0.021363, 0.0796533, 0.108646, 0.0711081, 0.0503555, 0.0927763, 
//				0.0927763, -0.00671407, -0.03296, 0.0354015, 0.0878933, 0.0552385, 
//				0.0335704, 0.0177007, -0.06592, -0.0555437, -0.0122074, 0.115055, 
//				0.157476, 0.0231941, -0.0732444, -0.0433363, -0.00579852, -0.0405896, 
//				-0.126652, -0.149236, -0.106204, -0.116581, -0.0668355, -0.00305185, 
//				-0.00305185, -0.0207526, -0.0546281, -0.052797, -0.00305185, 
//				-0.0338756, -0.0704978, 0.00457778, -0.00854518, -0.0292978, 
//				-0.0759911, -0.0808741, -0.0817896, -0.0497452, -0.0180059, 
//				-0.00946074, -0.00122074, -0.0152593, -0.0537126, -0.0695822, 
//				-0.0253304, -0.0338756, -0.0415052, -0.0781274, -0.0573748, 
//				0.0354015, 0.0466933, 0.0393689, 0.0393689, 0.00366222, -0.0619526, 
//				-0.0732444, 0.00915555, 0.00457778, 0.0271615, 0.0683615, 0., 
//				0.0503555, 0.0476089, 0.0897244, 0.106815, 0.106815, 0.13123, 
//				0.0842311, 0.0634785, 0.0778222, 0.17243, 0.122684, 0.0991852,
//				0.0833155, 0.0646992, 0.0991852, 0.0982696, 0.119022, 0.134892, 
//				0.173345, 0.176092, 0.174261, 0.189215, 0.160222, 0.142521, 0.144353, 
//				0.104984, 0.113224, 0.128178, 0.103153, 0.133061, 0.137639, 0.137639, 
//				0.153508, 0.10773, 0.167852, 0.171514, 0.103153, 0.0656148, 
//				0.0927763, 0.0552385, 0.0860622, 0.108646, 0.0766015, 0.0448622, 
//				-0.0347911, -0.0582904, -0.0442518, 0.0344859, 0.0833155, 0.0457778, 
//				-0.0320444, -0.0637837, -0.0930815, -0.0253304, 0.0167852, 0.0515763, 
//				0.0561541, -0.0122074, -0.0518815, -0.0518815, -0.0292978, 0.0747704, 
//				0.0946074, 0.0158696, -0.0122074, -0.0668355, -0.0836207, -0.0723289, 
//				-0.0180059, -0.0234993, -0.0601215, -0.0750755, -0.118412, 
//				-0.0930815, -0.0451674, -0.0384533, -0.0488296, -0.0781274, 
//				-0.102237, -0.0582904, -0.142827, -0.136113, -0.10712, -0.0949126, 
//				-0.120243, -0.158696, -0.145573, -0.0405896, -0.0180059, -0.0161748, 
//				-0.0283822, -0.0433363, -0.07416, -0.0302133, 0.0177007, 0.0796533, 
//				0.207221, 0.242622, 0.278329, 0.33845, 0.525834, 0.663778, 0.771508, 
//				0.926542, 1.05472, 1.23966, 1.46275, 1.73864, 2.0481, 2.27973, 
//				2.49062, 2.69967, 2.94443, 3.18735, 3.45378, 3.63384, 3.73425, 
//				3.72478, 3.70403, 3.66283, 3.56548, 3.43486, 3.30088, 3.16599, 
//				2.96518, 2.77017, 2.61452, 2.58736, 2.59011, 2.63314, 2.66518, 
//				2.72042, 2.81045, 2.96701, 3.14158, 3.25022, 3.35154, 3.3375, 
//				3.23893, 3.17911, 3.14615, 3.07688, 2.91818, 2.81411, 2.76345, 
//				2.65664, 2.65755, 2.72408, 2.86569, 2.89865, 2.96243, 3.02805, 
//				3.08512, 3.14707, 3.16111, 3.11716, 3.02133, 2.90231, 2.79641, 
//				2.68197, 2.65938, 2.67251, 2.73171, 2.77749, 2.8367, 2.94443, 
//				3.08908, 3.16965, 3.18644, 3.12174, 3.05796, 2.95572, 2.83761, 
//				2.73812, 2.66884, 2.61818, 2.52449, 2.42592, 2.44179, 2.39601,
//				2.36122, 2.36671, 2.2486, 2.10059, 1.84729, 1.61565, 1.31474, 
//				1.04068, 0.853298, 0.653401, 0.460219, 0.296335, 0.188299, 0.103153, 
//				0.0973541, 0.021363, -0.0891141, -0.184942, -0.30488, -0.316172, 
//				-0.326548, -0.286264, -0.241096, -0.156865, 0.00640889, 0.13123, 
//				0.275582, 0.362865, 0.388196, 0.417188, 0.350658, 0.186468, 
//				0.0100711, -0.0677511, -0.0537126, 0.00915555, -0.0546281, -0.129704, 
//				-0.129704, 0.00640889, 0.150761, 0.167852, 0.0955229, 0.0778222, 
//				0.121769, 0.140385, 0.0729392, 0.021363, 0.109562, 0.195013, 
//				0.272836, 0.245674, 0.162053, 0.135807, 0.136723, 0.0982696, 
//				0.0457778, 0., -0.0384533, -0.0180059, 0.0466933, 0.153508, 0.245674, 
//				0.300913, 0.285958, 0.250252, 0.250252, 0.221259, 0.116276, 
//				0.0195319, -0.0704978, 0.00457778, 0.135807, 0.206305, 0.259713, 
//				0.355236, 0.415357, 0.377819, 0.312204, 0.207221, 0.122684, 
//				0.0665304, 0.0503555, 0.1236, 0.209967, 0.230415, 0.233467, 0.260628, 
//				0.304575, 0.371105, 0.32899, 0.230415, 0.158391, 0.0918607, 0.112308, 
//				0.160222, 0.106815, 0.150761, 0.146184, 0.122684, 0.145268, 0.220344, 
//				0.26429, 0.189215, 0.110477, 0.112308};
		
		
//		double[] a = {-0.0335704, -0.0564592, 0.142827, 0.00824, -0.0570696, -0.00579851, 
//				0.117191, 0.0817897, -0.0582904, -0.0851466, 0.0393689, -0.0384533, 
//				-0.0311289, 0.0769066, -0.00549333, -0.0506607, -0.07416, 
//				-0.00732444, 0.0991852, 0.139775, -0.0186163, -0.0723288, -0.0366222, 
//				0.0582904, 0.0289926, -0.0375378, -0.0207526, 0.0424207, 0., 
//				-0.0994903, -0.0262459, 0.0683615, 0.0524918, -0.0326548, -0.0216681, 
//				-0.0158696, -0.0836207, 0.0103763, 0.0433363, 0.127262, 0.0424207, 
//				-0.134281, -0.0964385, 0.0299081, 0.0375378, -0.0347911, -0.0860622, 
//				-0.0225837, 0.0430311, -0.0103763, 0.0497452, 0.0637837, 0., 
//				-0.0177007, -0.0338755, 0.00183111, 0.0497452, -0.0308237, 
//				-0.0366222, 0.0750755, -0.013123, -0.0207526, -0.0466933, 
//				-0.00488296, -0.00091556, 0.0320444, 0.0317393, 0.00854518, 0.00824, 
//				-0.0140385, -0.0384533, -0.0158696, 0.0442518, -0.00854519, 
//				-0.00762962, -0.0366222, 0.0207526, 0.0927763, 0.0112919, 
//				-0.00732444, 0., -0.0357067, -0.0656148, -0.0112918, 0.0824, 
//				-0.00457777, 0.0225837, 0.0412, -0.0683615, 0.0503555, -0.00274667, 
//				0.0421156, 0.0170904, 0., 0.0244148, -0.0469985, -0.0207526, 
//				0.0143437, 0.0946074, -0.0497452, -0.0234993, -0.0158696, -0.0186163, 
//				0.0344859, -0.00091555, 0.0207526, 0.0158696, 0.0384533, 0.0027467, 
//				-0.0018311, 0.0149541, -0.0289926, -0.0177008, 0.0018312, -0.0393689, 
//				0.00824, 0.014954, -0.0250251, 0.0299081, 0.0045778, 0., 0.0158696, 
//				-0.0457778, 0.0601215, 0.0036622, -0.0683614, -0.0375378, 0.0271615, 
//				-0.0375378, 0.0308237, 0.0225837, -0.0320444, -0.0317393, -0.0796533, 
//				-0.0234993, 0.0140385, 0.0787378, 0.0488296, -0.0375378, -0.0778222, 
//				-0.0317392, -0.0292978, 0.0677511, 0.0421155, 0.0347911, 0.00457778, 
//				-0.0683615, -0.0396741, 0., 0.0225837, 0.104068, 0.019837, 
//				-0.0787378, -0.028077, -0.0546281, -0.0167852, 0.0112918, 0.054323, 
//				-0.00549333, -0.0366222, -0.0149541, -0.0433363, 0.0253304, 
//				0.0479141, 0.00671407, -0.0103763, -0.0292978, -0.0241096, 0.0439467, 
//				-0.0845363, 0.006714, 0.0289926, 0.0122074, -0.0253303, -0.0384533, 
//				0.0131229, 0.104984, 0.0225837, 0.00183111, -0.0122074, -0.0149541, 
//				-0.0308237, 0.0439467, 0.0479141, 0.0619526, 0.127567, 0.0354015, 
//				0.0357066, 0.0601215, 0.187384, 0.137944, 0.10773, 0.155034, 
//				0.128178, 0.184942, 0.22309, 0.275887, 0.309458, 0.231636, 0.210883, 
//				0.209051, 0.244759, 0.242927, 0.266427, 0.180059, 0.100406, 
//				-0.009461, -0.020753, -0.041199, -0.097355, -0.130619, -0.133976, 
//				-0.134892, -0.200812, -0.195013, -0.155644, -0.027162, 0.002747, 
//				0.043031, 0.032044, 0.055239, 0.09003, 0.15656, 0.174565, 0.108646, 
//				0.101322, -0.014039, -0.098574, -0.059817, -0.03296, -0.069277, 
//				-0.158696, -0.104068, -0.050661, -0.106815, 0.000916, 0.06653, 
//				0.141606, 0.03296, 0.063784, 0.065615, 0.057069, 0.061953, 0.014038, 
//				-0.043946, -0.095828, -0.119023, -0.105899, -0.114444, -0.022584, 
//				0.013123, 0.059206, 0.045778, 0.059205, 0.107731, 0.144657, 0.080569, 
//				0.016786, -0.0647, -0.063783, -0.102237, -0.118107, -0.09949, 
//				-0.069277, -0.050661, -0.093692, -0.098575, 0.01587, -0.045778, 
//				-0.034791, 0.005493, -0.118106, -0.148015, -0.253304, -0.231635, 
//				-0.300913, -0.274056, -0.187383, -0.199896, -0.193182, -0.163884, 
//				-0.108036, -0.0851466, -0.00579855, -0.0759911, -0.110477, 
//				-0.0958282, -0.119938, -0.0112919, -0.0103762, 0.0402844, 0.0451674, 
//				0.0842311, 0.163274, 0.124821, 0.144353, 0.087283, 0.0253304, 
//				0.0289925, -0.0665303, -0.16419, -0.176397, -0.0778222, 0.0140385, 
//				0.0628681, -0.0637837, -0.0750756, 0., 0.136113, 0.144353, 0.0170904, 
//				-0.0723289, -0.0177007, 0.0439467, 0.0186163, -0.067446, -0.0515763, 
//				0.0881985, 0.0854518, 0.0778222, -0.0271615, -0.0836207, -0.0262459, 
//				0.0009155, -0.0384533, -0.0524918, -0.0457778, -0.0384533, 0.0204474, 
//				0.0646992, 0.106815, 0.0921659, 0.0552385, -0.0149541, -0.0357066, 
//				0., -0.0289926, -0.104984, -0.0967437, -0.0900296, 0.0750755, 
//				0.13123, 0.0704977, 0.0534074, 0.095523, 0.0601214, -0.0375377, 
//				-0.0656148, -0.104984, -0.0845363, -0.0561541, -0.0161748, 0.0732445, 
//				0.0863673, 0.0204474, 0.0030519, 0.0271615, 0.0439466, 0.0665304, 
//				-0.0421156, -0.0985748, -0.0720236, -0.0665304, 0.0204474, 0.0479141, 
//				-0.0534074, 0.0439466, -0.0045777, -0.0234993, 0.0225837, 0.0750755, 
//				0.0439467, -0.0750755, -0.0787378, 0.0018311};
		//testX = a;
		double[] differences = getConsectiveDifferences(testX);
		
		
		double[] scan1 = new double[testX.length - 3];
		double[] indexes = new double[scan1.length];
		for(int i = 0; i < scan1.length; i++){
			//System.out.println("First Scan. At: " + i / (double)scan1.length);
			scan1[i] = runQDiff(i + 1, differences.length - 1, differences, theta);
			indexes[i] = i;
		}
		MathArrays.sortInPlace(scan1, indexes);
//		System.out.println("Indexes:");
//		for(int i = 0; i < indexes.length; i++){
//			System.out.print(indexes[i] + " , ");
//		}
//		System.out.println("Scan1:");
//		for(int i = 0; i < scan1.length; i++){
//			System.out.print(scan1[i] + " , ");
//		}
		int endOfFirstRegion = (int)indexes[indexes.length - 1];
//		System.out.println();
		double[] scan2 = new double[endOfFirstRegion - 1];
		double[] indexes2 = new double[scan2.length];
		for(int i = 0; i < scan2.length; i++){
			scan2[i] = runQDiff(i + 1, endOfFirstRegion, differences, theta);
			indexes2[i] = i;
		}
		MathArrays.sortInPlace(scan2, indexes2);
		
		int winner = (int)indexes2[indexes2.length - 1];
		//System.out.println("Winning index: " + winner);
		//int j = SPOperations.findFirstIndexGreaterorEqualToValue(getActivatedData().Data.data, testX[winner]);
		//System.out.println("Undiluted index: " + oldIndices[winner]);
		int j = getActivatedData().getBegin() + (int)oldIndices[winner];
		//System.out.println("Adjusted winning index: " + j);
		getActivatedData().setBegin(j);
		updateChart();
	}
	
	public double empiricalDiv(double[] x, double[] y, double theta){
		int m = x.length;
		int n = y.length;
		double term1 = 0;
		for(int i = 0; i < m; i++){
			for(int j = 0; j < n; j++){
				term1 += Math.pow(Math.abs(x[i] - y[j]), theta);
			}
		}
		term1 = term1 * 2 / m / n;
		
		double term2 = 0;
		for(int k = 0; k < m; k++){
			for(int i = 0; i <= k; i++){
				term2 += Math.pow(Math.abs(x[i] - x[k]), theta);
			}
		}
		term2 = term2 * Math.pow(CombinatoricsUtils.binomialCoefficientDouble(m, 2), -1);
		
		
		double term3 = 0; 
		for(int k = 0; k < n; k++){
			for(int i = 0; i <= k; i++){
				term3 += Math.pow(Math.abs(y[i] - y[k]), theta);
			}
		}
		term3 = term3 * Math.pow(CombinatoricsUtils.binomialCoefficientDouble(n, 2), -1);
		
		return term1 - term2 - term3;
	}
	
	public double Q(double[] x, double[] y, double theta){
		double m = x.length;
		double n = y.length;
		return m*n / (m + n) * empiricalDiv(x, y, theta);
	}
	
	public double[] getConsectiveDifferences(double[] x){
		double[] diff = new double[x.length -1];
		for(int i = 0; i < diff.length; i++){
			diff[i] = x[i + 1] - x[i];
		}
		return diff;
	}
	
	public double runQDiff(int t, int k, double[] diff, double theta){
		if(k > t + 1){
			double[] group1 = new double[t + 1];
			for(int i = 0; i < group1.length; i++){
				group1[i] = diff[i];
			}
			double[] group2 = new double[k - t];
			for(int i = 0; i < group2.length; i++){
				group2[i] = diff[i + t + 1];
			}
			return Q(group1, group2, theta);
		}
		else{
			return -4;
		}
	}
	
	@FXML
	public void lauchFitterButtonFired(){
		Stage primaryStage = new Stage();
		try {
			//BorderPane root = new BorderPane();
			FXMLLoader root1 = new FXMLLoader(getClass().getResource("/net/relinc/fitter/GUI/Home.fxml"));
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Calibration.fxml"));
			Scene scene = new Scene(root1.load());
			
			//Parent root = FXMLLoader.load(getClass().getResource("/fxml/Splashpage.fxml"));
			//Scene scene = new Scene(root);
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	        //primaryStage.getIcons().add(SPSettings.getRELLogo());
	        primaryStage.setTitle("SURE-Pulse Fitter");
			primaryStage.setScene(scene);
			HomeController c = root1.<HomeController>getController();
			c.renderGUI();
			if(getActivatedData().fitableDataset == null)
				getActivatedData().fitableDataset = convertToFitableDataset(getActivatedData());
			c.datasetsListView.getItems().add(getActivatedData().fitableDataset);
			c.datasetsListView.getSelectionModel().select(0);
			c.renderGUI();
			
			//c.stage = primaryStage;
			primaryStage.showAndWait();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void removeModifierButtonFired(){
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		m.enabled.set(false);
		m.removeModifier();
		updateChart();
	}
	
	@FXML
	public void applyModifierButtonFired(){
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		
		m.configureModifier(getActivatedData());
		m.enabled.set(true);
		m.activateModifier();
		
		updateChart();
	}
	
	@FXML
	public void doneTrimmingDataFired(){
		boolean allAreTrimmed = true;
		for(DataFile d : DataFiles){
			for(DataSubset subset : d.dataSubsets){
				if(subset.getBegin() == 0 && subset.getEnd() == subset.Data.data.length - 1)
					allAreTrimmed = false;
			}
		}
		if(!allAreTrimmed){
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("Not all datasets were trimmed.");
			alert.setContentText("Would you like to continue?");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
			    //close
			    Stage stage = (Stage) chart.getScene().getWindow();
			    stage.close();
			} else {
			    //do nothing
			}
		}
		else{
			//close
		    Stage stage = (Stage) chart.getScene().getWindow();
		    stage.close();
		}
	}

	private FitableDataset convertToFitableDataset(DataSubset activatedData) {
		if(activatedData == null)
			return null;
		ArrayList<Double> xValues = new ArrayList<>(activatedData.Data.timeData.length);
		ArrayList<Double> yValues = new ArrayList<>(activatedData.Data.timeData.length);

		for(int i = 0; i < activatedData.Data.timeData.length; i++){
			xValues.add(activatedData.Data.timeData[i]);
			yValues.add(activatedData.Data.data[i]);
		}
		FitableDataset d = new FitableDataset(xValues, yValues, activatedData.name);
		return d;
	}
	
	private Rectangle getRectangleFromPoints(Point2D p1, Point2D p2) {
		double beginX = Math.min(p1.getX(), p2.getX());
		double beginY = Math.min(p1.getY(), p2.getY());
		double width = Math.abs(p1.getX() - p2.getX());
		double height = Math.abs(p1.getY() - p2.getY());
		return new Rectangle(beginX, beginY, width, height);		
	}
	
	public void leftArrowButtonFired(){
		if(beginRadio.isSelected())
			getActivatedData().setBegin(getActivatedData().getBegin() - 1);
		else if(endRadio.isSelected())
			getActivatedData().setEnd(getActivatedData().getEnd() - 1);
		updateAnnotations();
	}
	
	public void rightArrowButtonFired(){
		if(beginRadio.isSelected())
			getActivatedData().setBegin(getActivatedData().getBegin() + 1);
		else if(endRadio.isSelected())
			getActivatedData().setEnd(getActivatedData().getEnd() + 1);
		updateAnnotations();
	}
	
	public void resetZoomFired(){
		xAxis.setAutoRanging(true);
		yAxis.setAutoRanging(true);
		updateChart();
	}
	
	public double[] polynomialSmooth(double[] inputXData, double[] inputYData, int range, int degree){
		double[] smoothedData = new double[inputYData.length];
		for(int i = 0; i < smoothedData.length; i++){
			//System.out.println(i);
			int begin = i - range / 2;
			int end = i + range / 2;
			if(begin < 0)
				begin = 0;
			if(end >= inputYData.length)
				end = inputYData.length - 1;
			
			//this is somewhat slow, could be improved by only sendind data in range.
			//System.out.println("array Length: " + Arrays.copyOfRange(inputXData, begin, end+1).length);
			//System.out.println("Ending at: " + (end - begin));
			
			
			double[] fittedData = SPOperations.getFittedData(Arrays.copyOfRange(inputXData, begin, end + 1), Arrays.copyOfRange(inputYData, begin, end + 1), 0, end - begin, degree);
			
			smoothedData[i] = fittedData[i - begin];
			
		}
		return smoothedData;
	}
	
	public double[] smoothArray( double[] values, double smoothing ){
		  double value = values[0]; // start with the first input
		  for (int i=1; i < values.length; ++i){
		    double currentValue = values[i];
		    value += (currentValue - value) / smoothing;
		    values[i] = value;
		  }
		  return values;
		}
	
//	public double[] filterData(double[] data, int range){
//		double[] output = new double[data.length];
//		for(int i = 0; i < data.length - range; i++){
//			int begin = i - range / 2;
//			int end = i + range / 2;
//			if(begin < 0)
//				begin = 0;
//			if(end >= data.length)
//				end = data.length - 1;
//			output[i] = average(data, begin, end);
//		}
//		return output;
//	}
	
	public double average(double[] a, int beginInclusive, int endInclusive){
		double sum = 0;
		for(int i = beginInclusive; i <= endInclusive; i++){
			sum += a[i];
		}
		return sum / (endInclusive - beginInclusive + 1);
	}
	
	public double[] lowPassFilter(double[] data, double d){
		double[] signal = data;
		
		double[] filter = new double[(int)(d)]; // box-car filter
		for(int i = 0; i < filter.length; i++){
			filter[i] = 1 / (double)filter.length;
		}
		double[] result = new double[signal.length + filter.length + 1];

		// Set result to zero:
		for (int i = 0; i < result.length; i++) {
			result[i] = 0;
		}

		// Do convolution:
		for (int i=0; i < signal.length; i++) 
		  for (int j=0; j < filter.length; j++)
		    result[i+j] = result[i+j] + signal[i] * filter[j];
		
		return result;
	}
	

//	function smoothArray( values, smoothing ){
//		  var value = values[0]; // start with the first input
//		  for (var i=1, len=values.length; i<len; ++i){
//		    var currentValue = values[i];
//		    value += (currentValue - value) / smoothing;
//		    values[i] = value;
//		  }
//		}

	public void update(){
		updateListView();
		updateChart();
		if(listView.getSelectionModel().selectedIndexProperty().getValue() == -1){
		listView.getSelectionModel().select(0);
	
	}
		
	}

	private void updateChart() {
		if(listView.getSelectionModel().getSelectedIndex() == -1)
			return;
		double[] xData = getActivatedData().Data.timeData;
		double[] yData = getActivatedData().Data.data;
		double[] filteredYData = yData.clone();
		double[] pochammerAdjustedData = new double[getActivatedData().getEnd() - getActivatedData().getBegin() + 1];
		double[] zeroedData = yData.clone();
		
		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
			filteredYData = SPMath.fourierLowPassFilter(filteredYData, getActivatedData().modifiers.getLowPassModifier().getLowPassValue(), 1.0 / (xData[1] - xData[0]));
		}
		if(getActivatedData().modifiers.getZeroModifier().activated.get()){
			zeroedData = SPMath.subtractFrom(zeroedData, ((ZeroOffset)getActivatedData().modifiers.getModifier(ModifierEnum.ZERO)).getZero());
		}
		
		if(getActivatedData().modifiers.getPochammerModifier().activated.get()){
			if(getActivatedData() instanceof HopkinsonBarPulse){
				HopkinsonBarPulse pulse = (HopkinsonBarPulse)getActivatedData();
				pochammerAdjustedData = pulse.getPochammerAdjustedArray(barSetup);
			}
		}
		
		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> series2 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> series3 = new XYChart.Series<Number, Number>();
		XYChart.Series<Number, Number> zeroedSeries = new XYChart.Series<Number, Number>();
        series1.setName("Raw Data");
        series2.setName("Filtered");
        series3.setName("Pochammer-Chree Dispersion");
        zeroedSeries.setName("Zeroed");
        chart.setCreateSymbols(false);
        
        ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> filteredDataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> pochammerDataPoints = new ArrayList<Data<Number, Number>>();
        ArrayList<Data<Number, Number>> zeroedDataPoints = new ArrayList<Data<Number, Number>>();
        
        int beginIndex = getActivatedData().getIndexFromTimeValue(xAxis.getLowerBound());
        int endIndex = getActivatedData().getIndexFromTimeValue(xAxis.getUpperBound());
        if(xAxis.isAutoRanging()){
        	//it is zoomed out
        	beginIndex = 0;
        	endIndex = xData.length - 1;
        }
        int totalDataPoints = endIndex - beginIndex;
        
        int previousPochammerIndex = beginIndex;
        for(int i = beginIndex; i <= endIndex; i++){
        	if(logCB.isSelected()){
        		if(yData[i] == 0 || Math.log(Math.abs(yData[i])) > 50){
        			dataPoints.add(new Data<Number, Number>(xData[i], 0));
        		}
        		else{
        			dataPoints.add(new Data<Number, Number>(xData[i], Math.log(Math.abs(yData[i]))));
        		}
        		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
        			if(filteredYData[i] == 0 || Math.log(Math.abs(filteredYData[i])) > 50){
        				filteredDataPoints.add(new Data<Number, Number>(xData[i], 0));
            		}
            		else{
            			filteredDataPoints.add(new Data<Number, Number>(xData[i], Math.log(Math.abs(filteredYData[i]))));
            		}
            	}
        	}
        	else{
        		dataPoints.add(new Data<Number, Number>(xData[i], yData[i]));
        		if(getActivatedData().modifiers.getModifier(ModifierEnum.ZERO).activated.get())
        			zeroedDataPoints.add(new Data<Number, Number>(xData[i], zeroedData[i]));
        		if(getActivatedData().modifiers.getLowPassModifier().activated.get()){
            		filteredDataPoints.add(new Data<Number, Number>(xData[i], filteredYData[i]));
            	}
        		if(getActivatedData().modifiers.getPochammerModifier().activated.get()){
        			if(i >= getActivatedData().getBegin() && i <= getActivatedData().getEnd()){
        				int pochammerIndex = (int)((i - getActivatedData().getBegin()) / PochammerChreeDispersion.skip);
    					if (pochammerIndex != previousPochammerIndex) {
    						pochammerDataPoints.add(new Data<Number, Number>(xData[i],
    								pochammerAdjustedData[pochammerIndex]));
    						previousPochammerIndex = pochammerIndex;
    					}
        			}
        			
        		}
        	}
        	
        	i += totalDataPoints / dataPointsToShow;
        }
        
        series1.getData().addAll(dataPoints);
        series2.getData().addAll(filteredDataPoints);
        series3.getData().addAll(pochammerDataPoints);
        zeroedSeries.getData().addAll(zeroedDataPoints);
//        for(int i = 0; i < xData.length; i++){
//            series1.getData().add(new Data<Number, Number>(xData[i], yData[i]));
//            i += xData.length / dataPointsToShow;
//        }
        
        
        chart.getData().clear();
        chart.getData().addAll(series1);
        chart.getData().addAll(series2);
        chart.getData().addAll(series3);
        chart.getData().addAll(zeroedSeries);
        
        updateAnnotations();
	}
	
	public void updateAnnotations(){
		chart.clearVerticalMarkers();
        chart.addVerticalValueMarker(new Data<Number, Number>(greyLineVal, 0));
        chart.addVerticalRangeMarker(new Data<Number, Number>(getActivatedData().Data.timeData[getActivatedData().getBegin()], 
        		getActivatedData().Data.timeData[getActivatedData().getEnd()]), Color.BLUE);
	}
	
	public DataSubset getActivatedData() {
		int selectedIndex = listView.getSelectionModel().getSelectedIndex();
		return DataFiles.getAllDatasets().get(selectedIndex);
		
	}
	
	private void updateListView() {
		
//		ObservableList<String> items = FXCollections.observableArrayList (
//			    "Single", "Double", "Suite", "Family App");
//		ArrayList<String> dataDescriptors = new ArrayList<String>();
//		for(DataSubset d : DataFiles.getAllDatasets()){
//			dataDescriptors.add(d.name);
//		}
//		ObservableList<String> items = FXCollections.observableArrayList (dataDescriptors);
		ObservableList<DataSubset> subsets = FXCollections.observableArrayList (DataFiles.getAllDatasets());
		listView.setItems(subsets);
	}
	
	private void updateControls(){
		modifierChoiceBox.getItems().clear();
		modifierChoiceBox.getItems().addAll(getActivatedData().modifiers);
		modifierChoiceBox.getSelectionModel().select(0);
		
		while(beginEndHBox.getChildren().size() > 3)
			beginEndHBox.getChildren().remove(beginEndHBox.getChildren().size() - 1);
		if(getActivatedData() instanceof ReflectedPulse){
			beginEndHBox.getChildren().add(getReflectedBeginFromIncidentButton);
		}
		
		
	}
	
	private void updateModifierControls(){
		modifierControlsHBox.getChildren().clear();
		if(modifierChoiceBox.getSelectionModel().getSelectedItem() == null)
			modifierChoiceBox.getSelectionModel().select(0);
		Modifier m = modifierChoiceBox.getSelectionModel().getSelectedItem();
		if(m == null)
			return;
		for(Node node : m.getTrimDataHBoxControls())
			modifierControlsHBox.getChildren().add(node);
	}
	
	private void setReflectedBeginFromIncidentAndBarSetup(){
		int incidentCount = 0;
		IncidentPulse incidentPulse = null;
		for(DataSubset sub : DataFiles.getAllDatasets()){
			if(sub instanceof IncidentPulse){
				incidentCount++;
				incidentPulse = (IncidentPulse)sub;
			}
		}
		if(incidentCount != 1){
			Dialogs.showAlert("There must be 1 incident pulse.", stage);
			return;
		}
		ReflectedPulse reflectedPulse = (ReflectedPulse)getActivatedData();
		
		
		double beginIncidentTime = incidentPulse.Data.timeData[incidentPulse.getBegin()];
		double IncidWaveSpeed = barSetup.IncidentBar.getWaveSpeed();
		double timeToTravel = incidentPulse.strainGauge.distanceToSample / IncidWaveSpeed + 
				reflectedPulse.strainGauge.distanceToSample / IncidWaveSpeed; //distances to sample are the same. Same SG
		reflectedPulse.setBeginFromTimeValue(beginIncidentTime + timeToTravel);
		updateChart();
	}
	
}
