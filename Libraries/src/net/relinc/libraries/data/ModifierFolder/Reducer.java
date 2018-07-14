package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.SPSettings;

public class Reducer extends Modifier{

	private Integer origStartIndex = null;
	private Integer origEndIndex = null;
	private String reducerDescription = "Data Reducer BETA"; //for file writing.
	private int pointsToKeep;
	
	NumberTextField valueTF;
	HBox holdGrid = new HBox();
	
	public Reducer() {
		modifierEnum = ModifierEnum.REDUCER;
		valueTF = new NumberTextField("points", "points");
		valueTF.setText("1000");
		valueTF.updateLabelPosition();
		GridPane grid = new GridPane();

		grid.add(valueTF, 0, 0);
		grid.add(valueTF.unitLabel, 0, 0);
		
		holdGrid.getChildren().add(grid);
		holdGrid.setAlignment(Pos.CENTER);
		
		checkBox = new CheckBox("Enable Data Reducer");
		checkBox.selectedProperty().bindBidirectional(activated);
		checkBox.disableProperty().bind(enabled.not());
	}
	
	@Override
	public String toString() {
		return "Data Reducer";
	}

	@Override
	public double[] applyModifierToData(double[] fullData, DataSubset activatedData) {
		//this doesn't work. Need to change the data arrays in the dataSubset and re-render everything...
		
		if(pointsToKeep <= 0 || pointsToKeep > fullData.length)
			return fullData;
		
		//keep track of the original start index because it will change when the data is reduced.
		if(!(enabled.get() && activated.get())){
			if(origStartIndex != null && activatedData != null)
				activatedData.setBegin(origStartIndex);
			if(origEndIndex != null && activatedData != null)
				activatedData.setEnd(origEndIndex);
			return fullData;
		}
		
		//set the original if it's not been set.
		if(origStartIndex == null && activatedData != null)
			origStartIndex = activatedData.getBegin();
		if(origEndIndex == null && activatedData != null)
			origEndIndex = activatedData.getEnd();
		//move pointsToKeep to the end.
		int space = fullData.length / pointsToKeep;
		if(space == 0)
			space = 1;
		pointsToKeep = fullData.length / space;
		double[] sparse = new double[Math.min(fullData.length, pointsToKeep)];
		
		int idx = 0;
		for(int i = 0; i < sparse.length; i++){
			sparse[i] = fullData[idx];
			idx += space;
		}
		if(activatedData != null){
			activatedData.setBegin(activatedData.getBegin() / space);
			activatedData.setEnd(activatedData.getEnd() / space);
		}
		
		//the data needs to be re-rendered after the getTrimmedData returns different stuff.
		//The correlation changes. Need to make a flag for each dataset that says if it needs to be re-rendered...
		
		return sparse;
	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		ArrayList<Node> list = new ArrayList<>();
		list.add(holdGrid);
		return list;
	}

	@Override
	public String getStringForFileWriting() {
		return enabled.get() ? reducerDescription + ":" + pointsToKeep + SPSettings.lineSeperator : "";
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(reducerDescription)){
			//it was saved, so it is enabled
			pointsToKeep = Integer.parseInt(val);
			enabled.set(true);
			activated.set(true);
		}
	}

	@Override
	public void readModifierFromString(String line) {
		setValuesFromLine(line);
	}

	@Override
	public void configureModifier(DataSubset dataSubset) {
		pointsToKeep = valueTF.getDouble().intValue();
	}

	public double getPointsToKeep() {
		return pointsToKeep;
	}

	public void setPointsToKeep(int pointsToKeep) {
		this.pointsToKeep = pointsToKeep;
	}

}
