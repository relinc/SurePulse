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

/**
 * Created by mark on 1/6/20.
 */
public class NewReferenceController implements Initializable {
    Stage stage;

    List<Double> strain = new ArrayList();
    List<Double> stress = new ArrayList();

    ToggleGroup unitsToggleGroup = new ToggleGroup();


    @FXML
    LineChart<NumberAxis, NumberAxis> chart;

    @FXML
    RadioButton englishRadioButton;

    @FXML
    RadioButton metricRadioButton;

    @FXML
    TextField stressConversionTextBox;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        englishRadioButton.setSelected(true);
        englishRadioButton.setToggleGroup(unitsToggleGroup);
        metricRadioButton.setToggleGroup(unitsToggleGroup);

        unitsToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                render();
            }
        });
    }

    @FXML
    public void mpaButtonClicked() {
        stressConversionTextBox.setText("145.038");
    }

    @FXML
    public void ksiButtonClicked() {
        stressConversionTextBox.setText("1000");
    }

    @FXML
    public void paButtonClicked() {
        stressConversionTextBox.setText("0.000145038");
    }

    @FXML
    public void psiButtonClicked() {
        stressConversionTextBox.setText("1");

    }

    @FXML
    public void applyButtonClicked() {
        render();
    }

    private List<Double> convertStress(List<Double> stress) {
        // multiply by conversion factor
        List<Double> result = new ArrayList<Double>();

        Double conversionFactor = Double.parseDouble(stressConversionTextBox.getText());

        for(int i = 0; i < stress.size(); i++) {
            Double val = conversionFactor * stress.get(i); // convert to psi

            // if english, convert to ksi
            if(englishRadioButton.isSelected()) {
                val = val * .001;

            } else {
                val = val * 6.89476;
            }
            result.add(val);
        }

        return result;
    }

    public void render() {
        chart.getXAxis().setLabel("Strain");
        chart.getYAxis().setLabel("Stress");
        chart.setTitle("Stress-Strain Reference");
        chart.getData().clear();

        List<Double> convertedStress = convertStress(stress);

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
            stress.add(Double.parseDouble(output.get(0).get(i)));
        }

        render();
    }
}
