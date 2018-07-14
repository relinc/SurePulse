package net.relinc.shotcaller.GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import net.relinc.libraries.fxControls.NumberTextField;
import net.relinc.libraries.staticClasses.Dialogs;
import net.relinc.libraries.staticClasses.SPOperations;

public class HomeController {
	@FXML GridPane prevGrid;
	@FXML GridPane nextGrid;
	@FXML Button calculateButton;
	NumberTextField prevStrainRate = new NumberTextField("", "");
	NumberTextField prevStrikerBarLength= new NumberTextField("", "");
	NumberTextField prevPressure = new NumberTextField("", "");
	
	NumberTextField nextStrainRate = new NumberTextField("", "");
	NumberTextField nextStrikerBarLength = new NumberTextField("", "");
	NumberTextField nextPressure = new NumberTextField("", "");
	public Stage stage;
	
	public void initialize(){
		int row = 0;
		prevGrid.add(prevStrainRate, 1, row++);
		prevGrid.add(prevStrikerBarLength, 1, row++);
		prevGrid.add(prevPressure, 1, row++);
		
		row = 0;
		nextGrid.add(nextStrainRate, 1, row++);
		nextGrid.add(nextStrikerBarLength, 1, row++);
		nextGrid.add(nextPressure, 1, row++);
		
		calculateButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(prevStrainRate.getDouble() == -1 || prevStrikerBarLength.getDouble() == -1 || prevPressure.getDouble() == -1){
					Dialogs.showErrorDialog("Error", "Not enough values", "You must fill in all the previous values.", stage);
					return;
				}
				int count = 0;
				if(nextStrainRate.getDouble() != -1)
					count++;
				if(nextStrikerBarLength.getDouble() != -1)
					count++;
				if(nextPressure.getDouble() != -1)
					count++;
				
				if(count != 2){
					Dialogs.showErrorDialog("Error", "Incorrect number of values", "You must leave one text box blank.", stage);
					return;
				}
				
				if(nextStrainRate.getDouble() == -1)
					calculateStrainRate();
				else if(nextStrikerBarLength.getDouble() == -1)
					calculateStrikerBarLength();
				else if(nextPressure.getDouble() == -1)
					calculatePressure();
			}
			
		});
	}
	
	private void calculateStrainRate() {
		double sr = prevStrainRate.getDouble() * Math.sqrt(nextPressure.getDouble() / prevPressure.getDouble() * prevStrikerBarLength.getDouble() / nextStrikerBarLength.getDouble());
		nextStrainRate.setNumberText(Double.toString(SPOperations.round(sr, 3)));
	}

	private void calculateStrikerBarLength() {
    	double sb = nextPressure.getDouble() / prevPressure.getDouble() * prevStrikerBarLength.getDouble() / Math.pow(nextStrainRate.getDouble() / prevStrainRate.getDouble(), 2);
        nextStrikerBarLength.setText(Double.toString(SPOperations.round(sb, 3)));
	}

	private void calculatePressure() {
		double pr = Math.pow(nextStrainRate.getDouble() / prevStrainRate.getDouble(), 2) * prevPressure.getDouble() / prevStrikerBarLength.getDouble() * nextStrikerBarLength.getDouble();
		nextPressure.setText(Double.toString(SPOperations.round(pr, 3)));
	}
	
	
}
