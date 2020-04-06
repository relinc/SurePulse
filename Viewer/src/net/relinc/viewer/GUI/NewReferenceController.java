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
import net.relinc.libraries.referencesample.*;
import net.relinc.libraries.staticClasses.SPOperations;
import net.relinc.libraries.staticClasses.SPSettings;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


    @FXML
    LineChart<NumberAxis, NumberAxis> xyChart;

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

    @FXML
    LineChart<NumberAxis, NumberAxis> knChart;

    @FXML
    TextField knYoungsModulusTextField;

    @FXML
    TextField knYieldStressTextField;

    @FXML
    TextField knKTextField;

    @FXML
    TextField knNTextField;

    @FXML
    TextField knNameTextField;


    @FXML
    TextField johnsonCookYoungsModulusTextField;
    @FXML
    TextField johnsonCookReferenceYieldStressTextField;
    @FXML
    TextField johnsonCookStrainRateTextField;

    @FXML
    TextField johnsonCookReferenceStrainRateTextField;
    @FXML
    TextField johnsonCookRoomTemperatureTextField;
    @FXML
    TextField johnsonCookMeltingPointTextField;
    @FXML
    TextField johnsonCookSampleTemperatureTextField;
    @FXML
    TextField johnsonCookYieldStressTextField;
    @FXML
    TextField johnsonCookIntensityCoefficientTextField;
    @FXML
    TextField johnsonCookStrainRateCoefficientTextField;
    @FXML
    TextField johnsonCookStrainHardeningCoefficientTextField;
    @FXML
    TextField johnsonCookThermalSofteningCoefficientTextField;
    @FXML
    TextField johnsonCookNameTextField;

    @FXML
    LineChart<NumberAxis, NumberAxis> johnsonCookChart;


    @FXML
    TextField ludwigYoungsModulusTextField;
    @FXML
    TextField ludwigYieldStressTextField;
    @FXML
    TextField ludwigIntensityCoefficientTextField;
    @FXML
    TextField ludwigStrainHardeningCoefficientTextField;
    @FXML
    TextField ludwigNameTextField;

    @FXML
    LineChart<NumberAxis, NumberAxis> ludwigChart;


    @FXML
    TextField cowperSymondsYoungsModulusTextField;
    @FXML
    TextField cowperSymondsReferenceYieldStressTextField;
    @FXML
    TextField cowperSymondsStrainRateTextField;

    @FXML
    TextField cowperSymondsYieldStressTextField;
    @FXML
    TextField cowperSymondsIntensityCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainRateCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainHardeningCoefficientTextField;
    @FXML
    TextField cowperSymondsStrainRateSensitivityCoefficientTextField;
    @FXML
    TextField cowperSymondsNameTextField;
    @FXML
    LineChart<NumberAxis, NumberAxis> cowperSymondsChart;


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
                    renderAll();
                }
            });
        });


        Stream.of(knYoungsModulusTextField, knYieldStressTextField, knKTextField, knNTextField).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderKN();
                }
            });
        });


        Stream.of(
                johnsonCookYoungsModulusTextField,
                johnsonCookReferenceYieldStressTextField,
                johnsonCookStrainRateTextField,
                johnsonCookReferenceStrainRateTextField,
                johnsonCookRoomTemperatureTextField,
                johnsonCookMeltingPointTextField,
                johnsonCookSampleTemperatureTextField,
                johnsonCookYieldStressTextField,
                johnsonCookIntensityCoefficientTextField,
                johnsonCookStrainRateCoefficientTextField,
                johnsonCookStrainHardeningCoefficientTextField,
                johnsonCookThermalSofteningCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderJohnsonCook();
                }
            });
        });

        Stream.of(
                ludwigYoungsModulusTextField,
                ludwigYieldStressTextField,
                ludwigIntensityCoefficientTextField,
                ludwigStrainHardeningCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderLudwig();
                }
            });
        });


        Stream.of(
                cowperSymondsYoungsModulusTextField,
                cowperSymondsReferenceYieldStressTextField,
                cowperSymondsStrainRateTextField,
                cowperSymondsYieldStressTextField,
                cowperSymondsIntensityCoefficientTextField,
                cowperSymondsStrainRateCoefficientTextField,
                cowperSymondsStrainHardeningCoefficientTextField,
                cowperSymondsStrainRateSensitivityCoefficientTextField
        ).forEach(textField -> {
            textField.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    renderCowperSymonds();
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
        renderXY();
    }

    private List<Double> toPa(List<Double> inputStress) {
        List<Double> result = new ArrayList<Double>();
        Double conversionFactor = Double.parseDouble(stressConversionTextBox.getText());
        for (int i = 0; i < inputStress.size(); i++) {
            Double val = inputStress.get(i) * conversionFactor * 6.895e+6; // convert to Pa
            result.add(val);
        }
        return result;
    }

    @FXML
    public void kn6061Clicked() {
        knYoungsModulusTextField.setText("68.9e9");
        knYieldStressTextField.setText("252e6");
        knKTextField.setText("530e6");
        knNTextField.setText("0.14048");
        knNameTextField.setText("6061 (KN)");
    }

    @FXML
    public void kn7075Clicked() {
        knYoungsModulusTextField.setText("71.7e9");
        knYieldStressTextField.setText("473e6");
        knKTextField.setText("673e6");
        knNTextField.setText("0.045");
        knNameTextField.setText("7075 (KN)");
    }

    @FXML
    public void knSaveButtonClicked() {

        Double youngsMod;
        Double yieldStress;
        Double K;
        Double N;
        try {
            youngsMod = Double.parseDouble(knYoungsModulusTextField.getText());
            yieldStress = Double.parseDouble(knYieldStressTextField.getText());
            K = Double.parseDouble(knKTextField.getText());
            N = Double.parseDouble(knNTextField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse!");
            return;
        }

        ReferenceSampleKN reference = new ReferenceSampleKN(knNameTextField.getText(), null, K, N, youngsMod, yieldStress);

        saveReference(reference, knNameTextField.getText());

        stage.close();
    }


    @FXML
    public void johnsonCook6061Clicked() {
        johnsonCookYoungsModulusTextField.setText("68.9e9");
        johnsonCookReferenceYieldStressTextField.setText("252e6");
        johnsonCookStrainRateTextField.setText("1.0");

        johnsonCookReferenceStrainRateTextField.setText("1.0");
        johnsonCookRoomTemperatureTextField.setText("294.26");
        johnsonCookMeltingPointTextField.setText("925.37");
        johnsonCookSampleTemperatureTextField.setText("300.0");
        johnsonCookYieldStressTextField.setText("252e6");
        johnsonCookIntensityCoefficientTextField.setText("203.4e6");
        johnsonCookStrainRateCoefficientTextField.setText("0.011");
        johnsonCookStrainHardeningCoefficientTextField.setText("0.35");
        johnsonCookThermalSofteningCoefficientTextField.setText("1.34");

        johnsonCookNameTextField.setText("6061 (Johnson Cook)");
    }

    @FXML
    public void johnsonCook7075Clicked() {
        johnsonCookYoungsModulusTextField.setText("71.7e9");
        johnsonCookReferenceYieldStressTextField.setText("4.73e8");
        johnsonCookStrainRateTextField.setText("1.0");

        johnsonCookReferenceStrainRateTextField.setText("1.0");
        johnsonCookRoomTemperatureTextField.setText("294.26");
        johnsonCookMeltingPointTextField.setText("893");
        johnsonCookSampleTemperatureTextField.setText("300.0");
        johnsonCookYieldStressTextField.setText("4.73e8");
        johnsonCookIntensityCoefficientTextField.setText("2.1e8");
        johnsonCookStrainRateCoefficientTextField.setText("0.033");
        johnsonCookStrainHardeningCoefficientTextField.setText("0.3813");
        johnsonCookThermalSofteningCoefficientTextField.setText("1.0");

        johnsonCookNameTextField.setText("7075 (Johnson Cook)");
    }

    @FXML
    public void johnsonCookDqskSteelClicked() {
        johnsonCookYoungsModulusTextField.setText("206e9");
        johnsonCookReferenceYieldStressTextField.setText("13e6");
        johnsonCookStrainRateTextField.setText("1.0");

        johnsonCookReferenceStrainRateTextField.setText("1.0");
        johnsonCookRoomTemperatureTextField.setText("294.26");
        johnsonCookMeltingPointTextField.setText("1808");
        johnsonCookSampleTemperatureTextField.setText("300.0");
        johnsonCookYieldStressTextField.setText("1.3e7");
        johnsonCookIntensityCoefficientTextField.setText("7.3e8");
        johnsonCookStrainRateCoefficientTextField.setText("0.045");
        johnsonCookStrainHardeningCoefficientTextField.setText("0.15");
        johnsonCookThermalSofteningCoefficientTextField.setText("0.5");

        johnsonCookNameTextField.setText("DQSK (Johnson Cook)");
    }

    @FXML
    public void johnsonCookSaveButtonClicked() {
        Optional<ReferenceSampleJohnsonCook> optionalSample = parseJohnsonCookSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, johnsonCookNameTextField.getText());

            stage.close();
        });

    }

    @FXML
    public void ludwig6061Clicked() {
        ludwigYoungsModulusTextField.setText("68.9e9");
        ludwigYieldStressTextField.setText("252e6");

        ludwigIntensityCoefficientTextField.setText("208.8e6");
        ludwigStrainHardeningCoefficientTextField.setText("0.28");

        ludwigNameTextField.setText("6061 (Ludwig)");
    }

    @FXML
    public void ludwig7075Clicked() {
        ludwigYoungsModulusTextField.setText("7.17e10");
        ludwigYieldStressTextField.setText("4.73e8");

        ludwigIntensityCoefficientTextField.setText("1.295e8");
        ludwigStrainHardeningCoefficientTextField.setText("0.293");

        ludwigNameTextField.setText("7075 (Ludwig)");
    }

    @FXML
    public void ludwigSaveButtonClicked() {
        Optional<ReferenceSampleLudwig> optionalSample = parseLudwigSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, ludwigNameTextField.getText());

            stage.close();
        });
    }

    @FXML
    public void cowperSymonds6061Clicked() {
        cowperSymondsYoungsModulusTextField.setText("68.9e9");
        cowperSymondsReferenceYieldStressTextField.setText("252e6");
        cowperSymondsStrainRateTextField.setText("6.7e-4");

        cowperSymondsYieldStressTextField.setText("252e6");
        Double d = 1 * (600e6*72e9) / (72e9 - 600e6);
        cowperSymondsIntensityCoefficientTextField.setText(d.toString());
        cowperSymondsStrainRateCoefficientTextField.setText("25000.0");
        cowperSymondsStrainHardeningCoefficientTextField.setText("1.0");
        cowperSymondsStrainRateSensitivityCoefficientTextField.setText("0.95");

        cowperSymondsNameTextField.setText("6061 (Cowper Symonds)");
    }


    @FXML
    public void cowperSymondsSaveButtonClicked() {
        Optional<ReferenceSampleCowperSymonds> optionalSample = parseCowperSymondsSample();

        optionalSample.ifPresent(sample -> {
            saveReference(sample, cowperSymondsNameTextField.getText());

            stage.close();
        });
    }

    private List<Double> toChartUnit(List<Double> stressPa) {
        List<Double> result = new ArrayList<Double>();

        for (int i = 0; i < stressPa.size(); i++) {
            Double val = stressPa.get(i);

            // if english, convert to ksi
            if (englishRadioButton.isSelected()) {
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

    public void renderAxisLabels() {
        Stream.of(xyChart, knChart, ludwigChart, cowperSymondsChart, johnsonCookChart).forEach(chart -> {
            chart.getXAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Strain (" + (englishRadioButton.isSelected() ? "in/in" : "mm/mm") + ")");
            chart.getYAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Stress (" + (englishRadioButton.isSelected() ? "Ksi" : "Mpa") + ")");
        });
    }


    public void renderXY() {

        renderAxisLabels();
//        xyChart.getXAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Strain (" + (englishRadioButton.isSelected() ? "in/in" : "mm/mm") + ")");
//        xyChart.getYAxis().setLabel((chartEngineeringRadioButton.isSelected() ? "Engineering" : "True") + " Stress (" + (englishRadioButton.isSelected() ? "Ksi" : "Mpa") + ")");
        xyChart.setTitle("Stress-Strain Reference");
        xyChart.getData().clear();
        xyChart.animatedProperty().setValue(false);

        StressStrain inputStressStrain = getStressStrainFromInput();

        StressStrain convertedStressStrain = chartEngineeringRadioButton.isSelected() ? StressStrain.toEngineering(inputStressStrain) : StressStrain.toTrue(inputStressStrain);

        List<Double> chartStress = toChartUnit(convertedStressStrain.getStress());

        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < convertedStressStrain.getStrain().size(); i++) {
            series.getData().add(new XYChart.Data(convertedStressStrain.getStrain().get(i), chartStress.get(i)));
        }

        xyChart.getData().add(series);
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
        for (int i = 0; i < numRows; i++) {
            rawStrainData.add(Double.parseDouble(output.get(0).get(i)));
            rawStressData.add(Double.parseDouble(output.get(1).get(i)));
        }

        renderXY();
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

    public StressStrainMode getStressStrainMode() {
        return chartEngineeringRadioButton.isSelected() ? StressStrainMode.ENGINEERING : StressStrainMode.TRUE;
    }


    public void renderKN() {
        renderAxisLabels();

        System.out.println("Rendering!");
        knChart.getData().clear();
        knChart.animatedProperty().setValue(false);

        Double youngsMod;
        Double yieldStress;
        Double K;
        Double N;
        try {
            youngsMod = Double.parseDouble(knYoungsModulusTextField.getText());
            yieldStress = Double.parseDouble(knYieldStressTextField.getText());
            K = Double.parseDouble(knKTextField.getText());
            N = Double.parseDouble(knNTextField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse!");
            return;
        }

        ReferenceSampleKN knSample = new ReferenceSampleKN("render", "TEST", K, N, youngsMod, yieldStress);

        List<Double> chartStress = toChartUnit(knSample.getStress(getStressStrainMode() , StressUnit.PA));
        List<Double> chartStrain = knSample.getStrain(getStressStrainMode());

        XYChart.Series series = new XYChart.Series();
        for (int i = 0; i < chartStress.size(); i++) {
            series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
        }

        knChart.getData().add(series);
    }

    public Optional<ReferenceSampleJohnsonCook> parseJohnsonCookSample() {
        Double YoungsModulus;
        Double ReferenceYieldStress;
        Double StrainRate;

        Double ReferenceStrainRate;
        Double RoomTemperature;
        Double MeltingPoint;
        Double SampleTemperature;
        Double YieldStress;
        Double IntensityCoefficient;
        Double StrainRateCoefficient;
        Double StrainHardeningCoefficient;
        Double ThermalSofteningCoefficient;

        try {
            YoungsModulus = Double.parseDouble(johnsonCookYoungsModulusTextField.getText());
            ReferenceYieldStress = Double.parseDouble(johnsonCookReferenceYieldStressTextField.getText());
            StrainRate = Double.parseDouble(johnsonCookStrainRateTextField.getText());

            ReferenceStrainRate = Double.parseDouble(johnsonCookReferenceStrainRateTextField.getText());
            RoomTemperature = Double.parseDouble(johnsonCookRoomTemperatureTextField.getText());
            MeltingPoint = Double.parseDouble(johnsonCookMeltingPointTextField.getText());
            SampleTemperature = Double.parseDouble(johnsonCookSampleTemperatureTextField.getText());
            YieldStress = Double.parseDouble(johnsonCookYieldStressTextField.getText());
            IntensityCoefficient = Double.parseDouble(johnsonCookIntensityCoefficientTextField.getText());
            StrainRateCoefficient = Double.parseDouble(johnsonCookStrainRateCoefficientTextField.getText());
            StrainHardeningCoefficient = Double.parseDouble(johnsonCookStrainHardeningCoefficientTextField.getText());
            ThermalSofteningCoefficient = Double.parseDouble(johnsonCookThermalSofteningCoefficientTextField.getText());
        } catch (NumberFormatException e) {
            System.out.println("Failed to parse JC params.");
            return Optional.empty();
        }

        ReferenceSampleJohnsonCook sample = new ReferenceSampleJohnsonCook(
                johnsonCookNameTextField.getText(),
                "TEST",

                YoungsModulus,
                ReferenceYieldStress,
                StrainRate,

                ReferenceStrainRate,
                RoomTemperature,
                MeltingPoint,
                SampleTemperature,
                YieldStress,
                IntensityCoefficient,
                StrainRateCoefficient,
                StrainHardeningCoefficient,
                ThermalSofteningCoefficient
        );

        return Optional.of(sample);
    }

    public void renderJohnsonCook() {
        renderAxisLabels();
        johnsonCookChart.getData().clear();
        johnsonCookChart.animatedProperty().setValue(false);

        Optional<ReferenceSampleJohnsonCook> sampleOptional = parseJohnsonCookSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(getStressStrainMode(), StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(getStressStrainMode());

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            johnsonCookChart.getData().add(series);
        });


    }

    public Optional<ReferenceSampleLudwig> parseLudwigSample() {
        Double youngsModulus;
        Double yieldStress;
        Double intensityCoefficient;
        Double strainHardeningCoefficient;

        try {
            youngsModulus = Double.parseDouble(ludwigYoungsModulusTextField.getText());
            yieldStress = Double.parseDouble(ludwigYieldStressTextField.getText());

            intensityCoefficient = Double.parseDouble(ludwigIntensityCoefficientTextField.getText());
            strainHardeningCoefficient = Double.parseDouble(ludwigStrainHardeningCoefficientTextField.getText());
        } catch(NumberFormatException e) {
            return Optional.empty();
        }

        return Optional.of(new ReferenceSampleLudwig(
                ludwigNameTextField.getText(),
                "",
                youngsModulus,
                yieldStress,
                intensityCoefficient,
                strainHardeningCoefficient
        ));

    }

    public void renderLudwig() {
        renderAxisLabels();
        ludwigChart.getData().clear();
        ludwigChart.animatedProperty().setValue(false);

        Optional<ReferenceSampleLudwig> sampleOptional = parseLudwigSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(getStressStrainMode(), StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(getStressStrainMode());

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            ludwigChart.getData().add(series);
        });
    }

    public Optional<ReferenceSampleCowperSymonds> parseCowperSymondsSample() {
        Double YoungsModulusTextField;
        Double ReferenceYieldStressTextField;
        Double StrainRateTextField;

        Double YieldStressTextField;
        Double IntensityCoefficientTextField;
        Double StrainRateCoefficientTextField;
        Double StrainHardeningCoefficientTextField;
        Double StrainRateSensitivityCoefficientTextField;

        try {
            YoungsModulusTextField = Double.parseDouble(cowperSymondsYoungsModulusTextField.getText());
            ReferenceYieldStressTextField = Double.parseDouble(cowperSymondsReferenceYieldStressTextField.getText());
            StrainRateTextField = Double.parseDouble(cowperSymondsStrainRateTextField.getText());

            YieldStressTextField = Double.parseDouble(cowperSymondsYieldStressTextField.getText());
            IntensityCoefficientTextField = Double.parseDouble(cowperSymondsIntensityCoefficientTextField.getText());
            StrainRateCoefficientTextField = Double.parseDouble(cowperSymondsStrainRateCoefficientTextField.getText());
            StrainHardeningCoefficientTextField = Double.parseDouble(cowperSymondsStrainHardeningCoefficientTextField.getText());
            StrainRateSensitivityCoefficientTextField = Double.parseDouble(cowperSymondsStrainRateSensitivityCoefficientTextField.getText());
        } catch(NumberFormatException e) {
            return Optional.empty();
        }

        return Optional.of(new ReferenceSampleCowperSymonds(
                cowperSymondsNameTextField.getText(),
                "",

                YoungsModulusTextField,
                ReferenceYieldStressTextField,
                StrainRateTextField,

                YieldStressTextField,
                IntensityCoefficientTextField,
                StrainRateCoefficientTextField,
                StrainHardeningCoefficientTextField,
                StrainRateSensitivityCoefficientTextField
        ));
    }

    public void renderCowperSymonds() {
        renderAxisLabels();
        cowperSymondsChart.getData().clear();
        cowperSymondsChart.animatedProperty().setValue(false);

        Optional<ReferenceSampleCowperSymonds> sampleOptional = parseCowperSymondsSample();

        sampleOptional.ifPresent(sample -> {
            List<Double> chartStress = toChartUnit(sample.getStress(getStressStrainMode(), StressUnit.PA));
            List<Double> chartStrain = sample.getStrain(getStressStrainMode());

            XYChart.Series series = new XYChart.Series();
            for (int i = 0; i < chartStress.size(); i++) {
                series.getData().add(new XYChart.Data(chartStrain.get(i), chartStress.get(i)));
            }

            cowperSymondsChart.getData().add(series);
        });
    }


    public void renderAll() {
        renderXY();
        renderKN();
        renderJohnsonCook();
        renderLudwig();
        renderCowperSymonds();
    }
}

