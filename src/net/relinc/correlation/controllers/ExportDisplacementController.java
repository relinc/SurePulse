package net.relinc.correlation.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.relinc.correlation.application.Target;
import net.relinc.correlation.staticClasses.SPTargetTracker;
import net.relinc.correlation.staticClasses.SPTargetTracker.DisplacementDirection;

public class ExportDisplacementController {
	@FXML RadioButton xDisplacementRadioButton;
	@FXML RadioButton yDisplacementRadioButton;
	@FXML RadioButton xyDisplacementRadioButton;
	@FXML LineChart<Number, Number> chart;
	@FXML ListView<Target> targetsListView;
	@FXML Button exportButton;
	
	ToggleGroup group = new ToggleGroup();
	public double inchToPixelRatio;
	public double lengthOfSample;
	public boolean useSmoothedPoints;
	public int beginIndex;
	public List<File> imagePaths;
	public Stage stage;
	public boolean exportToProcessor;
	public List<Double> displacement;
	
	public void initialize(){
		xDisplacementRadioButton.setToggleGroup(group);
		xDisplacementRadioButton.setText("Horizontal Displacement (X axis)");
		yDisplacementRadioButton.setToggleGroup(group);
		yDisplacementRadioButton.setText("Vertical Displacement (Y axis)");
		xyDisplacementRadioButton.setToggleGroup(group);
		xyDisplacementRadioButton.setText("Euclidean Distance Displacement");
		
		targetsListView.setCellFactory(new Callback<ListView<Target>, ListCell<Target>>() {
		      @Override
		      public CheckBoxListCell<Target> call(ListView<Target> listView) {
		        final CheckBoxListCell<Target> listCell = new CheckBoxListCell<Target>()
		        {
		          @Override
		          public void updateItem(Target item, boolean empty) {
		            super.updateItem(item, empty);
		            if (empty) {
		              setText(null);
		            } else {
		              setText(item.getName());
		            }
		          }
		        };
		        listCell.setSelectedStateCallback(new Callback<Target, ObservableValue<Boolean>>() {
					@Override
					public ObservableValue<Boolean> call(Target param) {
						return param.selectedProperty();
					}
				});
		        return listCell;
		      }
		    });
		
		xDisplacementRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderChart();
			}
		});
		yDisplacementRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderChart();
			}
		});
		xyDisplacementRadioButton.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				renderChart();
			}
		});
		
		
		
		exportButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				double[] dis = getDisplacement();
				if(dis != null)
					Arrays.stream(dis).boxed().collect(Collectors.toList()).stream().forEach(d -> displacement.add(d));;
			    Stage stage = (Stage)exportButton.getScene().getWindow();
			    stage.close();
			}
		});
	}
	
	public void renderChart() {
		System.out.println("Render chart fired");
		double[] displacement = getDisplacement();
		chart.getData().clear();
		if(displacement == null)
			return;
		System.out.println("adding data");
		XYChart.Series<Number, Number> series1 = new XYChart.Series<Number, Number>();
		series1.setName("Displacement");
		chart.setTitle("Displacement");

		ArrayList<Data<Number, Number>> dataPoints = new ArrayList<Data<Number, Number>>();

		for (int i = 0; i < displacement.length; i++)
			dataPoints.add(new Data<Number, Number>(i, displacement[i]));

		series1.getData().addAll(dataPoints);

		chart.getData().addAll(series1);

	}
	
	public double[] getDisplacement(){
		List<Target> selectedItems = targetsListView.getItems().stream().filter(t -> t.selectedProperty().get()).collect(Collectors.toList());
		if(selectedItems.size() <= 0 || selectedItems.size() >= 3)
			return null;
		if(selectedItems.size() == 2){
			Target t1 = selectedItems.get(0);
			Target t2 = selectedItems.get(1);
			return SPTargetTracker.calculateRelativeDisplacement(t1, t2, inchToPixelRatio, useSmoothedPoints, getDisplacementDirection());
		}
		else if(selectedItems.size() == 1){
			Target t1 = selectedItems.get(0);
			return SPTargetTracker.calculateDisplacement(t1, inchToPixelRatio, useSmoothedPoints, getDisplacementDirection());
		}
		else{
			System.out.println("Giraffe");
			return null;
		}
		
	}
	
	public DisplacementDirection getDisplacementDirection(){
		Toggle t = group.getSelectedToggle();
		if(t.equals(xDisplacementRadioButton))
			return DisplacementDirection.X;
		else if(t.equals(yDisplacementRadioButton))
			return DisplacementDirection.Y;
		else if(t.equals(xyDisplacementRadioButton))
			return DisplacementDirection.XY;
		else 
			return null;
	}

	public void fillTargetsListView(ObservableList<Target> items) {
		for(Target t : items){
			t.selectedProperty().addListener(new ChangeListener<Boolean>() {
				@Override
				public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
					renderChart();
				}
			});
			targetsListView.getItems().add(t);
		}
		
		targetsListView.getItems().get(0).selectedProperty().set(true);
		targetsListView.getItems().get(1).selectedProperty().set(true);
	}
	
}
