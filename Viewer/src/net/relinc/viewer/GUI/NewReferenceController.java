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
import net.relinc.libraries.referencesample.ReferenceSample;
import net.relinc.libraries.referencesample.ReferenceSampleXY;
import net.relinc.libraries.referencesample.StressStrain;
import net.relinc.libraries.referencesample.StressStrainMode;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;


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

    List<Double> rawStrainData = new ArrayList();
    List<Double> rawStressData = new ArrayList();

    // private StressStrain stressStrain = new StressStrain(new ArrayList<Double>(), new ArrayList<Double>(), StressStrainMode.ENGINEERING);



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

        inputEngineeringRadioButton.setSelected(true);
        inputEngineeringRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);
        inputTrueRadioButton.setToggleGroup(inputEngineeringTrueToggleGroup);

        chartEngineeringRadioButton.setSelected(true);
        chartEngineeringRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);
        chartTrueRadioButton.setToggleGroup(chartEngineeringTrueToggleGroup);


        Stream.of(unitsToggleGroup, inputEngineeringTrueToggleGroup, chartEngineeringTrueToggleGroup).forEach(toggleGroup -> {
            toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
                @Override
                public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                    render();
                }
            });
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
            Double val = inputStress.get(i) * conversionFactor  * 6.895e+6; // convert to Pa
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
                // metric convert to Kpa
                val = val * 1e-6;
            }
            result.add(val);
        }

        return result;
    }


    private StressStrain getStressStrainFromInput() {
        return new StressStrain(toPa(rawStressData), rawStrainData, inputEngineeringRadioButton.isSelected() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE);
    }


    public void render() {
        chart.getXAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Strain (" + (englishRadioButton.isSelected() ? "in/in" : "mm/mm") + ")");
        chart.getYAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Stress (" + (englishRadioButton.isSelected() ? "Ksi" : "Mpa") + ")");
        chart.setTitle("Stress-Strain Reference");
        chart.getData().clear();
        chart.animatedProperty().setValue(false);

        StressStrain inputStressStrain = getStressStrainFromInput();

        StressStrain convertedStressStrain = chartEngineeringRadioButton.isSelected() ? StressStrain.toEngineering(inputStressStrain) : StressStrain.toTrue(inputStressStrain);

        List<Double> chartStress = toChartUnit(convertedStressStrain.getStress());

        XYChart.Series series = new XYChart.Series();
        for(int i = 0; i < convertedStressStrain.getStrain().size(); i++) {
            series.getData().add(new XYChart.Data(convertedStressStrain.getStrain().get(i), chartStress.get(i)));
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

        rawStrainData.clear();
        rawStressData.clear();
        for(int i = 0; i < numRows; i++) {
            rawStrainData.add(Double.parseDouble(output.get(0).get(i)));
            rawStressData.add(Double.parseDouble(output.get(1).get(i)));
        }

        render();
    }

    public static void saveReference(ReferenceSample s, String name) {
        // Save to file.
        String jsonString = s.getJson();

        SPOperations.writeStringToFile(jsonString, SPSettings.referencesLocation + "/" + name + ".json");

    }

    @FXML
    public void saveButtonClicked() {
        ReferenceSampleXY reference = new ReferenceSampleXY(nameTextField.getText(), getStressStrainFromInput(), null);

        saveReference(reference, nameTextField.getText());

        stage.close();
    }


}

