package net.relinc.viewer.GUI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;
import net.relinc.datafileparser.application.Home;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


// one json file?
// All controls in left pane?
// - new
// - delete
// - export
// - import
// - Parameter implementations? Option from top-level window?
// - Parameter implementation editable?
// - Which units to save in?

public class NewReferenceController implements Initializable {
    Stage stage;

    List<Double> strain = new ArrayList();
    List<Double> stress = new ArrayList();



    @FXML
    LineChart<NumberAxis, NumberAxis> chart;

    @FXML
    RadioButton englishRadioButton;
    @FXML
    RadioButton metricRadioButton;

    ToggleGroup unitsToggleGroup = new ToggleGroup();


    @FXML
    TextField stressConversionTextBox;

    @FXML
    RadioButton inputTrueRadioButton;
    @FXML
    RadioButton inputEngineeringRadioButton;

    ToggleGroup inputEngineeringTrueToggleGroup = new ToggleGroup();


    @FXML
    RadioButton chartEngineeringRadioButton;
    @FXML
    RadioButton chartTrueRadioButton;

    ToggleGroup chartEngineeringTrueToggleGroup = new ToggleGroup();

    @FXML
    TextField nameTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        englishRadioButton.setSelected(true);
        englishRadioButton.setToggleGroup(unitsToggleGroup);
        metricRadioButton.setToggleGroup(unitsToggleGroup);

        inputEngineeringRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);
        inputTrueRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);

        chartEngineeringRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);
        chartTrueRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);

        unitsToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                render();
            }
        });
    }

    @FXML
    public void mpaButtonClicked() {
        stressConversionTextBox.setText("0.145038");
    }

    @FXML
    public void ksiButtonClicked() {
        stressConversionTextBox.setText("1");
    }

    @FXML
    public void paButtonClicked() {
        stressConversionTextBox.setText("1.45038e-7");
    }

    @FXML
    public void psiButtonClicked() {
        stressConversionTextBox.setText("0.001");

    }

    @FXML
    public void applyButtonClicked() {
        render();
    }

    private List<Double> toPa(List<Double> inputStress) {
        List<Double> result = new ArrayList<Double>();
        Double conversionFactor = Double.parseDouble(stressConversionTextBox.getText());
        for(int i = 0; i < inputStress.size(); i++) {
            Double val = conversionFactor * inputStress.get(i); // convert to psi
            result.add(val);
        }
        return result;
    }

    private List<Double> toChartUnit(List<Double> stressPa) {
        List<Double> result = new ArrayList<Double>();

        for(int i = 0; i < stressPa.size(); i++) {
            Double val = stressPa.get(i);

            // if english, convert to ksi
            if(englishRadioButton.isSelected()) {
                val = val * 1.45038e-7;
            } else {
                val = val * 1e-6;
            }
            result.add(val);
        }

        if(chartEngineeringRadioButton.isSelected() && inputTrueRadioButton.isSelected()) {
            // convert from true stress to engineering stress


        } else if(chartTrueRadioButton.isSelected() && inputEngineeringRadioButton.isSelected()) {
            // convert from engineering stress to true stress

        }

        return result;
    }



    public void render() {
        chart.getXAxis().setLabel("Strain");
        chart.getYAxis().setLabel("Stress");
        chart.setTitle("Stress-Strain Reference");
        chart.getData().clear();

        List<Double> convertedStress = toChartUnit(toPa(stress));

        XYChart.Series series = new XYChart.Series();
        for(int i = 0; i < strain.size(); i++) {
            series.getData().add(new XYChart.Data(strain.get(i), convertedStress.get(i)));
        }

        chart.getData().add(series);
    }

    @FXML
    public void importXYClicked() {
        Stage anotherStage = new Stage();
        List<List<String>> output = new ArrayList<List<String>>();
        new Home(anotherStage, output);
        anotherStage.showAndWait();

        int numRows = output.get(0).size();

        strain.clear();
        stress.clear();
        for(int i = 0; i < numRows; i++) {
            strain.add(Double.parseDouble(output.get(0).get(i)));
            stress.add(Double.parseDouble(output.get(1).get(i)));
        }

        render();
    }



}

