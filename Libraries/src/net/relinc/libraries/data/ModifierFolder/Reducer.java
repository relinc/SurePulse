package net.relinc.libraries.data.ModifierFolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import net.relinc.libraries.data.DataSubset;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.SPSettings;

public class Reducer extends Modifier{

	private String reducerDescription = "Data Reducer BETA"; //for file writing.
	private int userDataPoints; // User can define how many datapoints, either higher or lower than the original data set.

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
	public ModifierResult applyModifier(double[] x, double[] y, DataSubset data) {
		if(shouldApply()) {
			double[] newX = this.sampleData(x, this.userDataPoints);
			double[] newY = this.applyModifierToData(x, y, data);
			return new ModifierResult(newX, newY, (this.userDataPoints - 1.0) / (x.length - 1.0));
		}
		return new ModifierResult(x, y, 1.0);
	}

	public double[] applyModifierToData(double[] time, double[] fullData, DataSubset activatedData) {
		// If the fitter polynomial modifier is activated, we should use that for interpolation.
		// Else, do linear interpolation between the two closest points.


		Fitter fitter = activatedData == null ? null : activatedData.getModifiers().getFitterModifier(); // kinda weird
		if(fitter != null && fitter.activated.get() && fitter.enabled.get()) {

			fitter.applyModifierToData(time, fullData); // this has side effects on `fitter`


			double[] sampledTime = sampleData(time, userDataPoints);
			fullData = Arrays.stream(sampledTime).map(t -> fitter.fitable.computeY(t)).toArray();
		} else {
			fullData = sampleData(fullData, userDataPoints);
		}

		return fullData;

	}

	@Override
	public List<Node> getTrimDataHBoxControls() {
		ArrayList<Node> list = new ArrayList<>();
		list.add(holdGrid);
		return list;
	}

	@Override
	public String getStringForFileWriting() {
		return enabled.get() ? reducerDescription + ":" + userDataPoints + SPSettings.lineSeperator : "";
	}

	@Override
	public void setValuesFromDescriptorValue(String descrip, String val) {
		if(descrip.equals(reducerDescription)){
			//it was saved, so it is enabled
			userDataPoints = Integer.parseInt(val);
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
		userDataPoints = valueTF.getDouble().intValue();
	}

	public double getUserDataPoints() {
		return userDataPoints;
	}

	public void setUserDataPoints(int userDataPoints) {
		this.userDataPoints = userDataPoints;
	}


	public static double[] sampleData(double[] source, int numDataPoints) {
		if(numDataPoints == source.length) {
			return source;
		} else if(numDataPoints == 0) {
			return new double[0];
		} else {
			double[] result = new double[numDataPoints];
			double indexRatio = (source.length-1) / (1.0 * (numDataPoints - 1));
			for(int i = 0; i < result.length; i++) {
				double origIndex = i * indexRatio;
				int origIndexLower = (int)origIndex;
				int origIndexUpper = origIndexLower + 1;
				double lowerPart = 1.0 - (origIndex - origIndexLower);
				double upperPart = 1.0 - lowerPart;

				if(i == result.length - 1) {
					result[i] = source[origIndexLower];
				} else {
					try{
						result[i] = lowerPart * source[origIndexLower] + upperPart * source[origIndexUpper];
					} catch(Exception e) {
						e.printStackTrace();
					}
				}


			}
			return result;
		}
	}

}

